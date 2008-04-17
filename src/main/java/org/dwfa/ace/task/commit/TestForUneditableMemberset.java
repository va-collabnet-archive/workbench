package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = {
		@Spec(directory = "tasks/ace/commit", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/nehta/precommit", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/nehta/commit", type = BeanType.TASK_BEAN) })
public class TestForUneditableMemberset extends AbstractConceptTest {

	private static final long serialVersionUID = 1;
	private static final int dataVersion = 1;
	private I_TermFactory termFactory = null;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}// End method writeObject

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			//
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}// End method readObject

	@Override
	public List<AlertToDataConstraintFailure> test(I_GetConceptData concept,
			boolean forCommit) throws TaskFailedException {
		termFactory = LocalVersionedTerminology.get();

		List<AlertToDataConstraintFailure> alerts = new ArrayList<AlertToDataConstraintFailure>();
		/*
		 * Get destination rels with generates rel type
		 */
		try {
			I_IntSet allowedTypes = termFactory.newIntSet();
			try {
				allowedTypes
						.add(ConceptConstants.GENERATES_REL.localize().getNid());
			} catch (RuntimeException e) {
				AceLog.getAppLog().log(Level.WARNING, this.getName() + " test failed secondary to: " + e.getLocalizedMessage());
				return alerts;
			}
			for (I_GetConceptData dest : concept.getDestRelOrigins(null,
					allowedTypes, null, true)) {
				alerts.addAll(testConcept(dest, forCommit));
			}// End for loop

		} catch (IOException e) {
			throw new TaskFailedException(e);
		}
		return alerts;
	}// End method test

	private List<AlertToDataConstraintFailure> testConcept(
			I_GetConceptData concept, boolean forCommit)
			throws TaskFailedException {
		try {
			UUID[] ids = new UUID[1];
			ids[0] = UUID.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da");
			I_GetConceptData refsetConcept = termFactory.getConcept(ids);

			/*
			 * Get "is a" source rels for concept
			 */
			I_IntSet allowedTypes = termFactory.newIntSet();
			allowedTypes.add(ConceptConstants.SNOMED_IS_A.localize().getNid());
			allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize()
					.getNid());

			for (I_GetConceptData src : concept.getSourceRelTargets(null,
					allowedTypes, null, true)) {
				if (src.getConceptId() == refsetConcept.getConceptId()) {

					String alertString = "<html>Uneditable concept:<br> <font color='blue'>"
							+ concept.getInitialText()
							+ "</font><br>Please cancel edits...";

					AlertToDataConstraintFailure.ALERT_TYPE alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
					if (forCommit) {
						alertType = AlertToDataConstraintFailure.ALERT_TYPE.ERROR;
					}
					AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(
							alertType, alertString, concept);

					ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
					alertList.add(alert);
					return alertList;

				}// End if
			}// End for loop

		} catch (TerminologyException e) {
			throw new TaskFailedException(e);
		} catch (IOException e) {
			throw new TaskFailedException(e);
		}
		return new ArrayList<AlertToDataConstraintFailure>();
	}// End method testConcept

}// End class TestForUneditableMemberset
