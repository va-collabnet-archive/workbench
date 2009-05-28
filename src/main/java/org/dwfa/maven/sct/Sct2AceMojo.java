package org.dwfa.maven.sct;

import java.io.*;
import java.util.Arrays;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type3UuidFactory;

/**
 * Sct2AceMojo converts SNOMED text release files to ACE text import files.
 * 
 * @goal sct2ace
 * @requiresDependencyResolution compile
 * @requiresProject false
 * 
 */

public class Sct2AceMojo extends AbstractMojo {
	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	File buildDirectory;

	class SCTConceptRecord implements Comparable<Object> {
		private long id; // CONCEPTID
		private int status; // CONCEPTSTATUS
		private int isprimitive; // ISPRIMITIVE

		public SCTConceptRecord(long i, int s, int p) {
			id = i;
			status = s;
			isprimitive = p;
		}

		public int compareTo(Object obj) {
			SCTConceptRecord tmp = (SCTConceptRecord) obj;
			if (this.id < tmp.id) {
				return -1; // instance less than received
			} else if (this.id > tmp.id) {
				return 1; // instance greater than received
			}
			return 0; // instance == received
		}

		public String toString() {
			return id + "\t" + status + "\t" + isprimitive + "\r\n";
		}

		public String toStringAce(String date, String path) throws IOException,
				TerminologyException {

			UUID u = Type3UuidFactory.fromSNOMED(id);

			return u + "\t" + getStatusString(status) + "\t" + isprimitive
					+ "\t" + date + "\t" + path + "\r\n";
		}
	}

	class SCTDescriptionRecord implements Comparable<Object> {
		private long id; // DESCRIPTIONID
		private int status; // DESCRIPTIONSTATUS
		private long conceptId; // CONCEPTID
		private String termText; // TERM
		private int capStatus; // INITIALCAPITALSTATUS -- capitalization
		private int descriptionType; // DESCRIPTIONTYPE
		private String languageCode; // LANGUAGECODE

		public SCTDescriptionRecord(long dId, int s, long cId, String text,
				int cStat, int typeInt, String lang) {
			id = dId;
			status = s;
			conceptId = cId;
			termText = new String(text);
			capStatus = cStat;
			descriptionType = typeInt;
			languageCode = new String(lang);
		}

		public int compareTo(Object obj) {
			SCTDescriptionRecord tmp = (SCTDescriptionRecord) obj;
			if (this.id < tmp.id) {
				return -1; // instance less than received
			} else if (this.id > tmp.id) {
				return 1; // instance greater than received
			}
			return 0; // instance == received
		}

		public String toString() {
			return id + "\t" + status + "\t" + conceptId + "\t" + termText
					+ "\t" + capStatus + "\t" + descriptionType + "\t"
					+ languageCode + "\r\n";
		}

		public String toStringAce(String date, String path) throws IOException,
				TerminologyException {

			UUID u = Type3UuidFactory.fromSNOMED(id);
			UUID c = Type3UuidFactory.fromSNOMED(conceptId);

			String descType = ArchitectonicAuxiliary.getSnomedDescriptionType(
					descriptionType).getUids().iterator().next().toString();

			return u + "\t" // description uuid
					+ getStatusString(status) + "\t" // status uuid
					+ c + "\t" // concept uuid
					+ termText + "\t" // term
					+ capStatus + "\t" // capitalization status
					+ descType + "\t" // description type uuid
					+ languageCode + "\t" // language code
					+ date + "\t" // effective date
					+ path + "\r\n"; // path uuid
		}
	}

	class SCTRelationshipRecord implements Comparable<Object> {
		private long id; // RELATIONSHIPID
		private int status; // status is computed for relationships
		private long conceptOneID; // CONCEPTID1
		private long relationshipType; // RELATIONSHIPTYPE
		private long conceptTwoID; // CONCEPTID2
		private int characteristic; // CHARACTERISTICTYPE
		private int refinability; // REFINABILITY
		private int group; // RELATIONSHIPGROUP
		private boolean exceptionFlag; // to handle Concept ID change exception

		public SCTRelationshipRecord(long relID, int st, long cOneID,
				long relType, long cTwoID, int characterType, int r, int grp) {
			id = relID; // RELATIONSHIPID
			status = st; // status is computed for relationships
			conceptOneID = cOneID; // CONCEPTID1
			relationshipType = relType; // RELATIONSHIPTYPE
			conceptTwoID = cTwoID; // CONCEPTID2
			characteristic = characterType; // CHARACTERISTICTYPE
			refinability = r; // REFINABILITY
			group = grp; // RELATIONSHIPGROUP
			exceptionFlag = false;
		}

		public int compareTo(Object obj) {
			SCTRelationshipRecord tmp = (SCTRelationshipRecord) obj;
			if (this.id < tmp.id) {
				return -1; // instance less than received
			} else if (this.id > tmp.id) {
				return 1; // instance greater than received
			}
			return 0; // instance == received
		}

		public String toString() {
			return id + "\t" + status + "\t" + conceptOneID + "\t"
					+ relationshipType + "\t" + conceptTwoID + "\r\n";
		}

		public String toStringAce(String date, String path) throws IOException,
				TerminologyException {

			UUID u;
			if (exceptionFlag) {
				// Use negative SNOMED ID for exceptions
				 u = Type3UuidFactory.fromSNOMED(-id);
			} else {
				 u = Type3UuidFactory.fromSNOMED(id);
			}

			UUID cOne = Type3UuidFactory.fromSNOMED(conceptOneID);
			UUID relType = Type3UuidFactory.fromSNOMED(relationshipType);
			UUID cTwo = Type3UuidFactory.fromSNOMED(conceptTwoID);

			String chType = ArchitectonicAuxiliary.getSnomedCharacteristicType(
					characteristic).getUids().iterator().next().toString();
			String reType = ArchitectonicAuxiliary.getSnomedRefinabilityType(
					refinability).getUids().iterator().next().toString();

			return u + "\t" // relationship uuid
					+ getStatusString(status) + "\t" // status uuid

					+ cOne + "\t" // source concept uuid
					+ relType + "\t" // relationship type uuid
					+ cTwo + "\t" // destination concept uuid

					+ chType + "\t" // characteristic type uuid
					+ reType + "\t" // refinability uuid

					+ group + "\t" // relationship group -- integer
					+ date + "\t" + path + "\r\n";
		}
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("BEGIN execute()");

		// Setup build directory
		String buildDir = buildDirectory.getAbsolutePath();
		getLog().info("Build Directory: " + buildDir);

		try {
			// Create multiple directories
			String sctArfOut = "/classes/org/ihtsdo/sct/sct-arf";
			boolean success = (new File(buildDir + sctArfOut)).mkdirs();
			if (success) {
				getLog().info("OUTPUT DIRECTORY: " + buildDir + sctArfOut);
			}
		} catch (Exception e) { // Catch exception if any
			getLog().info("Error: could not create directories");
		}

		// Setup snomedPath UUID String
		String snomedPathUUID = ArchitectonicAuxiliary.Concept.SNOMED_CORE
				.getUids().iterator().next().toString();

		// Setup status UUID String Array
		setupStatusStrings();

		// :TODO: tie in current directory via Maven Plug-in
		// "target/generated-source/org/snomed/2003-01-31/"
		// sct_concepts_20030131.txt
		// sct_relationships_20030131.txt
		// sct_descriptions_20030131.txt

		try {
			long start = System.currentTimeMillis();

			processConceptsFiles(buildDir, snomedPathUUID);
			processDescriptionsFiles(buildDir, snomedPathUUID);
			processRelationshipsFiles(buildDir, snomedPathUUID);

			getLog().info(
					"CONVERSION TIME: "
							+ ((System.currentTimeMillis() - start) / 1000)
							+ " seconds");

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		getLog().info("PROCESSING COMPLETE ");

	}

	/*
	 * ORDER: CONCEPTID CONCEPTSTATUS FULLYSPECIFIEDNAME CTV3ID SNOMEDID
	 * ISPRIMITIVE
	 * 
	 * KEEP: CONCEPTID CONCEPTSTATUS ISPRIMITIVE
	 * 
	 * IGNORE: FULLYSPECIFIEDNAME CTV3ID SNOMEDID
	 */
	protected void processConceptsFiles(String bDir, String sctPath)
			throws Exception {
		int count1, count2; // records in arrays 1 & 2
		String fName1, fName2; // file path name
		String revDate;
		SCTConceptRecord[] a1, a2, a3 = null;

		getLog().info("START CONCEPTS PROCESSING...");
		// Get base SNOMED directory
		String snomedDirStr = bDir + "/generated-source/org/snomed";
		getLog().info("SNOMED Directory: " + snomedDirStr);

		// Get list of dated SNOMED sub directory names
		File snomedDir = new File(snomedDirStr);
		String[] children = snomedDir.list();
		if (children == null) {
			getLog().info("FAILED: NO SUBDIRECTORIES " + snomedDirStr);
			return;
		}

		// Setup output file
		String outFileName = bDir
				+ "/classes/org/ihtsdo/sct/sct-arf/concepts.txt";
		BufferedWriter bw;
		getLog().info("sct-arf OUTPUT: " + outFileName);
		bw = new BufferedWriter(new FileWriter(outFileName));
		bw.write("concept uuid\tstatus uuid\tprimitive\t"
				+ "effective date\tpath uuid" + "\r\n");
		// Read in file1 as master file
		revDate = cleanDate(children[0]) + " 00:00:00";
		fName1 = buildFileName(snomedDirStr, children[0], "sct_concepts_");
		File file1 = new File(fName1);
		if (!file1.exists()) {
			fName1 = buildFileName2(snomedDirStr, children[0], "sct_concepts");
			getLog().info("ALT FILE NAME 1:  " + fName1);
		}

		count1 = countFileLines(fName1);
		getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
		a1 = new SCTConceptRecord[count1];
		parseConcepts(fName1, a1, count1);
		writeConcepts(bw, a1, count1, revDate, sctPath);

		for (int i = 1; i < children.length; i++) {
			// Get filename of file or directory
			revDate = cleanDate(children[i]) + " 00:00:00";
			fName2 = buildFileName(snomedDirStr, children[i], "sct_concepts_");
			File file2 = new File(fName2);
			if (!file2.exists()) {
				fName2 = buildFileName2(snomedDirStr, children[i],
						"sct_concepts");
				getLog().info("ALT FILE NAME 2:  " + fName2);
			}

			count2 = countFileLines(fName2);
			getLog().info("Counted: " + count2 + " records, " + fName2);

			// Parse in file2
			a2 = new SCTConceptRecord[count2];
			parseConcepts(fName2, a2, count2);

			int r1 = 0, r2 = 0, r3 = 0; // reset record indices
			int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
			a3 = new SCTConceptRecord[count2]; // max3
			while ((r1 < count1) && (r2 < count2)) {

				switch (compareConcept(a1[r1], a2[r2])) {
				case 1: // SAME CONCEPT, skip to next
					r1++;
					r2++;
					nSame++;
					break;

				case 2: // MODIFIED CONCEPT
					// Write history
					bw.write(a2[r2].toStringAce(revDate, sctPath));
					// Update master via pointer assignment
					a1[r1] = a2[r2];
					r1++;
					r2++;
					nMod++;
					break;

				case 3: // ADDED CONCEPT
					// Write history
					bw.write(a2[r2].toStringAce(revDate, sctPath));
					// Hold pointer to append to master
					a3[r3] = a2[r2];
					r2++;
					r3++;
					nAdd++;
					break;

				case 4: // DROPPED CONCEPT
					// see ArchitectonicAuxiliary.getStatusFromId()
					if (a1[r1].status != 1) { // if not RETIRED
						a1[r1].status = 1; // set to RETIRED
						bw.write(a1[r1].toStringAce(revDate, sctPath));
					}
					r1++;
					nDrop++;
					break;

				}
			} // WHILE (NOT END OF EITHER A1 OR A2)

			// NOT MORE TO COMPARE, HANDLE REMAINING CONCEPTS
			if (r1 < count1) {
				getLog().info("ERROR: MISSED CONCEPT RECORDS r1 < count1");
			}

			if (r2 < count2) {
				while (r2 < count2) { // ADD REMAINING INPUT
					// Write history
					bw.write(a2[r2].toStringAce(revDate, sctPath));
					// Add to append array
					a3[r3] = a2[r2];
					nAdd++;
					r2++;
					r3++;
				}
			}

			// Check counter numbers to master and input file record counts
			countCheck(count1, count2, nSame, nMod, nAdd, nDrop);

			// SETUP NEW MASTER ARRAY
			a2 = new SCTConceptRecord[count1 + nAdd];
			r2 = 0;
			while (r2 < count1) {
				a2[r2] = a1[r2];
				r2++;
			}
			r3 = 0;
			while (r3 < nAdd) {
				a2[r2] = a3[r3];
				r2++;
				r3++;
			}
			count1 = count1 + nAdd;
			a1 = a2;
			Arrays.sort(a1);

		} // FOR (EACH FILE)

		bw.close(); // Need to be sure to the close file!
	}

	protected void processDescriptionsFiles(String bDir, String sctPath)
			throws Exception {
		int count1, count2; // records in arrays 1 & 2
		String fName1, fName2; // file path name
		String revDate;
		SCTDescriptionRecord[] a1, a2, a3 = null;

		getLog().info("START DESCRIPTIONS PROCESSING...");
		// Get base SNOMED directory
		String snomedDirStr = bDir + "/generated-source/org/snomed";
		getLog().info("SNOMED Directory: " + snomedDirStr);

		// Get list of dated SNOMED sub directory names
		File snomedDir = new File(snomedDirStr);
		String[] children = snomedDir.list();
		if (children == null) {
			getLog().info("FAILED: NO SUBDIRECTORIES " + snomedDirStr);
			return;
		}

		// Setup exception report
		String erFileName = bDir
				+ "/classes/org/ihtsdo/sct/sct-arf/descriptions_report.txt";
		BufferedWriter er;
		er = new BufferedWriter(new FileWriter(erFileName));
		getLog().info("exceptions report OUTPUT: " + erFileName);

		// Setup output file
		String outFileName = bDir
				+ "/classes/org/ihtsdo/sct/sct-arf/descriptions.txt";
		BufferedWriter bw;
		getLog().info("sct-arf OUTPUT: " + outFileName);
		bw = new BufferedWriter(new FileWriter(outFileName));
		bw.write("description uuid\tstatus uuid\t" + "concept uuid\t"
				+ "term\t" + "capitalization status\t"
				+ "description type uuid\t" + "language code\t"
				+ "effective date\tpath uuid" + "\r\n");
		// Read in file1 as master file
		revDate = cleanDate(children[0]) + " 00:00:00";
		fName1 = buildFileName(snomedDirStr, children[0], "sct_descriptions_");
		File file1 = new File(fName1);
		if (!file1.exists()) {
			fName1 = buildFileName2(snomedDirStr, children[0],
					"sct_descriptions");
			getLog().info("ALT FILE NAME 1:  " + fName1);
		}
		count1 = countFileLines(fName1);
		getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
		a1 = new SCTDescriptionRecord[count1];
		parseDescriptions(fName1, a1, count1);
		writeDescriptions(bw, a1, count1, revDate, sctPath);

		for (int i = 1; i < children.length; i++) {
			// Get filename of file or directory
			revDate = cleanDate(children[i]) + " 00:00:00";
			fName2 = buildFileName(snomedDirStr, children[i],
					"sct_descriptions_");
			File file2 = new File(fName2);
			if (!file2.exists()) {
				fName2 = buildFileName2(snomedDirStr, children[i],
						"sct_descriptions");
				getLog().info("ALT FILE NAME 2:  " + fName2);
			}
			count2 = countFileLines(fName2);
			getLog().info("Counted: " + count2 + " records, " + fName2);

			// Parse in file2
			a2 = new SCTDescriptionRecord[count2];
			parseDescriptions(fName2, a2, count2);

			int r1 = 0, r2 = 0, r3 = 0; // reset record indices
			int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
			a3 = new SCTDescriptionRecord[count2];
			while ((r1 < count1) && (r2 < count2)) {

				switch (compareDescription(a1[r1], a2[r2])) {
				case 1: // SAME DESCRIPTION, skip to next
					r1++;
					r2++;
					nSame++;
					break;

				case 2: // MODIFIED DESCRIPTION
					// Write history
					bw.write(a2[r2].toStringAce(revDate, sctPath));

					// REPORT DESCRIPTION CHANGE EXCEPTION
					if (a1[r1].conceptId != a2[r2].conceptId) {
						er.write("** CONCEPTID CHANGE ** WAS/IS \r\n");
						er.write("id\tstatus\t" + "conceptId\t" + "termText\t"
								+ "capStatus\t" + "descriptionType\t"
								+ "languageCode\r\n");
						er.write(a1[r1].toString());
						er.write(a2[r2].toString());
						er.write("description uuid\t" + "status uuid\t"
								+ "concept uuid\t" + "term\t"
								+ "capitalization status\t"
								+ "description type uuid\t" + "language code\t"
								+ "effective date\tpath uuid\r\n");
						er.write(a1[r1].toStringAce(revDate, sctPath));
						er.write(a2[r2].toStringAce(revDate, sctPath));
					}

					// Update master via pointer assignment
					a1[r1] = a2[r2];
					r1++;
					r2++;
					nMod++;
					break;

				case 3: // ADDED DESCRIPTION
					// Write history
					bw.write(a2[r2].toStringAce(revDate, sctPath));
					// Hold pointer to append to master
					a3[r3] = a2[r2];
					r2++;
					r3++;
					nAdd++;
					break;

				case 4: // DROPPED DESCRIPTION
					// see ArchitectonicAuxiliary.getStatusFromId()
					if (a1[r1].status != 1) { // if not RETIRED
						a1[r1].status = 1; // set to RETIRED
						bw.write(a1[r1].toStringAce(revDate, sctPath));
					}
					r1++;
					nDrop++;
					break;

				}
			} // WHILE (NOT END OF EITHER A1 OR A2)

			// NOT MORE TO COMPARE, HANDLE REMAINING CONCEPTS
			if (r1 < count1) {
				getLog().info("ERROR: MISSED DESCRIPTION RECORDS r1 < count1");
			}

			if (r2 < count2) {
				while (r2 < count2) { // ADD REMAINING INPUT
					// Write history
					bw.write(a2[r2].toStringAce(revDate, sctPath));
					// Add to append array
					a3[r3] = a2[r2];
					nAdd++;
					r2++;
					r3++;
				}
			}

			// Check counter numbers to master and input file record counts
			countCheck(count1, count2, nSame, nMod, nAdd, nDrop);

			// SETUP NEW MASTER ARRAY
			a2 = new SCTDescriptionRecord[count1 + nAdd];
			r2 = 0;
			while (r2 < count1) {
				a2[r2] = a1[r2];
				r2++;
			}
			r3 = 0;
			while (r3 < nAdd) {
				a2[r2] = a3[r3];
				r2++;
				r3++;
			}
			count1 = count1 + nAdd;
			a1 = a2;
			Arrays.sort(a1);

		} // FOR (EACH FILE)

		bw.close(); // Need to be sure to the close file!
		er.close(); // Need to be sure to the close file!
	}

	protected void processRelationshipsFiles(String bDir, String sctPath)
			throws Exception {
		int count1, count2; // records in arrays 1 & 2
		String fName1, fName2; // file path name
		String revDate;
		SCTRelationshipRecord[] a1, a2, a3 = null;

		getLog().info("START RELATIONSHIPS PROCESSING...");
		// Get base SNOMED directory
		String snomedDirStr = bDir + "/generated-source/org/snomed";
		getLog().info("SNOMED Directory: " + snomedDirStr);

		// Get list of dated SNOMED sub directory names
		File snomedDir = new File(snomedDirStr);
		String[] children = snomedDir.list();
		if (children == null) {
			getLog().info("FAILED: NO SUBDIRECTORIES " + snomedDirStr);
			return;
		}

		// Setup exception report
		String erFileName = bDir
				+ "/classes/org/ihtsdo/sct/sct-arf/relationships_report.txt";
		BufferedWriter er;
		er = new BufferedWriter(new FileWriter(erFileName));
		getLog().info("exceptions report OUTPUT: " + erFileName);

		// Setup output file
		String outFileName = bDir
				+ "/classes/org/ihtsdo/sct/sct-arf/relationships.txt";
		BufferedWriter bw;
		getLog().info("sct-arf OUTPUT: " + outFileName);
		bw = new BufferedWriter(new FileWriter(outFileName));
		bw.write("concept uuid\tstatus uuid\tprimitive\t"
				+ "effective date\tpath uuid" + "\r\n");
		// Read in file1 as master file
		revDate = cleanDate(children[0]) + " 00:00:00";
		fName1 = buildFileName(snomedDirStr, children[0], "sct_relationships_");
		File file1 = new File(fName1);
		if (!file1.exists()) {
			fName1 = buildFileName2(snomedDirStr, children[0],
					"sct_relationships");
			getLog().info("ALT FILE NAME 1:  " + fName1);
		}
		count1 = countFileLines(fName1);
		getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
		a1 = new SCTRelationshipRecord[count1];
		parseRelationships(fName1, a1, count1);
		writeRelationships(bw, a1, count1, revDate, sctPath);

		for (int i = 1; i < children.length; i++) {
			// Get filename of file or directory
			revDate = cleanDate(children[i]) + " 00:00:00";
			fName2 = buildFileName(snomedDirStr, children[i],
					"sct_relationships_");
			File file2 = new File(fName2);
			if (!file2.exists()) {
				fName2 = buildFileName2(snomedDirStr, children[i],
						"sct_relationships");
				getLog().info("ALT FILE NAME 2:  " + fName2);
			}
			count2 = countFileLines(fName2);
			getLog().info("Counted: " + count2 + " records, " + fName2);

			// Parse in file2
			a2 = new SCTRelationshipRecord[count2];
			parseRelationships(fName2, a2, count2);

			int r1 = 0, r2 = 0, r3 = 0; // reset record indices
			int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
			a3 = new SCTRelationshipRecord[count2];
			while ((r1 < count1) && (r2 < count2)) {
				switch (compareRelationship(a1[r1], a2[r2])) {
				case 1: // SAME RELATIONSHIP, skip to next
					r1++;
					r2++;
					nSame++;
					break;

				case 2: // MODIFIED RELATIONSHIP

					// REPORT & HANDLE CHANGE EXCEPTION
					if ((a1[r1].conceptOneID != a2[r2].conceptOneID)
							|| (a1[r1].conceptTwoID != a2[r2].conceptTwoID)) {
						er.write("** CONCEPTID CHANGE ** WAS/IS \r\n");
						er.write("id\t" + "status\t" + "conceptOneID\t"
								+ "relationshipType\t" + "conceptTwoID\r\n");
						er.write(a1[r1].toString());
						er.write(a2[r2].toString());
						er.write("relationship uuid\t" + "status uuid\t"
								+ "source concept uuid\t"
								+ "relationship type uuid\t"
								+ "destination concept uuid\t"
								+ "characteristic type uuid\t"
								+ "refinability uuid\t"
								+ "relationship group\t" + "effective date\t"
								+ "path uuid" + "\r\n");
						er.write(a1[r1].toStringAce(revDate, sctPath));
						er.write(a2[r2].toStringAce(revDate, sctPath));

						// RETIRE & WRITE MASTER RELATIONSHIP a1[r1]
						a1[r1].status = 1; // set to RETIRED
						bw.write(a1[r1].toStringAce(revDate, sctPath));

						// SET EXCEPTIONFLAG for subsequence writes
						// WILL WRITE INPUT RELATIONSHIP w/ NEGATIVE SNOMEDID
						a2[r2].exceptionFlag = true;
					}
					
					// Write history
					bw.write(a2[r2].toStringAce(revDate, sctPath));

					// Update master via pointer assignment
					a1[r1] = a2[r2];
					r1++;
					r2++;
					nMod++;
					break;

				case 3: // ADDED RELATIONSHIP
					// Write history
					bw.write(a2[r2].toStringAce(revDate, sctPath));

					// hold pointer to append to master
					a3[r3] = a2[r2];
					r2++;
					r3++;
					nAdd++;
					break;

				case 4: // DROPPED RELATIONSHIP
					// see ArchitectonicAuxiliary.getStatusFromId()
					if (a1[r1].status != 1) { // if not RETIRED
						a1[r1].status = 1; // set to RETIRED
						bw.write(a1[r1].toStringAce(revDate, sctPath));
					}
					r1++;
					nDrop++;
					break;

				} // SWITCH (COMPARE RELATIONSHIP)
			} // WHILE (NOT END OF EITHER A1 OR A2)

			// NOT MORE TO COMPARE, HANDLE REMAINING CONCEPTS
			if (r1 < count1) {
				getLog().info("ERROR: MISSED RELATIONSHIP RECORDS r1 < count1");
			}

			if (r2 < count2) {
				while (r2 < count2) { // ADD REMAINING INPUT
					// Write history
					bw.write(a2[r2].toStringAce(revDate, sctPath));

					//
					a3[r3] = a2[r2];
					nAdd++;
					r2++;
					r3++;
				}
			}

			// Check counter numbers to master and input file record counts
			countCheck(count1, count2, nSame, nMod, nAdd, nDrop);

			// SETUP NEW MASTER ARRAY
			a2 = new SCTRelationshipRecord[count1 + nAdd];
			r2 = 0;
			while (r2 < count1) {
				a2[r2] = a1[r2];
				r2++;
			}
			r3 = 0;
			while (r3 < nAdd) {
				a2[r2] = a3[r3];
				r2++;
				r3++;
			}
			count1 = count1 + nAdd;
			a1 = a2;
			Arrays.sort(a1);

		} // FOR (EACH FILE)

		bw.close(); // Need to be sure to the close file!
		er.close(); // Need to be sure to the close file!
	}

	private int compareConcept(SCTConceptRecord c1, SCTConceptRecord c2) {
		if (c1.id == c2.id) {
			if ((c1.status == c2.status) && (c1.isprimitive == c2.isprimitive))
				return 1; // SAME
			else
				return 2; // MODIFIED

		} else if (c1.id > c2.id) {
			return 3; // ADDED

		} else {
			return 4; // DROPPED
		}
	}

	private int compareDescription(SCTDescriptionRecord c1,
			SCTDescriptionRecord c2) {
		if (c1.id == c2.id) {
			if ((c1.status == c2.status) && (c1.conceptId == c2.conceptId)
					&& c1.termText.equals(c2.termText)
					&& (c1.capStatus == c2.capStatus)
					&& (c1.descriptionType == c2.descriptionType)
					&& c1.languageCode.equals(c2.languageCode))
				return 1; // SAME
			else
				return 2; // MODIFIED

		} else if (c1.id > c2.id) {
			return 3; // ADDED

		} else {
			return 4; // DROPPED
		}
	}

	private int compareRelationship(SCTRelationshipRecord c1,
			SCTRelationshipRecord c2) {
		if (c1.id == c2.id) {
			if ((c1.status == c2.status)
					&& (c1.conceptOneID == c2.conceptOneID)
					&& (c1.relationshipType == c2.relationshipType)
					&& (c1.conceptTwoID == c2.conceptTwoID)
					&& (c1.characteristic == c2.characteristic)
					&& (c1.refinability == c2.refinability)
					&& (c1.group == c2.group))
				return 1; // SAME
			else
				return 2; // MODIFIED

		} else if (c1.id > c2.id) {
			return 3; // ADDED

		} else {
			return 4; // DROPPED
		}
	}

	protected void parseConcepts(String fName, SCTConceptRecord[] a, int count)
			throws Exception {

		long start = System.currentTimeMillis();

		BufferedReader r = new BufferedReader(new FileReader(fName));
		StreamTokenizer st = new StreamTokenizer(r);
		st.resetSyntax();
		st.wordChars('\u001F', '\u00FF');
		st.whitespaceChars('\t', '\t');
		st.eolIsSignificant(true);
		int concepts = 0;

		skipLineOne(st);
		int tokenType = st.nextToken();
		while ((tokenType != StreamTokenizer.TT_EOF) && (concepts < count)) {
			// CONCEPTID
			long conceptKey = Long.parseLong(st.sval);
			// CONCEPTSTATUS
			tokenType = st.nextToken();
			int conceptStatus = Integer.parseInt(st.sval);
			// FULLYSPECIFIEDNAME: Ignore, already in the descriptions table
			tokenType = st.nextToken();
			// CTV3ID: Do nothing with the legacy CTV3ID
			tokenType = st.nextToken();
			// SNOMEDID: Do nothing with the legacy SNOMED id
			tokenType = st.nextToken();
			// ISPRIMITIVE
			tokenType = st.nextToken();
			int defChar = Integer.parseInt(st.sval);

			// Save to sortable array
			a[concepts] = new SCTConceptRecord(conceptKey, conceptStatus,
					defChar);
			concepts++;

			// CR
			tokenType = st.nextToken();
			// LF
			tokenType = st.nextToken();
			// Beginning of loop
			tokenType = st.nextToken();
		}

		Arrays.sort(a);

		getLog().info(
				"Parse & sort time: " + concepts + " concepts, "
						+ (System.currentTimeMillis() - start)
						+ " milliseconds");
	}

	protected void parseDescriptions(String fName, SCTDescriptionRecord[] a,
			int count) throws Exception {

		long start = System.currentTimeMillis();

		BufferedReader r = new BufferedReader(new FileReader(fName));
		StreamTokenizer st = new StreamTokenizer(r);
		st.resetSyntax();
		st.wordChars('\u001F', '\u00FF');
		st.whitespaceChars('\t', '\t');
		st.eolIsSignificant(true);
		int descriptions = 0;

		skipLineOne(st);
		int tokenType = st.nextToken();
		while ((tokenType != StreamTokenizer.TT_EOF) && (descriptions < count)) {
			// DESCRIPTIONID
			long descriptionId = Long.parseLong(st.sval);
			// DESCRIPTIONSTATUS
			tokenType = st.nextToken();
			int status = Integer.parseInt(st.sval);
			// CONCEPTID
			tokenType = st.nextToken();
			long conceptId = Long.parseLong(st.sval);
			// TERM
			tokenType = st.nextToken();
			String text = st.sval;
			// INITIALCAPITALSTATUS
			tokenType = st.nextToken();
			int capStatus = Integer.parseInt(st.sval);
			// DESCRIPTIONTYPE
			tokenType = st.nextToken();
			int typeInt = Integer.parseInt(st.sval);
			// LANGUAGECODE
			tokenType = st.nextToken();
			String lang = st.sval;

			// Save to sortable array
			a[descriptions] = new SCTDescriptionRecord(descriptionId, status,
					conceptId, text, capStatus, typeInt, lang);
			descriptions++;

			// CR
			tokenType = st.nextToken();
			// LF
			tokenType = st.nextToken();
			// Beginning of loop
			tokenType = st.nextToken();
		}

		Arrays.sort(a);

		getLog().info(
				"Parse & sort time: " + descriptions + " descriptions, "
						+ (System.currentTimeMillis() - start)
						+ " milliseconds");
	}

	protected void parseRelationships(String fName, SCTRelationshipRecord[] a,
			int count) throws Exception {

		long start = System.currentTimeMillis();

		BufferedReader r = new BufferedReader(new FileReader(fName));
		StreamTokenizer st = new StreamTokenizer(r);
		st.resetSyntax();
		st.wordChars('\u001F', '\u00FF');
		st.whitespaceChars('\t', '\t');
		st.eolIsSignificant(true);
		int relationships = 0;

		skipLineOne(st);
		int tokenType = st.nextToken();
		while ((tokenType != StreamTokenizer.TT_EOF) && (relationships < count)) {
			// RELATIONSHIPID
			long relID = Long.parseLong(st.sval);
			// ADD STATUS VALUE: see ArchitectonicAuxiliary.getStatusFromId()
			int status = 0; // status added as CURRENT '0' for parsed record
			// CONCEPTID1
			tokenType = st.nextToken();
			long conceptOneID = Long.parseLong(st.sval);
			// RELATIONSHIPTYPE
			tokenType = st.nextToken();
			long relationshipTypeConceptID = Long.parseLong(st.sval);
			// CONCEPTID2
			tokenType = st.nextToken();
			long conceptTwoID = Long.parseLong(st.sval);
			// CHARACTERISTICTYPE
			tokenType = st.nextToken();
			int characteristic = Integer.parseInt(st.sval);
			// REFINABILITY
			tokenType = st.nextToken();
			int refinability = Integer.parseInt(st.sval);
			// RELATIONSHIPGROUP
			tokenType = st.nextToken();
			int group = Integer.parseInt(st.sval);

			// Save to sortable array
			a[relationships] = new SCTRelationshipRecord(relID, status,
					conceptOneID, relationshipTypeConceptID, conceptTwoID,
					characteristic, refinability, group);
			relationships++;

			// CR
			tokenType = st.nextToken();
			// LF
			tokenType = st.nextToken();
			// Beginning of loop
			tokenType = st.nextToken();

		}

		Arrays.sort(a);

		getLog().info(
				"Parse & sort time: " + relationships + " relationships, "
						+ (System.currentTimeMillis() - start)
						+ " milliseconds");
	}

	protected void writeConcepts(Writer w, SCTConceptRecord[] a, int count,
			String releaseDate, String path) throws Exception {

		long start = System.currentTimeMillis();

		for (int i = 0; i < count; i++) {
			w.write(a[i].toStringAce(releaseDate, path));
		}

		getLog().info(
				"Output time: " + count + " records, "
						+ (System.currentTimeMillis() - start)
						+ " milliseconds");
	}

	protected void writeDescriptions(Writer w, SCTDescriptionRecord[] a,
			int count, String releaseDate, String path) throws Exception {

		long start = System.currentTimeMillis();

		for (int i = 0; i < count; i++) {
			w.write(a[i].toStringAce(releaseDate, path));
		}

		getLog().info(
				"Output time: " + count + " records, "
						+ (System.currentTimeMillis() - start)
						+ " milliseconds");
	}

	protected void writeRelationships(Writer w, SCTRelationshipRecord[] a,
			int count, String releaseDate, String path) throws Exception {

		long start = System.currentTimeMillis();

		for (int i = 0; i < count; i++) {
			w.write(a[i].toStringAce(releaseDate, path));
		}

		getLog().info(
				"Output time: " + count + " records, "
						+ (System.currentTimeMillis() - start)
						+ " milliseconds");
	}

	private static String buildFileName(String p, String d, String r) {
		return p + "/" + d + "/" + r + d.substring(0, 4) + d.substring(5, 7)
				+ d.substring(8, 10) + ".txt";

	}

	private static String buildFileName2(String p, String d, String r) {
		return p + "/" + d + "/" + r + ".txt";

	}

	// Convert yyyy-MM-dd to yyyyMMdd
	private static String cleanDate(String s) {
		return s.substring(0, 4) + s.substring(5, 7) + s.substring(8, 10);
	}

	private void skipLineOne(StreamTokenizer st) throws IOException {
		int tokenType = st.nextToken();
		while (tokenType != StreamTokenizer.TT_EOL) {
			tokenType = st.nextToken();
		}
	}

	private void countCheck(int count1, int count2, int same, int modified,
			int added, int dropped) {

		// CHECK COUNTS TO MASTER FILE1 RECORD COUNT
		if ((same + modified + dropped) == count1) {
			getLog().info(
					"PASSED1:: SAME+MODIFIED+DROPPED = " + same + "+"
							+ modified + "+" + dropped + " = "
							+ (same + modified + dropped) + " == " + count1);
		} else {
			getLog().info(
					"FAILED1:: SAME+MODIFIED+DROPPED = " + same + "+"
							+ modified + "+" + dropped + " = "
							+ (same + modified + dropped) + " != " + count1);
		}

		// CHECK COUNTS TO UPDATE FILE2 RECORD COUNT
		if ((same + modified + added) == count2) {
			getLog().info(
					"PASSED2:: SAME+MODIFIED+ADDED   = " + same + "+"
							+ modified + "+" + added + " = "
							+ (same + modified + added) + " == " + count2);
		} else {
			getLog().info(
					"FAILED2:: SAME+MODIFIED+ADDED   = " + same + "+"
							+ modified + "+" + added + " = "
							+ (same + modified + added) + " != " + count2);
		}

	}

	private static int countFileLines(String fileName) {
		int lineCount = 0;
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(fileName));
			try {
				while (br.readLine() != null) {
					lineCount++;
				}
			} finally {
				br.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// lineCount NOTE: COUNT -1 BECAUSE FIRST LINE SKIPPED
		// lineCount NOTE: REQUIRES THAT LAST LINE IS VALID RECORD
		return lineCount - 1;
	}

	// :TODO: refine exception policy try vs. throw
	private String[] statusStr;

	private String getStatusString(int j) {
		return statusStr[j + 2];
	}

	private void setupStatusStrings() {
		statusStr = new String[14];
		int i = 0;
		int j = -2;
		while (j < 12) {
			String s;
			try {
				s = ArchitectonicAuxiliary.getStatusFromId(j).getUids()
						.iterator().next().toString();
				statusStr[i] = new String(s);
			} catch (IOException e) {
				statusStr[i] = null;
				e.printStackTrace();
			} catch (TerminologyException e) {
				statusStr[i] = null;
				e.printStackTrace();
			}
			i++;
			j++;
		}
	}

}
