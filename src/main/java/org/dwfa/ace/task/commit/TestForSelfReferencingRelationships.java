package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = {
		@Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestForSelfReferencingRelationships extends AbstractConceptTest {

	private static final long serialVersionUID = 1;
	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			//
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	@Override
	public List<AlertToDataConstraintFailure> test(I_GetConceptData concept,
			boolean forCommit) throws TaskFailedException {
		List<AlertToDataConstraintFailure> alerts = new ArrayList<AlertToDataConstraintFailure>();
		try {
			for (I_RelVersioned rel : concept.getSourceRels()) {
				alerts.addAll(testRel(concept, rel, forCommit));
			}
			for (I_RelVersioned rel : concept.getUncommittedSourceRels()) {
				alerts.addAll(testRel(concept, rel, forCommit));
			}
		} catch (IOException e) {
			throw new TaskFailedException(e);
		}
		return alerts;
	}

	private List<AlertToDataConstraintFailure> testRel(
			I_GetConceptData concept, I_RelVersioned rel, boolean forCommit) {
		ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
		if (rel.getC1Id() == rel.getC2Id()) {
			String alertString = "<html>Self-referencing relationship found for concept<br> <font color='blue'>"
					+ "</font><br>Please edit this relationship before commit...";
			AlertToDataConstraintFailure.ALERT_TYPE alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
			if (forCommit) {
				alertType = AlertToDataConstraintFailure.ALERT_TYPE.ERROR;
			}
			AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(
					alertType, alertString, concept);
			alertList.add(alert);
		}
		return alertList;
	}

}
