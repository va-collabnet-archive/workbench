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
package org.dwfa.ace.task.address;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.utypes.UniversalAcePosition;
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

@BeanList(specs = { @Spec(directory = "tasks/ide/address", type = BeanType.TASK_BEAN) })
public class GetAddressesAndPositionsAndChangeStatusFromConceptStatusChanges extends AbstractTask {

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String activeConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String addressListPropName = ProcessAttachmentKeys.ADDRESS_LIST.getAttachmentKey();
    private String positionListPropName = ProcessAttachmentKeys.POSITION_LIST.getAttachmentKey();
    private String statusValuePropName = ProcessAttachmentKeys.STATUS_CONCEPT.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(activeConceptPropName);
        out.writeObject(addressListPropName);
        out.writeObject(positionListPropName);
        out.writeObject(statusValuePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            profilePropName = (String) in.readObject();
            activeConceptPropName = (String) in.readObject();
            addressListPropName = (String) in.readObject();
            positionListPropName = (String) in.readObject();
            statusValuePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            I_TermFactory tf = Terms.get();

            I_ConfigAceFrame workingProfile = (I_ConfigAceFrame) process.getProperty(profilePropName);
            if (workingProfile == null) {
                workingProfile = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            }

            Object conceptObj = process.getProperty(activeConceptPropName);
            I_GetConceptData concept = AceTaskUtil.getConceptFromObject(conceptObj);

            List<? extends I_ConceptAttributeTuple> attrTupels = concept.getConceptAttributeTuples(
                workingProfile.getAllowedStatus(), workingProfile.getViewPositionSetReadOnly(), 
                workingProfile.getPrecedence(), workingProfile.getConflictResolutionStrategy());

            I_GetConceptData newStatus = AceTaskUtil.getConceptFromProperty(process, statusValuePropName);

            I_IntSet pathSet = Terms.get().newIntSet();

            ArrayList<UniversalAcePosition> positionList = new ArrayList<UniversalAcePosition>();
            for (I_ConceptAttributeTuple t : attrTupels) {
                positionList.add(new UniversalAcePosition(tf.getUids(t.getPathId()),
                    tf.convertToThickVersion(t.getVersion())));
                pathSet.add(t.getPathId());
           
                I_ConceptAttributePart part = (I_ConceptAttributePart) t.makeAnalog(newStatus.getConceptNid(), t.getPathId(), Long.MAX_VALUE);
                t.getConVersioned().addVersion(part);
                Terms.get().addUncommitted(concept);
            }
            ArrayList<String> addressList = new ArrayList<String>();

            I_IntList inboxDescTypeList = Terms.get().newIntList();
            inboxDescTypeList.add(ArchitectonicAuxiliary.Concept.USER_INBOX.localize().getNid());
            for (int pathId : pathSet.getSetValues()) {
                I_GetConceptData pathConcept = Terms.get().getConcept(pathId);
                I_DescriptionTuple inboxDesc = pathConcept.getDescTuple(inboxDescTypeList, config);
                if (inboxDesc == null) {
                    worker.getLogger().info("Cannot find inbox for: " + pathConcept.getInitialText());
                    worker.getLogger().info(" inboxDescTypeList: " + inboxDescTypeList.getListArray());
                    for (I_DescriptionVersioned desc : pathConcept.getDescriptions()) {
                        for (I_DescriptionTuple tuple : desc.getTuples()) {
                            worker.getLogger().info(" desc tuple: " + tuple);
                        }
                    }
                } else {
                    addressList.add(inboxDesc.getText());
                }
            }
            process.setProperty(addressListPropName, addressList);
            process.setProperty(positionListPropName, positionList);
            worker.getLogger().info("Selected status values have these positions: " + positionList);
            worker.getLogger().info("Got addresses from status values: " + addressList);

            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getActiveConceptPropName() {
        return activeConceptPropName;
    }

    public void setActiveConceptPropName(String propName) {
        this.activeConceptPropName = propName;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String newStatusPropName) {
        this.profilePropName = newStatusPropName;
    }

    public String getPositionListPropName() {
        return positionListPropName;
    }

    public void setPositionListPropName(String pathListListPropName) {
        this.positionListPropName = pathListListPropName;
    }

    public String getAddressListPropName() {
        return addressListPropName;
    }

    public void setAddressListPropName(String addressListPropName) {
        this.addressListPropName = addressListPropName;
    }

    public String getStatusValuePropName() {
        return statusValuePropName;
    }

    public void setStatusValuePropName(String statusValuePropName) {
        this.statusValuePropName = statusValuePropName;
    }

}
