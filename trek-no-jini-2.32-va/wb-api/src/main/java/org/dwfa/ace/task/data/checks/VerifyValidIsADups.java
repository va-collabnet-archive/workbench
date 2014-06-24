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
 * <h1>VerifyValidIsADups</h1> <br>
 * <p>
 * The <code>VerifyValidIsADups</code> class checks the users view paths for any
 * source
 * </p>
 * <p>
 * concepts that have a status of <code>IS_A_DUP_REL</code>.
 * </p>
 * <p>
 * If the concept has a <code>IS_A_DUP_REL</code> relationship, then check to
 * ensure that
 * </p>
 * <p>
 * either the source or destination concept has a status of
 * <code>DUPLICATE_PENDING_RETIREMENT</code>.
 * </p>
 * 
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
public class VerifyValidIsADups extends AbstractTask {

    /*
     * Priavte instance variables
     */
    private static final long serialVersionUID = 1;
    private static final int dataVersion = 2;
    private String activeConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    private I_ConfigAceFrame config;
    private I_TermFactory termFact;

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
     */
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            termFact = Terms.get();
            I_IntSet isDupTypeSet = termFact.newIntSet();
            isDupTypeSet.add(termFact.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_DUP_REL.getUids()));

            I_GetConceptData selectedConcept = (I_GetConceptData) process.getProperty(activeConceptPropName);
            // if(propName != null){
            // Object obj = process.getProperty(propName);
            // selectedConcept = AceTaskUtil.getConceptFromObject(obj);
            // }
            // else{
            // selectedConcept = Terms.get().getConcept(
            // config.getHierarchySelection().getConceptNid() );
            // }

            I_IntSet pendingRetirementStatusSet = termFact.newIntSet();
            pendingRetirementStatusSet.add(termFact.uuidToNative(ArchitectonicAuxiliary.Concept.DUPLICATE_PENDING_RETIREMENT.getUids()));

            for (I_GetConceptData child : selectedConcept.getSourceRelTargets(pendingRetirementStatusSet, isDupTypeSet,
                config.getViewPositionSetReadOnly(), 
                config.getPrecedence(), config.getConflictResolutionStrategy())) {
                return Condition.FALSE;
            }

            for (I_GetConceptData child : selectedConcept.getDestRelOrigins(pendingRetirementStatusSet, isDupTypeSet,
                config.getViewPositionSetReadOnly(), 
                config.getPrecedence(), config.getConflictResolutionStrategy())) {
                return Condition.FALSE;
            }

            // for (I_GetConceptData child:
            // selectedConcept.getSourceRelTargets(config.getAllowedStatus(),
            // isDupTypeSet, config.getViewPositionSet(), true)) {
            // for(I_GetConceptData grandChild:
            // child.getSourceRelTargets(pendingRetirementStatusSet,null,
            // config.getViewPositionSet(), true)){
            // return Condition.FALSE;
            // }
            // for(I_GetConceptData grandChild:
            // child.getDestRelOrigins(pendingRetirementStatusSet,null,
            // config.getViewPositionSet(), true)){
            // return Condition.FALSE;
            // }
            // }

            return Condition.TRUE;

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

    // private boolean performCheck(Set<I_GetConceptData> relTargets){
    // for (I_GetConceptData child:
    // selectedConcept.getSourceRelTargets(config.getAllowedStatus(),
    // isDupTypeSet, config.getViewPositionSet(), true)) {
    //        	
    // for(I_GetConceptData grandChild:
    // child.getSourceRelTargets(pendingRetirementStatusSet,null,
    // config.getViewPositionSet(), true)){
    // return Condition.TRUE;
    // }
    // for(I_GetConceptData grandChild:
    // child.getDestRelOrigins(pendingRetirementStatusSet,null,
    // config.getViewPositionSet(), true)){
    // return Condition.TRUE;
    // }
    // }
    // }

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

}// End class VerifyValidIsADups
