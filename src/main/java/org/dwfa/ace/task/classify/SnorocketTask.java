package org.dwfa.ace.task.classify;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_WriteDirectToDb;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class SnorocketTask extends AbstractTask implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int dataVersion = 1;

	// :@@@:BEGIN: why override?
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	// :@@@:BEGIN: why override?
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		final int objDataVersion = in.readInt();

		if (objDataVersion <= dataVersion) {

		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	// :@@@:END:

	private Logger logger;
	I_ConfigAceFrame config = null;
	I_ShowActivity gui = null;

	private boolean continueSnorocket = true;

	public void actionPerformed(ActionEvent arg0) {
		continueSnorocket = false;
	}

	// CORE CONSTANTS
	private static int isaNid = Integer.MIN_VALUE;
	private static int rootNid = Integer.MIN_VALUE;
	private static int isCURRENT = Integer.MIN_VALUE;
	private static int isRETIRED = Integer.MIN_VALUE;
	private static int isOPTIONAL_REFINABILITY = Integer.MIN_VALUE;
	private static int isNOT_REFINABLE = Integer.MIN_VALUE;
	private static int isMANDATORY_REFINABILITY = Integer.MIN_VALUE;
	private static int isCh_STATED_RELATIONSHIP = Integer.MIN_VALUE;
	private static int isCh_DEFINING_CHARACTERISTIC = Integer.MIN_VALUE;
	private static int isCh_STATED_AND_INFERRED_RELATIONSHIP = Integer.MIN_VALUE;
	private static int isCh_STATED_AND_SUBSUMED_RELATIONSHIP = Integer.MIN_VALUE;
	private static int sourceUnspecifiedNid;

	// INPUT PATHS
	int cEditPathNid = Integer.MIN_VALUE; // :TODO: move to logging
	I_Path cEditIPath = null;
	List<I_Position> cEditPathPos = null; // Edit (Stated) Path I_Positions

	// OUTPUT PATHS
	int cClassPathNid; // :TODO: move to logging
	I_Path cClassIPath; // Used for write back value
	List<I_Position> cClassPathPos; // Classifier (Inferred) Path I_Positions

	// MASTER DATA SETS
	List<SnoRel> snorelsEditPath;
	List<SnoRel> snorelsClassOut;
	List<SnoRel> snorelsClassPath;

	// :TODO: can reduce memory use here.
	private class SnoRel implements Comparable<Object> {
		private I_RelVersioned relObj;
		private int relId;
		private int c1Id;
		private int c2Id;
		private int typeId;
		private int group;
		private int path;
		private int version;
		private int refinable;
		private int character;
		private int uid; // unique "up counter" id number

		public SnoRel(int relId, int c1Id, int c2Id, int typeId, int group,
				int path, int version, int refinibility, int characteristic,
				int i, I_RelVersioned relObj) {
			this.relId = relId;
			this.c1Id = c1Id;
			this.c2Id = c2Id;
			this.typeId = typeId;
			this.group = group;
			this.path = path;
			this.version = version;
			this.refinable = refinibility;
			this.character = characteristic;
			this.relObj = relObj;
			uid = i;
		}

		public void setUid(int uid) {
			this.uid = uid;
		}

		public String toString() {
			String s = relId + "\t" + c1Id + "\t" + c2Id + "\t" + typeId + "\t"
					+ group + "\t" + path + "\t" + version + "\t" + refinable
					+ "\t" + character + "\t" + uid;
			return s;
		}

		public String toStringHdr() {
			String s = "\r\n::: ******\t" + "relId     \t" + "c1Id      \t"
					+ "c2Id      \t" + "typeId    \t" + "group\t"
					+ "path      \t" + "version   \t" + "ref.      \t"
					+ "char.     \t" + "uid";
			return s;
		}

		public int compareTo(Object o) {
			SnoRel tmp = (SnoRel) o;
			int thisMore = 1;
			int thisLess = -1;
			if (this.c1Id > tmp.c1Id) {
				return thisMore;
			} else if (this.c1Id < tmp.c1Id) {
				return thisLess;
			} else {
				if (this.c2Id > tmp.c2Id) {
					return thisMore;
				} else if (this.c2Id < tmp.c2Id) {
					return thisLess;
				} else {
					if (this.typeId > tmp.typeId) {
						return thisMore;
					} else if (this.typeId < tmp.typeId) {
						return thisLess;
					} else {
						if (this.group > tmp.group) {
							return thisMore;
						} else if (this.group < tmp.group) {
							return thisLess;
						} else {
							return 0; // this == received
						}
					}
				}
			}
		} // SnoRel.compareTo()

	} // class SnoRel

	private class ProcessConcepts implements I_ProcessConcepts {
		private Logger logger;
		private I_SnorocketFactory rocket;
		private List<SnoRel> snorels;
		private List<I_Position> fromPathPos;

		// STATISTICS COUNTERS
		private int countConSeen = 0;
		private int countConRoot = 0;
		private int countConDuplVersion = 0;
		private int countConAdded = 0; // ADDED TO SNOROCKET
		private int countRelAdded = 0; // ADDED TO SNOROCKET
		private int countRelAddedGroups = 0; // Count rels with non-zero group
		private int countRelAddedGroup1 = 0;
		private int countRelAddedGroup2 = 0;
		private int countRelAddedGroup3 = 0;
		private int countRelAddedGroup4Up = 0;
		private int countRelDuplVersion = 0; // SAME PATH, SAME VERSION

		private int countRelCharStated = 0;
		private int countRelCharDefining = 0;
		private int countRelCharStatedInferred = 0;
		private int countRelCharStatedSubsumed = 0;

		private int countRelRefNot = 0;
		private int countRelRefOpt = 0;
		private int countRelRefMand = 0;

		public ProcessConcepts(Logger logger, I_SnorocketFactory rocket,
				List<SnoRel> snorels, List<I_Position> fromPathPos)
				throws TerminologyException, IOException {
			this.logger = logger;
			this.rocket = rocket;
			this.snorels = snorels;
			this.fromPathPos = fromPathPos;
		}

		public void processConcept(I_GetConceptData concept) throws Exception {
			if (++countConSeen % 25000 == 0) {
				logger.info("::: [SnorocketTask] Concepts viewed:\t"
						+ countConSeen);
			}
			if (concept.getConceptId() == rootNid) {
				if (rocket != null)
					rocket.addConcept(concept.getConceptId(), false); // @@@
				countConAdded++;
				countConRoot++;
				return;
			}

			// GET LATEST CONCEPT PART
			List<I_ConceptAttributePart> cParts;
			cParts = concept.getConceptAttributes().getVersions();
			I_ConceptAttributePart cPart1 = null;
			for (I_Position pos : fromPathPos) { // FOR PATHS_IN_PRIORITY_ORDER
				for (I_ConceptAttributePart cPart : cParts) {
					if (pos.getPath().getConceptId() == cPart.getPathId()) {
						if (cPart1 == null) {
							cPart1 = cPart; // ...... KEEP FIRST INSTANCE
						} else if (cPart1.getVersion() < cPart.getVersion()) {
							cPart1 = cPart; // ...... KEEP MORE RECENT PART
						} else if (cPart1.getVersion() == cPart.getVersion()) {
							countConDuplVersion++; // COUNT DUPLICATE CASE
						}
					} // if ON_THIS_PATH
				} // for EACH_CONCEPT_PART
				if (cPart1 != null) {
					break; // IF FOUND ON THIS PATH, STOP SEARCHING
				}
			} // for PATHS_IN_PRIORITY_ORDER

			if (cPart1.getStatusId() != isCURRENT) // @@@ "active" scope
				return; // IF (NOT_CURRENT) RETURN;

			// PROCESS CURRENT RELATIONSHIPS
			List<SnoRel> rels = findRelationships(concept);
			if (rels != null) {
				// Add Concept to Snorocket
				countConAdded++;
				if (rocket != null)
					rocket.addConcept(concept.getConceptId(), cPart1
							.isDefined());

				for (SnoRel x : rels) {
					countRelAdded++;
					if (countRelAdded % 25000 == 0) {
						// ** GUI: ProcessConcepts
						gui.setValue(countRelAdded);
						gui.setProgressInfoLower("rels processed "
								+ countRelAdded);
					}
					if (x.group >= 0)
						countRelAddedGroups++;
					if (x.group == 1)
						countRelAddedGroup1++;
					if (x.group == 2)
						countRelAddedGroup2++;
					if (x.group == 3)
						countRelAddedGroup3++;
					if (x.group >= 4)
						countRelAddedGroup4Up++;
					x.setUid(countRelAdded); // Update SnoRel sequential uid
					if (snorels != null)
						snorels.add(x); // Add to master input set
					// Add Relationship to Snorocket
					if (rocket != null)
						rocket.addRelationship(x.c1Id, x.typeId, x.c2Id,
								x.group);
				}
			}
		}

		private List<SnoRel> findRelationships(I_GetConceptData concept)
				throws IOException {
			// STATISTICS VARIABLES
			int tmpCountRelCharStated = 0;
			int tmpCountRelCharDefining = 0;
			int tmpCountRelCharStatedInferred = 0;
			int tmpCountRelCharStatedSubsumed = 0;
			int tmpCountRelRefNot = 0;
			int tmpCountRelRefOpt = 0;
			int tmpCountRelRefMand = 0;

			// OUTPUT VARIABLES
			boolean isSnomedConcept = false;
			List<SnoRel> statedRels = new ArrayList<SnoRel>();

			// FOR ALL SOURCE RELS
			for (I_RelVersioned rel : concept.getSourceRels()) {
				// FIND MOST_RECENT REL PART, ON HIGHEST_PRIORITY_PATH
				I_RelPart rPart1 = null;
				for (I_Position pos : fromPathPos) { // PATHS_IN_PRIORITY_ORDER
					for (I_RelPart rPart : rel.getVersions()) {
						if (pos.getPath().getConceptId() == rPart.getPathId()) {
							if (rPart1 == null) {
								rPart1 = rPart; // ... KEEP FIRST_INSTANCE
							} else if (rPart1.getVersion() < rPart.getVersion()) {
								rPart1 = rPart; // ... KEEP MORE_RECENT PART
							} else if (rPart1.getVersion() == rPart
									.getVersion()) {
								countRelDuplVersion++; // !!! MAKE TEMP COUNTER
								if (rPart.getStatusId() == isCURRENT)
									rPart1 = rPart; // KEEP CURRENT PART
							}
						}
					}
					if (rPart1 != null) {
						break; // IF FOUND ON THIS PATH, STOP SEARCHING
					}
				}

				if ((rPart1 != null) && (rPart1.getStatusId() == isCURRENT)) {
					// must FIND at least one SNOMED IS-A relationship
					if (rPart1.getTypeId() == isaNid) {
						isSnomedConcept = true;
					}

					// !!! SET UP STATED FORMS LOOP
					int p1c = rPart1.getCharacteristicId();
					boolean keep = false;
					if (p1c == isCh_DEFINING_CHARACTERISTIC) {
						keep = true;
						tmpCountRelCharDefining++;
					} else if (p1c == isCh_STATED_RELATIONSHIP) {
						keep = true;
						tmpCountRelCharStated++;
					} else if (p1c == isCh_STATED_AND_INFERRED_RELATIONSHIP) {
						keep = true;
						tmpCountRelCharStatedInferred++;
					} else if (p1c == isCh_STATED_AND_SUBSUMED_RELATIONSHIP) {
						keep = true;
						tmpCountRelCharStatedSubsumed++;
					}

					if (keep) {
						int p1rfn = rPart1.getRefinabilityId();
						if (p1rfn == isOPTIONAL_REFINABILITY)
							tmpCountRelRefOpt++;
						else if (p1rfn == isNOT_REFINABLE)
							tmpCountRelRefNot++;
						else if (p1rfn == isMANDATORY_REFINABILITY)
							tmpCountRelRefMand++;

						SnoRel relationship = new SnoRel(rel.getRelId(), rel
								.getC1Id(), rel.getC2Id(), rPart1.getTypeId(),
								rPart1.getGroup(), rPart1.getPathId(), rPart1
										.getVersion(), rPart1
										.getRefinabilityId(), rPart1
										.getCharacteristicId(), -1, rel);
						statedRels.add(relationship);
					}
				}
			}

			if (isSnomedConcept) {
				countRelCharStated += tmpCountRelCharStated;
				countRelCharDefining += tmpCountRelCharDefining;
				countRelCharStatedInferred += tmpCountRelCharStatedInferred;
				countRelCharStatedSubsumed += tmpCountRelCharStatedSubsumed;

				countRelRefNot += tmpCountRelRefNot;
				countRelRefOpt += tmpCountRelRefOpt;
				countRelRefMand += tmpCountRelRefMand;

				return statedRels;
			} else
				return null;
		}

		// STATS FROM PROCESS CONCEPTS (CLASSIFIER INPUT)
		public String getStats(long startTime) {
			String s = new String("\r\n::: [SnorocketTask] ProcessConcepts()");
			if (startTime > 0) {
				long lapseTime = System.currentTimeMillis() - startTime;
				s += "\r\n::: [Time] get vodb data: \t" + lapseTime
						+ "\t(mS)\t" + (((float) lapseTime / 1000) / 60)
						+ "\t(min)";
				s += "\r\n:::";
			}

			s += "\r\n::: concepts viewed:     \t" + countConSeen;
			s += "\r\n::: concepts added:      \t" + countConAdded;
			s += "\r\n::: relationships added: \t" + countRelAdded;
			s += "\r\n:::";
			s += "\r\n::: rel group ==1:  \t" + countRelAddedGroup1;
			s += "\r\n::: rel group ==2:  \t" + countRelAddedGroup2;
			s += "\r\n::: rel group ==3:  \t" + countRelAddedGroup3;
			s += "\r\n::: rel group >=4:  \t" + countRelAddedGroup4Up;
			s += "\r\n::: rel group TOTAL:\t" + countRelAddedGroups;

			s += "\r\n::: ";
			s += "\r\n::: concept root added:  \t" + countConRoot;
			s += "\r\n::: con version conflict:\t" + countConDuplVersion;
			s += "\r\n::: rel version conflict:\t" + countRelDuplVersion;
			s += "\r\n::: ";
			s += "\r\n::: Defining:         \t" + countRelCharDefining;
			s += "\r\n::: Stated:           \t" + countRelCharStated;
			s += "\r\n::: Stated & Inferred:\t" + countRelCharStatedInferred;
			s += "\r\n::: Stated & Subsumed:\t" + countRelCharStatedSubsumed;
			s += "\r\n:::            TOTAL=\t"
					+ (countRelCharStated + countRelCharDefining
							+ countRelCharStatedInferred + countRelCharStatedSubsumed);
			s += "\r\n::: ";
			s += "\r\n::: Optional Refinability: \t" + countRelRefOpt;
			s += "\r\n::: Not Refinable:         \t" + countRelRefNot;
			s += "\r\n::: Mandatory Refinability:\t" + countRelRefMand;
			s += "\r\n:::                  TOTAL=\t"
					+ (countRelRefNot + countRelRefOpt + countRelRefMand);
			s += "\r\n::: ";
			s += "\r\n";
			return s;
		}
	}

	private class ProcessResults implements I_SnorocketFactory.I_Callback {
		private List<SnoRel> snorels;
		// STATISTICS COUNTER
		private int countRel = 0;

		public ProcessResults(List<SnoRel> snorels) {
			this.snorels = snorels;
		}

		public void addRelationship(int conceptId1, int roleId, int conceptId2,
				int group) {
			SnoRel relationship = new SnoRel(0, conceptId1, conceptId2, roleId,
					group, 0, 0, 0, 0, -1, null);
			snorels.add(relationship);
			countRel++;
			if (countRel % 25000 == 0) {
				// ** GUI: ProcessResults
				gui.setValue(countRel);
				gui.setProgressInfoLower("rels processed " + countRel);
			}
		}

		public String toStringStats(long startTime) {
			String s = new String("\r\n::: [SnorocketTask] ProcessResults()");
			if (startTime > 0) {
				long lapseTime = System.currentTimeMillis() - startTime;
				s += "\r\n::: [Time] Get Solution Set: \t" + lapseTime
						+ "\t(mS)\t" + (((float) lapseTime / 1000) / 60)
						+ "\t(min)";
				s += "\r\n";
			}
			s += "\r\n::: Solution Set Relationships:\t" + countRel;
			s += "\r\n::: ";
			return s;
		}
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		logger = worker.getLogger();
		logger.info("::: Test Classifier Input start evaluate()");

		I_TermFactory tf = LocalVersionedTerminology.get();

		try {
			config = tf.getActiveAceFrameConfig();

			// SETUP CORE NATIVES IDs
			// :TODO: isaNid & rootNid should come from preferences config
			isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
			rootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());
			isCURRENT = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT
					.getUids()); // 0 CURRENT, 1 RETIRED
			isRETIRED = tf.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED
					.getUids());
			isOPTIONAL_REFINABILITY = tf
					.uuidToNative(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY
							.getUids());
			isNOT_REFINABLE = tf
					.uuidToNative(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
							.getUids());
			isMANDATORY_REFINABILITY = tf
					.uuidToNative(ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY
							.getUids());
			isCh_STATED_RELATIONSHIP = tf
					.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP
							.getUids());
			isCh_DEFINING_CHARACTERISTIC = tf
					.uuidToNative(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
							.getUids());
			isCh_STATED_AND_INFERRED_RELATIONSHIP = tf
					.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_INFERRED_RELATIONSHIP
							.getUids());
			isCh_STATED_AND_SUBSUMED_RELATIONSHIP = tf
					.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_SUBSUMED_RELATIONSHIP
							.getUids());
			sourceUnspecifiedNid = tf
					.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
							.getUids());
			logger.info(toStringNids());

			// SETUP CLASSIFIER
			I_SnorocketFactory rocket = (I_SnorocketFactory) Class.forName(
					"au.csiro.snorocket.ace.SnorocketFactory").newInstance();
			rocket.setIsa(isaNid);

			// GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
			// :TODO: change to classifier path setting
			if (config.getEditingPathSet().size() != 1) {
				throw new TaskFailedException(
						"Profile must have only one edit path. Found: "
								+ tf.getActiveAceFrameConfig()
										.getEditingPathSet());
			}

			// GET ALL EDIT_PATH ORIGINS
			I_GetConceptData cEditPathObj = config.getClassifierInputPath();
			cEditPathNid = cEditPathObj.getConceptId();
			cEditIPath = tf.getPath(cEditPathObj.getUids());
			cEditPathPos = new ArrayList<I_Position>();
			cEditPathPos.add(tf.newPosition(cEditIPath, Integer.MAX_VALUE));
			addPathOrigins(cEditPathPos, cEditIPath);

			// GET ALL CLASSIFER_PATH ORIGINS
			I_GetConceptData cClassPathObj = config.getClassifierOutputPath();
			cClassPathNid = cClassPathObj.getConceptId();
			cClassIPath = tf.getPath(cClassPathObj.getUids());
			cClassPathPos = new ArrayList<I_Position>();
			cClassPathPos.add(tf.newPosition(cClassIPath, Integer.MAX_VALUE));
			addPathOrigins(cClassPathPos, cClassIPath);

			// LOG ITEMS OF INTEREST
			logger.info(toStringPathPos(cEditPathPos, "Edit Path"));
			logger.info(toStringPathPos(cClassPathPos, "Classifier Path"));
			// logger.info(toStringFocusSet(tf));

			// ** GUI: 1. LOAD DATA **
			continueSnorocket = true;
			gui = tf.newActivityPanel(true); // in activity viewer
			gui.addActionListener(this);
			gui.setProgressInfoUpper("Classifier 1/5: load data");
			gui.setIndeterminate(false);
			gui.setMaximum(1000000);
			gui.setValue(0);

			// GET EDIT_PATH RELS & ADD TO SNOROCKET
			snorelsEditPath = new ArrayList<SnoRel>();
			long startTime = System.currentTimeMillis();
			ProcessConcepts pcEdit = new ProcessConcepts(logger, rocket, null,
					cEditPathPos);
			tf.iterateConcepts(pcEdit);
			logger.info("\r\n::: [SnorocketTask] GET STATED PATH DATA"
					+ pcEdit.getStats(startTime));
			logger.info(verifySnoRels(snorelsEditPath));

			// ** GUI: 1. LOAD DATA -- done **
			if (continueSnorocket) {
				gui.setProgressInfoLower("edit path rels = "
						+ pcEdit.countRelAdded);
				gui.complete(); // PHASE 1. DONE
			} else {
				gui.setProgressInfoLower("classification stopped by user");
				gui.complete(); // PHASE 1. DONE
				return Condition.CONTINUE;
			}
			continueSnorocket = true;
			snorelsEditPath = null; // :MEMORY:
			pcEdit = null; // :MEMORY:

			// ** GUI: 2 RUN CLASSIFIER **
			gui = tf.newActivityPanel(true); // in activity viewer
			gui.addActionListener(this);
			gui.setProgressInfoUpper("Classifier 2/5: classify data");
			gui.setProgressInfoLower("... can take 4 to 6 minutes ...");
			gui.setIndeterminate(true);

			// RUN CLASSIFIER
			startTime = System.currentTimeMillis();
			logger.info("::: Starting Classifier... ");
			rocket.classify();
			logger.info("::: Time to classify (ms): "
					+ (System.currentTimeMillis() - startTime));

			// ** GUI: PHASE 2. -- done
			if (continueSnorocket) {
				gui.setProgressInfoLower("time (seconds): "
						+ ((System.currentTimeMillis() - startTime) / 1000));
				gui.complete();
			} else {
				gui.setProgressInfoLower("classification stopped by user");
				gui.complete(); // PHASE 1. DONE
				return Condition.CONTINUE;
			}

			// ** GUI: 3 GET CLASSIFIER RESULTS **
			gui = tf.newActivityPanel(true); // in activity viewer
			gui.addActionListener(this);
			gui.setProgressInfoUpper("Classifier 3/5: retrieve solution set");
			gui.setIndeterminate(false);
			gui.setMaximum(1000000);
			gui.setValue(0);

			// GET CLASSIFER RESULTS
			snorelsClassOut = new ArrayList<SnoRel>();
			worker.getLogger().info("::: GET CLASSIFIER RESULTS...");
			startTime = System.currentTimeMillis();
			ProcessResults pr = new ProcessResults(snorelsClassOut);
			rocket.getResults(pr);
			logger.info(pr.toStringStats(startTime));

			// ** GUI: 3 -- done
			if (continueSnorocket) {
				gui.setProgressInfoLower("solution set rels = " + pr.countRel);
				gui.complete(); // 3 GET CLASSIFIER RESULTS -- done
				rocket = null; // :MEMORY:
				pr = null; // :MEMORY:
			} else {
				gui.setProgressInfoLower("classification stopped by user");
				gui.complete(); // PHASE 1. DONE
				return Condition.CONTINUE;
			}

			// ** GUI: 4 GET CLASSIFIER PATH DATA **
			gui = tf.newActivityPanel(true); // in activity viewer
			gui.addActionListener(this);
			gui.setProgressInfoUpper("Classifier 4/5: compare results");
			gui.setIndeterminate(false);
			gui.setMaximum(1000000);
			gui.setValue(0);

			// GET CLASSIFIER_PATH RELS
			snorelsClassPath = new ArrayList<SnoRel>();
			startTime = System.currentTimeMillis();
			ProcessConcepts pcClass = new ProcessConcepts(logger, null,
					snorelsClassPath, cClassPathPos);
			tf.iterateConcepts(pcClass);
			logger.info("\r\n::: [SnorocketTask] GET INFERRED PATH DATA"
					+ pcClass.getStats(startTime));
			logger.info(verifySnoRels(snorelsClassPath));

			// COMPARE RESULTS
			logger.info(compareResults(snorelsClassPath, snorelsClassOut));

			// ** GUI: 4 -- done
			if (continueSnorocket) {
				gui.setProgressInfoLower("classifier path prior rels = "
						+ pcClass.countRelAdded);
				gui.complete(); // 3 GET CLASSIFIER RESULTS -- done
				pcClass = null; // :MEMORY:
			} else {
				gui.setProgressInfoLower("classification stopped by user");
				gui.complete(); // PHASE 1. DONE
				return Condition.CONTINUE;
			}

			// ** GUI: 5 WRITE BACK RESULTS **
			gui.complete(); // PHASE 5. DONE
			gui = tf.newActivityPanel(true); // in activity viewer
			gui.addActionListener(this);
			gui.setProgressInfoUpper("Classifier 5/5: write back updates"
					+ " to classifier path");
			gui.setIndeterminate(true);

			// WRITEBACK RESULTS
			startTime = System.currentTimeMillis();
			logger.info(compareAndWriteBack(snorelsClassPath, snorelsClassOut,
					cClassPathNid));
			logger.info(toStringTime(startTime, "WRITEBACK"));

			// ** GUI: 5 COMPLETE **
			gui.setProgressInfoLower("writeback to classifier path complete");
			gui.complete(); // PHASE 5. DONE

		} catch (TerminologyException e) {
			logger.info("\r\n::: TerminologyException");
			e.printStackTrace();
			throw new TaskFailedException("::: TerminologyException", e);
		} catch (IOException e) {
			logger.info("\r\n::: IOException");
			e.printStackTrace();
			throw new TaskFailedException("::: IOException", e);
		} catch (Exception e) {
			logger.info("\r\n::: Exception");
			e.printStackTrace();
			throw new TaskFailedException("::: Exception", e);
		}

		snorelsClassPath = null; // :MEMORY:
		snorelsClassOut = null; // :MEMORY:
		return Condition.CONTINUE;
	}

	private void addPathOrigins(List<I_Position> origins, I_Path p) {
		origins.addAll(p.getOrigins());
		for (I_Position o : p.getOrigins()) {
			addPathOrigins(origins, o.getPath());
		}
	}

	private String compareResults(List<SnoRel> snorelOld, List<SnoRel> snorelNew) {
		// STATISTICS COUNTERS
		int countTotalIn = 0;
		int countTotalOut = 0;
		int countSame = 0;
		int countSameISA = 0;
		int countAdded = 0;
		int countAddedISA = 0;
		int countDropped = 0;
		int countDroppedISA = 0;

		// HISTORY
		int historySize = 100;
		int historyStartC1ID = -2147480230; // Integer.MIN_VALUE
		int histIdxIn = 0;
		int histIdxOut = 0;
		SnoRel[] historyIn = new SnoRel[historySize];
		SnoRel[] historyOut = new SnoRel[historySize];

		long startTime = System.currentTimeMillis();
		Collections.sort(snorelOld);
		Collections.sort(snorelNew);

		Iterator<SnoRel> iIterator = snorelOld.iterator(); // typ. class path
		Iterator<SnoRel> oIterator = snorelNew.iterator(); // typ. class output
		SnoRel inRel = iIterator.next();
		SnoRel outRel = oIterator.next();

		while (iIterator.hasNext() && oIterator.hasNext()) {
			switch (compareSnoRel(inRel, outRel)) {
			case 1: // SAME
				countSame++;
				countTotalIn++;
				countTotalOut++;
				if (outRel.typeId == isaNid)
					countSameISA++;
				if (true && outRel.c1Id >= historyStartC1ID) {
					if (histIdxIn < historySize)
						historyIn[histIdxIn++] = inRel;
					if (histIdxOut < historySize)
						historyOut[histIdxOut++] = outRel;
				}

				// NEXT BOTH INPUT AND OUTPUT
				inRel = iIterator.next();
				outRel = oIterator.next();
				break;

			case 2: // ADDED IN CLASSIFIER OUTPUT
				// WRITEBACK NEW ISAs
				countAdded++;
				countTotalOut++;
				// GATHER STATISTICS
				if (outRel.typeId == isaNid)
					countAddedISA++;
				// GATHER HISTORY SNAPSHOT
				if (true && outRel.c1Id >= historyStartC1ID) {
					if (histIdxOut < historySize)
						historyOut[histIdxOut++] = outRel;
				}
				// NEXT OUTPUT
				outRel = oIterator.next();
				break;

			case 3: // DROPPED FROM CLASSIFIER INPUT
				// WRITEBACK RETIRED ANYTHING
				countDropped++;
				countTotalIn++;
				// GATHER STATISTICS
				if (inRel.typeId == isaNid)
					countDroppedISA++;
				// GATHER HISTORY SNAPSHOT
				if (true && inRel.c1Id >= historyStartC1ID) {
					if (histIdxIn < historySize)
						historyIn[histIdxIn++] = inRel;
				}
				// NEXT INPUT
				inRel = iIterator.next();
				break;
			}
		}

		// HANDLE REMAINDER AT THE END OF EITHER INPUT OR OUTPUT
		while (oIterator.hasNext()) { // extra output is CASE 2 -- added
			countAdded++;
			countTotalOut++;
			// GATHER STATISTICS
			if (outRel.typeId == isaNid)
				countAddedISA++;
			// GATHER HISTORY SNAPSHOT
			if (true && outRel.c1Id >= historyStartC1ID) {
				if (histIdxOut < historySize)
					historyOut[histIdxOut++] = outRel;
			}
			outRel = oIterator.next();
		}

		while (iIterator.hasNext()) { // extra input is CASE 3 -- dropped
			countDropped++;
			countTotalIn++;
			// GATHER STATISTICS
			if (inRel.typeId == isaNid)
				countDroppedISA++;
			// GATHER HISTORY SNAPSHOT
			if (true && inRel.c1Id >= historyStartC1ID) {
				if (histIdxIn < historySize)
					historyIn[histIdxIn++] = inRel;
			}
			inRel = iIterator.next();
		}

		String s = new String("\r\n::: [SnorocketTask] compareResults()");
		long lapseTime = System.currentTimeMillis() - startTime;
		s += "\r\n::: [Time] Sort/Compare Input & Output: \t" + lapseTime
				+ "\t(mS)\t" + (((float) lapseTime / 1000) / 60) + "\t(min)";
		s += "\r\n";
		s += "\r\n::: ";
		s += "\r\n::: countTotalIn:      \t" + countTotalIn;
		s += "\r\n::: countTotalOut:      \t" + countTotalOut;
		s += "\r\n::: countSame:      \t" + countSame;
		s += "\r\n::: countSameISA:   \t" + countSameISA;
		s += "\r\n::: countAdded:     \t" + countAdded;
		s += "\r\n::: countAddedISA:  \t" + countAddedISA;
		s += "\r\n::: countDropped:   \t" + countDropped;
		s += "\r\n::: countDroppedISA:\t" + countDroppedISA;
		s += "\r\n::: ";

		s += "\r\n::: HISTORY SNAPSHOT";
		s += "\r\n::: \t" + historyIn[0].toStringHdr();
		for (histIdxIn = 0; histIdxIn < historySize; histIdxIn++) {
			s += "\r\n::: \t" + historyIn[histIdxIn].toString();
			s += "\r\n::: \t" + historyOut[histIdxIn].toString();
		}

		return s;
	}

	private String compareAndWriteBack(List<SnoRel> snorelOld,
			List<SnoRel> snorelNew, int writeToNid)
			throws TerminologyException, IOException {
		I_TermFactory tf = LocalVersionedTerminology.get();
		I_WriteDirectToDb di = tf.getDirectInterface();
		int versionTime;
		versionTime = tf.convertToThinVersion(System.currentTimeMillis());

		// STATISTICS COUNTERS
		int countWriteBack = 0;
		int countSame = 0;
		int countSameISA = 0;
		int countAdded = 0;
		int countAddedISA = 0;
		int countDropped = 0;
		int countDroppedISA = 0;

		long startTime = System.currentTimeMillis();
		Collections.sort(snorelOld);
		Collections.sort(snorelNew);

		Iterator<SnoRel> iIterator = snorelOld.iterator(); // typ. class path
		Iterator<SnoRel> oIterator = snorelNew.iterator(); // typ. class output
		SnoRel inRel = iIterator.next();
		SnoRel outRel = oIterator.next();
		while (iIterator.hasNext() && oIterator.hasNext()) {
			if (++countWriteBack % 100000 == 0) {
				logger
						.info("::: [SnorocketTask] WRITEBACK#\t"
								+ countWriteBack);
			}
			switch (compareSnoRel(inRel, outRel)) {
			case 1: // SAME
				countSame++;
				if (outRel.typeId == isaNid)
					countSameISA++;
				inRel = iIterator.next();
				outRel = oIterator.next();
				break;

			case 2: // ADDED IN CLASSIFIER OUTPUT
				countAdded++;
				if (outRel.typeId == isaNid)
					countAddedISA++;
				// @@@ WRITEBACK NEW ISAs --> ALL NEW RELATIONS FOR NOW !!!
				// GENERATE NEW REL ID -- AND WRITE TO DB
				// @@@ Should this case create a new REL ID ???
				Collection<UUID> rUids = new ArrayList<UUID>();
				rUids.add(UUID.randomUUID());
				// (Collection<UUID>, int, I_Path, int)
				int newRelNid = di.uuidToNativeDirectWithGeneration(rUids,
						sourceUnspecifiedNid, cClassIPath, versionTime);

				// CREATE RELATIONSHIP OBJECT -- IN MEMORY
				// (int relNid, int conceptNid, int relDestinationNid)
				I_RelVersioned newRel = di.newRelationshipBypassCommit(
						newRelNid, outRel.c1Id, outRel.c2Id);

				// CREATE RELATIONSHIP PART W/ TermFactory-->VobdEnv
				I_RelPart newRelPart = tf.newRelPart(); // I_RelPart
				newRelPart.setTypeId(outRel.typeId); // from classifier
				newRelPart.setGroup(outRel.group); // from classifier
				newRelPart.setCharacteristicId(isCh_DEFINING_CHARACTERISTIC);
				newRelPart.setRefinabilityId(isOPTIONAL_REFINABILITY);
				newRelPart.setStatusId(isCURRENT);
				newRelPart.setVersion(versionTime);
				newRelPart.setPathId(writeToNid); // via preferences
				newRel.addVersionNoRedundancyCheck(newRelPart);
				di.writeRel(newRel); // WRITE TO DB
				outRel = oIterator.next();
				break;

			case 3: // DROPPED FROM CLASSIFIER INPUT
				// WRITEBACK RETIRED ANYTHING
				countDropped++;
				if (inRel.typeId == isaNid)
					countDroppedISA++;

				// CREATE RELATIONSHIP PART W/ TermFactory-->VobdEnv
				I_RelPart relPart3 = tf.newRelPart(); // I_RelPart
				relPart3.setTypeId(inRel.typeId); // from classifier
				relPart3.setGroup(inRel.group); // from classifier
				relPart3.setCharacteristicId(inRel.character); // fixed
				relPart3.setRefinabilityId(inRel.refinable); // fixed
				relPart3.setStatusId(isRETIRED);
				relPart3.setVersion(versionTime);
				relPart3.setPathId(writeToNid); // via preferences
				inRel.relObj.addVersionNoRedundancyCheck(relPart3);
				di.writeRel(inRel.relObj); // WRITE TO DB

				inRel = iIterator.next();
				break;
			}

		}

		// HANDLE REMAINDER AT THE END OF EITHER INPUT OR OUTPUT
		while (oIterator.hasNext()) { // extra output is CASE 2 -- added
			countAdded++;
			if (outRel.typeId == isaNid)
				countAddedISA++;
			// @@@ WRITEBACK NEW ISAs --> ALL NEW RELATIONS FOR NOW !!!
			// GENERATE NEW REL ID -- AND WRITE TO DB
			// @@@ Should this case create a new REL ID ???
			Collection<UUID> rUids = new ArrayList<UUID>();
			rUids.add(UUID.randomUUID());
			// (Collection<UUID>, int, I_Path, int)
			int newRelNid = di.uuidToNativeDirectWithGeneration(rUids,
					sourceUnspecifiedNid, cClassIPath, versionTime);

			// CREATE RELATIONSHIP OBJECT -- IN MEMORY
			// (int relNid, int conceptNid, int relDestinationNid)
			I_RelVersioned newRel = di.newRelationshipBypassCommit(newRelNid,
					outRel.c1Id, outRel.c2Id);

			// CREATE RELATIONSHIP PART W/ TermFactory-->VobdEnv
			I_RelPart newRelPart = tf.newRelPart(); // I_RelPart
			newRelPart.setTypeId(outRel.typeId); // from classifier
			newRelPart.setGroup(outRel.group); // from classifier
			newRelPart.setCharacteristicId(isCh_DEFINING_CHARACTERISTIC);
			newRelPart.setRefinabilityId(isOPTIONAL_REFINABILITY);
			newRelPart.setStatusId(isCURRENT);
			newRelPart.setVersion(versionTime);
			newRelPart.setPathId(writeToNid); // via preferences
			newRel.addVersionNoRedundancyCheck(newRelPart);
			di.writeRel(newRel); // WRITE TO DB
			outRel = oIterator.next();
		}

		while (iIterator.hasNext()) { // extra input is CASE 3 -- dropped
			// WRITEBACK RETIRED ANYTHING
			countDropped++;
			if (inRel.typeId == isaNid)
				countDroppedISA++;

			// CREATE RELATIONSHIP PART W/ TermFactory-->VobdEnv
			I_RelPart relPart3 = tf.newRelPart(); // I_RelPart
			relPart3.setTypeId(inRel.typeId); // from classifier
			relPart3.setGroup(inRel.group); // from classifier
			relPart3.setCharacteristicId(inRel.character); // fixed
			relPart3.setRefinabilityId(inRel.refinable); // fixed
			relPart3.setStatusId(isRETIRED);
			relPart3.setVersion(versionTime);
			relPart3.setPathId(writeToNid); // via preferences
			inRel.relObj.addVersionNoRedundancyCheck(relPart3);
			di.writeRel(inRel.relObj); // WRITE TO DB

			inRel = iIterator.next();
		}

		// CHECKPOINT DATABASE
		tf.getDirectInterface().sync();

		String s = new String("\r\n::: [SnorocketTask] compareAndWriteBack()");
		long lapseTime = System.currentTimeMillis() - startTime;
		s += "\r\n::: [Time] Sort/Compare/Writeback: \t" + lapseTime
				+ "\t(mS)\t" + (((float) lapseTime / 1000) / 60) + "\t(min)";
		s += "\r\n";
		s += "\r\n::: ";
		s += "\r\n::: countSame:      \t" + countSame;
		s += "\r\n::: countSameISA:   \t" + countSameISA;
		s += "\r\n::: countAdded:     \t" + countAdded;
		s += "\r\n::: countAddedISA:  \t" + countAddedISA;
		s += "\r\n::: countDropped:   \t" + countDropped;
		s += "\r\n::: countDroppedISA:\t" + countDroppedISA;
		s += "\r\n::: ";
		return s;
	}

	private int compareSnoRel(SnoRel inR, SnoRel outR) {
		if ((inR.c1Id == outR.c1Id) && (inR.c2Id == outR.c2Id)
				&& (inR.typeId == outR.typeId) && (inR.group == outR.group)) {
			return 1; // SAME
		} else if (inR.c1Id > outR.c1Id) {
			return 2; // ADDED
		} else if ((inR.c1Id == outR.c1Id) && (inR.c2Id > outR.c2Id)) {
			return 2; // ADDED
		} else if ((inR.c1Id == outR.c1Id) && (inR.c2Id == outR.c2Id)
				&& (inR.typeId > outR.typeId)) {
			return 2; // ADDED
		} else if ((inR.c1Id == outR.c1Id) && (inR.c2Id == outR.c2Id)
				&& (inR.typeId == outR.typeId) && (inR.group > outR.group)) {
			return 2; // ADDED
		} else {
			return 3; // DROPPED
		}
	}

	/**
	 * 
	 * @return Classifier input and output paths as a string.
	 */
	private String toStringPathPos(List<I_Position> pathPos, String pStr) {
		// BUILD STRING
		String s = new String("\r\n::: [SnorocketTask] PATH ID -- " + pStr);
		try {
			for (I_Position position : pathPos) {
				s += "\r\n::: ... PathID:\t"
						+ position.getPath().getConceptId() + "\tVersion:\t"
						+ position.getVersion() + "\tUUIDs:\t"
						+ position.getPath().getUniversal();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		s += "\r\n:::";
		return s;
	}

	private String toStringTime(long startTime, String label) {
		String s = new String("\r\n::: [Time] " + label + " \t");
		long lapseTime = System.currentTimeMillis() - startTime;
		s += lapseTime + "\t(mS)\t" + (((float) lapseTime / 1000) / 60)
				+ "\t(min)";
		return s;
	}

	private String toStringNids() {
		String s = "\r\n::: [SnorocketTask]";
		s += "\r\n:::   isaNid:\t" + isaNid;
		s += "\r\n:::   rootNid:\t" + rootNid;
		s += "\r\n:::   isCURRENT:\t" + isCURRENT;
		s += "\r\n:::   isRETIRED:\t" + isRETIRED;

		s += "\r\n:::   isOPTIONAL_REFINABILITY: \t" + isOPTIONAL_REFINABILITY;
		s += "\r\n:::   isNOT_REFINABLE:         \t" + isNOT_REFINABLE;
		s += "\r\n:::   isMANDATORY_REFINABILITY:\t" + isMANDATORY_REFINABILITY;

		s += "\r\n:::   isCh_STATED_RELATIONSHIP:\t" + isCh_STATED_RELATIONSHIP;
		s += "\r\n:::   isCh_DEFINING_CHARACTERISTIC:\t"
				+ isCh_DEFINING_CHARACTERISTIC;
		s += "\r\n:::   isCh_STATED_AND_INFERRED_RELATIONSHIP:\t"
				+ isCh_STATED_AND_INFERRED_RELATIONSHIP;
		s += "\r\n:::   isCh_STATED_AND_SUBSUMED_RELATIONSHIP:\t"
				+ isCh_STATED_AND_SUBSUMED_RELATIONSHIP;
		s += "\r\n";
		return s;
	}

	private String toStringFocusSet(I_TermFactory tf) {
		String s = new String("\r\n::: [SnorocketTask] FOCUS SET");
		// LOG SPECIFIC RELATIONS SET
		// VIEW *ALL* CASE1 RELS, BASED ON C1
		int focusCase1OutNid[] = { -2147481934, -2147458073, -2147481931,
				-2147255612, -2144896203, -2147481929 };
		s = "\r\n::: ALL CASE1 RELS, BASED ON C1, NO FILTERS";
		s += "\r\n::: ****" + "\tRelId     " + "\tCId1      " + "\tCId2      "
				+ "\tType      " + "\tGroup" + "\tStatus    " + "\tRefin.    "
				+ "\tChar.     " + "\tPathID    " + "\tVersion   ";
		Integer x = 0;
		try {
			for (int c1 : focusCase1OutNid) {
				I_GetConceptData relSource;
				relSource = tf.getConcept(c1);
				List<I_RelVersioned> lrv = relSource.getSourceRels();
				for (I_RelVersioned rv : lrv) {

					Integer iR = rv.getRelId();
					Integer iA = rv.getC1Id();
					Integer iB = rv.getC2Id();
					List<I_RelPart> parts = rv.getVersions();
					for (I_RelPart p : parts) {
						x++;
						Integer i1 = p.getTypeId();
						Integer i2 = p.getGroup();
						Integer i3 = p.getStatusId();
						Integer i4 = p.getRefinabilityId();
						Integer i5 = p.getCharacteristicId();
						Integer i6 = p.getPathId();
						Integer i7 = p.getVersion();
						s += "\r\n::: ... \t" + iR.toString() + "\t"
								+ iA.toString() + "\t" + iB.toString() + "\t"
								+ i1.toString() + "\t" + i2.toString() + "\t"
								+ i3.toString() + "\t" + i4.toString() + "\t"
								+ i5.toString() + "\t" + i6.toString() + "\t"
								+ i7.toString() + "\t" + x.toString();
					}
				}
			}
			s += "\r\n:::";
			for (int c1Nid : focusCase1OutNid) {
				I_GetConceptData sourceRel = tf.getConcept(c1Nid);
				List<I_RelVersioned> lsr = sourceRel.getSourceRels();
				for (I_RelVersioned rv : lsr) {

					Integer iR = rv.getRelId();
					Integer iA = rv.getC1Id();
					Integer iB = rv.getC2Id();
					s += "\r\n::: ... \tRelId:\t" + iR.toString() + "\tCId1:\t"
							+ iA.toString() + "\tCId2:\t" + iB.toString();
					s += "\r\n::: UUIDs:\t" + rv.getUniversal();
					s += "\r\n:::";
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	} // toStringFocusSet()

	private String verifySnoRels(List<SnoRel> snorels) {
		String s = "\r\n::: [SnorocketTask] verifyInput()";
		// COUNTS
		int countInputDupl = 0;
		int countInputDuplNoGroup = 0;

		// TIMINGS
		long timeLapseSort = 0;

		long startTime = System.currentTimeMillis();
		Collections.sort(snorels);
		timeLapseSort = System.currentTimeMillis() - startTime;

		Iterator<SnoRel> i = snorels.iterator();
		SnoRel a = null;
		if (i.hasNext()) {
			a = i.next();
		}
		while (i.hasNext()) {
			SnoRel b = i.next();
			boolean isSame = (a.c1Id == b.c1Id) && (a.c2Id == b.c2Id)
					&& (a.typeId == b.typeId) && (a.group == b.group);
			if (isSame)
				countInputDupl++;
			a = b;
		}

		i = snorels.iterator();
		if (i.hasNext()) {
			a = i.next();
		}
		while (i.hasNext()) {
			SnoRel b = i.next();
			boolean isSame = (a.c1Id == b.c1Id) && (a.c2Id == b.c2Id)
					&& (a.typeId == b.typeId);
			if (isSame)
				countInputDuplNoGroup++;
			a = b;
		}

		s += "\r\n::: Sort Time (mS)\t" + timeLapseSort;
		s += "\r\n::: Duplicate CID1-CID2-TYPE-GROUP:\t" + countInputDupl;
		s += "\r\n::: Duplicate CID1-CID2-TYPE:      \t"
				+ countInputDuplNoGroup;
		s += "\r\n::: ";
		return s;
	} // verifyInput()

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// nothing to do.

	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

}