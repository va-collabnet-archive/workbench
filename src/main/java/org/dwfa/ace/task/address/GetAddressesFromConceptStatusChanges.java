package org.dwfa.ace.task.address;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
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
import org.dwfa.tapi.TerminologyException;

public class GetAddressesFromConceptStatusChanges extends AbstractTask {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;
   
    private String activeConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String addressListPropName = ProcessAttachmentKeys.ADDRESS_LIST.getAttachmentKey();

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(profilePropName);
      out.writeObject(activeConceptPropName);
      out.writeObject(addressListPropName);
   }

   private void readObject(ObjectInputStream in) throws IOException,
         ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         profilePropName = (String) in.readObject();
         activeConceptPropName = (String) in.readObject();
         addressListPropName = (String) in.readObject();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }

   }

   public void complete(I_EncodeBusinessProcess process, I_Work worker)
         throws TaskFailedException {
      // Nothing to do...

   }

   public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
         throws TaskFailedException {
      try {
         I_ConfigAceFrame config = (I_ConfigAceFrame) worker
            .readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
         
         I_ConfigAceFrame workingProfile = (I_ConfigAceFrame) process.readProperty(profilePropName);
         
         Object conceptObj = process.readProperty(activeConceptPropName);
         I_GetConceptData concept = AceTaskUtil.getConceptFromObject(conceptObj);
         
         List<I_ConceptAttributeTuple> attrTupels = concept.getConceptAttributeTuples(workingProfile.getAllowedStatus(),
               workingProfile.getViewPositionSet());
         I_IntSet pathSet = LocalVersionedTerminology.get().newIntSet();
         
         for (I_ConceptAttributeTuple t: attrTupels) {
            pathSet.add(t.getPathId());
         }
         ArrayList<String> addressList = new ArrayList<String>();
         for (int pathId: pathSet.getSetValues()) {
            I_GetConceptData pathConcept = LocalVersionedTerminology.get().getConcept(pathId);
            I_IntList inboxDescTypeList  = LocalVersionedTerminology.get().newIntList();
            inboxDescTypeList.add(ArchitectonicAuxiliary.Concept.USER_INBOX.localize().getNid());
            I_DescriptionTuple inboxDesc = pathConcept.getDescTuple(inboxDescTypeList, config);
            addressList.add(inboxDesc.getText());
         }
         process.setProperty(addressListPropName, addressList);
         
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

}
