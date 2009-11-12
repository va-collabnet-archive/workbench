package org.dwfa.ace.task.classify;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_WriteDirectToDb;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
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

/**
 * Some previous notes... !!! needs to be reviewed / cleaned up...
 * <p>
 * 
 * <ol>
 * <li>Simplify the test to determine if a concept is part of the classification
 * hierarchy. The <code>isParentOfOrEqualTo</code> method is to slow. We will
 * instead check based on the following assumptions.
 * <ul>
 * <li>The is-a relationship is unique to the classification. For example, the
 * SNOMED is-a has a different concept id than the ace-auxiliary is-a
 * relationship. So every concept (except the concept root) will have at least
 * one is-a relationship of the proper type.
 * <li>There is a single root concept, and that root is part of the set of
 * included concept
 * <li>Assume that the versions are linear, independent of path, and therefore
 * the status with the latest date on an allowable path is the latest status.
 * <li>Assume that relationships to retired concepts will have a special status
 * so that retired concepts are not encountered by following current
 * relationships
 * 
 * <ul>
 * </ol>
 * These assumptions should allow determination of included concepts in linear
 * time - O(n), with a relatively small constant since they can be performed
 * with a simple integer comparison on the concept type.
 * 
 * <p>
 * <br>
 * 
 * The following assumptions are designed to determine included concepts in
 * linear time O(n). The constant time is kept small by doing an integer
 * comparison of the concept type.
 * <p>
 * <b>ASSUMPTIONS</b>
 * <ul>
 * <li>The IS-A relationship concept id is unique to the classification system.
 * <li>There is a single root concept; and, that root is part of the included
 * concepts.
 * <li>The status with the latest date on an allowable path is the latest
 * status.
 * <li>Retired concepts are not encountered by following current relationships.
 * </ul>
 * 
 * 
 */

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class SnorocketTask extends AbstractTask implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		final int objDataVersion = in.readInt();

		if (objDataVersion <= dataVersion) {

		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
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
	List<SnoRel> cEditSnoRels; // "Edit Path" Concepts
	List<SnoCon> cEditSnoCons; // "Edit Path" Relationships
	List<SnoRel> cClassSnoRels; // "Classifier Path" Relationships
	List<SnoRel> cRocketSnoRels; // "Snorocket Results Set" Relationships

	// USER INTERFACE
	private Logger logger;
	I_TermFactory tf = null;
	I_ConfigAceFrame config = null;
	I_ShowActivity gui = null;
	private boolean continueThisAction = true;

	public void actionPerformed(ActionEvent arg0) {
		continueThisAction = false;
	}

	/**
	 * <b><font color=blue>ProcessResults</font></b><br>
	 * <b>First Classification Run</b><br>
	 * <i>The Classifier may create a new part for an existing relationship.<br>
	 * </i> PROCESS:<br>
	 * <code>Search for existing concept to get relationship with same origin/dest/type.<br>
	 * CASE (search == empty)<br>
	 * create new origin/dest/type "inferred" relationship w/ status "current"<br>
	 * add part to the new relationship on the TO PATH<br>
	 * CASE (search == success)<br>
	 * * add part to the existing "stated" relationship on the TO PATH</code>
	 * <p>
	 * <b>Subsequent Classification Runs</b><br>
	 * Case A. NEW: Create new relationship with status CURRENT. see "new" above
	 * <br>
	 * Case B. SAME: Do nothing.<br>
	 * Case C. ABSENT: WAS in previous, NOT in current. Set status to RETIRED.
	 * 
	 */

	private class ProcessResults implements I_SnorocketFactory.I_Callback {
		private List<SnoRel> snorels;
		// STATISTICS COUNTER
		private int countRel = 0;

		public ProcessResults(List<SnoRel> snorels) {
			this.snorels = snorels;
		}

		public void addRelationship(int conceptId1, int roleId, int conceptId2,
				int group) {
			countRel++;
			SnoRel relationship = new SnoRel(conceptId1, conceptId2, roleId,
					group, countRel);
			snorels.add(relationship);
			if (countRel % 25000 == 0) {
				// ** GUI: ProcessResults
				gui.setValue(countRel);
				gui.setProgressInfoLower("rels processed " + countRel);
			}
		}

		public String toStringStats(long startTime) {
			StringBuffer s = new StringBuffer();
			s.append("\r\n::: [SnorocketTask] ProcessResults()");
			if (startTime > 0) {
				long lapseTime = System.currentTimeMillis() - startTime;
				s.append("\r\n::: [Time] Get Solution Set: \t" + lapseTime
						+ "\t(mS)\t" + (((float) lapseTime / 1000) / 60)
						+ "\t(min)");
				s.append("\r\n");
			}
			s.append("\r\n::: Solution Set Relationships:\t" + countRel);
			s.append("\r\n::: ");
			return s.toString();
		}
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		logger = worker.getLogger();
		logger.info("\r\n::: [SnorocketTask] evaluate() -- begin");

		tf = LocalVersionedTerminology.get();

		if (setupCoreNids().equals(Condition.STOP))
			return Condition.STOP;
		logger.info(toStringNids());

		// GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
		if (setupPaths().equals(Condition.STOP))
			return Condition.STOP;
		logger.info(toStringPathPos(cEditPathPos, "Edit Path"));
		logger.info(toStringPathPos(cClassPathPos, "Classifier Path"));
		// logger.info(toStringFocusSet(tf));

		try {
			// SETUP CLASSIFIER
			I_SnorocketFactory rocket = (I_SnorocketFactory) Class.forName(
					"au.csiro.snorocket.ace.SnorocketFactory").newInstance();
			rocket.setIsa(isaNid);

			// ** GUI: 1. LOAD DATA **
			continueThisAction = true;
			gui = tf.newActivityPanel(true, tf.getActiveAceFrameConfig()); // in activity viewer
			gui.addActionListener(this);
			gui.setProgressInfoUpper("Classifier 1/5: load data");
			gui.setIndeterminate(false);
			gui.setMaximum(1000000);
			gui.setValue(0);

			// GET EDIT_PATH RELS & ADD TO SNOROCKET
			cEditSnoCons = new ArrayList<SnoCon>();
			cEditSnoRels = new ArrayList<SnoRel>();
			long startTime = System.currentTimeMillis();
			SnoPathProcess pcEdit = new SnoPathProcess(logger, null,
					cEditSnoCons, cEditSnoRels, cEditPathPos, gui);
			tf.iterateConcepts(pcEdit);
			logger.info("\r\n::: [SnorocketTask] GET STATED PATH DATA"
					+ pcEdit.getStats(startTime));

			Collections.sort(cEditSnoCons);
			rocket.addConcept(rootNid, false); // @@@
			for (SnoCon sc : cEditSnoCons)
				rocket.addConcept(sc.id, sc.isDefined);

			// SORT BY [ROLE-C1-GROUP-C2]
			Comparator<SnoRel> comp = new Comparator<SnoRel>() {
				public int compare(SnoRel o1, SnoRel o2) {
					int thisMore = 1;
					int thisLess = -1;
					if (o1.typeId > o2.typeId) {
						return thisMore;
					} else if (o1.typeId < o2.typeId) {
						return thisLess;
					} else {
						if (o1.c1Id > o2.c1Id) {
							return thisMore;
						} else if (o1.c1Id < o2.c1Id) {
							return thisLess;
						} else {

							if (o1.group > o2.group) {
								return thisMore;
							} else if (o1.group < o2.group) {
								return thisLess;
							} else {

								if (o1.c2Id > o2.c2Id) {
									return thisMore;
								} else if (o1.c2Id < o2.c2Id) {
									return thisLess;
								} else {
									return 0; // this == received
								}
							}
						}
					}
				} // compare()
			};
			Collections.sort(cEditSnoRels, comp);
			for (SnoRel sr : cEditSnoRels)
				rocket.addRelationship(sr.c1Id, sr.typeId, sr.c2Id, sr.group);
			logger.info("\r\n::: [SnorocketTask] SORTED & ADDED CONs, RELs"
					+ " *** LAPSE TIME = " + toStringLapseSec(startTime)
					+ " ***");

			// ** GUI: 1. LOAD DATA -- done **
			if (continueThisAction) {
				gui.setProgressInfoLower("edit path rels = "
						+ pcEdit.countRelAdded + ", lapsed time = "
						+ toStringLapseSec(startTime));
				gui.complete(); // PHASE 1. DONE
			} else {
				gui.setProgressInfoLower("classification stopped by user");
				gui.complete(); // PHASE 1. DONE
				return Condition.CONTINUE;
			}
			cEditSnoRels = null; // :MEMORY:
			pcEdit = null; // :MEMORY:

			// ** GUI: 2 RUN CLASSIFIER **
			gui = tf.newActivityPanel(true, tf.getActiveAceFrameConfig()); // in activity viewer
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
			if (continueThisAction) {
				gui.setProgressInfoLower("classification complete, time = "
						+ toStringLapseSec(startTime));
				gui.complete();
			} else {
				gui.setProgressInfoLower("classification stopped by user");
				gui.complete(); // PHASE 1. DONE
				return Condition.CONTINUE;
			}

			// ** GUI: 3 GET CLASSIFIER RESULTS **
			gui = tf.newActivityPanel(true, tf.getActiveAceFrameConfig()); // in activity viewer
			gui.addActionListener(this);
			gui.setProgressInfoUpper("Classifier 3/5: retrieve solution set");
			gui.setIndeterminate(false);
			gui.setMaximum(1000000);
			gui.setValue(0);

			// GET CLASSIFER RESULTS
			cRocketSnoRels = new ArrayList<SnoRel>();
			worker.getLogger().info("::: GET CLASSIFIER RESULTS...");
			startTime = System.currentTimeMillis();
			ProcessResults pr = new ProcessResults(cRocketSnoRels);
			rocket.getResults(pr);
			logger.info(pr.toStringStats(startTime));

			// ** GUI: 3 -- done
			if (continueThisAction) {
				gui.setProgressInfoLower("solution set rels = " + pr.countRel
						+ ", lapsed time = " + toStringLapseSec(startTime));
				gui.complete(); // 3 GET CLASSIFIER RESULTS -- done
				rocket = null; // :MEMORY:
				pr = null; // :MEMORY:
			} else {
				gui.setProgressInfoLower("classification stopped by user");
				gui.complete(); // PHASE 1. DONE
				return Condition.CONTINUE;
			}

			// ** GUI: 4 GET CLASSIFIER PATH DATA **
			gui = tf.newActivityPanel(true, tf.getActiveAceFrameConfig()); // in activity viewer
			gui.addActionListener(this);
			String tmpS = "Classifier 4/5: get previously inferred & compare";
			gui.setProgressInfoUpper(tmpS);
			gui.setIndeterminate(false);
			gui.setMaximum(1000000);
			gui.setValue(0);

			// GET CLASSIFIER_PATH RELS
			cClassSnoRels = new ArrayList<SnoRel>();
			startTime = System.currentTimeMillis();
			SnoPathProcess pcClass = new SnoPathProcess(logger, null, null,
					cClassSnoRels, cClassPathPos, gui);
			tf.iterateConcepts(pcClass);
			logger.info("\r\n::: [SnorocketTask] GET INFERRED PATH DATA"
					+ pcClass.getStats(startTime));

			// ** GUI: 4 -- done
			if (continueThisAction) {
				gui.setProgressInfoLower("classifier path prior rels = "
						+ pcClass.countRelAdded + ", lapsed time = "
						+ toStringLapseSec(startTime));
				gui.complete(); // -- done
			} else {
				gui.setProgressInfoLower("classification stopped by user");
				gui.complete(); // PHASE 1. DONE
				return Condition.CONTINUE;
			}
			pcClass = null; // :MEMORY:

			// ** GUI: 5 WRITE BACK RESULTS **
			gui.complete(); // PHASE 5. DONE
			gui = tf.newActivityPanel(true, tf.getActiveAceFrameConfig()); // in activity viewer
			gui.addActionListener(this);
			gui.setProgressInfoUpper("Classifier 5/5: write back updates"
					+ " to classifier path");
			gui.setIndeterminate(true);

			// WRITEBACK RESULTS
			startTime = System.currentTimeMillis();
			logger.info(compareAndWriteBack(cClassSnoRels, cRocketSnoRels,
					cClassPathNid));
			logger.info("\r\n::: *** WRITEBACK *** LAPSED TIME =\t"
					+ toStringLapseSec(startTime) + " ***");

			// ** GUI: 5 COMPLETE **
			gui.setProgressInfoLower("writeback completed, lapsed time = "
					+ toStringLapseSec(startTime));
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

		cClassSnoRels = null; // :MEMORY:
		cRocketSnoRels = null; // :MEMORY:
		return Condition.CONTINUE;
	}

	private void addPathOrigins(List<I_Position> origins, I_Path p) {
		origins.addAll(p.getOrigins());
		for (I_Position o : p.getOrigins()) {
			addPathOrigins(origins, o.getPath());
		}
	}

	/**
	 * 
	 * USE CASE: <b>A</b> = ClassPath (previously inferred), <b>B</b> = Rocket
	 * (newly inferred)<br>
	 * <br>
	 * Differing previously inferred <b>A</b> are retired. Differing new
	 * inferred <b>B</b> are created as CURRENT, NEW VERSION. <br>
	 * <BR>
	 * <font color=#990099> IMPLEMENTATION NOTE: <code>snorelA</code> and
	 * <code>snorelB</code> MUST be pre-sorted in C1-Group-Type-C2 order for
	 * this routine. Pre-sorting is used to provide overall computational
	 * efficiency.</font>
	 * 
	 * @param <code>List&lt;SnoRel&gt; snorelA // previously inferred</code>
	 * @param <code>List&lt;SnoRel&gt; snorelB // currently inferred</code>
	 * @param <code><b>int</b> classPathNid // classifier path native id </code>
	 * @return
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private String compareAndWriteBack(List<SnoRel> snorelA,
			List<SnoRel> snorelB, int classPathNid)
			throws TerminologyException, IOException {
		I_WriteDirectToDb di = tf.getDirectInterface();
		int vTime;
		vTime = tf.convertToThinVersion(System.currentTimeMillis());

		// STATISTICS COUNTERS
		int countConSeen = 0;
		int countSame = 0;
		int countSameISA = 0;
		int countA_Diff = 0;
		int countA_DiffISA = 0;
		int countA_Total = 0;
		int countB_Diff = 0;
		int countB_DiffISA = 0;
		int countB_Total = 0;

		long startTime = System.currentTimeMillis();
		Collections.sort(snorelA);
		Collections.sort(snorelB);

		// Typically, A is the Classifier Path (for previously inferred)
		// Typically, B is the SnoRocket Results Set (for newly inferred)
		Iterator<SnoRel> itA = snorelA.iterator();
		Iterator<SnoRel> itB = snorelB.iterator();
		SnoRel rel_A = itA.next();
		SnoRel rel_B = itB.next();
		boolean done_A = false;
		boolean done_B = false;

		logger.info("\r\n::: [SnorocketTask]" + "\r\n::: snorelA.size() = \t"
				+ snorelA.size() + "\r\n::: snorelB.size() = \t"
				+ snorelB.size());

		// BY SORT ORDER, LOWER NUMBER ADVANCES FIRST
		while (!done_A && !done_B) {
			if (++countConSeen % 25000 == 0) {
				logger.info("::: [SnorocketTask] compareAndWriteBack @ #\t"
						+ countConSeen);
			}
			// Actual write back approximately 16,380 per minute

			if (rel_A.c1Id == rel_B.c1Id) {
				// COMPLETELY PROCESS ALL C1 FOR BOTH IN & OUT
				// PROCESS C1 WITH GROUP == 0
				int thisC1 = rel_A.c1Id;

				// PROCESS WHILE BOTH HAVE GROUP 0
				while (rel_A.c1Id == thisC1 && rel_B.c1Id == thisC1
						&& rel_A.group == 0 && rel_B.group == 0 && !done_A
						&& !done_B) {

					// PROGESS GROUP ZERO
					switch (compareSnoRel(rel_A, rel_B)) {
					case 1: // SAME
						// GATHER STATISTICS
						countSame++;
						countA_Total++;
						countB_Total++;
						if (rel_A.typeId == isaNid)
							countSameISA++;
						// NOTHING TO WRITE IN THIS CASE
						if (itA.hasNext())
							rel_A = itA.next();
						else
							done_A = true;
						if (itB.hasNext())
							rel_B = itB.next();
						else
							done_B = true;
						break;

					case 2: // REL_A > REL_B -- B has extra stuff
						// WRITEBACK REL_B (Classifier Results) AS CURRENT
						countB_Diff++;
						countB_Total++;
						if (rel_B.typeId == isaNid)
							countB_DiffISA++;
						writeBackCurrent(rel_B, classPathNid, di, vTime);

						if (itB.hasNext())
							rel_B = itB.next();
						else
							done_B = true;
						break;

					case 3: // REL_A < REL_B -- A has extra stuff
						// WRITEBACK REL_A (Classifier Input) AS RETIRED
						// GATHER STATISTICS
						countA_Diff++;
						countA_Total++;
						if (rel_A.typeId == isaNid)
							countA_DiffISA++;
						writeBackRetired(rel_A, classPathNid, di, vTime);

						if (itA.hasNext())
							rel_A = itA.next();
						else
							done_A = true;
						break;
					} // switch
				}

				// REMAINDER LIST_A GROUP 0 FOR C1
				while (rel_A.c1Id == thisC1 && rel_A.group == 0 && !done_A) {
					countA_Diff++;
					countA_Total++;
					if (rel_A.typeId == isaNid)
						countA_DiffISA++;
					writeBackRetired(rel_A, classPathNid, di, vTime);
					if (itA.hasNext())
						rel_A = itA.next();
					else
						done_A = true;
					break;
				}

				// REMAINDER LIST_B GROUP 0 FOR C1
				while (rel_B.c1Id == thisC1 && rel_B.group == 0 && !done_B) {
					countB_Diff++;
					countB_Total++;
					if (rel_B.typeId == isaNid)
						countB_DiffISA++;
					writeBackCurrent(rel_B, classPathNid, di, vTime);
					if (itB.hasNext())
						rel_B = itB.next();
					else
						done_B = true;
					break;
				}

				// ** SEGMENT GROUPS **
				SnoGrpList groupList_A = new SnoGrpList();
				SnoGrpList groupList_B = new SnoGrpList();
				SnoGrp groupA = null;
				SnoGrp groupB = null;

				// SEGMENT GROUPS IN LIST_A
				int prevGroup = Integer.MIN_VALUE;
				while (rel_A.c1Id == thisC1 && !done_A) {
					if (rel_A.group != prevGroup) {
						groupA = new SnoGrp();
						groupList_A.add(groupA);
					}

					groupA.add(rel_A);

					prevGroup = rel_A.group;
					if (itA.hasNext())
						rel_A = itA.next();
					else
						done_A = true;
				}
				// SEGMENT GROUPS IN LIST_B
				prevGroup = Integer.MIN_VALUE;
				while (rel_B.c1Id == thisC1 && !done_B) {
					if (rel_B.group != prevGroup) {
						groupB = new SnoGrp();
						groupList_B.add(groupB);
					}

					groupB.add(rel_B);

					prevGroup = rel_B.group;
					if (itA.hasNext())
						rel_B = itB.next();
					else
						done_B = true;
				}

				// FIND GROUPS IN GROUPLIST_A WITHOUT AN EQUAL IN GROUPLIST_B
				// WRITE THESE GROUPED RELS AS "RETIRED"
				SnoGrpList groupList_NotEqual;
				if (groupList_A.size() > 0) {
					groupList_NotEqual = groupList_A.whichNotEqual(groupList_B);
					for (SnoGrp sg : groupList_NotEqual)
						for (SnoRel sr_A : sg)
							writeBackRetired(sr_A, classPathNid, di, vTime);
					countA_Total += groupList_A.countRels();
					countA_Diff += groupList_NotEqual.countRels();
				}

				// FIND GROUPS IN GROUPLIST_B WITHOUT AN EQUAL IN GROUPLIST_A
				// WRITE THESE GROUPED RELS AS "NEW, CURRENT"
				if (groupList_B.size() > 0) {
					groupList_NotEqual = groupList_B.whichNotEqual(groupList_A);
					for (SnoGrp sg : groupList_NotEqual)
						for (SnoRel sr_B : sg)
							writeBackCurrent(sr_B, classPathNid, di, vTime);
					countB_Total += groupList_A.countRels();
					countB_Diff += groupList_NotEqual.countRels();
				}
			} else if (rel_A.c1Id > rel_B.c1Id) {
				// CASE 2: LIST_B HAS CONCEPT NOT IN LIST_A
				// COMPLETELY *ADD* ALL THIS C1 FOR REL_B AS NEW, CURRENT
				int thisC1 = rel_B.c1Id;
				while (rel_B.c1Id == thisC1) {
					countB_Diff++;
					countB_Total++;
					if (rel_B.typeId == isaNid)
						countB_DiffISA++;
					writeBackCurrent(rel_B, classPathNid, di, vTime);
					if (itB.hasNext()) {
						rel_B = itB.next();
					} else {
						done_B = true;
						break;
					}
				}

			} else {
				// CASE 3: LIST_A HAS CONCEPT NOT IN LIST_B
				// COMPLETELY *RETIRE* ALL THIS C1 FOR REL_A
				int thisC1 = rel_A.c1Id;
				while (rel_A.c1Id == thisC1) {
					countA_Diff++;
					countA_Total++;
					if (rel_A.typeId == isaNid)
						countA_DiffISA++;
					writeBackRetired(rel_A, classPathNid, di, vTime);
					if (itA.hasNext()) {
						rel_A = itA.next();
					} else {
						done_A = true;
						break;
					}
				}
			}
		}

		// AT THIS POINT, THE PREVIOUS C1 HAS BE PROCESSED COMPLETELY
		// AND, EITHER REL_A OR REL_B HAS BEEN COMPLETELY PROCESSED
		// AND, ANY REMAINDER IS ONLY ON REL_LIST_A OR ONLY ON REL_LIST_B
		// AND, THAT REMAINDER HAS A "STANDALONE" C1 VALUE
		// THEREFORE THAT REMAINDER WRITEBACK COMPLETELY
		// AS "NEW CURRENT" OR "OLD RETIRED"
		//
		// LASTLY, IF .NOT.DONE_A THEN THE NEXT REL_A IN ALREADY IN PLACE
		while (!done_A) {
			countA_Diff++;
			countA_Total++;
			if (rel_A.typeId == isaNid)
				countA_DiffISA++;
			// COMPLETELY UPDATE ALL REMAINING REL_A AS RETIRED
			writeBackRetired(rel_A, classPathNid, di, vTime);
			if (itA.hasNext()) {
				rel_A = itA.next();
			} else {
				done_A = true;
				break;
			}
		}

		while (!done_B) {
			countB_Diff++;
			countB_Total++;
			if (rel_B.typeId == isaNid)
				countB_DiffISA++;
			// COMPLETELY UPDATE ALL REMAINING REL_B AS NEW, CURRENT
			writeBackCurrent(rel_B, classPathNid, di, vTime);
			if (itB.hasNext()) {
				rel_B = itB.next();
			} else {
				done_B = true;
				break;
			}
		}

		// CHECKPOINT DATABASE
		tf.getDirectInterface().sync();

		StringBuffer s = new StringBuffer();
		s.append("\r\n::: [SnorocketTask] compareAndWriteBack()");
		long lapseTime = System.currentTimeMillis() - startTime;
		s.append("\r\n::: [Time] Sort/Compare Input & Output: \t" + lapseTime
				+ "\t(mS)\t" + (((float) lapseTime / 1000) / 60) + "\t(min)");
		s.append("\r\n");
		s.append("\r\n::: ");
		s.append("\r\n::: countSame:     \t" + countSame);
		s.append("\r\n::: countSameISA:  \t" + countSameISA);
		s.append("\r\n::: countA_Diff:   \t" + countA_Diff);
		s.append("\r\n::: countA_DiffISA:\t" + countA_DiffISA);
		s.append("\r\n::: countA_Total:  \t" + countA_Total);
		s.append("\r\n::: countB_Diff:   \t" + countB_Diff);
		s.append("\r\n::: countB_DiffISA:\t" + countB_DiffISA);
		s.append("\r\n::: countB_Total:  \t" + countB_Total);
		s.append("\r\n::: ");

		return s.toString();
	}

	private void writeBackRetired(SnoRel rel_A, int writeToNid,
			I_WriteDirectToDb di, int versionTime) throws IOException {
		// CREATE RELATIONSHIP PART W/ TermFactory-->VobdEnv
		I_RelPart relPart3 = tf.newRelPart(); // I_RelPart
		relPart3.setTypeId(rel_A.typeId); // from classifier
		relPart3.setGroup(rel_A.group); // from classifier
		relPart3.setCharacteristicId(rel_A.getCharacteristicId());
		relPart3.setRefinabilityId(rel_A.getRefinabilityId());
		relPart3.setStatusId(isRETIRED);
		relPart3.setVersion(versionTime);
		relPart3.setPathId(writeToNid); // via preferences
		rel_A.relVers.addVersionNoRedundancyCheck(relPart3);
		di.writeRel(rel_A.relVers); // WRITE TO DB
	}

	private void writeBackCurrent(SnoRel rel_B, int writeToNid,
			I_WriteDirectToDb di, int versionTime) throws TerminologyException,
			IOException {
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
				rel_B.c1Id, rel_B.c2Id);

		// CREATE RELATIONSHIP PART W/ TermFactory-->VobdEnv
		I_RelPart newRelPart = tf.newRelPart(); // I_RelPart
		newRelPart.setTypeId(rel_B.typeId); // from classifier
		newRelPart.setGroup(rel_B.group); // from classifier
		newRelPart.setCharacteristicId(isCh_DEFINING_CHARACTERISTIC);
		newRelPart.setRefinabilityId(isOPTIONAL_REFINABILITY);
		newRelPart.setStatusId(isCURRENT);
		newRelPart.setVersion(versionTime);
		newRelPart.setPathId(writeToNid); // via preferences
		newRel.addVersionNoRedundancyCheck(newRelPart);
		di.writeRel(newRel); // WRITE TO DB
	}

	private int compareSnoRel(SnoRel inR, SnoRel outR) {
		if ((inR.c1Id == outR.c1Id) && (inR.group == outR.group)
				&& (inR.typeId == outR.typeId) && (inR.c2Id == outR.c2Id)) {
			return 1; // SAME
		} else if (inR.c1Id > outR.c1Id) {
			return 2; // ADDED
		} else if ((inR.c1Id == outR.c1Id) && (inR.group > outR.group)) {
			return 2; // ADDED
		} else if ((inR.c1Id == outR.c1Id) && (inR.group == outR.group)
				&& (inR.typeId > outR.typeId)) {
			return 2; // ADDED
		} else if ((inR.c1Id == outR.c1Id) && (inR.group == outR.group)
				&& (inR.typeId == outR.typeId) && (inR.c2Id > outR.c2Id)) {
			return 2; // ADDED
		} else {
			return 3; // DROPPED
		}
	} // compareSnoRel

	private Condition setupCoreNids() {
		I_TermFactory tf = LocalVersionedTerminology.get();

		// SETUP CORE NATIVES IDs
		try {
			config = tf.getActiveAceFrameConfig();

			// SETUP CORE NATIVES IDs
			// :TODO: isaNid & rootNid should come from preferences config
			isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
			rootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());
			if (config.getClassifierIsaType() != null) {
				int checkIsaNid = tf.uuidToNative(config.getClassifierIsaType()
						.getUids());
				if (checkIsaNid != isaNid) {
					logger.severe("\r\n::: SERVERE ERROR isaNid MISMACTH ****");
				}
			} else {
				String errStr = "Profile must have only one edit path. Found: "
						+ tf.getActiveAceFrameConfig().getEditingPathSet();
				AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
						new TaskFailedException(errStr));
				return Condition.STOP;
			}

			if (config.getClassificationRoot() != null) {
				int checkRootNid = tf.uuidToNative(config
						.getClassificationRoot().getUids());
				if (checkRootNid != rootNid) {
					logger.severe("\r\n::: SERVERE ERROR rootNid MISMACTH ***");
				}
			} else {
				String errStr = "Profile must have only one edit path. Found: "
						+ tf.getActiveAceFrameConfig().getEditingPathSet();
				AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
						new TaskFailedException(errStr));
				return Condition.STOP;
			}

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
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Condition.CONTINUE;
	}

	private Condition setupPaths() {
		// GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
		try {
			if (config.getEditingPathSet().size() != 1) {
				String errStr = "Profile must have only one edit path. Found: "
						+ tf.getActiveAceFrameConfig().getEditingPathSet();
				AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
						new TaskFailedException(errStr));
				return Condition.STOP;
			}

			// GET ALL EDIT_PATH ORIGINS
			I_GetConceptData cEditPathObj = config.getClassifierInputPath();
			if (cEditPathObj == null) {
				String errStr = "Classifier Input (Edit) Path -- not set in Classifier preferences tab!";
				AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
						new Exception(errStr));
				return Condition.STOP;
			}

			cEditPathNid = cEditPathObj.getConceptId();
			cEditIPath = tf.getPath(cEditPathObj.getUids());
			cEditPathPos = new ArrayList<I_Position>();
			cEditPathPos.add(tf.newPosition(cEditIPath, Integer.MAX_VALUE));
			addPathOrigins(cEditPathPos, cEditIPath);

			// GET ALL CLASSIFER_PATH ORIGINS
			I_GetConceptData cClassPathObj = config.getClassifierOutputPath();
			if (cClassPathObj == null) {
				String errStr = "Classifier Output (Inferred) Path -- not set in Classifier preferences tab!";
				AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
						new Exception(errStr));
				return Condition.STOP;
			}
			cClassPathNid = cClassPathObj.getConceptId();
			cClassIPath = tf.getPath(cClassPathObj.getUids());
			cClassPathPos = new ArrayList<I_Position>();
			cClassPathPos.add(tf.newPosition(cClassIPath, Integer.MAX_VALUE));
			addPathOrigins(cClassPathPos, cClassIPath);

		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Condition.CONTINUE;
	}

	/**
	 * 
	 * @return Classifier input and output paths as a string.
	 */
	private String toStringPathPos(List<I_Position> pathPos, String pStr) {
		// BUILD STRING
		StringBuffer s = new StringBuffer();
		s.append("\r\n::: [SnorocketTask] PATH ID -- " + pStr);
		try {
			for (I_Position position : pathPos) {
				s.append("\r\n::: ... PathID:\t"
						+ position.getPath().getConceptId() + "\tVersion:\t"
						+ position.getVersion() + "\tUUIDs:\t"
						+ position.getPath().getUniversal());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		s.append("\r\n:::");
		return s.toString();
	}

	/**
	 * Given the <code>startTime</code>, computes <code>lapsedTime</code> by use
	 * the time of calling <code>toStringTime()</code> as the
	 * <code>stopTime</code>.
	 * 
	 * @param <code>long startTime</code> // in milliseconds
	 * @param label
	 * @return
	 */
	@SuppressWarnings("unused")
	private String toStringLapseMin(long startTime) {
		StringBuilder s = new StringBuilder();
		long stopTime = System.currentTimeMillis();
		long lapseTime = stopTime - startTime;
		s.append((((float) lapseTime / 1000) / 60) + " (minutes)");
		return s.toString();
	}

	private String toStringLapseSec(long startTime) {
		StringBuilder s = new StringBuilder();
		long stopTime = System.currentTimeMillis();
		long lapseTime = stopTime - startTime;
		s.append(((float) lapseTime / 1000) + " (seconds)");
		return s.toString();
	}

	private String toStringNids() {
		StringBuilder s = new StringBuilder();
		s.append("\r\n::: [SnoTaskComparePaths]");
		s.append("\r\n:::\t" + isaNid + "\t : isaNid");
		s.append("\r\n:::\t" + rootNid + "\t : rootNid");
		s.append("\r\n:::\t" + isCURRENT + "\t : isCURRENT");
		s.append("\r\n:::\t" + isRETIRED + "\t : isRETIRED");
		s.append("\r\n:::\t" + isOPTIONAL_REFINABILITY
				+ "\t : isOPTIONAL_REFINABILITY");
		s.append("\r\n:::\t" + isNOT_REFINABLE + "\t : isNOT_REFINABLE");
		s.append("\r\n:::\t" + isMANDATORY_REFINABILITY
				+ "\t : isMANDATORY_REFINABILITY");

		s.append("\r\n:::\t" + isCh_STATED_RELATIONSHIP
				+ "\t : isCh_STATED_RELATIONSHIP");
		s.append("\r\n:::\t" + isCh_DEFINING_CHARACTERISTIC
				+ "\t : isCh_DEFINING_CHARACTERISTIC");
		s.append("\r\n:::\t" + isCh_STATED_AND_INFERRED_RELATIONSHIP
				+ "\t : isCh_STATED_AND_INFERRED_RELATIONSHIP");
		s.append("\r\n:::\t" + isCh_STATED_AND_SUBSUMED_RELATIONSHIP
				+ "\t : isCh_STATED_AND_SUBSUMED_RELATIONSHIP");
		s.append("\r\n");
		return s.toString();
	}

	@SuppressWarnings("unused")
	private String toStringFocusSet(I_TermFactory tf) {
		StringBuilder s = new StringBuilder();
		s.append("\r\n::: [SnorocketTask] FOCUS SET");
		// LOG SPECIFIC RELATIONS SET
		// VIEW *ALL* CASE1 RELS, BASED ON C1
		int focusCase1OutNid[] = { -2147481934, -2147458073, -2147481931,
				-2147255612, -2144896203, -2147481929 };
		s.append("\r\n::: ALL CASE1 RELS, BASED ON C1, NO FILTERS");
		s.append("\r\n::: ****" + "\tRelId     " + "\tCId1      "
				+ "\tCId2      " + "\tType      " + "\tGroup" + "\tStatus    "
				+ "\tRefin.    " + "\tChar.     " + "\tPathID    "
				+ "\tVersion   ");
		Integer x = 0;
		try {
			for (int c1 : focusCase1OutNid) {
				I_GetConceptData relSource;
				relSource = tf.getConcept(c1);
				List<? extends I_RelVersioned> lrv = relSource.getSourceRels();
				for (I_RelVersioned rv : lrv) {

					Integer iR = rv.getRelId();
					Integer iA = rv.getC1Id();
					Integer iB = rv.getC2Id();
					List<? extends I_RelPart> parts = rv.getVersions();
					for (I_RelPart p : parts) {
						x++;
						Integer i1 = p.getTypeId();
						Integer i2 = p.getGroup();
						Integer i3 = p.getStatusId();
						Integer i4 = p.getRefinabilityId();
						Integer i5 = p.getCharacteristicId();
						Integer i6 = p.getPathId();
						Integer i7 = p.getVersion();
						s.append("\r\n::: ... \t" + iR.toString() + "\t"
								+ iA.toString() + "\t" + iB.toString() + "\t"
								+ i1.toString() + "\t" + i2.toString() + "\t"
								+ i3.toString() + "\t" + i4.toString() + "\t"
								+ i5.toString() + "\t" + i6.toString() + "\t"
								+ i7.toString() + "\t" + x.toString());
					}
				}
			}
			s.append("\r\n:::");
			for (int c1Nid : focusCase1OutNid) {
				I_GetConceptData sourceRel = tf.getConcept(c1Nid);
				List<? extends I_RelVersioned> lsr = sourceRel.getSourceRels();
				for (I_RelVersioned rv : lsr) {

					Integer iR = rv.getRelId();
					Integer iA = rv.getC1Id();
					Integer iB = rv.getC2Id();
					s.append("\r\n::: ... \tRelId:\t" + iR.toString()
							+ "\tCId1:\t" + iA.toString() + "\tCId2:\t"
							+ iB.toString());
					s.append("\r\n::: UUIDs:\t" + rv.getUniversal());
					s.append("\r\n:::");
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s.toString();
	} // toStringFocusSet()

	@SuppressWarnings("unused")
	private String toStringSnoRel(List<SnoRel> list, int start, int count,
			String comment) {
		StringBuilder s = new StringBuilder(250 + count * 110);
		s.append("\r\n::: [SnorocketTask] SnoRel Listing -- " + comment);
		if (list.size() > 0 && start >= 0 && ((start + count) < list.size())) {
			s.append("\r\n::: \t" + list.get(0).toStringHdr());
			for (int i = start; i < start + count; i++) {
				SnoRel sr = list.get(i);
				s.append("\r\n::: \t" + sr.toString());
			}
		} else {
			s.append("\r\n::: *** RANGE ERROR ***");
			s.append("\r\n::: ");
		}

		return s.toString();
	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// nothing to do.

	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

}