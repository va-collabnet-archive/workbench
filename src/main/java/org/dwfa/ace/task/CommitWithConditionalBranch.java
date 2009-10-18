package org.dwfa.ace.task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = 
{ @Spec(directory = "tasks/ide/db", type = BeanType.TASK_BEAN)})
public class CommitWithConditionalBranch extends AbstractTask {
	private static final long serialVersionUID = 1;

	private static final int dataVersion = 1;

	private String errorsAndWarningsPropName = ProcessAttachmentKeys.ERRORS_AND_WARNINGS.getAttachmentKey();

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(errorsAndWarningsPropName);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			errorsAndWarningsPropName = (String) in.readObject();
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
		try {
			List<AlertToDataConstraintFailure> errorsAndWarnings = 
				LocalVersionedTerminology.get().getCommitErrorsAndWarnings();
			process.setProperty(errorsAndWarningsPropName, errorsAndWarnings);
			if (errorsAndWarnings.size() > 0) {
				LocalVersionedTerminology.get().cancel();
				return Condition.FALSE;
			}
			LocalVersionedTerminology.get().commit();
			return Condition.TRUE;
		} catch (Exception ex) {
			throw new TaskFailedException();
		}
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
		return CONDITIONAL_TEST_CONDITIONS;
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {};
	}

}
