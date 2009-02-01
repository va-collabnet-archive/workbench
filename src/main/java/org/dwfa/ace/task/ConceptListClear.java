package org.dwfa.ace.task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JList;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/listview", type = BeanType.TASK_BEAN) })
public class ConceptListClear extends AbstractTask {

	private static final long serialVersionUID = 1;

	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			// nothing to read...
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		I_ConfigAceFrame config = (I_ConfigAceFrame) worker
				.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
		JList conceptList = config.getBatchConceptList();
		I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
		model.clear();
		return Condition.CONTINUE;
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do

	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {};
	}
}
