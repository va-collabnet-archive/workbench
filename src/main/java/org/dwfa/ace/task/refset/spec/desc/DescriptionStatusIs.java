package org.dwfa.ace.task.refset.spec.desc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.task.refset.spec.AddStructuralQueryToRefsetSpec;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Task for adding a "description status is" structural query to a refset spec.
 * @author Chrissy Hill
 *
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN) })
public class DescriptionStatusIs extends AddStructuralQueryToRefsetSpec {
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

	protected int getStructuralQueryTokenId() throws IOException, TerminologyException {
		return RefsetAuxiliary.Concept.DESC_STATUS_IS.localize().getNid();
	}

}
