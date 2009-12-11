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
import java.util.UUID;
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

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private static int conceptCount = 0;
    private static int classifyConceptCount = 0;
    private static int relCount = 0;
    private static int relsOnPathCount = 0;
    private static int activeRelCount = 0;
    private static int statedRelCount = 0;
    private static int statedAndInferredCount = 0;
    private static int inferredRelCount = 0;
    private static int statedAndSubsumedCount = 0;
    private static int multipleRelEntriesForVersion = 0;
    private static int multipleRelEntriesForVersion2 = 0;
    private static int multipleAttrEntriesForVersion = 0;

    private static int isaId = Integer.MIN_VALUE;
    private static int pathNid = Integer.MIN_VALUE;
    private static int activeNid = Integer.MIN_VALUE;
    private static int optionalRefinabilityNid = Integer.MIN_VALUE;
    private static int inferredCharacteristicNid = Integer.MIN_VALUE;
    private static int statedAndInferredNid = Integer.MIN_VALUE;
    private static int unspecifiedUuidNid = Integer.MIN_VALUE;
    private static int version = Integer.MAX_VALUE;

    private static I_SupportClassifier tf;
    private static I_IntSet allowedPaths;
    private static I_IntSet statedForms;
    private static I_IntSet activeStatus;

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

    private static class FilterConcept {

        private Logger logger;

        private int rootNid;
        private int isaId;

        FilterConcept(Logger logger, int rootNid, int isaId) throws TerminologyException, IOException {
            this.logger = logger;
            this.rootNid = rootNid;
            this.isaId = isaId;

            allowedPaths = tf.newIntSet();
            for (I_Position p : tf.getActiveAceFrameConfig().getViewPositionSet()) {
                addPathIds(allowedPaths, p);
            }

            I_ConfigAceFrame frameConfig = tf.getActiveAceFrameConfig();
            activeStatus = frameConfig.getAllowedStatus();
            statedForms = tf.newIntSet();
            statedForms.add(getNid(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP));
            statedForms.add(getNid(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC));
            statedForms.add(getNid(ArchitectonicAuxiliary.Concept.STATED_AND_INFERRED_RELATIONSHIP));
            statedForms.add(getNid(ArchitectonicAuxiliary.Concept.STATED_AND_SUBSUMED_RELATIONSHIP));
        }

        private void addPathIds(I_IntSet allowedPaths, I_Position p) throws TerminologyException, IOException {
            allowedPaths.add(p.getPath().getConceptId());
            for (I_Position origin : p.getPath().getOrigins()) {
                addPathIds(allowedPaths, origin);
            }
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
                            if (allowedPaths.contains(part.getPathId())) {
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
                        if (latestPart != null && latestPart.getRelTypeId() == isaId && activeStatus.contains(latestPart.getStatusId())) {
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
                    if (allowedPaths.contains(attributePart.getPathId())) {
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

                if (latestAttributePart == null || activeStatus.contains(latestAttributePart.getConceptStatus()) == false) {
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

        public I_IntSet getActiveStatus() {
            return activeStatus;
        }
    }

    private static class TestForPrimitiveAndAdd implements Runnable {

        private I_SnorocketFactory rocket;
        private I_GetConceptData concept;

        private FilterConcept filter;
        private Semaphore s;

        public TestForPrimitiveAndAdd(I_SnorocketFactory rocket, I_GetConceptData concept, FilterConcept filter, Semaphore s) {
            super();
            this.rocket = rocket;
            this.concept = concept;
            this.filter = filter;
            this.s = s;
        }

        public void run() {
            I_ConceptAttributePart attribute = filter.ok(concept);
            if (attribute != null) {
                rocket.addConcept(concept.getConceptId(), attribute.isDefined());
                classifyConceptCount++;
                try {
                    for (I_RelVersioned rel : concept.getSourceRels()) {
                        try {
                            relCount++;

                            I_RelPart latestPart = null;
                            for (I_RelPart part : rel.getVersions()) {
                                if (allowedPaths.contains(part.getPathId())) {
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
                                if (filter.getActiveStatus().contains(latestPart.getStatusId())) {
                                    activeRelCount++;
                                    if (statedForms.contains(latestPart.getCharacteristicId())) {
                                        statedRelCount++;
                                        rocket.addRelationship(rel.getC1Id(), latestPart.getRelTypeId(), rel.getC2Id(), latestPart
                                                .getGroup());
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

    private static class ProcessConcepts implements I_ProcessConcepts, Callable<Boolean> {

        private I_Work worker;
        private I_SnorocketFactory rocket;
        private ExecutorService executionService;
        private FilterConcept filter;
        private Semaphore s = new Semaphore(20);

        public ProcessConcepts(I_Work worker, I_SnorocketFactory rocket, FilterConcept filter, ExecutorService executionService) {
            super();
            this.worker = worker;
            this.rocket = rocket;
            this.executionService = executionService;
            this.filter = filter;
        }

        public void processConcept(I_GetConceptData concept) throws Exception {
            conceptCount++;
            s.acquire();
            executionService.execute(new TestForPrimitiveAndAdd(rocket, concept, filter, s));
        }

        public Boolean call() throws Exception {
            I_TermFactory termFactory = LocalVersionedTerminology.get();
            termFactory.iterateConcepts(this);
            worker.getLogger().info("Processed concepts: " + conceptCount);
            return true;
        }

    }

    private static class ProcessResults implements Callable<Boolean>, I_SnorocketFactory.I_Callback {

        private I_Work worker;
        private I_SnorocketFactory rocket;
        private ExecutorService executionService;

        private I_TermFactory termFactory = LocalVersionedTerminology.get();
        public I_GetConceptData relCharacteristic;
        public I_GetConceptData relRefinability;
        public I_GetConceptData relStatus;
        private Semaphore resultSemaphore = new Semaphore(20);
        private int returnedRelCount = 0;

        public ProcessResults(final I_Work worker, final I_SnorocketFactory rocket, ExecutorService executorService) throws Exception {
            this.worker = worker;
            this.rocket = rocket;
            this.executionService = executorService;

            relCharacteristic = termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
            relRefinability = termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());
            relStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

            worker.getLogger().info("Inferred id is " + relCharacteristic.getConceptId());
            worker.getLogger().info("Inferred UUIDs are " + relCharacteristic.getUids());
            worker.getLogger().info("Inferred concept is " + relCharacteristic);
        }

        public Boolean call() throws Exception {
            worker.getLogger().info("get results");
            rocket.getResults(this);
            worker.getLogger().info("Returned " + returnedRelCount + " rels.");

            return true;
        }

        public void addRelationship(int conceptId1, int roleId, int conceptId2, int group) {
            try {
                resultSemaphore.acquire();
                returnedRelCount++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            AddNewRelationship addNewRelationship = new AddNewRelationship(conceptId1, roleId, conceptId2, group, resultSemaphore);
            executionService.execute(addNewRelationship);
        }

    }

    private static class AddNewRelationship implements Runnable {

        private int c1;
        private int typeId;
        private int c2;
        private int group;
        private Semaphore resultSemaphore;

        public AddNewRelationship(int conceptId1, int typeId, int conceptId2, int group, Semaphore resultSemaphore) {
            this.c1 = conceptId1;
            this.typeId = typeId;
            this.c2 = conceptId2;
            this.group = group;
            this.resultSemaphore = resultSemaphore;
        }

        public void run() {
            try {
                I_GetConceptData concept1 = tf.getConcept(c1);
                synchronized (concept1) {
                    boolean found = false;
                    for (I_RelVersioned rel : concept1.getSourceRels()) {
                        if (rel.getC2Id() == c2) {
                            I_RelPart latestPart = null;
                            for (I_RelPart part : rel.getVersions()) {
                                // check the rel parts to see if there is a
                                // proper
                                // is-a.
                                if (allowedPaths.contains(part.getPathId())) {
                                    if (latestPart == null) {
                                        latestPart = part;
                                    } else {
                                        if (latestPart.getVersion() <= part.getVersion()) {
                                            latestPart = part;
                                        }
                                    }
                                }
                            }

                            if (latestPart != null) {
                                if ((latestPart.getRelTypeId() == typeId) && (latestPart.getGroup() == group)) {
                                    if (activeStatus.contains(latestPart.getStatusId())) {
                                        statedAndInferredCount++;
                                        found = true;
                                        // check that defining characteristic is
                                        // proper and possibly add part if not.
                                        if (latestPart.getCharacteristicId() != statedAndInferredNid) {
                                            I_RelPart newPart = latestPart.duplicate();
                                            newPart.setVersion(version);
                                            newPart.setStatusId(activeNid);
                                            newPart.setCharacteristicId(statedAndInferredNid);
                                            rel.addVersion(newPart);
                                            tf.writeRel(rel);
                                        }
                                    } else {
                                        inferredRelCount++;
                                        found = true;
                                        // add part with active status
                                        I_RelPart newPart = latestPart.duplicate();
                                        newPart.setVersion(version);
                                        newPart.setStatusId(activeNid);
                                        rel.addVersion(newPart);
                                        tf.writeRel(rel);
                                    }
                                }
                            }
                        }
                        if (found) {
                            break;
                        }
                    }
                    if (found == false) {
                        inferredRelCount++;
                        I_RelVersioned newRel = tf.newRelationship(UUID.randomUUID(), unspecifiedUuidNid, c1, c2, pathNid, version,
                                activeNid, typeId, inferredCharacteristicNid, optionalRefinabilityNid, group);
                        tf.writeRel(newRel);
                        concept1.getSourceRels().add(newRel);
                    }
                }

            } catch (TerminologyException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                resultSemaphore.release();
            }
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
            statedAndInferredCount = 0;
            inferredRelCount = 0;
            statedAndSubsumedCount = 0;

            long startTime = System.currentTimeMillis();

            final I_SnorocketFactory rocket = (I_SnorocketFactory) process.readAttachement(ProcessKey.SNOROCKET.getAttachmentKey());
            tf = (I_SupportClassifier) LocalVersionedTerminology.get();

            if (tf.getActiveAceFrameConfig().getEditingPathSet().size() != 1) {
                throw new TaskFailedException("Profile must have only one edit path. Found: "
                        + tf.getActiveAceFrameConfig().getEditingPathSet());
            }

            pathNid = tf.getActiveAceFrameConfig().getEditingPathSet().iterator().next().getConceptId();
            activeNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
            optionalRefinabilityNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            inferredCharacteristicNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.getUids());
            statedAndInferredNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_INFERRED_RELATIONSHIP.getUids());
            unspecifiedUuidNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());
            int snomedRootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());
            isaId = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
            rocket.setIsa(isaId);

            FilterConcept filter = new FilterConcept(worker.getLogger(), snomedRootNid, isaId);

            ExecutorService executionService = Executors.newFixedThreadPool(6);
            Future<Boolean> conceptFuture = executionService.submit(new ProcessConcepts(worker, rocket, filter, executionService));

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

//            worker.getLogger().info("Starting get results. ");
//            getClassifierResults(worker, rocket);
//            worker.getLogger().info("Finished get results. ");
//            worker.getLogger().info(
//                    "Stated and inferred: " + statedAndInferredCount + " stated and subsumbed: " + statedAndSubsumedCount
//                            + " inferred count: " + inferredRelCount);
//
//            tf.commit();
//            worker.getLogger().info("Finished commit. ");

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    private void getClassifierResults(I_Work worker, I_SnorocketFactory rocket) throws Exception {
        long startTime = System.currentTimeMillis();
        version = tf.convertToThinVersion(startTime);
        ExecutorService executionService = Executors.newFixedThreadPool(15);

        Future<Boolean> resultFuture = executionService.submit(new ProcessResults(worker, rocket, executionService));
        resultFuture.get();

        worker.getLogger().info("LCW getResults time: " + (System.currentTimeMillis() - startTime));

        executionService.shutdown();
        startTime = System.currentTimeMillis();
        executionService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        worker.getLogger().info("LCW termination time: " + (System.currentTimeMillis() - startTime));
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

    private static int getNid(I_ConceptualizeUniversally concept) throws TerminologyException, IOException {
        return tf.uuidToNative(concept.getUids());
    }

}
