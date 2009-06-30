package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = {
		@Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
		@Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestForFullySpecifiedName extends AbstractConceptTest {

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
			ArrayList<I_DescriptionVersioned> descriptions = new ArrayList<I_DescriptionVersioned>();
			for (I_DescriptionTuple desc : concept.getDescriptionTuples(
					activeProfile.getAllowedStatus(), null, allPositions, true)) {
				descriptions.add(desc.getDescVersioned());
			}
			for (I_DescriptionVersioned desc : concept
					.getUncommittedDescriptions()) {
				descriptions.add(desc);
			}
			return testDescriptions(concept, descriptions, forCommit);
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

	private List<AlertToDataConstraintFailure> testDescriptions(
			I_GetConceptData concept,
			ArrayList<I_DescriptionVersioned> descriptions, boolean forCommit)
			throws Exception {
		ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
		boolean found = false;
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		I_GetConceptData fully_specified_description_type_aux = termFactory
				.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids());
		I_GetConceptData active_status_con = termFactory
				.getConcept(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE
						.getUids());
		I_GetConceptData current_status_con = termFactory
				.getConcept(org.dwfa.cement.ArchitectonicAuxiliary.Concept.CURRENT
						.getUids());
		I_GetConceptData limited_status_con = termFactory
				.getConcept(org.dwfa.cement.ArchitectonicAuxiliary.Concept.LIMITED
						.getUids());
		HashMap<String, ArrayList<I_DescriptionVersioned>> langs = new HashMap<String, ArrayList<I_DescriptionVersioned>>();
		for (I_DescriptionVersioned desc : descriptions) {
			for (I_DescriptionPart part : desc.getVersions()) {
				// if (part.getVersion() == Integer.MAX_VALUE) {
				if (part.getTypeId() == fully_specified_description_type_aux
						.getConceptId()) {
					found = true;
					if (part.getText().matches(".*\\(\\?+\\).*")) {
						alertList
								.add(new AlertToDataConstraintFailure(
										AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
										"<html>Unedited semantic tag", concept));
						// return alertList;
					}
					if (part.getText().length() > 255) {
						alertList
								.add(new AlertToDataConstraintFailure(
										(forCommit ? AlertToDataConstraintFailure.ALERT_TYPE.ERROR
												: AlertToDataConstraintFailure.ALERT_TYPE.WARNING),
										"<html>FSN exceeds 255 characters",
										concept));
						// return alertList;
					}
					if (part.getStatusId() == active_status_con.getConceptId()
							|| part.getStatusId() == current_status_con
									.getConceptId()
							|| part.getStatusId() == limited_status_con
									.getConceptId()) {
						String lang = part.getLang();
						if (langs.get(lang) != null) {
							for (I_DescriptionVersioned d : langs.get(lang)) {
								if (d.getDescId() != desc.getDescId()) {
									alertList
											.add(new AlertToDataConstraintFailure(
													AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
													"<html>More than one FSN for "
															+ lang, concept));
								}
							}
							langs.get(lang).add(desc);
						} else {
							ArrayList<I_DescriptionVersioned> dl = new ArrayList<I_DescriptionVersioned>();
							dl.add(desc);
							langs.put(lang, dl);
						}
						// ///////////
						// System.out.println("Searching...");
						Hits hits = termFactory.doLuceneSearch("\""
								+ part.getText().replace("(", "\\(").replace(
										")", "\\)") + "\"");
						// System.out.println("Found " + hits.length());
						for (int i = 0; i < hits.length(); i++) {
							// if (i == 10000)
							// break;
							Document doc = hits.doc(i);
							int cnid = Integer.parseInt(doc.get("cnid"));
							int dnid = Integer.parseInt(doc.get("dnid"));
							if (cnid == concept.getConceptId())
								continue;
							I_DescriptionVersioned potential_fsn = termFactory
									.getDescription(dnid, cnid);
							for (I_DescriptionPart part_search : potential_fsn
									.getVersions()) {
								// System.out.println("Hit: "
								// + part_search.getVersion() + "\t"
								// + part_search.getText());
								// if (part_search.getVersion() ==
								// Integer.MAX_VALUE) {
								if (part_search.getTypeId() == fully_specified_description_type_aux
										.getConceptId()
										&& part_search.getText().equals(
												part.getText())
										&& part_search.getLang().equals(
												part.getLang())) {
									alertList
											.add(new AlertToDataConstraintFailure(
													AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
													"<html>FSN already used",
													concept));
									// }
								}
							}
						}
						// ///////////
					}
				}
				// }
			}
		}
		// This might work once we get the SNOMED version of FSN down
		// if (!found) {
		// alertList.add(new AlertToDataConstraintFailure(
		// AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
		// "<html>No fully Specified name", concept));
		// }
		return alertList;
	}
}
