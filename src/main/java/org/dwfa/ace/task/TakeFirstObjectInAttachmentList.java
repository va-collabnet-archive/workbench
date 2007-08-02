package org.dwfa.ace.task;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/assignments", type = BeanType.TASK_BEAN) })
public class TakeFirstObjectInAttachmentList extends AbstractTask {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   private String listPropName = ProcessAttachmentKeys.ADDRESS_LIST.getAttachmentKey();

   private String objectPropName = ProcessAttachmentKeys.ASSIGNEE.getAttachmentKey();


   private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
       out.writeObject(listPropName);
       out.writeObject(objectPropName);
   }

   private void readObject(ObjectInputStream in) throws IOException,
           ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion == dataVersion) {
           listPropName = (String) in.readObject();
           objectPropName = (String) in.readObject();
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
           List temporaryList =
               (List) process.readProperty(listPropName);

           if (worker.getLogger().isLoggable(Level.FINE)) {
               worker.getLogger().fine(("Removing first item in property list."));
           }

           process.setProperty(this.objectPropName, temporaryList.remove(0));
           return Condition.CONTINUE;
       } catch (IllegalArgumentException e) {
           throw new TaskFailedException(e);
       } catch (InvocationTargetException e) {
           throw new TaskFailedException(e);
       } catch (IntrospectionException e) {
           throw new TaskFailedException(e);
       } catch (IllegalAccessException e) {
           throw new TaskFailedException(e);
       } 
   }

   public Collection<Condition> getConditions() {
       return CONTINUE_CONDITION;
   }

   public int[] getDataContainerIds() {
       return new int[] {};
   }

   public String getListPropName() {
       return listPropName;
   }

   public void setListPropName(String listName) {
       this.listPropName = listName;
   }

   public String getObjectPropName() {
       return objectPropName;
   }

   public void setObjectPropName(String conceptKey) {
       this.objectPropName = conceptKey;
   }

}
