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
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.classify.I_SnorocketFactory.I_Callback;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.cement.SNOMED.Concept;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class ClassifySelected extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    // /**
    // * Bean property
    // */
    // private String factoryClass =
    // "org.dwfa.ace.task.classify.StubSnorocketFactory";

    // /**
    // * Bean property
    // */
    // private TermEntry classifyRoot = new
    // TermEntry(Type3UuidFactory.SNOMED_ROOT_UUID);

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

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        long startTime = System.currentTimeMillis();

        try {
            final I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            final I_GetConceptData termComponent = config.getHierarchySelection();
            final I_TermFactory termFactory = LocalVersionedTerminology.get();

            worker.getLogger().info("** Classifying: " + termComponent);
            final Concept is_a = SNOMED.Concept.IS_A;
            int isaId = termFactory.uuidToNative(is_a.getUids());
            worker.getLogger().info("**** isaId: " + isaId + ": " + is_a);

            final I_SnorocketFactory rocket = (I_SnorocketFactory) process.readAttachement(ProcessKey.SNOROCKET.getAttachmentKey());

            new ConceptProcesser(termFactory, rocket).processConcept(worker, termComponent);

            rocket.classify();

            worker.getLogger().info("Classified! " + (System.currentTimeMillis() - startTime) + "s");

            rocket.getResults(new I_Callback() {
                public void addRelationship(int conceptId1, int roleId, int conceptId2, int group) {
                    System.err.println("###### " + conceptId1 + " " + roleId + " " + conceptId2);
                }
            });

        } catch (IOException e) {
            handleException(e);
        } catch (TerminologyException e) {
            handleException(e);
        }

        return Condition.CONTINUE;
    }

    private void handleException(Exception e) throws TaskFailedException {
        throw new TaskFailedException(e);
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    class ConceptProcesser {

        final protected I_SnorocketFactory rocket;

        final protected I_IntSet isaTypes;
        final protected int definingCharacteristic;
        final protected I_IntSet activeStatus;
        final protected Set<I_Position> latestStated;

        ConceptProcesser(final I_TermFactory termFactory, final I_SnorocketFactory rocket) throws TerminologyException,
                IOException {
            this.rocket = rocket;

            final org.dwfa.cement.ArchitectonicAuxiliary.Concept defining_characteristic = ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC;
            definingCharacteristic = termFactory.getConcept(defining_characteristic.getUids()).getConceptId();

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

        }

        final void processConcept(I_Work worker, final I_GetConceptData concept) throws IOException,
                TerminologyException {
            final int conceptId = concept.getConceptId();

            final List<I_ConceptAttributeTuple> tuples = concept.getConceptAttributeTuples(activeStatus, latestStated);
            if (tuples.size() == 1) {
                worker.getLogger().info(
                    "Add concept: " + conceptId + " : " + concept.getId() + ": " + tuples.get(0).isDefined());
                rocket.addConcept(conceptId, tuples.get(0).isDefined());

                final List<I_RelTuple> relTuples = concept.getSourceRelTuples(activeStatus, null, latestStated, true);
                for (I_RelTuple relTuple : relTuples) {
                    if (definingCharacteristic == relTuple.getCharacteristicId()) {
                        worker.getLogger().info(
                            "Add relationship: " + relTuple.getRelTypeId() + " " + relTuple.getC2Id());
                        rocket.addRelationship(relTuple.getC1Id(), relTuple.getRelTypeId(), relTuple.getC2Id(),
                            relTuple.getGroup());
                    }
                }

            } else if (tuples.size() > 1) {
                throw new AssertionError("Unexpected number of tuples: " + tuples.size() + " for " + concept);
            }
        }

    }

}
