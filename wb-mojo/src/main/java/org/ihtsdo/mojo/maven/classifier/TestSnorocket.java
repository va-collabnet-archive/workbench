package org.ihtsdo.mojo.maven.classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.dwfa.ace.task.classify.SnoCon;
import org.dwfa.ace.task.classify.SnoConGrp;
import org.dwfa.ace.task.classify.SnoGrp;
import org.dwfa.ace.task.classify.SnoGrpList;
import org.dwfa.ace.task.classify.SnoQuery;
import org.dwfa.ace.task.classify.SnoRel;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rf2.constant.I_Constants;

import au.csiro.snorocket.core.IFactory_123;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_Callback;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_EquivalentCallback;
import au.csiro.snorocket.snapi.Snorocket_123;

public class TestSnorocket {

	private static ArrayList<SnoConStringID> cEditSnoCons;
	private static ArrayList<SnoRel> cEditSnoRels;
	private static HashMap<Integer, String> conRefList;
	private static HashMap<String,Integer> conStrList;
	private static Log logger;
	private static ArrayList<SnoRel> cRocketSnoRels;
	private static Integer isa;
	private static String inferredRelationship="900000000000011006"   ;
	private static String statedRelationship="900000000000010007"   ;
	private static String someModif="900000000000451002"   ;


	//params
	private static String moduleToWrite="900000000000207008";
	private static String releaseDate="20140131";
	private static String concepts="/Users/ar/Downloads/Archive 2/sct2_Concept_Snapshot_INT_20140131.txt";
	private static String statedRels="/Users/ar/Downloads/Archive 2/sct2_StatedRelationship_Snapshot_INT_20140131.txt";
	private static String inferredRels="/Users/ar/Downloads/Archive 2/sct2_Relationship_Snapshot_INT_20140131.txt";
	private static String outputRels="/Users/ar/Downloads/Archive 2/sct2_ouputRelationship_Snapshot_INT_20140131.txt";
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		TestSnorocket tsr=new TestSnorocket();
		tsr.execute();
		tsr=null;

	}
	public void execute(){

		try {
			logger = new SystemStreamLog();

	        logger.info("\r\n::: [Test Snorocket] execute() -- begin");
	        SnoQuery.initAll();
			cEditSnoCons = new ArrayList<SnoConStringID>();
			cEditSnoRels = new ArrayList<SnoRel>();
			conRefList=new HashMap<Integer,String>();
			conStrList=new HashMap<String,Integer>();

			loadConceptFilesTomap(concepts);


			HashSet<String>parentConcepts=new HashSet<String>();
			parentConcepts.add("410662002"); //concept model attribute
			File relationshipFile=new File(statedRels);
			int[] roles =getRoles(parentConcepts,relationshipFile); 
			int ridx = roles.length;
			if (roles.length > 100) {
				String errStr = "Role types exceeds 100. This will cause a memory issue. "
						+ "Please check that role root is set to 'Concept mode attribute'";
				logger.error(errStr);
				throw new MojoFailureException(errStr);
			}
			final int reserved = 2;
			int cidx=reserved;
			int margin = cEditSnoCons.size() >> 2; // Add 50%
			int[] intArray = new int[cEditSnoCons.size() + margin + reserved];
			intArray[IFactory_123.TOP_CONCEPT] = IFactory_123.TOP;
			intArray[IFactory_123.BOTTOM_CONCEPT] = IFactory_123.BOTTOM;

			Collections.sort(cEditSnoCons);
			if (cEditSnoCons.get(0).id <= Integer.MIN_VALUE + reserved) {
				throw new MojoFailureException("::: SNOROCKET: TOP & BOTTOM nids NOT reserved");
			}
			for (SnoCon sc : cEditSnoCons) {
				intArray[cidx++] = sc.id;
			}
			// Fill array to make binary search work correctly.
			Arrays.fill(intArray, cidx, intArray.length, Integer.MAX_VALUE);
			int root=conStrList.get("138875005");
			Snorocket_123 rocket_123 = new Snorocket_123(intArray, cidx, roles, ridx,
					root);

			// SnomedMetadata :: ISA
			isa=conStrList.get(GetDescendants.ISA_SCTID);
			rocket_123.setIsaNid(isa);

			// SnomedMetadata :: ROLE_ROOTS
			rocket_123.setRoleRoot(isa, true); // @@@
			int roleRoot=conStrList.get("410662002");
			rocket_123.setRoleRoot(roleRoot, false);

			// SET DEFINED CONCEPTS
			for (int i = 0; i < cEditSnoCons.size(); i++) {
				if (cEditSnoCons.get(i).isDefined) {
					rocket_123.setConceptIdxAsDefined(i + reserved);
				}
			}
			cEditSnoCons = null; // :MEMORY:

			loadRelationshipFilesTomap(statedRels);
			// ADD RELATIONSHIPS
			Collections.sort(cEditSnoRels);
			for (SnoRel sr : cEditSnoRels) {
				int err = rocket_123.addRelationship(sr.c1Id, sr.typeId, sr.c2Id, sr.group);
				if (err > 0) {
					StringBuilder sb = new StringBuilder();
					if ((err & 1) == 1) {
						sb.append(" --UNDEFINED_C1-- ");
					}
					if ((err & 2) == 2) {
						sb.append(" --UNDEFINED_ROLE-- ");
					}
					if ((err & 4) == 4) {
						sb.append(" --UNDEFINED_C2-- ");
					}
					logger.info("\r\n::: " + sb /* :!!!: + dumpSnoRelStr(sr) */);
				}
			}

			cEditSnoRels = null; // :MEMORY:

			conStrList = null; // :MEMORY:
			System.gc();

			// RUN CLASSIFIER
			long startTime = System.currentTimeMillis();
			logger.info("::: Starting Classifier... ");
			rocket_123.classify();
			logger.info("::: Time to classify (ms): " + (System.currentTimeMillis() - startTime));

			// GET CLASSIFER EQUIVALENTS
			logger.info("::: GET EQUIVALENT CONCEPTS...");
			startTime = System.currentTimeMillis();
			ProcessEquiv pe = new ProcessEquiv();
			rocket_123.getEquivalents(pe);
			logger.info("\r\n::: [SnorocketMojo] ProcessEquiv() count=" + pe.countConSet
					+ " time= " + toStringLapseSec(startTime));

			// GET CLASSIFER RESULTS
			cRocketSnoRels = new ArrayList<SnoRel>();
			logger.info("::: GET CLASSIFIER RESULTS...");
			startTime = System.currentTimeMillis();
			ProcessResults pr = new ProcessResults(cRocketSnoRels);
			rocket_123.getDistributionFormRelationships(pr);
			logger.info("\r\n::: [SnorocketMojo] GET CLASSIFIER RESULTS count=" + pr.countRel
					+ " time= " + toStringLapseSec(startTime));

			pr = null; // :MEMORY:
			rocket_123 = null; // :MEMORY:
			System.gc();
			System.gc();

			// GET CLASSIFIER_PATH RELS
			startTime = System.currentTimeMillis();
			cEditSnoRels = new ArrayList<SnoRel>();

			cEditSnoCons = new ArrayList<SnoConStringID>();
			conRefList=new HashMap<Integer,String>();
			conStrList=new HashMap<String,Integer>();
			loadConceptFilesTomap(concepts);
			cEditSnoCons=null;
			loadRelationshipFilesTomap(inferredRels);
			conStrList=null;
			// FILTER RELATIONSHIPS
			int last = cEditSnoRels.size();
			for (int idx = last - 1; idx > -1; idx--) {
				if (Arrays.binarySearch(intArray, cEditSnoRels.get(idx).c2Id) < 0) {
					cEditSnoRels.remove(idx);
				}
			}


			// WRITEBACK RESULTS
			startTime = System.currentTimeMillis();
			logger.info(compareAndWriteBack(cEditSnoRels, cRocketSnoRels));

			logger.info("\r\n::: *** WRITEBACK *** LAPSED TIME =\t" + toStringLapseSec(startTime) + "\t ***");



		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String toStringLapseSec(long startTime) {
		StringBuilder s = new StringBuilder();
		long stopTime = System.currentTimeMillis();
		long lapseTime = stopTime - startTime;
		s.append((float) lapseTime / 1000).append(" (seconds)");
		return s.toString();
	}
	private static int[] getRoles(HashSet<String> parentConcepts, File relationshipFile) throws Exception {

		GetDescendants getDesc=new GetDescendants(parentConcepts, relationshipFile, null);
		getDesc.execute();
		HashSet<String> roles=getDesc.getDescendants();
		getDesc=null;
		roles.add(GetDescendants.ISA_SCTID);
		int[] result=new int[roles.size()];
		int resIdx=0;
		for (String role:roles){
			if (conStrList.get(role)==205588){
				boolean bstop=true;
			}
			result[resIdx]=conStrList.get(role);
			resIdx++;
		}
		roles=null;
		Arrays.sort(result);
		return result;
	}
	public static void loadConceptFilesTomap(String conceptFile )throws IOException, TerminologyException {


		FileInputStream rfis = new FileInputStream(conceptFile);
		InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
		BufferedReader rbr = new BufferedReader(risr);

		String line;
		String[] spl;
		rbr.readLine();
		int cont=Integer.MIN_VALUE + 3;
		boolean definitionStatusId ;
		while((line=rbr.readLine())!=null){

			spl=line.split("\t",-1);
			if (spl[2].equals("1")){
				cont++;
				conRefList.put(cont,spl[0]);
				conStrList.put(spl[0],cont);

				if (cont==205588){
					boolean bstop=true;
				}
				definitionStatusId = (spl[4].equals(I_Constants.FULLY_DEFINED));
				SnoConStringID conStr=new SnoConStringID(cont,spl[0],definitionStatusId);
				cEditSnoCons.add(conStr);
			}
		}
		rbr.close();
		rbr=null;
	}

	public static void loadRelationshipFilesTomap( String relationshipFile)throws IOException, TerminologyException {


		FileInputStream rfis = new FileInputStream(relationshipFile);
		InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
		BufferedReader rbr = new BufferedReader(risr);

		String line;
		String[] spl;
		rbr.readLine();
		int cont=Integer.MIN_VALUE + 3;

		while((line=rbr.readLine())!=null){

			spl=line.split("\t",-1);
			if (spl[2].equals("1") && (spl[8].equals(inferredRelationship)
					|| spl[8].equals(statedRelationship))){
				cont++;
				int c1=conStrList.get(spl[4]);
				int c2=conStrList.get(spl[5]);
				int rg=Integer.parseInt(spl[6]);
				int ty=conStrList.get(spl[7]);
				SnoRel rel=new SnoRel(c1,c2,ty,rg,cont);
				if (c1==205588){
					boolean bstop=true;
				}
				cEditSnoRels.add(rel);
			}
		}
		rbr.close();
		rbr=null;
	}
	private class ProcessResults implements I_Callback {

		private List<SnoRel> snorels;
		private int countRel = 0; // STATISTICS COUNTER

		public ProcessResults(List<SnoRel> snorels) {
			this.snorels = snorels;
			this.countRel = 0;
		}

		@Override
		public void addRelationship(int conceptId1, int roleId, int conceptId2, int group) {
			countRel++;
			SnoRel relationship = new SnoRel(conceptId1, conceptId2, roleId, group);
			snorels.add(relationship);
			if (countRel % 25000 == 0) {
				// ** GUI: ProcessResults
				logger.info("rels processed " + countRel);
			}
		}
	}

	private class ProcessEquiv implements I_EquivalentCallback {

		private int countConSet = 0; // STATISTICS COUNTER

		public ProcessEquiv() {
			SnoQuery.clearEquiv();
		}

		@Override
		public void equivalent(ArrayList<Integer> equivalentConcepts) {
			SnoQuery.getEquiv().add(new SnoConGrp(equivalentConcepts));
			countConSet += 1;
		}
	}

	private static String compareAndWriteBack(List<SnoRel> snorelA, List<SnoRel> snorelB)
			throws TerminologyException, IOException {
		// Actual write back approximately 16,380 per minute
		// Write back dropped to approximately 1,511 per minute
		long vTime = System.currentTimeMillis();

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

		FileOutputStream fos = new FileOutputStream( outputRels);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);

		bw.append("id");
		bw.append("\t");
		bw.append("effectiveTime");
		bw.append("\t");
		bw.append("active");
		bw.append("\t");
		bw.append("moduleId");
		bw.append("\t");
		bw.append("sourceId");
		bw.append("\t");
		bw.append("destinationId");
		bw.append("\t");
		bw.append("relationshipGroup");
		bw.append("\t");
		bw.append("typeId");
		bw.append("\t");
		bw.append("characteristicTypeId");
		bw.append("\t");
		bw.append("modifierId");
		bw.append("\r\n");

		// SETUP CLASSIFIER QUERY
		SnoQuery.clearDiff();

		long startTime = System.currentTimeMillis();
		Collections.sort(snorelA);
		Collections.sort(snorelB);

		// Typically, A is the Classifier Path (for previously inferred)
		// Typically, B is the SnoRocket Results Set (for newly inferred)
		Iterator<SnoRel> itA = snorelA.iterator();
		Iterator<SnoRel> itB = snorelB.iterator();
		SnoRel rel_A = null;
		boolean done_A = false;
		if (itA.hasNext()) {
			rel_A = itA.next();
		} else {
			done_A = true;
		}
		SnoRel rel_B = null;
		boolean done_B = false;
		if (itB.hasNext()) {
			rel_B = itB.next();
		} else {
			done_B = true;
		}

		logger.info("\r\n::: [SnorocketMojo]"
				+ "\r\n::: snorelA.size() = \t" + snorelA.size()
				+ "\r\n::: snorelB.size() = \t" + snorelB.size());

		// BY SORT ORDER, LOWER NUMBER ADVANCES FIRST
		while (!done_A && !done_B) {
			if (++countConSeen % 25000 == 0) {
				logger.info("::: [SnorocketMojo] compareAndWriteBack @ #\t" + countConSeen);
			}

			if (rel_A.c1Id == rel_B.c1Id) {
				// COMPLETELY PROCESS ALL C1 FOR BOTH IN & OUT
				// PROCESS C1 WITH GROUP == 0
				int thisC1 = rel_A.c1Id;

				// PROCESS WHILE BOTH HAVE GROUP 0
				while (rel_A.c1Id == thisC1 && rel_B.c1Id == thisC1 && rel_A.group == 0
						&& rel_B.group == 0 && !done_A && !done_B) {

					// PROGESS GROUP ZERO
					switch (compareSnoRel(rel_A, rel_B)) {
					case 1: // SAME
						// GATHER STATISTICS
						countSame++;
						countA_Total++;
						countB_Total++;
						if (rel_A.typeId == isa) {
							countSameISA++;
						}
						// NOTHING TO WRITE IN THIS CASE
						if (itA.hasNext()) {
							rel_A = itA.next();
						} else {
							done_A = true;
						}
						if (itB.hasNext()) {
							rel_B = itB.next();
						} else {
							done_B = true;
						}
						break;

					case 2: // REL_A > REL_B -- B has extra stuff
						// WRITEBACK REL_B (Classifier Results) AS CURRENT
						countB_Diff++;
						countB_Total++;
						if (rel_B.typeId == isa) {
							countB_DiffISA++;
						}
						writeBackCurrent(bw,rel_B);

						if (itB.hasNext()) {
							rel_B = itB.next();
						} else {
							done_B = true;
						}
						break;

					case 3: // REL_A < REL_B -- A has extra stuff
						// WRITEBACK REL_A (Classifier Input) AS RETIRED
						// GATHER STATISTICS
						countA_Diff++;
						countA_Total++;
						if (rel_A.typeId == isa) {
							countA_DiffISA++;
						}
						writeBackRetired(bw,rel_A);

						if (itA.hasNext()) {
							rel_A = itA.next();
						} else {
							done_A = true;
						}
						break;
					} // switch
				}

				// REMAINDER LIST_A GROUP 0 FOR C1
				while (rel_A.c1Id == thisC1 && rel_A.group == 0 && !done_A) {
					countA_Diff++;
					countA_Total++;
					if (rel_A.typeId == isa) {
						countA_DiffISA++;
					}
					writeBackRetired(bw,rel_A);
					if (itA.hasNext()) {
						rel_A = itA.next();
					} else {
						done_A = true;
						break;
					}
				}

				// REMAINDER LIST_B GROUP 0 FOR C1
				while (rel_B.c1Id == thisC1 && rel_B.group == 0 && !done_B) {
					countB_Diff++;
					countB_Total++;
					if (rel_B.typeId == isa) {
						countB_DiffISA++;
					}
					writeBackCurrent(bw,rel_B);
					if (itB.hasNext()) {
						rel_B = itB.next();
					} else {
						done_B = true;
						break;
					}
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
					if (itA.hasNext()) {
						rel_A = itA.next();
					} else {
						done_A = true;
					}
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
					if (itB.hasNext()) {
						rel_B = itB.next();
					} else {
						done_B = true;
					}
				}

				// FIND GROUPS IN GROUPLIST_A WITHOUT AN EQUAL IN GROUPLIST_B
				// WRITE THESE GROUPED RELS AS "RETIRED"
				SnoGrpList groupList_NotEqual;
				if (groupList_A.size() > 0) {
					groupList_NotEqual = groupList_A.whichNotEqual(groupList_B);
					for (SnoGrp sg : groupList_NotEqual) {
						for (SnoRel sr_A : sg) {
							writeBackRetired(bw,sr_A);
						}
					}
					countA_Total += groupList_A.countRels();
					countA_Diff += groupList_NotEqual.countRels();
				}

				// FIND GROUPS IN GROUPLIST_B WITHOUT AN EQUAL IN GROUPLIST_A
				// WRITE THESE GROUPED RELS AS "NEW, CURRENT"
				int rgNum = 0; // USED TO DETERMINE "AVAILABLE" ROLE GROUP NUMBERS
				if (groupList_B.size() > 0) {
					groupList_NotEqual = groupList_B.whichNotEqual(groupList_A);
					for (SnoGrp sg : groupList_NotEqual) {
						if (sg.get(0).group != 0) {
							rgNum = nextRoleGroupNumber(groupList_A, rgNum);
							for (SnoRel sr_B : sg) {
								sr_B.group = rgNum;
								writeBackCurrent(bw,sr_B);
							}
						} else {
							for (SnoRel sr_B : sg) {
								writeBackCurrent(bw,sr_B);
							}
						}
					}
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
					if (rel_B.typeId == isa) {
						countB_DiffISA++;
					}
					writeBackCurrent(bw,rel_B);
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
					if (rel_A.typeId == isa) {
						countA_DiffISA++;
					}
					writeBackRetired(bw,rel_A);
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
			if (rel_A.typeId == isa) {
				countA_DiffISA++;
			}
			// COMPLETELY UPDATE ALL REMAINING REL_A AS RETIRED
			writeBackRetired(bw,rel_A);
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
			if (rel_B.typeId == isa) {
				countB_DiffISA++;
			}
			// COMPLETELY UPDATE ALL REMAINING REL_B AS NEW, CURRENT
			writeBackCurrent(bw,rel_B);
			if (itB.hasNext()) {
				rel_B = itB.next();
			} else {
				done_B = true;
				break;
			}
		}

		bw.close();
		bw=null;
		osw=null;
		fos=null;
		// CHECKPOINT DATABASE

		StringBuilder s = new StringBuilder();
		s.append("\r\n::: [SnorocketMojo] compareAndWriteBack()");
		long lapseTime = System.currentTimeMillis() - startTime;
		s.append("\r\n::: [Time] Sort/Compare Input & Output: \t").append(lapseTime);
		s.append("\t(mS)\t").append(((float) lapseTime / 1000) / 60).append("\t(min)");
		s.append("\r\n");
		s.append("\r\n::: ");
		s.append("\r\n::: countSame:     \t").append(countSame);
		s.append("\r\n::: countSameISA:  \t").append(countSameISA);
		s.append("\r\n::: A == Classifier Output Path");
		s.append("\r\n::: countA_Diff:   \t").append(countA_Diff);
		s.append("\r\n::: countA_DiffISA:\t").append(countA_DiffISA);
		s.append("\r\n::: countA_Total:  \t").append(countA_Total);
		s.append("\r\n::: B == Classifier Solution Set");
		s.append("\r\n::: countB_Diff:   \t").append(countB_Diff);
		s.append("\r\n::: countB_DiffISA:\t").append(countB_DiffISA);
		s.append("\r\n::: countB_Total:  \t").append(countB_Total);
		s.append("\r\n::: ");

		return s.toString();
	}

	private static int nextRoleGroupNumber(SnoGrpList sgl, int gnum) {

		int testNum = gnum + 1;
		int sglSize = sgl.size();
		int trial = 0;
		while (trial <= sglSize) {

			boolean exists = false;
			for (int i = 0; i < sglSize; i++) {
				if (sgl.get(i).get(0).group == testNum) {
					exists = true;
				}
			}

			if (exists == false) {
				return testNum;
			} else {
				testNum++;
				trial++;
			}
		}

		return testNum;
	}

	private static void writeBackRetired(BufferedWriter bw,SnoRel rel_A)
			throws IOException {


		writeRF2TypeLine(bw,rel_A.toStringNid(),releaseDate,"0",moduleToWrite,conRefList.get(rel_A.c1Id),
				conRefList.get(rel_A.c2Id),rel_A.group,conRefList.get(rel_A.typeId),
				inferredRelationship, someModif);

		if (rel_A.typeId == isa) {
			SnoQuery.getIsaDropped().add(rel_A);
		} else {
			SnoQuery.getRoleDropped().add(rel_A);
		}

	}

	private static void writeBackCurrent(BufferedWriter bw,SnoRel rel_B)
			throws TerminologyException, IOException {
		if (rel_B.typeId == isa) {
			SnoQuery.getIsaAdded().add(rel_B);
		} else {
			SnoQuery.getRoleAdded().add(rel_B);
		}


		writeRF2TypeLine(bw,rel_B.toStringNid(),releaseDate,"1",moduleToWrite,conRefList.get(rel_B.c1Id),
				conRefList.get(rel_B.c2Id),rel_B.group,conRefList.get(rel_B.typeId),
				inferredRelationship, someModif);

	}

	public static void writeRF2TypeLine(BufferedWriter bw, String relationshipId, String effectiveTime, String active, String moduleId, String sourceId, String destinationId, int relationshipGroup, String relTypeId,
			String characteristicTypeId, String modifierId) throws IOException {
		bw.append( relationshipId + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + sourceId + "\t" + destinationId + "\t" + relationshipGroup + "\t" + relTypeId
				+ "\t" + characteristicTypeId + "\t" + modifierId);
		bw.append( "\r\n");
	}

	private static int compareSnoRel(SnoRel inR, SnoRel outR) {
		if ((inR.c1Id == outR.c1Id) && (inR.group == outR.group) && (inR.typeId == outR.typeId)
				&& (inR.c2Id == outR.c2Id)) {
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

}
