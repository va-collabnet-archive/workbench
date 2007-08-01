package org.dwfa.ace.task.copy;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

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

@BeanList(specs = { @Spec(directory = "tasks/ace/copy", type = BeanType.TASK_BEAN) })
public class CopyTermEntryToProperty extends AbstractTask {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;
   
    private String propertyName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey();
    private TermEntry termEntry = new TermEntry(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids());

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(termEntry);
      out.writeObject(propertyName);
   }

   private void readObject(ObjectInputStream in) throws IOException,
         ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         termEntry = (TermEntry) in.readObject();
         propertyName = (String) in.readObject();
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
         process.setProperty(propertyName, termEntry);
         return Condition.CONTINUE;
      } catch (IllegalArgumentException e) {
         throw new TaskFailedException(e);
      } catch (IllegalAccessException e) {
         throw new TaskFailedException(e);
      } catch (InvocationTargetException e) {
         throw new TaskFailedException(e);
      } catch (IntrospectionException e) {
         throw new TaskFailedException(e);
      } 
   }

   public Collection<Condition> getConditions() {
      return CONTINUE_CONDITION;
   }

   public int[] getDataContainerIds() {
      return new int[] {};
   }

   public String getPropertyName() {
      return propertyName;
   }

   public void setPropertyName(String propName) {
      this.propertyName = propName;
   }

   public TermEntry getTermEntry() {
      return termEntry;
   }

   public void setTermEntry(TermEntry newStatus) {
      this.termEntry = newStatus;
   }

}
