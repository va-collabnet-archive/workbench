/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_SupportClassifier;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This class is an experiment to improve load, classify, and write performance
 * over what is provided in FasterLoad, which loads quickly, but has horrible
 * write performance.
 * <p>
 * 
 * <ol>
 * <li>Simplify the test to determine if a concept is part of the classification
 * hierarchy. The <code>isParentOfOrEqualTo</code> method is to slow. We will
 * instead check based on the following assumptions.
 * <ul>
 * <li>The is-a relationship is unique to the classification. For example, the
 * SNOMED is-a has a different concept id than the ace-auxiliary is-a
 * relationship. So every concept (except the concept root) will have at least
 * one is-a relationship of the proper type.
 * <li>There is a single root concept, and that root is part of the set of
 * included concept
 * <li>Assume that the versions are linear, independent of path, and therefore
 * the status with the latest date on an allowable path is the latest status.
 * <li>Assume that relationships to retired concepts will have a special status
 * so that retired concepts are not encountered by following current
 * relationships
 * 
 * <ul>
 * </ol>
 * These assumptions should allow determination of included concepts in liner
 * time - O(n), with a relatively small constant since they can be performed
 * with a simple integer comparison on the concept type.
 * 
 * @author kec
 * 
 */

@BeanList(specs = { @Spec(directory = "tasks/ace/classify", type = BeanType.TASK_BEAN) })
public class LoadClassifyWrite extends AbstractTask {

    final static Object LOCK = new Object();
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private static volatile int conceptCount = 0;
    private static volatile int classifyConceptCount = 0;
    private static volatile int relCount = 0;
    private static volatile int relsOnPathCount = 0;
    private static volatile int activeRelCount = 0;
    private static volatile int statedRelCount = 0;
    
    private static int multipleRelEntriesForVersion = 0;
    private static int multipleRelEntriesForVersion2 = 0;
    private static int multipleAttrEntriesForVersion = 0;

    private static int isaId = Integer.MIN_VALUE;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        final int objDataVersion = in.readInt();

        if (objDataVersion <= dataVersion) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    private static class MockSnorocketFactory implements I_SnorocketFactory {

        private int conceptCount = 0;
        private int isACount = 0;
        private int relationshipCount = 0;

        synchronized public void addConcept(int conceptId, boolean fullyDefined) {
            conceptCount++;
        }

        public void setIsa(int id) {
            isACount++;
        }

        synchronized public void addRelationship(int c1, int rel, int c2, int group) {
            relationshipCount++;
        }

        public void classify() {
            System.err.println("*** " + conceptCount + "\t" + relationshipCount);
        }

        public void getResults(I_Callback callback) {
        }

        public I_SnorocketFactory createExtension() {
            // TODO Auto-generated method stub
            return null;
        }

        public InputStream getStream() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

    }

    private class FilterConcept {

        final private Logger logger;

        final private int rootNid;
        final private int isaId;
        
        final private Context context;

        FilterConcept(Logger logger, int rootNid, int isaId, Context context) throws TerminologyException, IOException {
            this.logger = logger;
            this.rootNid = rootNid;
            this.isaId = isaId;
            this.context = context;
        }

        I_ConceptAttributePart ok(I_GetConceptData concept) {
            try {

                if (concept.getConceptId() != rootNid) {
                    boolean foundIsa = false;
                    for (I_RelVersioned rel : concept.getSourceRels()) {
                        I_RelPart latestPart = null;
                        for (I_RelPart part : rel.getVersions()) {
                            // check the rel parts to see if there is a proper
                            // is-a.
                            if (context.allowedPaths.contains(part.getPathId())) {
                                if (latestPart == null) {
                                    latestPart = part;
                                } else if (latestPart.getVersion() <= part.getVersion()) {
                                    if (latestPart.getVersion() == part.getVersion()) {
                                        if (getLogger().isLoggable(Level.FINE)) {
                                            getLogger().log(Level.FINE,
                                                    "has multiple entries with same version: " + rel + " for " + concept);
                                        }
                                        multipleRelEntriesForVersion2++;
                                    }
                                    latestPart = part;
                                }
                            }
                        }
                        if (latestPart != null && latestPart.getRelTypeId() == isaId && context.activeStatus.contains(latestPart.getStatusId())) {
                            foundIsa = true;
                            break;
                        }
                        // check to see if it is an is-a
                    }
                    if (foundIsa == false) {
                        return null;
                    }
                }

                I_ConceptAttributePart latestAttributePart = null;
                for (I_ConceptAttributePart attributePart : concept.getConceptAttributes().getVersions()) {
                    if (context.allowedPaths.contains(attributePart.getPathId())) {
                        if (latestAttributePart == null) {
                            latestAttributePart = attributePart;
                        } else if (latestAttributePart.getVersion() <= attributePart.getVersion()) {
                            if (latestAttributePart.getVersion() == attributePart.getVersion()) {
                                if (getLogger().isLoggable(Level.FINE)) {
                                    getLogger().log(Level.FINE,
                                            "has multiple entries with same version: " + attributePart + " for " + concept);
                                }
                                multipleAttrEntriesForVersion++;
                            }
                            latestAttributePart = attributePart;
                        }
                    }
                }

                if (latestAttributePart == null || context.activeStatus.contains(latestAttributePart.getConceptStatus()) == false) {
                    return null;
                }
                return latestAttributePart;
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
            return null;
        }

        public Logger getLogger() {
            return logger;
        }
    }

    private class TestForPrimitiveAndAdd implements Runnable {

        final private I_SnorocketFactory rocket;
        final private I_GetConceptData concept;

        final private FilterConcept filter;
        final private Semaphore s;
        
        final private Context context;

        public TestForPrimitiveAndAdd(I_SnorocketFactory rocket, I_GetConceptData concept, FilterConcept filter, Semaphore s, Context context) {
            super();
            this.rocket = rocket;
            this.concept = concept;
            this.filter = filter;
            this.s = s;
            this.context = context;
        }

        public void run() {
synchronized (LOCK)
{
            I_ConceptAttributePart attribute = filter.ok(concept);
            if (attribute != null) {
                final boolean isDefined = attribute.isDefined();

                rocket.addConcept(concept.getConceptId(), isDefined);
                classifyConceptCount++;
                try {
                    for (I_RelVersioned rel : concept.getSourceRels()) {
                        try {
                            relCount++;

                            I_RelPart latestPart = null;
                            for (I_RelPart part : rel.getVersions()) {
                                if (context.allowedPaths.contains(part.getPathId())) {
                                    if (latestPart == null) {
                                        latestPart = part;
                                    } else if (latestPart.getVersion() <= part.getVersion()) {
                                        if (latestPart.getVersion() == part.getVersion()) {
                                            if (filter.getLogger().isLoggable(Level.FINE)) {
                                                filter.getLogger().log(Level.FINE,
                                                        "has multiple entries with same version: " + rel + " for " + concept);
                                            }
                                            multipleRelEntriesForVersion++;
                                        }
                                        latestPart = part;
                                    }
                                }
                            }

                            if (latestPart != null) {
                                relsOnPathCount++;
                                if (context.activeStatus.contains(latestPart.getStatusId())) {
                                    activeRelCount++;
                                    if (context.statedForms.contains(latestPart.getCharacteristicId())) {
                                        statedRelCount++;
                                        final int cId1 = rel.getC1Id();
                                        final int relId = latestPart.getRelTypeId();
                                        final int cId2 = rel.getC2Id();
                                        rocket.addRelationship(cId1, relId, cId2, latestPart.getGroup());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            filter.getLogger().log(Level.SEVERE, "Processing rel: " + rel + " for " + concept, e);
                        }
                    }
                } catch (Exception e) {
                    filter.getLogger().log(Level.SEVERE, e.getLocalizedMessage(), e);
                }
            }
            s.release();
        }
}
    }

    private class ProcessConcepts implements I_ProcessConcepts, Callable<Boolean> {

        final private I_Work worker;
        final private I_SnorocketFactory rocket;
        final private ExecutorService executionService;
        final private FilterConcept filter;
        final private Context context;

        final private Semaphore s = new Semaphore(20);

        public ProcessConcepts(I_Work worker, I_SnorocketFactory rocket, FilterConcept filter, ExecutorService executionService, Context context) {
            super();
            this.worker = worker;
            this.rocket = rocket;
            this.executionService = executionService;
            this.filter = filter;
            this.context = context;
        }

        public void processConcept(I_GetConceptData concept) throws Exception {
            s.acquire();
            conceptCount++;
            executionService.execute(new TestForPrimitiveAndAdd(rocket, concept, filter, s, context));
        }

        public Boolean call() throws Exception {
            I_TermFactory termFactory = LocalVersionedTerminology.get();
            termFactory.iterateConcepts(this);
            worker.getLogger().info("Processed concepts: " + conceptCount);
            return true;
        }

    }

    private static class Context {

        final private I_SupportClassifier tf;
        final private I_IntSet allowedPaths;
        final private I_IntSet statedForms;
        final private I_IntSet activeStatus;

        public Context() throws TerminologyException, IOException {
            tf = (I_SupportClassifier) LocalVersionedTerminology.get();

            final I_ConfigAceFrame frameConfig = tf.getActiveAceFrameConfig();

            allowedPaths = tf.newIntSet();
            for (I_Position p : tf.getActiveAceFrameConfig().getViewPositionSet()) {
                addPathIds(allowedPaths, p);
            }

            activeStatus = frameConfig.getAllowedStatus();
            statedForms = tf.newIntSet();
            statedForms.add(getNid(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP));
            statedForms.add(getNid(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC));
            statedForms.add(getNid(ArchitectonicAuxiliary.Concept.STATED_AND_INFERRED_RELATIONSHIP));
            statedForms.add(getNid(ArchitectonicAuxiliary.Concept.STATED_AND_SUBSUMED_RELATIONSHIP));
        }

        private int getNid(I_ConceptualizeUniversally concept) throws TerminologyException, IOException {
            return tf.uuidToNative(concept.getUids());
        }
    }

    private static void addPathIds(I_IntSet allowedPaths, I_Position p) throws TerminologyException, IOException {
        allowedPaths.add(p.getPath().getConceptId());
        for (I_Position origin : p.getPath().getOrigins()) {
            addPathIds(allowedPaths, origin);
        }
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            worker.getLogger().info("LCW start evaluate()");

            conceptCount = 0;
            classifyConceptCount = 0;
            relCount = 0;
            relsOnPathCount = 0;
            activeRelCount = 0;
            statedRelCount = 0;

            long startTime = System.currentTimeMillis();

            final I_SnorocketFactory rocket =
                (I_SnorocketFactory) process.readAttachement(ProcessKey.SNOROCKET.getAttachmentKey());
//                new MockSnorocketFactory();

            final Context context = new Context();
            
            if (context.tf.getActiveAceFrameConfig().getEditingPathSet().size() != 1) {
                throw new TaskFailedException("Profile must have only one edit path. Found: "
                        + context.tf.getActiveAceFrameConfig().getEditingPathSet());
            }

            final int snomedRootNid = context.tf.uuidToNative(SNOMED.Concept.ROOT.getUids());
            isaId = context.tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
            rocket.setIsa(isaId);

            FilterConcept filter = new FilterConcept(worker.getLogger(), snomedRootNid, isaId, context);

            ExecutorService executionService = Executors.newFixedThreadPool(6);
            Future<Boolean> conceptFuture = executionService.submit(new ProcessConcepts(worker, rocket, filter, executionService, context));

            conceptFuture.get();

            worker.getLogger().info("LCW load time (ms): " + (System.currentTimeMillis() - startTime));

            executionService.shutdown();
            startTime = System.currentTimeMillis();
            executionService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            worker.getLogger().info("LCW executionService termination time (ms): " + (System.currentTimeMillis() - startTime));
            worker.getLogger().info("Processed total concepts: " + conceptCount + " for classification: " + classifyConceptCount);
            worker.getLogger().info(
                    "Processed total rels: " + relCount + " rels on path: " + relsOnPathCount + " active: " + activeRelCount + " stated: "
                            + statedRelCount);
            worker.getLogger().info("Rels with multiple entries for version: " + multipleRelEntriesForVersion);
            worker.getLogger().info("Rels with multiple entries for version2: " + multipleRelEntriesForVersion2);
            worker.getLogger().info("Concepts with multiple entries for attribute: " + multipleAttrEntriesForVersion);

            worker.getLogger().info("Starting classify. ");

            startTime = System.currentTimeMillis();
            rocket.classify();
            worker.getLogger().info("LCW classify time: " + (System.currentTimeMillis() - startTime));

            process.writeAttachment(ProcessKey.SNOROCKET.getAttachmentKey(), rocket);

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    public void complete(I_EncodeBusinessProcess arg0, I_Work arg1) throws TaskFailedException {
        // nothing to do...
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
