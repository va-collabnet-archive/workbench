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
package org.dwfa.ace.task.assignment;

import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.tasks.process.LoadSetLaunchProcessFromURL;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Task which takes the assignment address list and launches a review process to
 * the assignment to the appropriate addresses
 * 
 * @author Susan Castillo
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/assignments", type = BeanType.TASK_BEAN) })
public class LaunchAssignmentProcess extends AbstractTask {

    private static class CircularQueue {
        List<String> elements;
        int current = 0;

        public CircularQueue(Collection<String> sourceElements, I_Work worker) {
            super();
            this.elements = new ArrayList<String>(sourceElements);
        }

        public String nextAdr(I_Work worker) {
            if (current == elements.size()) {
                current = 0;
            }
            current++;
            worker.getLogger().info("elements: " + elements.get(current - 1));
            return elements.get(current - 1);
        }
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String selectedAddressesPropName = ProcessAttachmentKeys.SELECTED_ADDRESSES.getAttachmentKey();
    private String conceptUuidPropName = ProcessAttachmentKeys.UUID_LIST.getAttachmentKey();
    private String processFileNamePropName = ProcessAttachmentKeys.PROCESS_FILENAME.getAttachmentKey();
    private String assigneeAddrPropName = ProcessAttachmentKeys.ASSIGNEE.getAttachmentKey();
    private String alternateAddrPropName = ProcessAttachmentKeys.ALT_ASSIGNEE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(selectedAddressesPropName);
        out.writeObject(conceptUuidPropName);
        out.writeObject(processFileNamePropName);
        out.writeObject(assigneeAddrPropName);
        out.writeObject(alternateAddrPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            selectedAddressesPropName = (String) in.readObject();
            conceptUuidPropName = (String) in.readObject();
            processFileNamePropName = (String) in.readObject();
            assigneeAddrPropName = (String) in.readObject();
            alternateAddrPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    @SuppressWarnings("unchecked")
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            List<String> selectedAdr = (List<String>) process.getProperty(selectedAddressesPropName);
            List<UUID> temporaryListUuid = (List<UUID>) process.getProperty(conceptUuidPropName);

            String processFileNameStr = (String) process.getProperty(processFileNamePropName);

            CircularQueue assigees = new CircularQueue(selectedAdr, worker);

            worker.getLogger().info("selectedAdr.size: " + selectedAdr.size());
            worker.getLogger().info("selectedAdr.size: " + selectedAdr);

            // Two duplicate reviewers
            String reviewerOne = assigees.nextAdr(worker);
            worker.getLogger().info("reviewerOne: " + reviewerOne);
            String reviewerTwo = assigees.nextAdr(worker);
            worker.getLogger().info("reviewerTwo: " + reviewerTwo);
            worker.getLogger().info("processFileNameStr: " + processFileNameStr);

            launchAssignment(process, worker, processFileNameStr, reviewerOne, reviewerTwo, temporaryListUuid);
            launchAssignment(process, worker, processFileNameStr, reviewerTwo, reviewerOne, temporaryListUuid);

            return Condition.CONTINUE;

        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (MalformedURLException e) {
            throw new TaskFailedException(e);
        } catch (PropertyVetoException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
    }

    private void launchAssignment(I_EncodeBusinessProcess process, I_Work worker, String processFileNameStr,
            String asignee, String alternate, List<UUID> uuid) throws IntrospectionException, IllegalAccessException,
            InvocationTargetException, TaskFailedException, PropertyVetoException, TerminologyException, IOException {

        I_GetConceptData concept = Terms.get().getConcept(uuid);
        I_ConfigAceFrame configFrame = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

        // worker.getLogger().info("originator is: " +
        // configFrame.getUsername());

        process.setProperty(assigneeAddrPropName, asignee);
        process.setProperty(alternateAddrPropName, alternate);

        LoadSetLaunchProcessFromURL launcher = new LoadSetLaunchProcessFromURL();

        launcher.setOriginator(configFrame.getUsername());
        launcher.setProcessSubject(concept.toString());

        launcher.setProcessURLString(new File(processFileNameStr).toURI().toURL().toExternalForm());

        // worker.getLogger().info("processURLString: " + processURLString);
        launcher.evaluate(process, worker);
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getSelectedAddressesPropName() {
        return selectedAddressesPropName;
    }

    public void setSelectedAddressesPropName(String selectedAddresses) {
        this.selectedAddressesPropName = selectedAddresses;
    }

    public String getConceptUuidPropName() {
        return conceptUuidPropName;
    }

    public void setConceptUuidPropName(String conceptUuidPropName) {
        this.conceptUuidPropName = conceptUuidPropName;
    }

    public String getProcessFileNamePropName() {
        return processFileNamePropName;
    }

    public void setProcessFileNamePropName(String processURLPropName) {
        this.processFileNamePropName = processURLPropName;
    }

    public String getAlternateAddrPropName() {
        return alternateAddrPropName;
    }

    public void setAlternateAddrPropName(String alternateAddrPropName) {
        this.alternateAddrPropName = alternateAddrPropName;
    }

    public String getAssigneeAddrPropName() {
        return assigneeAddrPropName;
    }

    public void setAssigneeAddrPropName(String assigneeAddrPropName) {
        this.assigneeAddrPropName = assigneeAddrPropName;
    }

}
