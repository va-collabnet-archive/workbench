package org.dwfa.ace.task.status;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JList;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/status", type = BeanType.TASK_BEAN) })
public class PutConceptWithAllowedStatusInListView extends AbstractTask {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

   private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
       out.writeObject(profilePropName);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion == dataVersion) {
           profilePropName = (String) in.readObject();
       } else {
           throw new IOException("Can't handle dataversion: " + objDataVersion);
       }
   }

   public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
       // Nothing to do
   }

   public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
       try {
          I_ConfigAceFrame profileForProcessing = (I_ConfigAceFrame) process.readProperty(profilePropName);
          SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
               I_ConfigAceFrame activeProfile = (I_ConfigAceFrame) worker
               .readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
               JList conceptList = activeProfile.getBatchConceptList();
               I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
               model.clear();
            }});
          
          StatusCounter statusCounter = new StatusCounter(LocalVersionedTerminology.get().newIntSet(), profileForProcessing, 1, null);
          LocalVersionedTerminology.get().iterateConcepts(statusCounter);
          final List<I_GetConceptData> conflicts = new ArrayList<I_GetConceptData>();
          
          for (int nid: statusCounter.getIdentifiedNids().getSetValues()) {
             conflicts.add(LocalVersionedTerminology.get().getConcept(nid));
          }
          worker.getLogger().info("StatusCounter results: " + statusCounter);
          SwingUtilities.invokeAndWait(new Runnable() {
             public void run() {
                I_ConfigAceFrame activeProfile = (I_ConfigAceFrame) worker
                .readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
                JList conceptList = activeProfile.getBatchConceptList();
                I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
                for (I_GetConceptData conflict: conflicts) {
                   model.addElement(conflict);
                }
             }});
           worker.getLogger().info("Used profile: " + profileForProcessing);
           return Condition.CONTINUE;
           
       } catch (IllegalArgumentException e) {
           throw new TaskFailedException(e);
       } catch (IntrospectionException e) {
           throw new TaskFailedException(e);
       } catch (IllegalAccessException e) {
           throw new TaskFailedException(e);
       } catch (InvocationTargetException e) {
           throw new TaskFailedException(e);
       } catch (Exception e) {
          throw new TaskFailedException(e);
      } 
   }

   public int[] getDataContainerIds() {
       return new int[] {};
   }

   public Collection<Condition> getConditions() {
       return AbstractTask.CONTINUE_CONDITION;
   }

   public String getProfilePropName() {
       return profilePropName;
   }

   public void setProfilePropName(String profilePropName) {
       this.profilePropName = profilePropName;
   }
}
