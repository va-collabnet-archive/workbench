package org.dwfa.ace.task.copy;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

@BeanList(specs = { @Spec(directory = "tasks/ide/copy", type = BeanType.TASK_BEAN) })
public class AddToTermMap extends AbstractTask {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;
   
    private String mapPropName = ProcessAttachmentKeys.TERM_ENTRY_MAP.getAttachmentKey();
    private TermEntry keyTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids());
    private TermEntry valueTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids());

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(keyTermEntry);
      out.writeObject(valueTermEntry);
      out.writeObject(mapPropName);
   }

   private void readObject(ObjectInputStream in) throws IOException,
         ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         keyTermEntry = (TermEntry) in.readObject();
         valueTermEntry = (TermEntry) in.readObject();
         mapPropName = (String) in.readObject();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }

   }

   public void complete(I_EncodeBusinessProcess process, I_Work worker)
         throws TaskFailedException {
      // Nothing to do...

   }

   @SuppressWarnings("unchecked")
   public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
         throws TaskFailedException {
      try {
         Map<TermEntry, TermEntry> termMap = (Map<TermEntry, TermEntry>) process.readProperty(mapPropName);
         if (termMap == null) {
            termMap = new HashMap<TermEntry, TermEntry>();
            process.setProperty(mapPropName, termMap);
         }
         termMap.put(keyTermEntry, valueTermEntry);
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

   public String getMapPropName() {
      return mapPropName;
   }

   public void setMapPropName(String propName) {
      this.mapPropName = propName;
   }

   public TermEntry getKeyTermEntry() {
      return keyTermEntry;
   }

   public void setKeyTermEntry(TermEntry newStatus) {
      this.keyTermEntry = newStatus;
   }

   public TermEntry getValueTermEntry() {
      return valueTermEntry;
   }

   public void setValueTermEntry(TermEntry valueTermEntry) {
      this.valueTermEntry = valueTermEntry;
   }

}
