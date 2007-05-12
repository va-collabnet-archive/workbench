package org.dwfa.ace.task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JList;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace", type = BeanType.TASK_BEAN) })
public class AddChildrenToList extends AbstractTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			//
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
					.readAttachement(AttachmentKeys.ACE_FRAME_CONFIG.name());
			
			I_GetConceptData concept = config.getHierarchySelection();
	
			JList conceptList = config.getBatchConceptList();
			I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
			
			for (I_GetConceptData child: concept.getDestRelOrigins(config.getAllowedStatus(), 
					config.getDestRelTypes(), config.getViewPositionSet(), true)) {
				model.addElement(child);
			}

			return Condition.CONTINUE;
		} catch (IOException e) {
			throw new TaskFailedException(e);
		}
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

}
