package org.dwfa.ace.task.conflict;

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
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/conflict", type = BeanType.TASK_BEAN) })
public class PutCompletedConceptsWithConflictsInListView extends AbstractTask {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 2;

   private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
   private TermEntry statusTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.DUAL_REVIEWED.getUids());
   private TermEntry retiredTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.DUPLICATE_PENDING_RETIREMENT.getUids());

   private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
       out.writeObject(profilePropName);
       out.writeObject(statusTermEntry);
       out.writeObject(retiredTermEntry);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion <= dataVersion) {
           profilePropName = (String) in.readObject();
           if (objDataVersion >= 2) {
              statusTermEntry = (TermEntry) in.readObject();
              retiredTermEntry = (TermEntry) in.readObject();
           } else {
              statusTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.DUAL_REVIEWED.getUids());
              retiredTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.DUPLICATE_PENDING_RETIREMENT.getUids());
           }
       } else {
           throw new IOException("Can't handle dataversion: " + objDataVersion);
       }
   }

   public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
       // Nothing to do
   }

   public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
       try {
          I_ConfigAceFrame profileForConflictDetection = (I_ConfigAceFrame) process.readProperty(profilePropName);
          SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
               I_ConfigAceFrame activeProfile = (I_ConfigAceFrame) worker
               .readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
               JList conceptList = activeProfile.getBatchConceptList();
               I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
               model.clear();
            }});
          
          I_IntSet completionStatusNids = LocalVersionedTerminology.get().newIntSet();
          completionStatusNids.add(AceTaskUtil.getConceptFromObject(statusTermEntry).getConceptId());
          completionStatusNids.add(AceTaskUtil.getConceptFromObject(retiredTermEntry).getConceptId());
          
          CompletedConceptConflictDetector conflictIdentifier = new CompletedConceptConflictDetector(LocalVersionedTerminology.get().newIntSet(), 
                LocalVersionedTerminology.get().newIntSet(), profileForConflictDetection, completionStatusNids);
          LocalVersionedTerminology.get().iterateConcepts(conflictIdentifier);
          final List<I_GetConceptData> conflicts = new ArrayList<I_GetConceptData>();
          
          for (int nid: conflictIdentifier.getConflictsNids().getSetValues()) {
             conflicts.add(LocalVersionedTerminology.get().getConcept(nid));
          }
          worker.getLogger().info("ConflictIdentifier results: " + conflictIdentifier);
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
           worker.getLogger().info("Used profile: " + profileForConflictDetection);
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

   public TermEntry getRetiredTermEntry() {
      return retiredTermEntry;
   }

   public void setRetiredTermEntry(TermEntry retiredTermEntry) {
      this.retiredTermEntry = retiredTermEntry;
   }

   public TermEntry getStatusTermEntry() {
      return statusTermEntry;
   }

   public void setStatusTermEntry(TermEntry statusTermEntry) {
      this.statusTermEntry = statusTermEntry;
   }
}
