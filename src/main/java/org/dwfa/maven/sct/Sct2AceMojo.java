package org.dwfa.maven.sct;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type3UuidFactory;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * Sct2AceMojo converts SNOMED text release files to ACE text import files.
 * 
 * @goal sct2ace
 * @requiresDependencyResolution compile
 * @requiresProject false
 * 
 */

/*
 * REQUIRMENTS:
 * 
 * 1. RELEASE DATE must be in either the SNOMED file name or parent folder name.
 * The date must have the format of yyyy-MM-dd.
 * 
 * 2. SNOMED EXTENSIONS must be mutually exclusive from SNOMED CORE.
 */

public class Sct2AceMojo extends AbstractMojo {
	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File buildDirectory;

	/**
	 * The greeting to display.
	 * 
	 * @parameter default-value=""
	 */
	private String targetSubDir;

	/**
	 * SCT Input Directories Array.
	 * 
	 * @parameter
	 * @required
	 */
	private String[] sctInputDirArray;

	private class SCTConceptRecord implements Comparable<Object> {
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

	private class SCTDescriptionRecord implements Comparable<Object> {
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

	private class SCTRelationshipRecord implements Comparable<Object> {
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

	private class SCTFile {
		File file;
		String revDate;
		String pathId;
		int set;

		public SCTFile(File f, String d, String pid, int s) {
			file = f;
			revDate = d;
			pathId = pid;
			set = s;
		}

		public String toString() {
			return pathId + " :: " + revDate + " :: " + file.getPath();
		}
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		// SHOW build directory from POM file
		String buildDir = buildDirectory.getAbsolutePath();
		getLog().info("POM Target Directory: " + buildDir);

		// SHOW input sub directory from POM file
		if (!targetSubDir.equals("")) {
			targetSubDir = "/" + targetSubDir;
			getLog().info("POM Input Sub Directory: " + targetSubDir);
		}

		// SHOW input directories from POM file
		for (int i = 0; i < sctInputDirArray.length; i++) {
			getLog().info(
					"POM Input Directory (" + i + "): " + sctInputDirArray[i]);
		}

		try {
			executeMojo(buildDir, targetSubDir, sctInputDirArray);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		getLog().info("POM PROCESSING COMPLETE ");
	}

	/*
	 * 1. build directory buildDir
	 */

	void executeMojo(String wDir, String subDir, String[] inDirs)
			throws Exception {
		long start = System.currentTimeMillis();
		getLog().info("*** SCT2ACE PROCESSING STARTED ***");

		// Setup build directory
		getLog().info("Build Directory: " + wDir);

		// Setup status UUID String Array
		setupStatusStrings();

		// SETUP OUTPUT directory target/classes/ace
		try {
			// Create multiple directories
			String aceOutDir = "/classes/ace";
			boolean success = (new File(wDir + aceOutDir)).mkdirs();
			if (success) {
				getLog().info("OUTPUT DIRECTORY: " + wDir + aceOutDir);
			}
		} catch (Exception e) { // Catch exception if any
			getLog().info("Error: could not create directories");
		}

		// SETUP CONCEPTS INPUT SCTFile ArrayList
		List<List<SCTFile>> listOfCDirs = new ArrayList<List<SCTFile>>(); // ***
		for (int i = 0; i < inDirs.length; i++) {
			ArrayList<SCTFile> listOfCFiles = new ArrayList<SCTFile>(); // ***

			getLog().info("CONCEPTS (" + i + "): " + wDir + subDir + inDirs[i]);

			// PARSE each sub-directory for "sct_descriptions*.txt" files
			File f1 = new File(wDir + subDir + inDirs[i]);
			ArrayList<File> fv = new ArrayList<File>();
			listFilesRecursive(fv, f1, "sct_concepts");

			Iterator<File> it = fv.iterator();
			while (it.hasNext()) {
				File f2 = it.next();
				// ADD SCTFile Entry
				String tempRevDate = getFileRevDate(f2);
				String tmpPathID = getFilePathID(f2, wDir, subDir);
				SCTFile tmpObj = new SCTFile(f2, tempRevDate, tmpPathID, i);
				listOfCFiles.add(tmpObj); // ***
				getLog().info("    FILE : " + f2.getName() + " " + tempRevDate);
			}
			listOfCDirs.add(listOfCFiles); // ***
		}

		// SETUP DESCRIPTIONS INPUT SCTFile ArrayList
		List<List<SCTFile>> listOfDDirs = new ArrayList<List<SCTFile>>(); // **
		for (int i = 0; i < inDirs.length; i++) {
			ArrayList<SCTFile> listOfDFiles = new ArrayList<SCTFile>(); // **
			getLog().info(
					"DESCRIPTIONS (" + i + "): " + wDir + subDir + inDirs[i]);

			// PARSE each sub-directory for "sct_descriptions*.txt" files
			File f1 = new File(wDir + subDir + inDirs[i]);
			ArrayList<File> fv = new ArrayList<File>();
			listFilesRecursive(fv, f1, "sct_descriptions");

			Iterator<File> it = fv.iterator();
			while (it.hasNext()) {
				File f2 = it.next();
				// ADD SCTFile Entry
				String tempRevDate = getFileRevDate(f2);
				String tmpPathID = getFilePathID(f2, wDir, subDir);
				SCTFile tmpObj = new SCTFile(f2, tempRevDate, tmpPathID, i);
				listOfDFiles.add(tmpObj); // **
				getLog().info("    FILE : " + f2.getName() + " " + tempRevDate);
			}
			listOfDDirs.add(listOfDFiles); // **
		}

		// SETUP RELATIONSHIPS INPUT SCTFile ArrayList
		List<List<SCTFile>> listOfRDirs = new ArrayList<List<SCTFile>>(); // *
		for (int i = 0; i < inDirs.length; i++) {
			ArrayList<SCTFile> listOfRFiles = new ArrayList<SCTFile>(); // *
			getLog().info(
					"RELATIONSHIPS (" + i + "): " + wDir + subDir + inDirs[i]);

			// PARSE each sub-directory for "sct_relationships*.txt" files
			File f1 = new File(wDir + subDir + inDirs[i]);
			ArrayList<File> fv = new ArrayList<File>();
			listFilesRecursive(fv, f1, "sct_relationships");

			Iterator<File> it = fv.iterator();
			while (it.hasNext()) {
				File f2 = it.next();
				// ADD SCTFile Entry
				String tempRevDate = getFileRevDate(f2);
				String tmpPathID = getFilePathID(f2, wDir, subDir);
				SCTFile tmpObj = new SCTFile(f2, tempRevDate, tmpPathID, i);
				listOfRFiles.add(tmpObj); // *
				getLog().info("    FILE : " + f2.getName() + " " + tempRevDate);
			}
			listOfRDirs.add(listOfRFiles); // *
		}

		processConceptsFiles(wDir, listOfCDirs); // ***
		processDescriptionsFiles(wDir, listOfDDirs); // **
		processRelationshipsFiles(wDir, listOfRDirs); // *

		getLog().info("*** SCT2ACE PROCESSING COMPLETED ***");
		getLog().info(
				"CONVERSION TIME: "
						+ ((System.currentTimeMillis() - start) / 1000)
						+ " seconds");
	}

	private String getFileRevDate(File f) {
		int pos;
		// Check file name for date yyyyMMdd
		// EXAMPLE: ../net/nhs/uktc/ukde/sct_relationships_uk_drug_20090401.txt
		pos = f.getName().length() - 12; // "yyyyMMdd.txt"
		String s1 = f.getName().substring(pos, pos + 8);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		dateFormat.setLenient(false);
		try {
			dateFormat.parse(s1);
		} catch (ParseException pe) {
			s1 = null;
		}

		// Check path for date yyyy-MM-dd
		// EXAMPLE: ../org/snomed/2003-01-31
		pos = f.getParent().length() - 10; // "yyyy-MM-dd"
		String s2 = f.getParent().substring(pos);
		// normalize date format
		s2 = s2.substring(0, 4) + s2.substring(5, 7) + s2.substring(8, 10);
		try {
			dateFormat.parse(s2);
		} catch (ParseException pe) {
			s2 = null;
		}

		// 
		if ((s1 != null) && (s2 != null)) {
			if (s1.equals(s2)) {
				return s1 + " 00:00:00";
			} else {
				return null; // !!! throw exception here
			}
		} else if (s1 != null) {
			return s1 + " 00:00:00";
		} else if (s2 != null) {
			return s2 + " 00:00:00";
		} else {
			return null;
		}
	}

	private String getFilePathID(File f, String baseDir, String subDir) {
		String puuid = null;
		UUID u;

		String s;
		if (subDir.equals("")) {
			// !!! @@@ :TODO: TEST NO SUBDIRECTORY CODE BRANCH
			s = f.getParent().substring(baseDir.length() - 1);
		} else {
			s = f.getParent().substring(baseDir.length() + subDir.length());
		}

		// :NYI: (Maybe) Additional checks if last directory branch is a date
		// @@@ (Maybe just use the directory branch for UUID)
		if (s.substring(0, 11).equals("/org/snomed")) {
			// SNOMED_CORE Path UUID
			puuid = ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()
					.iterator().next().toString();
			getLog().info("  PATH UUID: " + "SNOMED_CORE " + puuid);
		} else if (s.equals("/net/nhs/uktc/uke")) {
			// "UK Extensions" Path UUID
			try {
				u = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
						"NHS UK Extension Path");
				puuid = u.toString();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			getLog().info("  PATH UUID (uke): " + s + " " + puuid);
		} else if (s.equals("/net/nhs/uktc/ukde")) {
			// "UK Drug Extensions" Path UUID
			try {
				u = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
						"NHS UK Drug Extension Path");
				puuid = u.toString();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			getLog().info("  PATH UUID (ukde): " + s + " " + puuid);

		} else {
			// OTHER PATH UUID: based on directory path
			// !!! @@@ :TODO: TEST THIS CODE BRANCH
			try {
				u = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
						s);
				puuid = u.toString();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			getLog().info("  PATH UUID: " + s + " " + puuid);
		}

		return puuid;
	}

	/*
	 * 1. build directory buildDir
	 */

	private static void listFilesRecursive(ArrayList<File> list, File root,
			String prefix) {
		if (root.isFile()) {
			list.add(root);
			return;
		}
		File[] files = root.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile() && files[i].getName().endsWith(".txt")
					&& files[i].getName().startsWith(prefix)) {
				list.add(files[i]);
			}
			if (files[i].isDirectory()) {
				listFilesRecursive(list, files[i], prefix);
			}
		}
	}

	/*
	 * ORDER: CONCEPTID CONCEPTSTATUS FULLYSPECIFIEDNAME CTV3ID SNOMEDID
	 * ISPRIMITIVE
	 * 
	 * KEEP: CONCEPTID CONCEPTSTATUS ISPRIMITIVE
	 * 
	 * IGNORE: FULLYSPECIFIEDNAME CTV3ID SNOMEDID
	 */
	protected void processConceptsFiles(String wDir, List<List<SCTFile>> sctv)
			throws Exception {
		int count1, count2; // records in arrays 1 & 2
		String fName1, fName2; // file path name
		String revDate, pathID;
		SCTConceptRecord[] a1, a2, a3 = null;

		getLog().info("START CONCEPTS PROCESSING...");

		// SETUP CONCEPTS OUTPUT FILE
		String outFileName = wDir + "/classes/ace/concepts.txt";
		BufferedWriter bw;
		getLog().info("ACE CONCEPTS OUTPUT: " + outFileName);
		bw = new BufferedWriter(new FileWriter(outFileName));
		bw.write("concept uuid\tstatus uuid\tprimitive\t"
				+ "effective date\tpath uuid" + "\r\n");

		Iterator<List<SCTFile>> dit = sctv.iterator(); // Directory Iterator *
		while (dit.hasNext()) {
			List<SCTFile> fl = dit.next(); // File List *
			Iterator<SCTFile> fit = fl.iterator(); // File Iterator *

			// READ file1 as MASTER FILE
			SCTFile f1 = fit.next();
			fName1 = f1.file.getPath();
			revDate = f1.revDate;
			pathID = f1.pathId;

			count1 = countFileLines(fName1);
			getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
			a1 = new SCTConceptRecord[count1];
			parseConcepts(fName1, a1, count1);
			writeConcepts(bw, a1, count1, revDate, pathID);

			while (fit.hasNext()) {
				// SETUP CURRENT CONCEPTS INPUT FILE
				SCTFile f2 = fit.next();
				fName2 = f2.file.getPath();
				revDate = f2.revDate;
				pathID = f2.pathId;

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
						bw.write(a2[r2].toStringAce(revDate, pathID));
						// Update master via pointer assignment
						a1[r1] = a2[r2];
						r1++;
						r2++;
						nMod++;
						break;

					case 3: // ADDED CONCEPT
						// Write history
						bw.write(a2[r2].toStringAce(revDate, pathID));
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
							bw.write(a1[r1].toStringAce(revDate, pathID));
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
						bw.write(a2[r2].toStringAce(revDate, pathID));
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

			} // WHILE (EACH CONCEPTS INPUT FILE)
		} // WHILE (EACH CONCEPTS DIRECTORY) *

		bw.close(); // Need to be sure to the close file!
	}

	protected void processDescriptionsFiles(String wDir,
			List<List<SCTFile>> sctv) throws Exception {
		int count1, count2; // records in arrays 1 & 2
		String fName1, fName2; // file path name
		String revDate, pathID;
		SCTDescriptionRecord[] a1, a2, a3 = null;

		getLog().info("START DESCRIPTIONS PROCESSING...");
		// SETUP DESCRIPTIONS EXCEPTION REPORT
		String erFileName = wDir + "/classes/ace/descriptions_report.txt";
		BufferedWriter er;
		er = new BufferedWriter(new FileWriter(erFileName));
		getLog().info("exceptions report OUTPUT: " + erFileName);

		// SETUP DESCRIPTIONS OUTPUT FILE
		String outFileName = wDir + "/classes/ace/descriptions.txt";
		BufferedWriter bw;
		getLog().info("ACE DESCRIPTIONS OUTPUT: " + outFileName);
		bw = new BufferedWriter(new FileWriter(outFileName));
		bw.write("description uuid\tstatus uuid\t" + "concept uuid\t"
				+ "term\t" + "capitalization status\t"
				+ "description type uuid\t" + "language code\t"
				+ "effective date\tpath uuid" + "\r\n");

		Iterator<List<SCTFile>> dit = sctv.iterator(); // Directory Iterator **
		while (dit.hasNext()) {
			List<SCTFile> fl = dit.next(); // File List **
			Iterator<SCTFile> fit = fl.iterator(); // File Iterator **

			// READ file1 as MASTER FILE
			SCTFile f1 = fit.next();
			fName1 = f1.file.getPath();
			revDate = f1.revDate;
			pathID = f1.pathId;

			count1 = countFileLines(fName1);
			getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
			a1 = new SCTDescriptionRecord[count1];
			parseDescriptions(fName1, a1, count1);
			writeDescriptions(bw, a1, count1, revDate, pathID);

			while (fit.hasNext()) {
				// SETUP CURRENT CONCEPTS INPUT FILE
				SCTFile f2 = fit.next();
				fName2 = f2.file.getPath();
				revDate = f2.revDate;
				pathID = f2.pathId;

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
						bw.write(a2[r2].toStringAce(revDate, pathID));

						// REPORT DESCRIPTION CHANGE EXCEPTION
						if (a1[r1].conceptId != a2[r2].conceptId) {
							er.write("** CONCEPTID CHANGE ** WAS/IS \r\n");
							er.write("id\tstatus\t" + "conceptId\t"
									+ "termText\t" + "capStatus\t"
									+ "descriptionType\t" + "languageCode\r\n");
							er.write(a1[r1].toString());
							er.write(a2[r2].toString());
						}

						// Update master via pointer assignment
						a1[r1] = a2[r2];
						r1++;
						r2++;
						nMod++;
						break;

					case 3: // ADDED DESCRIPTION
						// Write history
						bw.write(a2[r2].toStringAce(revDate, pathID));
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
							bw.write(a1[r1].toStringAce(revDate, pathID));
						}
						r1++;
						nDrop++;
						break;

					}
				} // WHILE (NOT END OF EITHER A1 OR A2)

				// NOT MORE TO COMPARE, HANDLE REMAINING CONCEPTS
				if (r1 < count1) {
					getLog().info(
							"ERROR: MISSED DESCRIPTION RECORDS r1 < count1");
				}

				if (r2 < count2) {
					while (r2 < count2) { // ADD REMAINING INPUT
						// Write history
						bw.write(a2[r2].toStringAce(revDate, pathID));
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

			} // WHILE (EACH DESCRIPTIONS INPUT FILE)
		} // WHILE (EACH DESCRIPTIONS DIRECTORY) *

		bw.close(); // Need to be sure to the close file!
		er.close(); // Need to be sure to the close file!
	}

	protected void processRelationshipsFiles(String wDir,
			List<List<SCTFile>> sctv) throws Exception {
		int count1, count2; // records in arrays 1 & 2
		String fName1, fName2; // file path name
		String revDate, pathID;
		SCTRelationshipRecord[] a1, a2, a3 = null;

		getLog().info("START RELATIONSHIPS PROCESSING...");

		// Setup exception report
		String erFileName = wDir + "/classes/ace/relationships_report.txt";
		BufferedWriter er;
		er = new BufferedWriter(new FileWriter(erFileName));
		getLog().info("exceptions report OUTPUT: " + erFileName);

		// SETUP CONCEPTS OUTPUT FILE
		String outFileName = wDir + "/classes/ace/relationships.txt";
		BufferedWriter bw;
		getLog().info("ACE RELATIONSHIPS OUTPUT: " + outFileName);
		bw = new BufferedWriter(new FileWriter(outFileName));
		bw.write("relationship uuid\t" + "status uuid\t"
				+ "source concept uuid\t" + "relationship type uuid\t"
				+ "destination concept uuid\t" + "characteristic type uuid\t"
				+ "refinability uuid\t" + "relationship group\t"
				+ "effective date\t" + "path uuid" + "\r\n");

		Iterator<List<SCTFile>> dit = sctv.iterator(); // Directory Iterator *
		while (dit.hasNext()) {
			List<SCTFile> fl = dit.next(); // File List *
			Iterator<SCTFile> fit = fl.iterator(); // File Iterator *

			// READ file1 as MASTER FILE
			SCTFile f1 = fit.next();
			fName1 = f1.file.getPath();
			revDate = f1.revDate;
			pathID = f1.pathId;

			count1 = countFileLines(fName1);
			getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
			a1 = new SCTRelationshipRecord[count1];
			parseRelationships(fName1, a1, count1);
			writeRelationships(bw, a1, count1, revDate, pathID);

			while (fit.hasNext()) {
				// SETUP CURRENT RELATIONSHIPS INPUT FILE
				SCTFile f2 = fit.next();
				fName2 = f2.file.getPath();
				revDate = f2.revDate;
				pathID = f2.pathId;

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
							er
									.write("id\t" + "status\t"
											+ "conceptOneID\t"
											+ "relationshipType\t"
											+ "conceptTwoID\r\n");
							er.write(a1[r1].toString());
							er.write(a2[r2].toString());

							// RETIRE & WRITE MASTER RELATIONSHIP a1[r1]
							a1[r1].status = 1; // set to RETIRED
							bw.write(a1[r1].toStringAce(revDate, pathID));

							// SET EXCEPTIONFLAG for subsequence writes
							// WILL WRITE INPUT RELATIONSHIP w/ NEGATIVE
							// SNOMEDID
							a2[r2].exceptionFlag = true;
						}

						// Write history
						bw.write(a2[r2].toStringAce(revDate, pathID));

						// Update master via pointer assignment
						a1[r1] = a2[r2];
						r1++;
						r2++;
						nMod++;
						break;

					case 3: // ADDED RELATIONSHIP
						// Write history
						bw.write(a2[r2].toStringAce(revDate, pathID));

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
							bw.write(a1[r1].toStringAce(revDate, pathID));
						}
						r1++;
						nDrop++;
						break;

					} // SWITCH (COMPARE RELATIONSHIP)
				} // WHILE (NOT END OF EITHER A1 OR A2)

				// NOT MORE TO COMPARE, HANDLE REMAINING CONCEPTS
				if (r1 < count1) {
					getLog().info(
							"ERROR: MISSED RELATIONSHIP RECORDS r1 < count1");
				}

				if (r2 < count2) {
					while (r2 < count2) { // ADD REMAINING INPUT
						// Write history
						bw.write(a2[r2].toStringAce(revDate, pathID));

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

			} // WHILE (EACH INPUT RELATIONSHIPS FILE)
		} // WHILE (EACH RELATIONSHIPS DIRECTORY) *

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

		int CONCEPTID = 0;
		int CONCEPTSTATUS = 1;
		// int FULLYSPECIFIEDNAME = 2;
		// int CTV3ID = 3;
		// int SNOMEDID = 4;
		int ISPRIMITIVE = 5;

		BufferedReader br = new BufferedReader(new FileReader(fName));
		int concepts = 0;

		// Header row
		br.readLine();

		while (br.ready()) {
			String[] line = br.readLine().split("\t");
			long conceptKey = Long.parseLong(line[CONCEPTID]);
			int conceptStatus = Integer.parseInt(line[CONCEPTSTATUS]);
			int isPrimitive = Integer.parseInt(line[ISPRIMITIVE]);

			// Save to sortable array
			a[concepts] = new SCTConceptRecord(conceptKey, conceptStatus,
					isPrimitive);
			concepts++;
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
