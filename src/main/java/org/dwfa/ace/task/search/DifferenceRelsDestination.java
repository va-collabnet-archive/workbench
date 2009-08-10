package org.dwfa.ace.task.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = {
		@Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
		@Spec(directory = "search", type = BeanType.TASK_BEAN) })
public class DifferenceRelsDestination extends AbstractDifferenceRels {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion <= dataVersion) {
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}


	protected List<I_RelTuple> getTuplesToCompare(I_ConfigAceFrame frameConfig,
			I_GetConceptData conceptToTest, Set<I_Position> viewSet)
			throws IOException {
		return conceptToTest.getDestRelTuples(frameConfig.getAllowedStatus(), 
				null, viewSet, false);
	}

}
