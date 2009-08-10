package org.dwfa.ace.task.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.conflict.detector.DescriptionTupleConflictComparator;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;

public abstract class AbstractDifferenceDescription extends AbstractSearchTest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int dataVersion = 1;

	private static I_IntSet allowedTypes;

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

	@Override
	public boolean test(I_AmTermComponent component,
			I_ConfigAceFrame frameConfig) throws TaskFailedException {
		try {

			if (frameConfig.getViewPositionSet().size() < 2) {
				// Cannot be a difference if there are not two or more paths to
				// compare...
				return false;
			}
			if (allowedTypes == null) {
				allowedTypes = LocalVersionedTerminology.get().newIntSet();
				allowedTypes.addAll(getAllowedTypes());
			}
			I_GetConceptData conceptToTest;
			if (I_GetConceptData.class.isAssignableFrom(component.getClass())) {
				conceptToTest = (I_GetConceptData) component;
			} else if (I_DescriptionVersioned.class.isAssignableFrom(component
					.getClass())) {
				I_DescriptionVersioned desc = (I_DescriptionVersioned) component;
				conceptToTest = LocalVersionedTerminology.get().getConcept(
						desc.getConceptId());
			} else {
				return applyInversion(false);
			}

			Set<I_DescriptionTuple> firstSet = null;
			for (I_Position p: frameConfig.getViewPositionSet()) {
				Set<I_Position> viewSet = new HashSet<I_Position>();
				viewSet.add(p);
				List<I_DescriptionTuple> tuples = conceptToTest.getDescriptionTuples(
						frameConfig.getAllowedStatus(), allowedTypes,
						viewSet);
				if (firstSet == null) {
					firstSet = new TreeSet<I_DescriptionTuple>(new DescriptionTupleConflictComparator());
					firstSet.addAll(tuples);
				} else {
					int firstSetSize = firstSet.size();
					if (firstSetSize != tuples.size()) {
						return true;
					}
					firstSet.addAll(tuples);
					if (firstSet.size() != firstSetSize) {
						return true;
					}
				}
			}
			return false;
		} catch (IOException ex) {
			throw new TaskFailedException(ex);
		} catch (TerminologyException e) {
			throw new TaskFailedException(e);
		}
	}

	protected abstract int[] getAllowedTypes() throws IOException, TerminologyException;
}
