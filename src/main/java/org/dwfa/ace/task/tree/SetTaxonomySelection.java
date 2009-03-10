package org.dwfa.ace.task.tree;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
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

@BeanList(specs = { @Spec(directory = "tasks/ide/taxonomy", type = BeanType.TASK_BEAN) })
public class SetTaxonomySelection extends AbstractTask {
    /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   private String conceptPropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey();
   

	private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
       out.writeObject(conceptPropName);
   }

   private void readObject(ObjectInputStream in) throws IOException,
           ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion == dataVersion) {
           conceptPropName = (String) in.readObject();
       } else {
           throw new IOException(
                   "Can't handle dataversion: " + objDataVersion);
       }
   }

   public void complete(I_EncodeBusinessProcess process, I_Work worker)
                        throws TaskFailedException {
       // Nothing to do
   }

   public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
                               throws TaskFailedException {
       try {
           I_ConfigAceFrame configFrame = (I_ConfigAceFrame) worker
               .readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
           I_GetConceptData concept = AceTaskUtil.getConceptFromProperty(process, conceptPropName);
           configFrame.setHierarchySelectionAndExpand(concept);
           return Condition.CONTINUE;
       } catch (IllegalArgumentException e) {
           throw new TaskFailedException(e);
       } catch (IntrospectionException e) {
           throw new TaskFailedException(e);
		} catch (IllegalAccessException e) {
           throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
           throw new TaskFailedException(e);
		} catch (TerminologyException e) {
           throw new TaskFailedException(e);
		} catch (IOException e) {
           throw new TaskFailedException(e);
		}
   }

   public int[] getDataContainerIds() {
       return new int[] {};
   }

   public Collection<Condition> getConditions() {
       return AbstractTask.CONTINUE_CONDITION;
   }

   public String getConceptPropName() {
		return conceptPropName;
	}

	public void setConceptPropName(String conceptPropName) {
		this.conceptPropName = conceptPropName;
	}

}