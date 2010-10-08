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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_ProcessRelationships;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.id.Type3UuidFactory;

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class FasterLoad extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

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

    static class MockSnorocketFactory implements I_SnorocketFactory {

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

        public void addRoleComposition(int[] lhsIds, int rhsId) {
            // TODO Auto-generated method stub

        }

        public void addRoleRoot(int id, boolean inclusive) {
            // TODO Auto-generated method stub

        }

        public void addRoleNeverGrouped(int id) {
            // TODO Auto-generated method stub

        }

        public void getEquivConcepts(I_EquivalentCallback callback) {
            // TODO Auto-generated method stub

        }

    }

    private static class FilterConcept {

        final private I_TermFactory termFactory;
        final private Logger logger;

        final private I_IntSet activeStatus;
        final private I_IntSet allowedTypes;
        final private Set<I_Position> positions;
        final private int definingCharacteristic;
        final private I_GetConceptData root;

        FilterConcept(final I_TermFactory termFactory, final Logger logger) throws TerminologyException, IOException {
            this.termFactory = termFactory;
            this.logger = logger;

            this.root = termFactory.getConcept(new TermEntry(Type3UuidFactory.SNOMED_ROOT_UUID).getIds());

            final I_ConfigAceFrame frameConfig = termFactory.getActiveAceFrameConfig();
            activeStatus = frameConfig.getAllowedStatus();
            allowedTypes = frameConfig.getDestRelTypes();
            positions = frameConfig.getViewPositionSet();

            definingCharacteristic = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()).getConceptId();
        }

        boolean ok(I_GetConceptData concept) {
            try {
                boolean ok = false;
                // check that concept has an IdPart with a source that
                // corresponds to SNOMED CT
                // (the corresponding sourceId == the SCTID)
                for (final I_IdPart idPart : concept.getId().getVersions()) {
                    if (idPart.getSource() == -2147483005) { // FIXME find
                        // source of this
                        // constant
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    return false;
                }

                // the following test is too slow :-(
                // replaced with code above
                // final boolean addUncommitted = false;
                // if (!root.isParentOfOrEqualTo(concept, activeStatus,
                // allowedTypes, positions, addUncommitted)) {
                // return false;
                // }

                final List<I_ConceptAttributeTuple> tuples = concept.getConceptAttributeTuples(activeStatus, positions);
                if (tuples.size() > 1) {
                    throw new AssertionError("Unexpected number of tuples: " + tuples.size() + " for " + concept);
                }
                return 1 == tuples.size();
            } catch (IOException e) {
                return handleException(e);
            }
        }

        boolean ok(I_RelTuple relTuple) {
            return definingCharacteristic == relTuple.getCharacteristicId()
                && activeStatus.contains(relTuple.getStatusId()) && ok(relTuple.getC1Id()) && ok(relTuple.getC2Id())
                && ok(relTuple.getRelTypeId());
        }

        /**
         * Assumes that ok(concept) has already been called
         * 
         * @param concept
         * @return
         * @throws IOException
         */
        boolean isDefined(I_GetConceptData concept) {
            try {
                final List<I_ConceptAttributeTuple> tuples = concept.getConceptAttributeTuples(activeStatus, positions);
                if (tuples.size() > 1) {
                    throw new AssertionError("Unexpected number of tuples: " + tuples.size() + " for " + concept);
                }
                return tuples.get(0).isDefined();
            } catch (IOException e) {
                return handleException(e);
            }
        }

        boolean ok(int id) {
            try {
                return ok(termFactory.getConcept(id));
            } catch (TerminologyException e) {
                return handleException(e);
            } catch (IOException e) {
                return handleException(e);
            }
        }

        private boolean handleException(Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return false;
        }
    }

    private static class TestForPrimitiveAndAdd implements Runnable {

        final private I_SnorocketFactory rocket;
        final private I_GetConceptData concept;

        final private FilterConcept filter;

        public TestForPrimitiveAndAdd(I_SnorocketFactory rocket, I_GetConceptData concept, FilterConcept filter) {
            super();
            this.rocket = rocket;
            this.concept = concept;
            this.filter = filter;
        }

        public void run() {
            // check valid status
            if (filter.ok(concept)) {
                final boolean fullyDefined = filter.isDefined(concept);
                rocket.addConcept(concept.getConceptId(), fullyDefined);
            }
        }

    }

    private static class TestForAllowedRelAndAdd implements Runnable {

        final private I_SnorocketFactory rocket;
        final private I_RelVersioned versionedRel;
        final private FilterConcept filter;

        public TestForAllowedRelAndAdd(I_SnorocketFactory rocket, I_RelVersioned versionedRel, FilterConcept filter)
                throws TerminologyException, IOException {
            super();
            this.rocket = rocket;
            this.versionedRel = versionedRel;
            this.filter = filter;
        }

        public void run() {
            final I_RelTuple relTuple = versionedRel.getLastTuple();

            // check if defining and active
            if (filter.ok(relTuple)) {
                final int cId1 = relTuple.getC1Id();
                final int relTypeId = relTuple.getRelTypeId();
                final int cId2 = relTuple.getC2Id();
                final int group = relTuple.getGroup();

                rocket.addRelationship(cId1, relTypeId, cId2, group);
            }
        }

    }

    private static class ProcessConcepts implements I_ProcessConcepts, Callable<Boolean> {

        final private I_Work worker;
        final private I_SnorocketFactory rocket;
        final private ExecutorService executionService;
        final private FilterConcept filter;

        private int conceptCount = 0;

        public ProcessConcepts(I_Work worker, I_SnorocketFactory rocket, FilterConcept filter,
                ExecutorService executionService) {
            super();
            this.worker = worker;
            this.rocket = rocket;
            this.executionService = executionService;
            this.filter = filter;
        }

        public void processConcept(I_GetConceptData concept) throws Exception {
            conceptCount++;
            executionService.execute(new TestForPrimitiveAndAdd(rocket, concept, filter));
        }

        public Boolean call() throws Exception {
            final I_TermFactory termFactory = LocalVersionedTerminology.get();
            termFactory.iterateConcepts(this);
            worker.getLogger().info("Processed concepts: " + conceptCount);
            return true;
        }

    }

    private static class ProcessRelationships implements I_ProcessRelationships, Callable<Boolean> {

        final private I_Work worker;
        final private I_SnorocketFactory rocket;
        final private ExecutorService executionService;
        final private FilterConcept filter;

        private int relCount = 0;

        public ProcessRelationships(I_Work worker, I_SnorocketFactory rocket, FilterConcept filter,
                ExecutorService executionService) {
            super();
            this.worker = worker;
            this.rocket = rocket;
            this.executionService = executionService;
            this.filter = filter;
        }

        public void processRelationship(I_RelVersioned versionedRel) throws Exception {
            relCount++;
            executionService.execute(new TestForAllowedRelAndAdd(rocket, versionedRel, filter));
        }

        public Boolean call() throws Exception {
            final I_TermFactory termFactory = LocalVersionedTerminology.get();

            termFactory.iterateRelationships(this);
            worker.getLogger().info("Processed relationships: " + relCount);
            return true;
        }

    }

    static class ProcessResults implements Callable<Boolean>, I_SnorocketFactory.I_Callback {

        final private I_Work worker;
        final private I_SnorocketFactory rocket;
        final private ExecutorService executionService;

        final private I_TermFactory termFactory = LocalVersionedTerminology.get();
        final public I_ConfigAceFrame newConfig;
        final public I_GetConceptData relCharacteristic;
        final public I_GetConceptData relRefinability;
        final public I_GetConceptData relStatus;

        public ProcessResults(final I_Work worker, final I_SnorocketFactory rocket, ExecutorService executorService)
                throws Exception {
            this.worker = worker;
            this.rocket = rocket;
            this.executionService = executorService;

            final I_Path statedPath = termFactory.getPath(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids());
            final I_Path inferredPath = getInferredPath(statedPath, termFactory.getActiveAceFrameConfig());

            newConfig = NewDefaultProfile.newProfile("", "", "", "", "");
            newConfig.getEditingPathSet().clear();
            newConfig.addEditingPath(inferredPath);

            // relCharacteristic =
            // termFactory.getConcept(ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.getUids());
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

            return true;
        }

        public void addRelationship(int conceptId1, int roleId, int conceptId2, int group) {
            final AddNewRelationship addNewRelationship = new AddNewRelationship(conceptId1, roleId, conceptId2, group);
            final boolean threadSafe = false; // termFactory.newRelationship no
            // threadsafe :(
            if (threadSafe) {
                executionService.execute(addNewRelationship);
            } else {
                addNewRelationship.run();
            }
        }

        private I_Path getInferredPath(I_Path statedPath, I_ConfigAceFrame config) throws TerminologyException,
                IOException, Exception {
            final UUID pathUUID = UUID.fromString("0f71239a-a796-11dc-8314-0800200c9a66");
            try {
                return termFactory.getPath(new UUID[] { pathUUID });
            } catch (NoMappingException e) {
                // path doesn't exist - create it
                I_GetConceptData pathConcept = termFactory.newConcept(pathUUID, false, config);

                termFactory.newDescription(UUID.randomUUID(), pathConcept, "en", "SNOMED Inferred Path",
                    ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), config);

                termFactory.newDescription(UUID.randomUUID(), pathConcept, "en", "SNOMED Inferred Path",
                    ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), config);

                termFactory.newRelationship(UUID.randomUUID(), pathConcept,
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.RELEASE.getUids()),
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

                termFactory.commit();

                Set<I_Position> origins = new HashSet<I_Position>(statedPath.getOrigins());

                final I_Path inferredPath = termFactory.newPath(origins, pathConcept);

                termFactory.commit();

                return inferredPath;
            }
        }

        private class AddNewRelationship implements Runnable {

            final private int c1;
            final private int rel;
            final private int c2;
            final private int group;

            public AddNewRelationship(int conceptId1, int roleId, int conceptId2, int group) {
                this.c1 = conceptId1;
                this.rel = roleId;
                this.c2 = conceptId2;
                this.group = group;
            }

            public void run() {
                try {
                    final I_GetConceptData relSource = termFactory.getConcept(c1);
                    final I_GetConceptData relType = termFactory.getConcept(rel);
                    final I_GetConceptData relDestination = termFactory.getConcept(c2);

                    // worker.getLogger().info("*** " + relType +
                    // "\n\t" + relSource +
                    // "\n\t" + relDestination +
                    // "\t" + group);

                    final UUID newRelUid = UUID.randomUUID();
                    termFactory.newRelationship(newRelUid, relSource, relType, relDestination, relCharacteristic,
                        relRefinability, relStatus, group, newConfig);
                } catch (TerminologyException e) {
                    worker.getLogger().severe(e.getLocalizedMessage());
                } catch (IOException e) {
                    worker.getLogger().severe(e.getLocalizedMessage());
                }
            }

        }

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            worker.getLogger().info("FasterLoad start evaluate()");
            long startTime = System.currentTimeMillis();

            final I_SnorocketFactory rocket = (I_SnorocketFactory) Class.forName(
                "au.csiro.snorocket.ace.SnorocketFactory"
            // "org.dwfa.ace.task.classify.FasterLoad$MockSnorocketFactory"
            )
                .newInstance();
            final I_TermFactory termFactory = LocalVersionedTerminology.get();

            final int isaId = termFactory.getConcept(new UUID[] { Type3UuidFactory.SNOMED_ISA_REL_UUID })
                .getConceptId();
            rocket.setIsa(isaId);

            final FilterConcept filter = new FilterConcept(termFactory, worker.getLogger());

            final ExecutorService executionService = Executors.newFixedThreadPool(6);
            final Future<Boolean> conceptFuture = executionService.submit(new ProcessConcepts(worker, rocket, filter,
                executionService));
            final Future<Boolean> relFuture = executionService.submit(new ProcessRelationships(worker, rocket, filter,
                executionService));

            conceptFuture.get();
            relFuture.get();

            worker.getLogger().info("FasterLoad load time: " + (System.currentTimeMillis() - startTime));

            executionService.shutdown();
            startTime = System.currentTimeMillis();
            executionService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            worker.getLogger().info("FasterLoad termination time: " + (System.currentTimeMillis() - startTime));

            startTime = System.currentTimeMillis();
            rocket.classify();
            worker.getLogger().info("FasterLoad classify time: " + (System.currentTimeMillis() - startTime));

            getClassifierResults(worker, rocket);

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    private void getClassifierResults(I_Work worker, I_SnorocketFactory rocket) throws Exception {
        long startTime = System.currentTimeMillis();
        final ExecutorService executionService = Executors.newFixedThreadPool(6);

        final Future<Boolean> resultFuture = executionService.submit(new ProcessResults(worker, rocket,
            executionService));
        resultFuture.get();

        worker.getLogger().info("FasterLoad getResults time: " + (System.currentTimeMillis() - startTime));

        executionService.shutdown();
        startTime = System.currentTimeMillis();
        executionService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        worker.getLogger().info("FasterLoad termination time: " + (System.currentTimeMillis() - startTime));
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
