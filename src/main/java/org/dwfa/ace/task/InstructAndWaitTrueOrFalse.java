package org.dwfa.ace.task;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/instruct", type = BeanType.TASK_BEAN),
		@Spec(directory = "tasks/ide/wfpanel", type = BeanType.TASK_BEAN) })
public class InstructAndWaitTrueOrFalse extends InstructAndWaitStepOrCancel {

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

	
	protected String getTrueImage() {
		return "/16x16/plain/check.png";
	}

	protected String getFalseImage() {
		return "/16x16/plain/delete.png";
	}

}
