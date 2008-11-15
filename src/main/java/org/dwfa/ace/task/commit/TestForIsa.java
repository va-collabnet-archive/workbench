package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = {
		@Spec(directory = "tasks/ace/commit", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestForIsa extends AbstractConceptTest {

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
		try {
			ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
			I_TermFactory termFactory = LocalVersionedTerminology.get();
			I_ConfigAceFrame activeProfile = termFactory
					.getActiveAceFrameConfig();
			Set<I_Path> editingPaths = new HashSet<I_Path>(activeProfile
					.getEditingPathSet());
			I_GetConceptData is_a = termFactory.getConcept(SNOMED.Concept.IS_A
					.getUids());
			I_GetConceptData is_a_rel_aux = termFactory
					.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
							.getUids());
			Set<I_Position> positions = new HashSet<I_Position>();
			for (I_Path path : editingPaths) {
				positions.add(termFactory.newPosition(path, Integer.MAX_VALUE));
			}
			for (I_RelTuple rel : concept.getSourceRelTuples(activeProfile
					.getAllowedStatus(), null, positions, true)) {
				if (rel.getRelTypeId() == is_a.getConceptId()
						|| rel.getRelTypeId() == is_a_rel_aux.getConceptId())
					return alertList;
			}
			alertList.add(new AlertToDataConstraintFailure(
					AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
					"<html>No IS_A relationship", concept));
			return alertList;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

}
