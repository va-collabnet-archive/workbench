package org.dwfa.ace.task.path;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
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

@BeanList(specs = { @Spec(directory = "tasks/ide/path", type = BeanType.TASK_BEAN) })
public class NewEditPathForUser extends AbstractTask {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   /**
    * A concept that is to be the parent of the newly created path concept, and that is also associated with 
    * a valid path in the database, from which the new path's origin will be derived. 
    */
   private TermEntry parentPathTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.DEVELOPMENT.getUids());

   private String userPropName = ProcessAttachmentKeys.USERNAME.getAttachmentKey();

   private String originTime = "latest";

   private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(parentPathTermEntry);
      out.writeObject(originTime);
      out.writeObject(profilePropName);
      out.writeObject(userPropName);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         parentPathTermEntry = (TermEntry) in.readObject();
         originTime = (String) in.readObject();
         profilePropName = (String) in.readObject();
         userPropName = (String) in.readObject();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }

   }

   public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
      // Nothing to do...

   }

   public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
      try {
         String username = (String) process.readProperty(userPropName);
         I_TermFactory tf = LocalVersionedTerminology.get();
         I_ConfigAceFrame activeProfile = tf.getActiveAceFrameConfig();
         Set<I_Path> savedEditingPaths = new HashSet<I_Path>(activeProfile.getEditingPathSet());
         try {

            I_GetConceptData newPathConcept = NewEditPathForUserFromProperty.createComponents(username, tf, activeProfile, parentPathTermEntry);

            tf.commit();

            Set<I_Position> origins = new HashSet<I_Position>();

            I_Path parentPath = tf.getPath(parentPathTermEntry.ids);
            origins.add(tf.newPosition(parentPath, tf.convertToThinVersion(originTime)));

            I_Path editPath = tf.newPath(origins, newPathConcept);
            I_ConfigAceFrame profile = (I_ConfigAceFrame) process.readProperty(profilePropName);
            profile.getEditingPathSet().clear();
            profile.addEditingPath(editPath);
            profile.getViewPositionSet().clear();
            profile.addViewPosition(tf.newPosition(editPath, Integer.MAX_VALUE));
            tf.commit();

         } catch (Exception e) {
            throw new TaskFailedException(e);
         }
         activeProfile.getEditingPathSet().clear();
         activeProfile.getEditingPathSet().addAll(savedEditingPaths);
         return Condition.CONTINUE;
      } catch (Exception e) {
         throw new TaskFailedException(e);
      }
   }

   public Collection<Condition> getConditions() {
      return CONTINUE_CONDITION;
   }

   public int[] getDataContainerIds() {
      return new int[] {};
   }

   public String getUserPropName() {
      return userPropName;
   }

   public void setUserPropName(String profilePropName) {
      this.userPropName = profilePropName;
   }

   public String getOriginTime() {
      return originTime;
   }

   public void setOriginTime(String originTime) {
      this.originTime = originTime;
   }

   public TermEntry getParentPathTermEntry() {
      return parentPathTermEntry;
   }

   public void setParentPathTermEntry(TermEntry parentPath) {
      this.parentPathTermEntry = parentPath;
   }

   public String getProfilePropName() {
      return profilePropName;
   }

   public void setProfilePropName(String profilePropName) {
      this.profilePropName = profilePropName;
   }

}
