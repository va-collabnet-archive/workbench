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
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class ClassifyCurrent extends AbstractTask {

    private int definingCharacteristic = -1;
    private int statedCharacteristic = -1;
    private int currentStatus = -1;
    private int limitedStatus = -1;
    private Logger logger = null;

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

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        long startTime = System.currentTimeMillis();

        try {
            logger = worker.getLogger();
            definingCharacteristic = LocalVersionedTerminology.get().getConcept(
                ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()).getConceptId();
            statedCharacteristic = LocalVersionedTerminology.get().getConcept(
                ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()).getConceptId();
            currentStatus = LocalVersionedTerminology.get()
                .getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids())
                .getConceptId();
            limitedStatus = LocalVersionedTerminology.get()
                .getConcept(ArchitectonicAuxiliary.Concept.LIMITED.getUids())
                .getConceptId();

            // get uncommitted editing concept
            final I_HostConceptPlugins host = (I_HostConceptPlugins) worker.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());
            final I_GetConceptData termComponent = (I_GetConceptData) host.getTermComponent();

            if (null == termComponent) {
                throw new TaskFailedException("Could not find current concept.");
            }

            final I_SnorocketFactory rocket = (I_SnorocketFactory) process.readAttachement(ProcessKey.SNOROCKET.getAttachmentKey());
            final I_TermFactory termFactory = LocalVersionedTerminology.get();

            // load the new concept into classifer
            logger.info("** Classifying: " + termComponent);
            int isaId = termFactory.uuidToNative(SNOMED.Concept.IS_A.getUids());
            logger.info("**** isaId: " + isaId + ": " + SNOMED.Concept.IS_A);

            processConcept(termComponent, rocket);

            logger.info("init time: " + (System.currentTimeMillis() - startTime) + "s");

            rocket.classify();

            logger.info("Classified! " + (System.currentTimeMillis() - startTime) + "s");

            // rocket.getResults(new I_Callback() {
            // public void addRelationship(int conceptId1, int roleId, int
            // conceptId2, int group) {
            // System.err.println("###### " + conceptId1 + " " + roleId + " " +
            // conceptId2);
            // }
            // });
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

    private void processConcept(final I_GetConceptData concept, final I_SnorocketFactory rocket) throws IOException,
            TerminologyException {

        final int conceptId = concept.getConceptId();

        // if (concept.getConceptAttributes().getTuples().size() == 1 &&
        // concept.getUncommittedConceptAttributes() != null) {
        // add concept then add rel
        I_ConceptAttributeVersioned conceptAttributes = concept.getUncommittedConceptAttributes();
        if (null == conceptAttributes) {
            conceptAttributes = concept.getConceptAttributes();
        }

        final List<I_ConceptAttributeTuple> tuples = conceptAttributes.getTuples();
        boolean isDefined = false;

        I_ConceptAttributeTuple tu = tuples.get(tuples.size() - 1);
        if (tu.getConceptStatus() == currentStatus || tu.getConceptStatus() == limitedStatus) {
            isDefined = tu.isDefined();
        }

        else {
            return;
        }

        logger.info("Add concept: " + concept + " : " + concept.getId() + ": " + isDefined);

        rocket.addConcept(conceptId, isDefined);

        // add rels assume all the rels are for classification

        logger.info("Source Rels:");
        processRels(rocket, concept.getSourceRels());
        logger.info("Uncommitted Source Rels:");
        processRels(rocket, concept.getUncommittedSourceRels());
        // }
    }

    private void processRels(final I_SnorocketFactory rocket, final List<? extends I_RelVersioned> list) {
        if (null != list) {
            for (I_RelVersioned rel : list) {
                final I_RelTuple lastTuple = rel.getLastTuple();
                if (definingCharacteristic == lastTuple.getCharacteristicId()
                    || statedCharacteristic == lastTuple.getCharacteristicId()) {
                    if (lastTuple.getStatusId() == currentStatus || lastTuple.getStatusId() == limitedStatus) {
                        logger.info("Add relationship: " + lastTuple.getC1Id() + " " + lastTuple.getRelTypeId() + " "
                            + lastTuple.getC2Id());
                        rocket.addRelationship(lastTuple.getC1Id(), lastTuple.getRelTypeId(), lastTuple.getC2Id(),
                            lastTuple.getGroup());
                    }

                }
            }
        }
    }

}
