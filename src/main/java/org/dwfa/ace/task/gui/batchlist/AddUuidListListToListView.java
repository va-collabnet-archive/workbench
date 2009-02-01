package org.dwfa.ace.task.gui.batchlist;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.swing.JList;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/listview", type = BeanType.TASK_BEAN) })
public class AddUuidListListToListView extends AbstractTask {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;
   
   /**
    * Property name for a list of uuid lists, typically used to represent a list
    * of concepts in a transportable way. 
    */
    private String uuidListListPropName = ProcessAttachmentKeys.UUID_LIST_LIST.getAttachmentKey();


   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(uuidListListPropName);
   }

   private void readObject(ObjectInputStream in) throws IOException,
         ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         uuidListListPropName = (String) in.readObject();
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
         I_ConfigAceFrame config = (I_ConfigAceFrame) worker
               .readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            
         JList conceptList = config.getBatchConceptList();
         final I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
         
         final List<List<UUID>> idListList = (ArrayList<List<UUID>>) process.readProperty(uuidListListPropName);
         AceLog.getAppLog().info("Adding list of size: " + idListList.size());
         
         SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                for (List<UUID> idList: idListList) {
                    try {
                        I_GetConceptData conceptInList = AceTaskUtil.getConceptFromObject(idList);
                        model.addElement(conceptInList);
                    } catch (TerminologyException e) {
                        AceLog.getAppLog().alertAndLogException(e);
                        return;
                    } catch (IOException e) {
                        AceLog.getAppLog().alertAndLogException(e);
                        return;
                    }
                 }
            }
             
         });
         
         return Condition.CONTINUE;
      } catch (IntrospectionException e) {
         throw new TaskFailedException(e);
      } catch (IllegalAccessException e) {
         throw new TaskFailedException(e);
      } catch (InvocationTargetException e) {
         throw new TaskFailedException(e);
      } catch (InterruptedException e) {
          throw new TaskFailedException(e);
    }
   }

   public Collection<Condition> getConditions() {
      return CONTINUE_CONDITION;
   }

   public int[] getDataContainerIds() {
      return new int[] {};
   }

   public String getUuidListListPropName() {
      return uuidListListPropName;
   }

   public void setUuidListListPropName(String uuidListListPropName) {
      this.uuidListListPropName = uuidListListPropName;
   }

}
