package org.dwfa.ace.task.address;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/address", type = BeanType.TASK_BEAN) })

public class SetPropertyToProfileUser extends AbstractTask {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
   private String userPropName = ProcessAttachmentKeys.USERNAME.getAttachmentKey();

   private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
       out.writeObject(profilePropName);
       out.writeObject(userPropName);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion == dataVersion) {
           profilePropName = (String) in.readObject();
           userPropName = (String) in.readObject();
       } else {
           throw new IOException("Can't handle dataversion: " + objDataVersion);
       }
   }

   public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
       // Nothing to do
   }

   public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
       try {
           
           I_ConfigAceFrame profile = (I_ConfigAceFrame) process.readProperty(profilePropName);
           process.setProperty(userPropName, profile.getUsername());
           return Condition.CONTINUE;
           
       } catch (IllegalArgumentException e) {
           throw new TaskFailedException(e);
       } catch (IntrospectionException e) {
           throw new TaskFailedException(e);
       } catch (IllegalAccessException e) {
           throw new TaskFailedException(e);
       } catch (InvocationTargetException e) {
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

   public String getUserPropName() {
      return userPropName;
   }

   public void setUserPropName(String userPropName) {
      this.userPropName = userPropName;
   }

}
