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
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.AceTaskUtil;
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

@BeanList(specs = { @Spec(directory = "tasks/ide/address", type = BeanType.TASK_BEAN) })
public class GetAddressesFromConceptStatusChanges extends AbstractTask {

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String activeConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String addressListPropName = ProcessAttachmentKeys.ADDRESS_LIST.getAttachmentKey();
    private String pathListListPropName = ProcessAttachmentKeys.PATH_LIST_LIST.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(activeConceptPropName);
        out.writeObject(addressListPropName);
        out.writeObject(pathListListPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            profilePropName = (String) in.readObject();
            activeConceptPropName = (String) in.readObject();
            addressListPropName = (String) in.readObject();
            pathListListPropName = (String) in.readObject();
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

            I_ConfigAceFrame workingProfile = (I_ConfigAceFrame) process.getProperty(profilePropName);
            if (workingProfile == null) {
                workingProfile = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            }

            Object conceptObj = process.getProperty(activeConceptPropName);
            I_GetConceptData concept = AceTaskUtil.getConceptFromObject(conceptObj);

            List<? extends I_ConceptAttributeTuple> attrTupels = concept.getConceptAttributeTuples(
                workingProfile.getAllowedStatus(), workingProfile.getViewPositionSetReadOnly(),
                workingProfile.getPrecedence(), workingProfile.getConflictResolutionStrategy());
            I_IntSet pathSet = Terms.get().newIntSet();

            for (I_ConceptAttributeTuple t : attrTupels) {
                pathSet.add(t.getPathId());
            }
            ArrayList<String> addressList = new ArrayList<String>();
            ArrayList<Collection<UUID>> pathListList = new ArrayList<Collection<UUID>>();

            I_IntList inboxDescTypeList = Terms.get().newIntList();
            inboxDescTypeList.add(ArchitectonicAuxiliary.Concept.USER_INBOX.localize().getNid());
            for (int pathId : pathSet.getSetValues()) {

                I_GetConceptData pathConcept = Terms.get().getConcept(pathId);
                pathListList.add(pathConcept.getUids());
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
            process.setProperty(pathListListPropName, pathListList);
            worker.getLogger().info("Selected status values have these paths: " + pathListList);
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

    public String getPathListListPropName() {
        return pathListListPropName;
    }

    public void setPathListListPropName(String pathListListPropName) {
        this.pathListListPropName = pathListListPropName;
    }

    public String getAddressListPropName() {
        return addressListPropName;
    }

    public void setAddressListPropName(String addressListPropName) {
        this.addressListPropName = addressListPropName;
    }

}
