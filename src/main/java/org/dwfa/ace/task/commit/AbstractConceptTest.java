package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;

public abstract class AbstractConceptTest extends AbstractDataConstraintTest {

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
	public final List<AlertToDataConstraintFailure> test(I_Transact component,
			boolean forCommit) throws TaskFailedException {
		if (I_GetConceptData.class.isAssignableFrom(component.getClass())) {
			return test((I_GetConceptData) component, forCommit);
		}
		return new ArrayList<AlertToDataConstraintFailure>();
	}

	public abstract List<AlertToDataConstraintFailure> test(
			I_GetConceptData concept, boolean forCommit)
			throws TaskFailedException;

	public I_GetConceptData getConceptSafe(I_TermFactory termFactory,
			Collection<UUID> concepts) throws Exception {
		if (termFactory.hasId(concepts)) {
			return termFactory.getConcept(concepts);
		}
		return null;
	}

	public Set<I_Position> getPositions(I_TermFactory termFactory)
			throws Exception {
		I_ConfigAceFrame activeProfile = termFactory.getActiveAceFrameConfig();
		Set<I_Path> editingPaths = activeProfile.getEditingPathSet();
		Set<I_Position> allPositions = new HashSet<I_Position>();
		for (I_Path path : editingPaths) {
			allPositions.add(termFactory.newPosition(path, Integer.MAX_VALUE));
			for (I_Position position : path.getOrigins()) {
				addOriginPositions(termFactory, position, allPositions);
			}
		}
		return allPositions;
	}

	private void addOriginPositions(I_TermFactory termFactory,
			I_Position position, Set<I_Position> allPositions) {
		allPositions.add(position);
		for (I_Position originPosition : position.getPath().getOrigins()) {
			addOriginPositions(termFactory, originPosition, allPositions);
		}
	}

	public ArrayList<Integer> getActiveStatus(I_TermFactory termFactory)
			throws Exception {
		ArrayList<Integer> actives = new ArrayList<Integer>();
		for (ArchitectonicAuxiliary.Concept con : Arrays.asList(
				ArchitectonicAuxiliary.Concept.ACTIVE,
				ArchitectonicAuxiliary.Concept.CURRENT,
				ArchitectonicAuxiliary.Concept.LIMITED,
				ArchitectonicAuxiliary.Concept.PENDING_MOVE)) {
			I_GetConceptData c = getConceptSafe(termFactory, con.getUids());
			if (c != null)
				actives.add(c.getConceptId());
		}
		return actives;
	}

}
