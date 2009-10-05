package org.dwfa.ace.task.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;

public class DifferenceConceptStatus extends AbstractSearchTest {

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

	@Override
	public boolean test(I_AmTermComponent component,
			I_ConfigAceFrame frameConfig) throws TaskFailedException {
		try {

			if (frameConfig.getViewPositionSet().size() < 2) {
				// Cannot be a difference if there are not two or more paths to
				// compare...
				return applyInversion(false);
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
			
			I_ConceptAttributeTuple firstTuple = null;
			boolean firstPass = true;
			for (I_Position p: frameConfig.getViewPositionSet()) {
				Set<I_Position> positionSet = new HashSet<I_Position>();
				positionSet.add(p);
				List<I_ConceptAttributeTuple> tuples = conceptToTest.getConceptAttributeTuples(
						frameConfig.getAllowedStatus(), positionSet);
				if (firstPass) {
					if (tuples.size() > 0) {
						firstTuple = tuples.get(0);
					}
					firstPass = false;
				} else {
					if (tuples.size() > 0) {
						if (firstTuple == null) {
							return applyInversion(true);
						}
						return (applyInversion(firstTuple.getStatusId() != tuples.get(0).getStatusId()));
					} else {
						if (firstTuple != null) {
							return (applyInversion(true));
						}
					}
				}
			}
			return applyInversion(false);
		} catch (IOException ex) {
			throw new TaskFailedException(ex);
		} catch (TerminologyException e) {
			throw new TaskFailedException(e);
		}
	}
}
