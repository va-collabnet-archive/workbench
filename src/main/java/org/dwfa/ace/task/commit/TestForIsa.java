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
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = {
		@Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
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
			Set<I_Path> editingPaths = activeProfile.getEditingPathSet();

			if (activeProfile.getRoots().contains(concept.getConceptId()))
				return alertList;

			I_GetConceptData is_a = null;
			I_GetConceptData is_a_rel_aux = null;

			// check that the SNOMED is-a exists in the current database before
			// using it
			if (termFactory.hasId(SNOMED.Concept.IS_A.getUids().iterator()
					.next())) {
				is_a = termFactory.getConcept(SNOMED.Concept.IS_A.getUids());
			}

			// check that the Architectonic is-a exists before using it
			if (termFactory.hasId(ArchitectonicAuxiliary.Concept.IS_A_REL
					.getUids().iterator().next())) {
				is_a_rel_aux = termFactory
						.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
								.getUids());
			}

			Set<I_Position> allPositions = new HashSet<I_Position>();
			for (I_Path path : editingPaths) {
				allPositions.add(termFactory.newPosition(path,
						Integer.MAX_VALUE));
				for (I_Position position : path.getOrigins()) {
					addOriginPositions(termFactory, position, allPositions);
				}
			}

			// See if an ISA exists on the path
			for (I_RelTuple rel : concept.getSourceRelTuples(activeProfile
					.getAllowedStatus(), null, allPositions, true)) {
				if ((is_a != null && rel.getRelTypeId() == is_a.getConceptId())
						|| (is_a_rel_aux != null && rel.getRelTypeId() == is_a_rel_aux
								.getConceptId()))
					return alertList;
			}

			// Not on the path, see if one exists elsewhere
			boolean found = false;
			for (I_RelTuple rel : concept.getSourceRelTuples(activeProfile
					.getAllowedStatus(), null, null, true)) {
				if ((is_a != null && rel.getRelTypeId() == is_a.getConceptId())
						|| (is_a_rel_aux != null && rel.getRelTypeId() == is_a_rel_aux
								.getConceptId()))
					found = true;
			}
			alertList.add(new AlertToDataConstraintFailure(
					AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
					"<html>No IS_A relationship"
							+ (found ? " - check editing path settings" : ""),
					concept));
			return alertList;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

	private void addOriginPositions(I_TermFactory termFactory,
			I_Position position, Set<I_Position> allPositions) {
		allPositions.add(position);
		for (I_Position originPosition : position.getPath().getOrigins()) {
			addOriginPositions(termFactory, originPosition, allPositions);
		}
	}

}
