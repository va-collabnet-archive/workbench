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
package org.dwfa.ace.task.data.checks;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * <h1>VerifyNoPotentialDups</h1> <br>
 * <p>
 * The <code>VerifyNoPotentialDups</code> class checks the users view paths for
 * any source
 * </p>
 * <p>
 * concepts that have a status of <code>IS_POT_DUP_REL</code>.
 * </p>
 * <p>
 * It is added as a task under tasks/ide/data checks, which enables it to be
 * added to a business process.
 * </P>
 * 
 * <br>
 * <br>
 * 
 * @see <code>org.dwfa.bpa.tasks.AbstractTask</code>
 * @author PeterVawser
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/data checks", type = BeanType.TASK_BEAN) })
public class VerifyNoPotentialDups extends AbstractTask {

    /*
     * Priavte instance variables
     */
    private static final long serialVersionUID = 1;
    private static final int dataVersion = 2;
    private String activeConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();

    /*
     * Property getter/setter methods
     */
    public String getActiveConceptPropName() {
        return activeConceptPropName;
    }

    public void setActiveConceptPropName(String propName) {
        activeConceptPropName = propName;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(activeConceptPropName);
    }// End method writeObject

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            activeConceptPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }// End method readObject

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     * 
     */
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            I_TermFactory termFact = Terms.get();
            I_IntSet potDupTypeSet = termFact.newIntSet();
            potDupTypeSet.add(termFact.uuidToNative(ArchitectonicAuxiliary.Concept.IS_POT_DUP_REL.getUids()));

            I_GetConceptData selectedConcept = (I_GetConceptData) process.getProperty(activeConceptPropName);
            // if(activeConceptPropName != null){
            // Object obj = process.getProperty(activeConceptPropName);
            // selectedConcept = AceTaskUtil.getConceptFromObject(obj);
            // }
            // else{
            // selectedConcept = Terms.get().getConcept(
            // config.getHierarchySelection().getConceptId() );
            // }

            boolean statusValid = false;
            for (I_GetConceptData potDupConcept : selectedConcept.getSourceRelTargets(config.getAllowedStatus(),
                potDupTypeSet, config.getViewPositionSetReadOnly(), 
                config.getPrecedence(), config.getConflictResolutionStrategy())) {
                statusValid = false;
                I_IntSet statusTypeSet = termFact.newIntSet();
                statusTypeSet.add(termFact.uuidToNative(ArchitectonicAuxiliary.Concept.DUPLICATE_PENDING_RETIREMENT.getUids()));

                List<? extends I_ConceptAttributeTuple> statusTuples = potDupConcept.getConceptAttributeTuples(statusTypeSet,
                    config.getViewPositionSetReadOnly(), 
                    config.getPrecedence(), config.getConflictResolutionStrategy());

                for (I_ConceptAttributeTuple tuple : statusTuples) {
                    statusValid = true;
                    break;
                }
            }

            for (I_GetConceptData potDupConcept : selectedConcept.getDestRelOrigins(config.getAllowedStatus(),
                potDupTypeSet, config.getViewPositionSetReadOnly(), 
                config.getPrecedence(), config.getConflictResolutionStrategy())) {
                statusValid = false;
                I_IntSet statusTypeSet = termFact.newIntSet();
                statusTypeSet.add(termFact.uuidToNative(ArchitectonicAuxiliary.Concept.DUPLICATE_PENDING_RETIREMENT.getUids()));

                List<? extends I_ConceptAttributeTuple> statusTuples = potDupConcept.getConceptAttributeTuples(statusTypeSet,
                    config.getViewPositionSetReadOnly(), 
                    config.getPrecedence(), config.getConflictResolutionStrategy());

                for (I_ConceptAttributeTuple tuple : statusTuples) {
                    statusValid = true;
                    break;
                }
            }

            return statusValid ? Condition.TRUE : Condition.FALSE;

        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
    }// End method evaluate

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }// End method complete

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.CONDITIONAL_TEST_CONDITIONS;
    }// End method getConditions

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }// End getDataContainerIds

}// End class VerifyNoPotentialDups
