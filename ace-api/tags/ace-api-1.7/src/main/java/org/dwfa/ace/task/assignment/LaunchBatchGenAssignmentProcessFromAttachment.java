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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.tasks.process.LoadSetLaunchProcessFromAttachment;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/assignments", type = BeanType.TASK_BEAN) })

public class LaunchBatchGenAssignmentProcessFromAttachment extends AbstractTask {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;
   
   private String batchGenAssigneePropName = ProcessAttachmentKeys.SELECTED_ADDRESSES.getAttachmentKey();
   private String uuidListListPropName = ProcessAttachmentKeys.BATCH_UUID_LIST2.getAttachmentKey();
   private String proccessToLaunchPropName = ProcessAttachmentKeys.PROCESS_TO_LAUNCH.getAttachmentKey();
   private String processToAssignPropName = ProcessAttachmentKeys.TO_ASSIGN_PROCESS.getAttachmentKey();
   private String assigneeAddrPropName;
   
   private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
       out.writeObject(batchGenAssigneePropName);
       out.writeObject(uuidListListPropName);
       out.writeObject(proccessToLaunchPropName);
       out.writeObject(assigneeAddrPropName);
       out.writeObject(processToAssignPropName);
   }

   private void readObject(ObjectInputStream in) throws IOException,
           ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion == dataVersion) {
         batchGenAssigneePropName = (String) in.readObject();
         uuidListListPropName = (String) in.readObject();
         proccessToLaunchPropName = (String) in.readObject();
         assigneeAddrPropName = (String) in.readObject();
         processToAssignPropName = (String) in.readObject();
       } else {
           throw new IOException(
                   "Can't handle dataversion: " + objDataVersion);
       }
   }

   public void complete(I_EncodeBusinessProcess process, I_Work worker)
                        throws TaskFailedException {
       // Nothing to do
   }

   @SuppressWarnings("unchecked")
   public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
                               throws TaskFailedException {
       try {
         //list string returned from GetSelectedAddresses need only the 1st address in the list
         List<String> selectedAdr = (List<String>) process.readProperty(batchGenAssigneePropName);
         String assigneeStr = selectedAdr.get(0);
           ArrayList<Collection<UUID>> temporaryListList =
               (ArrayList<Collection<UUID>>) process.readProperty(uuidListListPropName);
                       

           String assignmentProcessFileNameStr = (String) process.readProperty(processToAssignPropName);
           worker.getLogger().info("proccessToLaunchPropName: " + proccessToLaunchPropName);
           worker.getLogger().info("assignmentProcessFileNameStr: " + assignmentProcessFileNameStr);
           worker.getLogger().info("assigneeStr: " + assigneeStr);
           worker.getLogger().info("temporaryListList: " + temporaryListList);
           
           launchAssignment(process, worker, assignmentProcessFileNameStr, assigneeStr, temporaryListList);

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
      } catch (ClassNotFoundException e) {
         throw new TaskFailedException(e);
      }
   }

   private void launchAssignment(I_EncodeBusinessProcess process, I_Work worker, 
         String assignmentProcessFileNameStr, String assignee, ArrayList<Collection<UUID>> uuidListList) 
   throws IntrospectionException, IllegalAccessException, InvocationTargetException, 
         TaskFailedException, PropertyVetoException, TerminologyException, IOException, ClassNotFoundException {
      
       I_ConfigAceFrame configFrame = (I_ConfigAceFrame) worker
       .readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
       
       //get the process name of the assignment process to set in the batch process name
       File assignmentProcessFile = new File(assignmentProcessFileNameStr);
       ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(assignmentProcessFile)));
       BusinessProcess assignmentProcess = (BusinessProcess) ois.readObject();
       ois.close();
       
       //get the number of assignments that will be generated and add to process name
       int sizeOfList =  uuidListList.size();
       
 //      worker.getLogger().info("originator is: " + configFrame.getUsername());
       
      LoadSetLaunchProcessFromAttachment launcher = new LoadSetLaunchProcessFromAttachment();

      String assignProcessNameStr = assignmentProcess.getName();
      
      launcher.setOriginator(configFrame.getUsername());
      launcher.setProcessName("Generate "+sizeOfList +" Assignments");
      launcher.setProcessSubject(assignProcessNameStr);

      launcher.setProcessPropName(proccessToLaunchPropName);
      launcher.setDestination(assignee);
      launcher.evaluate(process, worker);
   }

   public int[] getDataContainerIds() {
       return new int[] {};
   }

   public Collection<Condition> getConditions() {
       return AbstractTask.CONTINUE_CONDITION;
   }

   public String getAssigneeAddrPropName() {
      return assigneeAddrPropName;
   }

   public void setAssigneeAddrPropName(String assigneeAddrPropName) {
      this.assigneeAddrPropName = assigneeAddrPropName;
   }

   public String getBatchGenAssigneePropName() {
      return batchGenAssigneePropName;
   }

   public void setBatchGenAssigneePropName(String batchGenAssigneePropName) {
      this.batchGenAssigneePropName = batchGenAssigneePropName;
   }

   public String getProccessToLaunchPropName() {
      return proccessToLaunchPropName;
   }

   public void setProccessToLaunchPropName(String processFilePropName) {
      this.proccessToLaunchPropName = processFilePropName;
   }

   public String getProcessToAssignPropName() {
      return processToAssignPropName;
   }

   public void setProcessToAssignPropName(String processToAssignPropName) {
      this.processToAssignPropName = processToAssignPropName;
   }

   public String getUuidListListPropName() {
      return uuidListListPropName;
   }

   public void setUuidListListPropName(String uuidListListPropName) {
      this.uuidListListPropName = uuidListListPropName;
   }

}

