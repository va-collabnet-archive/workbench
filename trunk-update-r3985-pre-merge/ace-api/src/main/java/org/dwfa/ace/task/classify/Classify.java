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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
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

/*
 * EKM - Use LoadClassifyWrite instead
 * <br>
 * The evaluate method here exits before calling classify, looks to be in some
 * sort of debug state.
 * Deleting the short circuit and classifying with Snorocket blows up anyhow.
 * So mark this as deprecated.
 */
@Deprecated
@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class Classify extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 2;

    /**
     * Bean property
     */
    private String factoryClass = "org.dwfa.ace.task.classify.StubSnorocketFactory";

    /**
     * Bean property
     */
    private TermEntry classifyRoot = new TermEntry(Type3UuidFactory.SNOMED_ROOT_UUID);

    private transient I_SnorocketFactory snorocketFactory = null;

    private transient I_TermFactory termFactory;

    private transient int isaId;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(factoryClass);
        out.writeObject(classifyRoot);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        final int objDataVersion = in.readInt();

        if (objDataVersion > 0) {
            factoryClass = (String) in.readObject();
            if (objDataVersion > 1) {
                classifyRoot = (TermEntry) in.readObject();
            } else if (objDataVersion > dataVersion) {
                throw new IOException("Can't handle dataversion: " + objDataVersion);
            }
        } else { // <= 0
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            snorocketFactory = (I_SnorocketFactory) Class.forName(factoryClass).newInstance();
        } catch (InstantiationException e) {
            handleException(e);
        } catch (IllegalAccessException e) {
            handleException(e);
        } catch (ClassNotFoundException e) {
            handleException(e);
        }

        termFactory = LocalVersionedTerminology.get();

        try {
            // GET ROOT
            I_GetConceptData root = termFactory.getConcept(classifyRoot.ids);
            if (null == root) {
                throw new TaskFailedException("Classification root concept is null.  Id: " + classifyRoot.ids);
            }
            worker.getLogger().info("Classification Root: " + root);

            isaId = termFactory.getConcept(new UUID[] { Type3UuidFactory.SNOMED_ISA_REL_UUID }).getConceptId();
            worker.getLogger().info("isa ID: " + isaId + " : " + termFactory.getConcept(isaId));
            snorocketFactory.setIsa(isaId);

            final long start = System.currentTimeMillis();

            new IteratorWalker(termFactory, snorocketFactory, root).run();
            if (false) {
                new BreadthWalker(termFactory, snorocketFactory, root).run();
                new DepthWalker(termFactory, snorocketFactory, root).run();
            }

            worker.getLogger().info("Walker time: " + (System.currentTimeMillis() - start) / 1000.0 + "sec");
            if (true)
                return Condition.CONTINUE; // FIXME delete

            snorocketFactory.classify();

            worker.getLogger().info("Classified!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            getClassifierResults(worker, snorocketFactory);

            // // GET CHILDREN
            // I_IntSet relTypes = termFactory.newIntSet();
            // relTypes.add(termFactory.uuidToNative(Type3UuidFactory.SNOMED_ISA_REL_UUID));
            //            
            // I_IntSet statusType = termFactory.newIntSet();
            // statusType.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()).getConceptId());
            // statusType.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId());
            // statusType.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids()).getConceptId());
            //            
            // Set<I_Position> latestStated = new HashSet<I_Position>();
            // latestStated.add(termFactory.newPosition(statedPath,
            // Integer.MAX_VALUE));
            //            
            // boolean addUncommitted = false;
            //            
            // // get rels that point at snomedRoot
            // List<I_RelTuple> tuples = snomedRoot.getDestRelTuples(statusType,
            // relTypes, latestStated , addUncommitted);
            //            
            // worker.getLogger().info("Tuples: " + tuples);

            // CREATE CONCEPT
            // sampleCreateNewThings(root, inferredPath);
        } catch (TerminologyException e) {
            handleException(e);
        } catch (IOException e) {
            handleException(e);
        } catch (Exception e) {
            handleException(e);
        }

        snorocketFactory = null;

        return Condition.CONTINUE;
    }

    private void handleException(Exception e) throws TaskFailedException {
        snorocketFactory = null;
        throw new TaskFailedException(e);
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getFactoryClass() {
        return factoryClass;
    }

    public void setFactoryClass(String factoryClass) {
        this.factoryClass = factoryClass;
    }

    abstract class AbstractWalker {

        final protected I_TermFactory termFactory;
        final protected I_SnorocketFactory snorocketFactory;
        final protected I_GetConceptData root;

        final protected I_IntSet isaTypes;
        final protected int definingCharacteristic;
        final protected I_IntSet activeStatus;
        final protected Set<I_Position> latestStated;
        final protected boolean addUncommitted;
        final protected I_IntSet processed;

        AbstractWalker(final I_TermFactory termFactory, final I_SnorocketFactory snorocketFactory,
                final I_GetConceptData root) throws TerminologyException, IOException {
            this.termFactory = termFactory;
            this.snorocketFactory = snorocketFactory;
            this.root = root;

            // isaTypes = termFactory.newIntSet();
            // try {
            // isaTypes.add(termFactory.uuidToNative(Type3UuidFactory.SNOMED_ISA_REL_UUID));
            // } catch (TerminologyException e) {
            // getLogger().warning("SNOMED ISA not defined: " +
            // e.getLocalizedMessage());
            // }
            // isaTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

            definingCharacteristic = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()).getConceptId();

            final I_ConfigAceFrame frameConfig = termFactory.getActiveAceFrameConfig();
            activeStatus = frameConfig.getAllowedStatus();
            isaTypes = frameConfig.getDestRelTypes();
            latestStated = frameConfig.getViewPositionSet();

            // activeStatus = termFactory.newIntSet();
            // activeStatus.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()).getConceptId());
            // activeStatus.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId());
            // activeStatus.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids()).getConceptId());
            //            
            // final I_Path statedPath =
            // termFactory.getPath(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids());
            // latestStated = new HashSet<I_Position>();
            // latestStated.add(termFactory.newPosition(statedPath,
            // Integer.MAX_VALUE));

            addUncommitted = false;

            processed = termFactory.newIntSet();
        }

        abstract void run() throws IOException, TerminologyException;

        abstract void visit(int conceptId) throws IOException, TerminologyException;

        final void processConcept(final I_GetConceptData concept, final boolean visitChildren) throws IOException,
                TerminologyException {
            final int conceptId = concept.getConceptId();

            processed.add(conceptId);

            final List<I_ConceptAttributeTuple> tuples = concept.getConceptAttributeTuples(activeStatus, latestStated);
            if (tuples.size() == 1) {
                snorocketFactory.addConcept(conceptId, tuples.get(0).isDefined());

                final List<I_RelTuple> relTuples = concept.getSourceRelTuples(activeStatus, null, latestStated,
                    addUncommitted);
                for (I_RelTuple relTuple : relTuples) {
                    if (definingCharacteristic == relTuple.getCharacteristicId()) {
                        snorocketFactory.addRelationship(relTuple.getC1Id(), relTuple.getRelTypeId(),
                            relTuple.getC2Id(), relTuple.getGroup());
                    }
                }

                if (visitChildren) {
                    List<I_RelTuple> childTuples = concept.getDestRelTuples(activeStatus, isaTypes, latestStated,
                        addUncommitted);

                    for (I_RelTuple childTuple : childTuples) {
                        final int childId = childTuple.getC1Id();
                        snorocketFactory.addRelationship(childId, isaId, conceptId, 0);

                        if (!processed.contains(childId)) {
                            visit(childId);
                        }
                    }
                }
            } else if (tuples.size() > 1) {
                throw new AssertionError("Unexpected number of tuples: " + tuples.size() + " for " + concept);
            }
        }

    }

    class IteratorWalker extends AbstractWalker {

        IteratorWalker(final I_TermFactory termFactory, final I_SnorocketFactory snorocketFactory,
                final I_GetConceptData root) throws TerminologyException, IOException {
            super(termFactory, snorocketFactory, root);
        }

        @Override
        void run() throws IOException, TerminologyException {
            final I_ConfigAceFrame frameConfig = termFactory.getActiveAceFrameConfig();
            final I_IntSet allowedStatus = frameConfig.getAllowedStatus();
            final I_IntSet allowedTypes = frameConfig.getDestRelTypes();
            final Set<I_Position> positions = frameConfig.getViewPositionSet();
            int i = 0;
            final Iterator<I_GetConceptData> itr = termFactory.getConceptIterator();
            while (itr.hasNext() && i++ < 1000) {
                final I_GetConceptData concept = itr.next();

                if (root.isParentOfOrEqualTo(concept, allowedStatus, allowedTypes, positions, addUncommitted)) {
                    processConcept(concept, false);
                }
            }
        }

        @Override
        void visit(int conceptId) throws IOException, TerminologyException {
            throw new AssertionError("The visit method should never be called for this class.");
        }

    }

    class DepthWalker extends AbstractWalker {

        DepthWalker(final I_TermFactory termFactory, final I_SnorocketFactory snorocketFactory,
                final I_GetConceptData root) throws TerminologyException, IOException {
            super(termFactory, snorocketFactory, root);
        }

        @Override
        public void run() throws IOException, TerminologyException {
            visit(root.getConceptId());
        }

        @Override
        void visit(int conceptId) throws IOException, TerminologyException {
            processConcept(termFactory.getConcept(conceptId), true);
        }

    }

    class BreadthWalker extends AbstractWalker {

        private I_IntSet queue;

        BreadthWalker(final I_TermFactory termFactory, final I_SnorocketFactory snorocketFactory,
                final I_GetConceptData root) throws TerminologyException, IOException {
            super(termFactory, snorocketFactory, root);
        }

        @Override
        public void run() throws IOException, TerminologyException {
            queue = termFactory.newIntSet();
            queue.add(root.getConceptId());

            // while (true) {
            // FIXME While we're developing, we limit the depth of traversal
            for (int i = 0; i < 2; i++) {
                final int[] setValues = queue.getSetValues();
                processed.addAll(setValues);
                queue.clear();

                getLogger().info(
                    "**************************************** Depth: " + i + " #concepts: " + setValues.length);

                if (setValues.length < 1) {
                    break;
                }

                for (int parentId : setValues) {
                    final I_GetConceptData parentConcept = termFactory.getConcept(parentId);

                    processConcept(parentConcept, true);
                }
            }
        }

        @Override
        void visit(final int childId) {
            queue.add(childId);
        }

    }

    public TermEntry getClassifyRoot() {
        return classifyRoot;
    }

    public void setClassifyRoot(TermEntry classifyRoot) {
        this.classifyRoot = classifyRoot;
    }

    protected void getClassifierResults(final I_Work worker, final I_SnorocketFactory rocket)
            throws TerminologyException, IOException, Exception {
        final I_TermFactory termFactory = LocalVersionedTerminology.get();
        final I_Path statedPath = termFactory.getPath(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids());
        final I_Path inferredPath = getInferredPath(termFactory, statedPath, termFactory.getActiveAceFrameConfig());

        final I_ConfigAceFrame newConfig = NewDefaultProfile.newProfile("", "", "", "", "");
        newConfig.getEditingPathSet().clear();
        newConfig.addEditingPath(inferredPath);

        worker.getLogger().info("get results");

        // final I_GetConceptData relCharacteristic =
        // termFactory.getConcept(ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.getUids());
        final I_GetConceptData relCharacteristic = termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
        final I_GetConceptData relRefinability = termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());
        final I_GetConceptData relStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

        worker.getLogger().info("Inferred id is " + relCharacteristic.getConceptId());
        worker.getLogger().info("Inferred UUIDs are " + relCharacteristic.getUids());
        worker.getLogger().info("Inferred concept is " + relCharacteristic);

        // TODO
        // mark all existing inferred rels as not current
        // - need to find all current inferred rels and add a new part

        rocket.getResults(new I_SnorocketFactory.I_Callback() {
            public void addRelationship(int c1, int rel, int c2, int relGroup) {
                // worker.getLogger().info("*** " + c1 + " " + rel + " " + c2 +
                // " " + relGroup);

                try {
                    final I_GetConceptData relSource = termFactory.getConcept(c1);
                    final I_GetConceptData relType = termFactory.getConcept(rel);
                    final I_GetConceptData relDestination = termFactory.getConcept(c2);

                    worker.getLogger().info(
                        "*** " + getText(relType) + "\n\t" + getText(relSource) + "\n\t" + getText(relDestination)
                            + "\t" + relGroup);

                    final UUID newRelUid = UUID.randomUUID();

                    termFactory.newRelationship(newRelUid, relSource, relType, relDestination, relCharacteristic,
                        relRefinability, relStatus, relGroup, newConfig);
                } catch (TerminologyException e) {
                    worker.getLogger().severe(e.getLocalizedMessage());
                } catch (IOException e) {
                    worker.getLogger().severe(e.getLocalizedMessage());
                }
            }

            private String getText(I_GetConceptData concept) throws IOException, TerminologyException {
                try {
                    return concept.toString();
                } catch (RuntimeException e) {
                    // for (I_DescriptionVersioned dv:
                    // concept.getDescriptions()) {
                    // return dv.getUniversal().getVersions().get(0).getText();
                    // }
                    return "NONE: " + concept.getConceptId();
                }
            }
        });
    }

    private I_Path getInferredPath(I_TermFactory termFactory, I_Path statedPath, I_ConfigAceFrame config)
            throws TerminologyException, IOException, Exception {
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

}
