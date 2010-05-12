/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.mojo.maven.sct;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StreamTokenizer;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
 * <b>DESCRIPTION: </b><br>
 * 
 * Sct2AceMojo is a maven mojo which converts SNOMED release files to IHTSDO
 * (ACE) Workbench versioned import files in historical sequence.
 * <p>
 * 
 * <b>INPUTS:</b><br>
 * 
 * The POM needs to specify mutually exclusive extensions in separate
 * directories in the array <code>sctInputDirArray</code> parameter. Each
 * directory entry will be parsed to locate SNOMED formated text files in any
 * sub-directories. <br>
 * <br>
 * 
 * Each SNOMED file should contain a version date in the file name
 * <code>"sct_*yyyyMMdd.txt"</code>. If a valid date is not found in the file
 * name then the parent directory name will be checked for a date in the format
 * <code>"yyyy-MM-dd"</code>.
 * 
 * Versioning is performed for the files under the SAME
 * <code>sctInputDirArray[a]</code> directory. Records of the same primary ids
 * are compared in historical sequence to other records of the same primary ids
 * for all applicable files under directory <code>sctInputDirArray[a]</code>.
 * <p>
 * 
 * Set <code>includeCTV3ID</code> and/or <code>includeSNOMEDRTID</code> to true
 * to have the corresponding CTV3 IDs and SNOMED RT IDs to be included in
 * <code>ids.txt</code> output file. The default value is false to not include
 * the CTV3 IDs and SNOMED RT IDs.
 * 
 * <b>OUTPUTS:</b><br>
 * The following files are generated in {project.build.directory}/classes/ace:
 * <p>
 * <code>
 * &#160;&#160;&#160;&#160;concepts.txt, descriptions.txt, descriptions_report.txt<br>
 * &#160;&#160;&#160;&#160;ids.txt, relationships.txt, relationships_report.txt</code>
 * <p>
 * 
 * <b>REQUIRMENTS:</b><br>
 * 
 * 1. RELEASE DATE must be in either the SNOMED file name or the parent folder
 * name. The date must have the format of <code>yyyy-MM-dd</code> or
 * <code>yyyyMMdd</code>. <br>
 * 
 * 2. SNOMED EXTENSIONS must be mutually exclusive from SNOMED CORE and each
 * other; and, placed under separate <code>sctInputDirArray</code> directories.
 * <p>
 * 
 * <b>NOTES:</b><br>
 * Records are NOT VERSIONED between files under DIFFERENT
 * <code>sctInputDirArray</code> directories. The versioned output from
 * <code>sctInputDirArray[a+1]</code> is appended to the versioned output from
 * <code>sctInputDirArray[a]</code>. <br>
 * 
 * @author Marc E. Campbell
 * 
 * @goal sct2ace
 * @requiresDependencyResolution compile
 * @requiresProject false
 * 
 */

public class Sct2AceMojo extends AbstractMojo {

    private static final String FILE_SEPARATOR = File.separator;

    /**
     * Line terminator is deliberately set to CR-LF which is DOS style
     */
    private static final String LINE_TERMINATOR = "\r\n";

    private static final String TAB_CHARACTER = "\t";

    private static final String NHS_UK_DRUG_EXTENSION_FILE_PATH = FILE_SEPARATOR + "net" + FILE_SEPARATOR + "nhs"
        + FILE_SEPARATOR + "uktc" + FILE_SEPARATOR + "ukde";

    private static final String NHS_UK_EXTENSION_FILE_PATH = FILE_SEPARATOR + "net" + FILE_SEPARATOR + "nhs"
        + FILE_SEPARATOR + "uktc" + FILE_SEPARATOR + "uke";

    private static final String SNOMED_FILE_PATH = FILE_SEPARATOR + "org" + FILE_SEPARATOR + "snomed";

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File buildDirectory;

    /**
     * Applicable input sub directory under the build directory.
     * 
     * @parameter
     */
    private String targetSubDir = "";

    /**
     * SCT Input Directories Array. The directory array parameter supported
     * extensions via separate directories in the array.
     * 
     * Files under the SAME directory entry in the array will be versioned
     * relative each other. Each input directory in the array is treated as
     * mutually exclusive to others directories in the array.
     * 
     * @parameter
     * @required
     */
    private String[] sctInputDirArray;

    /**
     * If this contains anything, only convert paths which match one of the
     * enclosed regex
     * 
     * @parameter
     */
    private String[] inputFilters;

    /**
     * 
     * @parameter default-value="false"
     * 
     */
    private boolean includeCTV3ID;

    /**
     * 
     * @parameter default-value="false"
     * 
     */
    private boolean includeSNOMEDRTID;

    /**
     * Appends to the ace output files if they exist.
     * 
     * @parameter default-value="false"
     */
    private boolean appendToAceFiles;

    /**
     * Directory used to output the ACE format files
     * Default value "/classes/ace" set programmatically due to file separator
     * 
     * @parameter
     */
    private String outputDirectory = FILE_SEPARATOR + "classes" + FILE_SEPARATOR + "ace";

    private static String sourceCtv3Uuid = ArchitectonicAuxiliary.Concept.CTV3_ID.getUids()
        .iterator()
        .next()
        .toString();
    private static String sourceSnomedRtUuid = ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.getUids()
        .iterator()
        .next()
        .toString();

    private class SCTConceptRecord implements Comparable<Object> {
        private long id; // CONCEPTID
        private int status; // CONCEPTSTATUS
        private String ctv3id; // CTV3ID
        private String snomedrtid; // SNOMEDID (SNOMED RT ID)
        private int isprimitive; // ISPRIMITIVE

        public SCTConceptRecord(long i, int s, String ctv, String rt, int p) {
            id = i;
            status = s;
            ctv3id = ctv;
            snomedrtid = rt;
            isprimitive = p;
        }

        // method required for object to be sortable (comparable) in arrays
        public int compareTo(Object obj) {
            SCTConceptRecord tmp = (SCTConceptRecord) obj;
            if (this.id < tmp.id) {
                return -1; // instance less than received
            } else if (this.id > tmp.id) {
                return 1; // instance greater than received
            }
            return 0; // instance == received
        }

        // Create string to show some input fields for exception reporting
        public String toString() {
            return id + TAB_CHARACTER + status + TAB_CHARACTER + isprimitive + LINE_TERMINATOR;
        }

        // Create string for concepts.txt file
        public String toStringAce(String date, String path) throws IOException, TerminologyException {

            UUID u = Type3UuidFactory.fromSNOMED(id);

            return u + TAB_CHARACTER + getStatusString(status) + TAB_CHARACTER + isprimitive + TAB_CHARACTER + date
                + TAB_CHARACTER + path + LINE_TERMINATOR;
        }

        // Create string for ids.txt file
        public String toIdsTxt(String source, String date, String path) throws IOException, TerminologyException {

            String outputStr;
            UUID u = Type3UuidFactory.fromSNOMED(id);

            // STATUS FOR IDs IS SET TO CURRENT '0'
            outputStr = u // (canonical) primary uuid
                + TAB_CHARACTER + source // (canonical UUID) source system uuid
                + TAB_CHARACTER + id // (original primary) source id
                // + TAB_CHARACTER + getStatusString(status) -- PARSED STATUS
                // STATUS IS SET TO CURRENT '0' FOR ALL CASES
                + TAB_CHARACTER + getStatusString(0) // (canonical) status uuid
                + TAB_CHARACTER + date // (yyyyMMdd HH:mm:ss) effective date
                + TAB_CHARACTER + path + LINE_TERMINATOR; // (canonical) path
            // uuid

            if (ctv3id != null) {
                outputStr = outputStr + u // (canonical) primary uuid
                    + TAB_CHARACTER + sourceCtv3Uuid // (canonical UUID) source
                    // system uuid
                    + TAB_CHARACTER + ctv3id // (original primary) source id
                    // + TAB_CHARACTER + getStatusString(status) -- PARSED
                    // STATUS
                    // STATUS IS SET TO CURRENT '0' FOR ALL CASES
                    + TAB_CHARACTER + getStatusString(0) // (canonical) status
                    // uuid
                    + TAB_CHARACTER + date // (yyyyMMdd HH:mm:ss) effective date
                    + TAB_CHARACTER + path + LINE_TERMINATOR; // (canonical)
                // path uuid
            }
            if (snomedrtid != null) {
                outputStr = outputStr + u // (canonical) primary uuid
                    + TAB_CHARACTER + sourceSnomedRtUuid // (canonical UUID)
                    // source
                    // system uuid
                    + TAB_CHARACTER + snomedrtid // (original primary) source id
                    // + TAB_CHARACTER + getStatusString(status) -- PARSED
                    // STATUS
                    // STATUS IS SET TO CURRENT '0' FOR ALL CASES
                    + TAB_CHARACTER + getStatusString(0) // (canonical) status
                    // uuid
                    + TAB_CHARACTER + date // (yyyyMMdd HH:mm:ss) effective date
                    + TAB_CHARACTER + path + LINE_TERMINATOR; // (canonical)
                // path uuid
            }
            return outputStr;
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

        public SCTDescriptionRecord(long dId, int s, long cId, String text, int cStat, int typeInt, String lang) {
            id = dId;
            status = s;
            conceptId = cId;
            termText = new String(text);
            capStatus = cStat;
            descriptionType = typeInt;
            languageCode = new String(lang);
        }

        // method required for object to be sortable (comparable) in arrays
        public int compareTo(Object obj) {
            SCTDescriptionRecord tmp = (SCTDescriptionRecord) obj;
            if (this.id < tmp.id) {
                return -1; // instance less than received
            } else if (this.id > tmp.id) {
                return 1; // instance greater than received
            }
            return 0; // instance == received
        }

        // Create string to show some input fields for exception reporting
        public String toString() {
            return id + TAB_CHARACTER + status + TAB_CHARACTER + conceptId + TAB_CHARACTER + termText + TAB_CHARACTER
                + capStatus + TAB_CHARACTER + descriptionType + TAB_CHARACTER + languageCode + LINE_TERMINATOR;
        }

        // Create string for descriptions.txt file
        public String toStringAce(String date, String path) throws IOException, TerminologyException {

            UUID u = Type3UuidFactory.fromSNOMED(id);
            UUID c = Type3UuidFactory.fromSNOMED(conceptId);

            String descType = ArchitectonicAuxiliary.getSnomedDescriptionType(descriptionType)
                .getUids()
                .iterator()
                .next()
                .toString();

            return u + TAB_CHARACTER // description uuid
                + getStatusString(status) + TAB_CHARACTER // status uuid
                + c + TAB_CHARACTER // concept uuid
                + termText + TAB_CHARACTER // term
                + capStatus + TAB_CHARACTER // capitalization status
                + descType + TAB_CHARACTER // description type uuid
                + languageCode + TAB_CHARACTER // language code
                + date + TAB_CHARACTER // effective date
                + path + LINE_TERMINATOR; // path uuid
        }

        // Create string for ids.txt file
        public String toIdsTxt(String source, String date, String path) throws IOException, TerminologyException {

            UUID u = Type3UuidFactory.fromSNOMED(id);

            // STATUS IS SET TO
            return u // (canonical) primary uuid
                + TAB_CHARACTER + source // (canonical UUID) source system uuid
                + TAB_CHARACTER + id // (original primary) source id
                // + TAB_CHARACTER + getStatusString(status) -- PARSED STATUS
                // STATUS IS SET TO CURRENT '0' FOR ALL CASES
                + TAB_CHARACTER + getStatusString(0) // (canonical) status uuid
                + TAB_CHARACTER + date // (yyyyMMdd HH:mm:ss) effective date
                + TAB_CHARACTER + path + LINE_TERMINATOR; // (canonical) path
            // uuid
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

        public SCTRelationshipRecord(long relID, int st, long cOneID, long relType, long cTwoID, int characterType,
                int r, int grp) {
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

        // method required for object to be sortable (comparable) in arrays
        public int compareTo(Object obj) {
            SCTRelationshipRecord tmp = (SCTRelationshipRecord) obj;
            if (this.id < tmp.id) {
                return -1; // instance less than received
            } else if (this.id > tmp.id) {
                return 1; // instance greater than received
            }
            return 0; // instance == received
        }

        // Create string to show some input fields for exception reporting
        public String toString() {
            return id + TAB_CHARACTER + status + TAB_CHARACTER + conceptOneID + TAB_CHARACTER + relationshipType
                + TAB_CHARACTER + conceptTwoID + LINE_TERMINATOR;
        }

        // Create string for relationships.txt file
        public String toStringAce(String date, String path) throws IOException, TerminologyException {

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

            String chType = ArchitectonicAuxiliary.getSnomedCharacteristicType(characteristic)
                .getUids()
                .iterator()
                .next()
                .toString();
            String reType = ArchitectonicAuxiliary.getSnomedRefinabilityType(refinability)
                .getUids()
                .iterator()
                .next()
                .toString();

            return u + TAB_CHARACTER // relationship uuid
                + getStatusString(status) + TAB_CHARACTER // status uuid

                + cOne + TAB_CHARACTER // source concept uuid
                + relType + TAB_CHARACTER // relationship type uuid
                + cTwo + TAB_CHARACTER // destination concept uuid

                + chType + TAB_CHARACTER // characteristic type uuid
                + reType + TAB_CHARACTER // refinability uuid

                + group + TAB_CHARACTER // relationship group -- integer
                + date + TAB_CHARACTER + path + LINE_TERMINATOR;
        }

        // Create string for ids.txt file
        public String toIdsTxt(String source, String date, String path) throws IOException, TerminologyException {

            UUID u = Type3UuidFactory.fromSNOMED(id);

            return u // (canonical) primary uuid
                + TAB_CHARACTER + source // (canonical UUID) source system uuid
                + TAB_CHARACTER + id // (original primary) source id
                // + TAB_CHARACTER + getStatusString(status) -- PARSED STATUS
                // STATUS IS SET TO CURRENT '0' FOR ALL CASES
                + TAB_CHARACTER + getStatusString(0) // (canonical) status uuid
                + TAB_CHARACTER + date // (yyyyMMdd HH:mm:ss) effective date
                + TAB_CHARACTER + path + LINE_TERMINATOR; // (canonical) path
            // uuid
        }
    }

    private class SCTFile {
        File file;
        String revDate;
        String pathId;
        String sourceUuid;

        public SCTFile(File f, String d, String pid) {
            file = f;
            revDate = d;
            pathId = pid;
            sourceUuid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator().next().toString(); // @@@
            // Confirm
            // source
            // uuid
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
            targetSubDir = FILE_SEPARATOR + targetSubDir;
            getLog().info("POM Input Sub Directory: " + targetSubDir);
        }

        // SHOW input directories from POM file
        for (int i = 0; i < sctInputDirArray.length; i++) {
            sctInputDirArray[i] = sctInputDirArray[i].replace('/', File.separatorChar);
            getLog().info("POM Input Directory (" + i + "): " + sctInputDirArray[i]);
            if (!sctInputDirArray[i].startsWith(FILE_SEPARATOR)) {
                sctInputDirArray[i] = FILE_SEPARATOR + sctInputDirArray[i];
            }
        }

        executeMojo(buildDir, targetSubDir, sctInputDirArray, includeCTV3ID, includeSNOMEDRTID);
        getLog().info("POM PROCESSING COMPLETE ");
    }

    void executeMojo(String wDir, String subDir, String[] inDirs, boolean ctv3idTF, boolean snomedrtTF)
            throws MojoFailureException {
        long start = System.currentTimeMillis();
        getLog().info("*** SCT2ACE PROCESSING STARTED ***");

        // Setup build directory
        getLog().info("Build Directory: " + wDir);

        // Setup status UUID String Array
        setupStatusStrings();

        // SETUP OUTPUT directory
        try {
            // Create multiple directories
            String aceOutDir = outputDirectory;
            boolean success = (new File(wDir + aceOutDir)).mkdirs();
            if (success) {
                getLog().info("OUTPUT DIRECTORY: " + wDir + aceOutDir);
            }
        } catch (Exception e) { // Catch exception if any
            getLog().info("Error: could not create output directories");
            throw new MojoFailureException("Error: could not create output directories", e);
        }

        // SETUP CONCEPTS INPUT SCTFile ArrayList
        List<List<SCTFile>> listOfCDirs = getSnomedFiles(wDir, subDir, inDirs, "concept");

        // SETUP DESCRIPTIONS INPUT SCTFile ArrayList
        List<List<SCTFile>> listOfDDirs = getSnomedFiles(wDir, subDir, inDirs, "descriptions");

        // SETUP RELATIONSHIPS INPUT SCTFile ArrayList
        List<List<SCTFile>> listOfRDirs = getSnomedFiles(wDir, subDir, inDirs, "relationships");

        // SETUP "ids.txt" OUTPUT FILE
        String idsFileName = wDir + outputDirectory + FILE_SEPARATOR + "ids.txt";
        BufferedWriter idstxtWriter;
        try {
            idstxtWriter = new BufferedWriter(new FileWriter(idsFileName, appendToAceFiles));
        } catch (IOException e) {
            getLog().info("FAILED: could not create " + idsFileName);
            e.printStackTrace();
            throw new MojoFailureException("FAILED: could not create " + idsFileName, e);
        }
        getLog().info("ids.txt OUTPUT: " + idsFileName);
        // if (writeHeader) {
        // idstxtWriter.write("primary uuid" + TAB_CHARACTER + "source" +
        // TAB_CHARACTER + "source id" + TAB_CHARACTER +
        // "status uuid" + TAB_CHARACTER + ""
        // + "effective date" + TAB_CHARACTER + "path uuid" + LINE_TERMINATOR);
        // }

        // PROCESS SNOMED FILES
        try {
            processConceptsFiles(wDir, listOfCDirs, idstxtWriter, ctv3idTF, snomedrtTF);
        } catch (Exception e1) {
            getLog().info("FAILED: processConceptsFiles()");
            e1.printStackTrace();
            throw new MojoFailureException("FAILED: processConceptsFiles()", e1);
        }
        try {
            processDescriptionsFiles(wDir, listOfDDirs, idstxtWriter);
        } catch (Exception e1) {
            getLog().info("FAILED: processDescriptionsFiles()");
            e1.printStackTrace();
            throw new MojoFailureException("FAILED: processDescriptionsFiles()", e1);
        }
        try {
            processRelationshipsFiles(wDir, listOfRDirs, idstxtWriter);
        } catch (Exception e1) {
            getLog().info("FAILED: processRelationshipsFiles()");
            e1.printStackTrace();
            throw new MojoFailureException("FAILED: processRelationshipsFiles()", e1);
        }

        try {
            idstxtWriter.close();
        } catch (IOException e) {
            getLog().info("FAILED: error closing ids.txt");
            e.printStackTrace();
            throw new MojoFailureException("FAILED: error closing ids.txt", e);
        }
        getLog().info("*** SCT2ACE PROCESSING COMPLETED ***");
        getLog().info("CONVERSION TIME: " + ((System.currentTimeMillis() - start) / 1000) + " seconds");
    }

    private List<List<SCTFile>> getSnomedFiles(String wDir, String subDir, String[] inDirs, String pattern)
            throws MojoFailureException {

        List<List<SCTFile>> listOfDirs = new ArrayList<List<SCTFile>>();
        for (int ii = 0; ii < inDirs.length; ii++) {
            ArrayList<SCTFile> listOfFiles = new ArrayList<SCTFile>();

            getLog().info(
                String.format("%1$s (%2$s): %3$s%4$s%5$s", pattern.toUpperCase(), ii, wDir, subDir, inDirs[ii]));

            File f1 = new File(new File(wDir, subDir), inDirs[ii]);
            ArrayList<File> fv = new ArrayList<File>();
            listFilesRecursive(fv, f1, "sct_" + pattern);

            File[] files = new File[0];
            files = fv.toArray(files);
            Arrays.sort(files);

            FileFilter filter = new FileFilter() {
                public boolean accept(File pathname) {
                    if (inputFilters == null || inputFilters.length == 0) {
                        return true;
                    } else {
                        for (String filter : inputFilters) {
                            if (pathname.getAbsolutePath().replace(File.separatorChar, '/').matches(filter)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            };

            for (File f2 : files) {

                if (filter.accept(f2)) {
                    // ADD SCTFile Entry
                    String tempRevDate = getFileRevDate(f2);
                    String tmpPathID = getFilePathID(f2, wDir, subDir);
                    SCTFile tmpObj = new SCTFile(f2, tempRevDate, tmpPathID);
                    listOfFiles.add(tmpObj);
                    getLog().info("    FILE : " + f2.getName() + " " + tempRevDate);
                }

            }

            listOfDirs.add(listOfFiles);
        }
        return listOfDirs;
    }

    /*
     * ORDER: CONCEPTID CONCEPTSTATUS FULLYSPECIFIEDNAME CTV3ID SNOMEDID
     * ISPRIMITIVE
     * 
     * KEEP: CONCEPTID CONCEPTSTATUS ISPRIMITIVE
     * 
     * IGNORE: FULLYSPECIFIEDNAME CTV3ID SNOMEDID
     */
    protected void processConceptsFiles(String wDir, List<List<SCTFile>> sctv, Writer idstxt, boolean ctv3idTF,
            boolean snomedrtTF) throws Exception {
        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        String sourceUUID, revDate, pathID;
        SCTConceptRecord[] a1, a2, a3 = null;

        getLog().info("START CONCEPTS PROCESSING...");

        // SETUP CONCEPTS OUTPUT FILE
        String outFileName = wDir + outputDirectory + FILE_SEPARATOR + "concepts.txt";
        BufferedWriter bw;
        bw = new BufferedWriter(new FileWriter(outFileName, appendToAceFiles));
        getLog().info("ACE CONCEPTS OUTPUT: " + outFileName);
        // if (writeHeader) {
        // bw.write("concept uuid" + TAB_CHARACTER + "status uuid" +
        // TAB_CHARACTER + "primitive" + TAB_CHARACTER + ""
        // + "effective date" + TAB_CHARACTER + "path uuid" + LINE_TERMINATOR);
        // }

        Iterator<List<SCTFile>> dit = sctv.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<SCTFile> fl = dit.next(); // File List
            Iterator<SCTFile> fit = fl.iterator(); // File Iterator

            // READ file1 as MASTER FILE
            SCTFile f1 = fit.next();
            fName1 = f1.file.getPath();
            revDate = f1.revDate;
            pathID = f1.pathId;
            sourceUUID = f1.sourceUuid;

            count1 = countFileLines(fName1);
            getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
            a1 = new SCTConceptRecord[count1];
            parseConcepts(fName1, a1, count1, ctv3idTF, snomedrtTF);
            writeConcepts(bw, a1, count1, revDate, pathID);
            writeConceptIds(idstxt, a1, count1, sourceUUID, revDate, pathID);

            while (fit.hasNext()) {
                // SETUP CURRENT CONCEPTS INPUT FILE
                SCTFile f2 = fit.next();
                fName2 = f2.file.getPath();
                revDate = f2.revDate;
                pathID = f2.pathId;
                sourceUUID = f2.sourceUuid;

                count2 = countFileLines(fName2);
                getLog().info("Counted: " + count2 + " records, " + fName2);

                // Parse in file2
                a2 = new SCTConceptRecord[count2];
                parseConcepts(fName2, a2, count2, ctv3idTF, snomedrtTF);

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
                        idstxt.write(a2[r2].toIdsTxt(sourceUUID, revDate, pathID));
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
                    while (r2 < count2) { // ADD CONCEPT REMAINING INPUT
                        // Write history
                        bw.write(a2[r2].toStringAce(revDate, pathID));
                        idstxt.write(a2[r2].toIdsTxt(sourceUUID, revDate, pathID));
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

    protected void processDescriptionsFiles(String wDir, List<List<SCTFile>> sctv, Writer idstxt) throws Exception {
        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        String sourceUUID, revDate, pathID;
        SCTDescriptionRecord[] a1, a2, a3 = null;

        getLog().info("START DESCRIPTIONS PROCESSING...");
        // SETUP DESCRIPTIONS EXCEPTION REPORT
        String erFileName = wDir + outputDirectory + FILE_SEPARATOR + "descriptions_report.txt";
        BufferedWriter er;
        er = new BufferedWriter(new FileWriter(erFileName));
        getLog().info("exceptions report OUTPUT: " + erFileName);

        // SETUP DESCRIPTIONS OUTPUT FILE
        String outFileName = wDir + outputDirectory + FILE_SEPARATOR + "descriptions.txt";
        BufferedWriter bw;
        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName, appendToAceFiles), "UTF-8"));
        getLog().info("ACE DESCRIPTIONS OUTPUT: " + outFileName);
        // if (writeHeader) {
        // bw.write("description uuid" + TAB_CHARACTER + "status uuid" +
        // TAB_CHARACTER + "" + "concept uuid" +
        // TAB_CHARACTER + ""
        // + "term" + TAB_CHARACTER + "" + "capitalization status" +
        // TAB_CHARACTER + ""
        // + "description type uuid" + TAB_CHARACTER + "" + "language code" +
        // TAB_CHARACTER + ""
        // + "effective date" + TAB_CHARACTER + "path uuid" + LINE_TERMINATOR);
        // }

        Iterator<List<SCTFile>> dit = sctv.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<SCTFile> fl = dit.next(); // File List
            Iterator<SCTFile> fit = fl.iterator(); // File Iterator

            // READ file1 as MASTER FILE
            SCTFile f1 = fit.next();
            fName1 = f1.file.getPath();
            revDate = f1.revDate;
            pathID = f1.pathId;
            sourceUUID = f1.sourceUuid;

            count1 = countFileLines(fName1);
            getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
            a1 = new SCTDescriptionRecord[count1];
            parseDescriptions(fName1, a1, count1);
            writeDescriptions(bw, a1, count1, revDate, pathID);
            writeDescriptionIds(idstxt, a1, count1, sourceUUID, revDate, pathID);

            while (fit.hasNext()) {
                // SETUP CURRENT CONCEPTS INPUT FILE
                SCTFile f2 = fit.next();
                fName2 = f2.file.getPath();
                revDate = f2.revDate;
                pathID = f2.pathId;
                sourceUUID = f2.sourceUuid;

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
                            er.write("** CONCEPTID CHANGE ** WAS/IS " + LINE_TERMINATOR);
                            er.write("id" + TAB_CHARACTER + "status" + TAB_CHARACTER + "" + "conceptId" + TAB_CHARACTER
                                + "" + "termText" + TAB_CHARACTER + "" + "capStatus" + TAB_CHARACTER + ""
                                + "descriptionType" + TAB_CHARACTER + "" + "languageCode" + LINE_TERMINATOR);
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
                        idstxt.write(a2[r2].toIdsTxt(sourceUUID, revDate, pathID));
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
                    getLog().info("ERROR: MISSED DESCRIPTION RECORDS r1 < count1");
                }

                if (r2 < count2) {
                    while (r2 < count2) { // ADD REMAINING DESCRIPTION INPUT
                        // Write history
                        bw.write(a2[r2].toStringAce(revDate, pathID));
                        idstxt.write(a2[r2].toIdsTxt(sourceUUID, revDate, pathID));
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

    protected void processRelationshipsFiles(String wDir, List<List<SCTFile>> sctv, Writer idstxt) throws Exception {
        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        String sourceUUID, revDate, pathID;
        SCTRelationshipRecord[] a1, a2, a3 = null;

        getLog().info("START RELATIONSHIPS PROCESSING...");

        // Setup exception report
        String erFileName = wDir + outputDirectory + FILE_SEPARATOR + "relationships_report.txt";
        BufferedWriter er;
        er = new BufferedWriter(new FileWriter(erFileName));
        getLog().info("exceptions report OUTPUT: " + erFileName);

        // SETUP CONCEPTS OUTPUT FILE
        String outFileName = wDir + outputDirectory + FILE_SEPARATOR + "relationships.txt";
        BufferedWriter bw;
        bw = new BufferedWriter(new FileWriter(outFileName, appendToAceFiles));
        getLog().info("ACE RELATIONSHIPS OUTPUT: " + outFileName);
        // if (writeHeader) {
        // bw.write("relationship uuid" + TAB_CHARACTER + "" + "status uuid" +
        // TAB_CHARACTER + ""
        // + "source concept uuid" + TAB_CHARACTER + "" +
        // "relationship type uuid" + TAB_CHARACTER + ""
        // + "destination concept uuid" + TAB_CHARACTER + "" +
        // "characteristic type uuid" + TAB_CHARACTER + ""
        // + "refinability uuid" + TAB_CHARACTER + "" + "relationship group" +
        // TAB_CHARACTER + ""
        // + "effective date" + TAB_CHARACTER + "" + "path uuid" +
        // LINE_TERMINATOR);
        // }

        Iterator<List<SCTFile>> dit = sctv.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<SCTFile> fl = dit.next(); // File List
            Iterator<SCTFile> fit = fl.iterator(); // File Iterator

            // READ file1 as MASTER FILE
            SCTFile f1 = fit.next();
            fName1 = f1.file.getPath();
            revDate = f1.revDate;
            pathID = f1.pathId;
            sourceUUID = f1.sourceUuid;

            count1 = countFileLines(fName1);
            getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
            a1 = new SCTRelationshipRecord[count1];
            parseRelationships(fName1, a1, count1);
            writeRelationships(bw, a1, count1, revDate, pathID);
            writeRelationshipIds(idstxt, a1, count1, sourceUUID, revDate, pathID);

            while (fit.hasNext()) {
                // SETUP CURRENT RELATIONSHIPS INPUT FILE
                SCTFile f2 = fit.next();
                fName2 = f2.file.getPath();
                revDate = f2.revDate;
                pathID = f2.pathId;
                sourceUUID = f2.sourceUuid;

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
                            er.write("** CONCEPTID CHANGE ** WAS/IS " + LINE_TERMINATOR);
                            er.write("id" + TAB_CHARACTER + "" + "status" + TAB_CHARACTER + "" + "conceptOneID"
                                + TAB_CHARACTER + "" + "relationshipType" + TAB_CHARACTER + "" + "conceptTwoID"
                                + LINE_TERMINATOR);
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
                        idstxt.write(a2[r2].toIdsTxt(sourceUUID, revDate, pathID));
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
                    getLog().info("ERROR: MISSED RELATIONSHIP RECORDS r1 < count1");
                }

                if (r2 < count2) {
                    while (r2 < count2) { // ADD REMAINING RELATIONSHIP INPUT
                        // Write history
                        bw.write(a2[r2].toStringAce(revDate, pathID));
                        idstxt.write(a2[r2].toIdsTxt(sourceUUID, revDate, pathID));
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

    private int compareDescription(SCTDescriptionRecord c1, SCTDescriptionRecord c2) {
        if (c1.id == c2.id) {
            if ((c1.status == c2.status) && (c1.conceptId == c2.conceptId) && c1.termText.equals(c2.termText)
                && (c1.capStatus == c2.capStatus) && (c1.descriptionType == c2.descriptionType)
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

    private int compareRelationship(SCTRelationshipRecord c1, SCTRelationshipRecord c2) {
        if (c1.id == c2.id) {
            if ((c1.status == c2.status) && (c1.conceptOneID == c2.conceptOneID)
                && (c1.relationshipType == c2.relationshipType) && (c1.conceptTwoID == c2.conceptTwoID)
                && (c1.characteristic == c2.characteristic) && (c1.refinability == c2.refinability)
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

    protected void parseConcepts(String fName, SCTConceptRecord[] a, int count, boolean ctv3idTF, boolean snomedrtTF)
            throws Exception {

        String ctv3Str;
        String snomedrtStr;
        long start = System.currentTimeMillis();

        int CONCEPTID = 0;
        int CONCEPTSTATUS = 1;
        // int FULLYSPECIFIEDNAME = 2;
        int CTV3ID = 3;
        int SNOMEDID = 4; // SNOMED RT ID (Read Code)
        int ISPRIMITIVE = 5;

        BufferedReader br = new BufferedReader(new FileReader(fName));
        int concepts = 0;

        // Header row
        br.readLine();

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);
            long conceptKey = Long.parseLong(line[CONCEPTID]);
            int conceptStatus = Integer.parseInt(line[CONCEPTSTATUS]);
            if (ctv3idTF && (line[CTV3ID].length() > 2)) {
                ctv3Str = new String(line[CTV3ID]);
            } else {
                ctv3Str = null;
            }
            if (snomedrtTF && (line[SNOMEDID].length() > 2)) {
                snomedrtStr = new String(line[SNOMEDID]);
            } else {
                snomedrtStr = null;
            }

            int isPrimitive = Integer.parseInt(line[ISPRIMITIVE]);

            // Save to sortable array
            a[concepts] = new SCTConceptRecord(conceptKey, conceptStatus, ctv3Str, snomedrtStr, isPrimitive);
            concepts++;
        }

        Arrays.sort(a);

        getLog().info(
            "Parse & sort time: " + concepts + " concepts, " + (System.currentTimeMillis() - start) + " milliseconds");
    }

    protected void parseDescriptions(String fName, SCTDescriptionRecord[] a, int count) throws Exception {

        long start = System.currentTimeMillis();

        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fName), "UTF-8"));
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
            a[descriptions] = new SCTDescriptionRecord(descriptionId, status, conceptId, text, capStatus, typeInt, lang);
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
            "Parse & sort time: " + descriptions + " descriptions, " + (System.currentTimeMillis() - start)
                + " milliseconds");
    }

    protected void parseRelationships(String fName, SCTRelationshipRecord[] a, int count) throws Exception {

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
            // STATUS VALUE MUST BE ADDED BECAUSE NOT PRESENT IN SNOMED INPUT
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
            a[relationships] = new SCTRelationshipRecord(relID, status, conceptOneID, relationshipTypeConceptID,
                conceptTwoID, characteristic, refinability, group);
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
            "Parse & sort time: " + relationships + " relationships, " + (System.currentTimeMillis() - start)
                + " milliseconds");
    }

    protected void writeConcepts(Writer w, SCTConceptRecord[] a, int count, String releaseDate, String path)
            throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            w.write(a[i].toStringAce(releaseDate, path));
        }

        getLog().info("Output time: " + count + " records, " + (System.currentTimeMillis() - start) + " milliseconds");
    }

    protected void writeConceptIds(Writer w, SCTConceptRecord[] a, int count, String source, String releaseDate,
            String path) throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            w.write(a[i].toIdsTxt(source, releaseDate, path));
        }

        getLog().info("Output time: " + count + " records, " + (System.currentTimeMillis() - start) + " milliseconds");
    }

    protected void writeDescriptions(Writer w, SCTDescriptionRecord[] a, int count, String releaseDate, String path)
            throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            w.write(a[i].toStringAce(releaseDate, path));
        }

        getLog().info("Output time: " + count + " records, " + (System.currentTimeMillis() - start) + " milliseconds");
    }

    protected void writeDescriptionIds(Writer w, SCTDescriptionRecord[] a, int count, String source,
            String releaseDate, String path) throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            w.write(a[i].toIdsTxt(source, releaseDate, path));
        }

        getLog().info("Output time: " + count + " records, " + (System.currentTimeMillis() - start) + " milliseconds");
    }

    protected void writeRelationships(Writer w, SCTRelationshipRecord[] a, int count, String releaseDate, String path)
            throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            w.write(a[i].toStringAce(releaseDate, path));
        }

        getLog().info("Output time: " + count + " records, " + (System.currentTimeMillis() - start) + " milliseconds");
    }

    protected void writeRelationshipIds(Writer w, SCTRelationshipRecord[] a, int count, String source,
            String releaseDate, String path) throws Exception {

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            w.write(a[i].toIdsTxt(source, releaseDate, path));
        }

        getLog().info("Output time: " + count + " records, " + (System.currentTimeMillis() - start) + " milliseconds");
    }

    private void skipLineOne(StreamTokenizer st) throws IOException {
        int tokenType = st.nextToken();
        while (tokenType != StreamTokenizer.TT_EOL) {
            tokenType = st.nextToken();
        }
    }

    private void countCheck(int count1, int count2, int same, int modified, int added, int dropped) {

        // CHECK COUNTS TO MASTER FILE1 RECORD COUNT
        if ((same + modified + dropped) == count1) {
            getLog().info(
                "PASSED1:: SAME+MODIFIED+DROPPED = " + same + "+" + modified + "+" + dropped + " = "
                    + (same + modified + dropped) + " == " + count1);
        } else {
            getLog().info(
                "FAILED1:: SAME+MODIFIED+DROPPED = " + same + "+" + modified + "+" + dropped + " = "
                    + (same + modified + dropped) + " != " + count1);
        }

        // CHECK COUNTS TO UPDATE FILE2 RECORD COUNT
        if ((same + modified + added) == count2) {
            getLog().info(
                "PASSED2:: SAME+MODIFIED+ADDED   = " + same + "+" + modified + "+" + added + " = "
                    + (same + modified + added) + " == " + count2);
        } else {
            getLog().info(
                "FAILED2:: SAME+MODIFIED+ADDED   = " + same + "+" + modified + "+" + added + " = "
                    + (same + modified + added) + " != " + count2);
        }

    }

    private static int countFileLines(String fileName) throws MojoFailureException {
        int lineCount = 0;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(fileName));
            try {
                while (br.readLine() != null) {
                    lineCount++;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new MojoFailureException("FAILED: error counting lines in " + fileName, ex);
            } finally {
                br.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new MojoFailureException("FAILED: error open BufferedReader for " + fileName, ex);
        }

        // lineCount NOTE: COUNT -1 BECAUSE FIRST LINE SKIPPED
        // lineCount NOTE: REQUIRES THAT LAST LINE IS VALID RECORD
        return lineCount - 1;
    }

    private String getFileRevDate(File f) throws MojoFailureException {
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
                throw new MojoFailureException("FAILED: file name date " + "and directory name date do not agree. ");
            }
        } else if (s1 != null) {
            return s1 + " 00:00:00";
        } else if (s2 != null) {
            return s2 + " 00:00:00";
        } else {
            throw new MojoFailureException("FAILED: date can not be determined"
                + " from either file name date or directory name date.");
        }
    }

    private String getFilePathID(File f, String baseDir, String subDir) throws MojoFailureException {
        String puuid = null;
        UUID u;

        String s;
        if (subDir.equals("")) {
            // :NYI: TEST NO SUBDIRECTORY CODE BRANCH
            s = f.getParent().substring(baseDir.length() - 1);
        } else {
            s = f.getParent().substring(baseDir.length() + subDir.length());
        }

        // :NYI: (Maybe) Additional checks if last directory branch is a date
        // @@@ (Maybe just use the directory branch for UUID)
        if (f.getAbsolutePath().contains(SNOMED_FILE_PATH)) {
            // SNOMED_CORE Path UUID
            puuid = ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids().iterator().next().toString();
            getLog().info("  PATH UUID: " + "SNOMED_CORE " + puuid);
        } else if (s.startsWith(NHS_UK_EXTENSION_FILE_PATH)) {
            // "UK Extensions" Path UUID
            try {
                u = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, "NHS UK Extension Path");
                puuid = u.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                throw new MojoFailureException("FAILED: NHS UK Extension Path.."
                    + "getFilePathID() NoSuchAlgorithmException", e);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new MojoFailureException("FAILED: NHS UK Extension Path.."
                    + "getFilePathID() UnsupportedEncodingException", e);
            }
            getLog().info("  PATH UUID (uke): " + s + " " + puuid);
        } else if (s.startsWith(NHS_UK_DRUG_EXTENSION_FILE_PATH)) {
            // "UK Drug Extensions" Path UUID
            try {
                u = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, "NHS UK Drug Extension Path");
                puuid = u.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                throw new MojoFailureException("FAILED: NHS UK Drug Extension Path.. getFilePathID()"
                    + " NoSuchAlgorithmException", e);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new MojoFailureException("FAILED: NHS UK Drug Extension Path.. getFilePathID()"
                    + " UnsupportedEncodingException", e);
            }
            getLog().info("  PATH UUID (ukde): " + s + " " + puuid);

        } else {
            // OTHER PATH UUID: based on directory path
            try {
                u = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, s);
                puuid = u.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                throw new MojoFailureException("FAILED: getFilePathID() NoSuchAlgorithmException", e);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new MojoFailureException("FAILED: getFilePathID() " + "UnsupportedEncodingException", e);
            }
            getLog().info("  PATH UUID: " + s + " " + puuid);
        }

        return puuid;
    }

    /*
     * 1. build directory buildDir
     */

    private static void listFilesRecursive(ArrayList<File> list, File root, String prefix) {
        if (root.isFile()) {
            list.add(root);
            return;
        }
        File[] files = root.listFiles();
        Arrays.sort(files);
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && files[i].getName().endsWith(".txt") && files[i].getName().startsWith(prefix)) {
                list.add(files[i]);
            }
            if (files[i].isDirectory()) {
                listFilesRecursive(list, files[i], prefix);
            }
        }
    }

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
                s = ArchitectonicAuxiliary.getStatusFromId(j).getUids().iterator().next().toString();
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
