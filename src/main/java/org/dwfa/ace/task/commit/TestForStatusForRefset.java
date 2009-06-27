package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
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
public class TestForStatusForRefset extends AbstractConceptTest {

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

			Set<I_Position> allPositions = new HashSet<I_Position>();
			for (I_Path path : editingPaths) {
				allPositions.add(termFactory.newPosition(path,
						Integer.MAX_VALUE));
				for (I_Position position : path.getOrigins()) {
					addOriginPositions(termFactory, position, allPositions);
				}
			}

			I_GetConceptData active_status_con = termFactory
					.getConcept(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE
							.getUids());
			I_GetConceptData current_status_con = termFactory
					.getConcept(org.dwfa.cement.ArchitectonicAuxiliary.Concept.CURRENT
							.getUids());
			I_GetConceptData limited_status_con = termFactory
					.getConcept(org.dwfa.cement.ArchitectonicAuxiliary.Concept.LIMITED
							.getUids());

			for (I_ConceptAttributeTuple rel : concept
					.getConceptAttributeTuples(
							activeProfile.getAllowedStatus(), allPositions,
							true, true)) {
				if (rel.getConceptStatus() == active_status_con.getConceptId()
						|| rel.getConceptStatus() == current_status_con
								.getConceptId()
						|| rel.getConceptStatus() == limited_status_con
								.getConceptId())
					return alertList;
			}

			I_GetConceptData refset_con = termFactory
					.getConcept(org.dwfa.cement.RefsetAuxiliary.Concept.REFSET_IDENTITY
							.getUids());

			I_GetConceptData isa_con = termFactory
					.getConcept(SNOMED.Concept.IS_A.getUids());
			I_IntSet types = termFactory.newIntSet();
			types.add(isa_con.getConceptId());

			System.out.println(isa_con.getInitialText());

			isa_con = termFactory
					.getConcept(org.dwfa.cement.ArchitectonicAuxiliary.Concept.IS_A_REL
							.getUids());
			types.add(isa_con.getConceptId());

			for (I_GetConceptData refset : refset_con.getDestRelOrigins(
					activeProfile.getAllowedStatus(), types, allPositions,
					true, true)) {
				System.out.println(refset.getInitialText());
				for (I_ThinExtByRefVersioned mem : termFactory
						.getRefsetExtensionMembers(refset.getConceptId())) {
					// List<I_ThinExtByRefVersioned> extensions = termFactory
					// .getAllExtensionsForComponent(refset.getConceptId(),
					// true);
					// for (I_ThinExtByRefVersioned ext : extensions) {
					// System.out.println(ext.getComponentId() + " "
					// + termFactory.getConcept(ext.getComponentId()));
					if (mem.getComponentId() == concept.getConceptId()) {
						alertList
								.add(new AlertToDataConstraintFailure(
										AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
										"<html>Refset, but inactive", concept));
						return alertList;
					}
				}
			}
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
