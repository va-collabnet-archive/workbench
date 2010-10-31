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
package org.ihtsdo.mojo.maven.rf1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.id.Type3UuidFactory;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * 
 * @author Marc E. Campbell
 *
 * @goal rf1-subsets-to-arf
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class Rf1ToArfSubsetsMojo extends AbstractMojo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final String FILE_SEPARATOR = File.separator;

    /**
     * Line terminator is deliberately set to CR-LF which is DOS style
     */
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";

    private static UUID uuidCurrent;
    private static UUID uuidRetired;

    /**
     * Start date (inclusive)
     * 
     * @parameter
     */
    private Date dateStart;

    /**
     * Stop date inclusive
     * 
     * @parameter
     */
    private Date dateStop;

    /**
     * Location of the target directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    /**
     * Applicable input sub directory under the target directory.
     * 
     * @parameter
     */
    private String targetSubDir = "";

    /**
     * Input Directories Array. The directory array parameter supported
     * extensions via separate directories in the array.
     * 
     * @parameter
     * @required
     */
    private Rf1Dir[] rf1Dirs;

    /**
     * Array Original SCT Id for subset and UUID Name for subset.
     * 
     * @parameter
     * @required
     */
    private Rf1SubsetId[] rf1SubsetIds;

    /**
     * Directory used to output the eConcept format files
     * Default value "/classes" set programmatically due to file separator
     * 
     * @parameter default-value="classes"
     */
    private String outputDirectory;

    /**
     * 
     * @parameter default-value="subsets"
     */
    private String outputFileName;

    /**
     * 
     * @parameter
     */
    private ArrayList<String> fileFilterList;

    private String scratchDirectory = FILE_SEPARATOR + "tmp_steps";

    private String fNameSubsetRefsetArf;

    private HashMap<Long, Long> mapSubsetIdToOriginalSctId; // Key = SctId, Value=SctId
    private HashMap<Long, String> mapSubsetIdToPathUuid; // Key = SctId, Value=UUIDString
    private HashMap<Long, String> mapSubsetIdToRefsetUuid; // Key = SctId, Value=UUIDString

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("::: BEGIN Rf1SubsetsToArf");

        // SHOW target directory from POM file
        String targetDir = targetDirectory.getAbsolutePath();
        getLog().info("    POM: Target Directory: " + targetDir);

        // SHOW input sub directory from POM file
        if (!targetSubDir.equals("")) {
            targetSubDir = FILE_SEPARATOR + targetSubDir;
            getLog().info("    POM: Target Sub Directory: " + targetSubDir);
        }

        if (rf1Dirs == null)
            rf1Dirs = new Rf1Dir[0];
        for (int i = 0; i < rf1Dirs.length; i++) {
            rf1Dirs[i].setDirName(rf1Dirs[i].getDirName().replace('/', File.separatorChar));
            getLog().info("    POM: Input Directory (" + i + ") = " + rf1Dirs[i]);
            if (!rf1Dirs[i].getDirName().startsWith(FILE_SEPARATOR)) {
                rf1Dirs[i].setDirName(FILE_SEPARATOR + rf1Dirs[i].getDirName());
            }
        }

        // SHOW input sub directory from POM file
        if (!outputDirectory.equals("")) {
            outputDirectory = FILE_SEPARATOR + outputDirectory;
            getLog().info("    POM: Output Directory: " + outputDirectory);
        }

        executeMojo(targetDir, targetSubDir, rf1Dirs, rf1SubsetIds, fileFilterList, outputDirectory, outputFileName);
        getLog().info("::: END Rf1SubsetsToArf");
    }

    public void executeMojo(String tDir, String tSubDir, Rf1Dir[] inDirs, Rf1SubsetId[] subsetIds,
            ArrayList<String> fFilters, String outDir, String outFileName) throws MojoFailureException {
        uuidCurrent = ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next();
        uuidRetired = ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next();

        getLog().info("::: Target Directory: " + tDir);
        getLog().info("::: Target Sub Directory:     " + tSubDir);
        getLog().info("::: Start date (inclusive) = " + dateStart);
        getLog().info("::: Stop date (inclusive) =  " + dateStop);

        for (int i = 0; i < inDirs.length; i++)
            getLog().info("::: Input Directory (" + i + ") = " + inDirs[i].getDirName());

        for (Rf1SubsetId idRec : subsetIds)
            getLog().info("::: SubsetId " + idRec.toString());

        // Setup target (build) directory
        getLog().info("    Target Build Directory: " + tDir);

        // Setup target (build) directory
        BufferedWriter bwRefSet = null;
        fNameSubsetRefsetArf = tDir + scratchDirectory + FILE_SEPARATOR + outFileName
                + "_integer.refset";

        try {
            // FILE & DIRECTORY SETUP
            // Create multiple directories
            boolean success = (new File(tDir + outDir)).mkdirs();
            if (success) {
                getLog().info("::: Output Directory: " + tDir + outDir);
            }

            String tmpDir = scratchDirectory;
            success = (new File(tDir + tmpDir)).mkdirs();
            if (success) {
                getLog().info("::: Scratch Directory: " + tDir + tmpDir);
            }

            // SETUP REFSET OUTPUT FILE
            bwRefSet = new BufferedWriter(new FileWriter(fNameSubsetRefsetArf));
            getLog().info("::: REFSET SUBSETS OUTPUT: " + fNameSubsetRefsetArf);

            // SETUP SUBSET ID TO UUID MAP
            List<List<RF1File>> fileListList = null;
            List<RF1File> allFiles = new ArrayList<RF1File>();

            ArrayList<String> filter = new ArrayList<String>();
            filter.add("DER1_");
            filter.add("Subsets");
            filter.add(".TXT");
            if (fFilters != null)
                filter.addAll(fFilters);
            fileListList = Rf1Dir.getRf1Files(tDir, tSubDir, inDirs, filter, dateStart, dateStop);
            logFileListList(inDirs, fileListList);
            for (List<RF1File> fList : fileListList)
                allFiles.addAll(fList);
            parseSubsetIdToOriginalUuidMap(allFiles);

            // PROCESS SUBSET MEMBERS FILES FOR EACH ROOT DIRECTORY
            filter = new ArrayList<String>();
            filter.add("DER1_");
            filter.add("SubsetMembers");
            filter.add(".TXT");
            if (fFilters != null)
                filter.addAll(fFilters);
            for (Rf1Dir d : inDirs) {
                Rf1Dir[] tmpDirs = { d };
                fileListList = Rf1Dir.getRf1Files(tDir, tSubDir, tmpDirs, filter, dateStart,
                        dateStop);
                logFileListList(tmpDirs, fileListList);
                processSubsetMembers(fileListList, bwRefSet);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new MojoFailureException("File Not Found ", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("IO Exception ", e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new MojoFailureException("No such algorithm ", e);
        }
    }

    private int compareMember(Rf1SubsetMember m1, Rf1SubsetMember m2) {
        if ((m1.subsetId == m2.subsetId) && (m1.memberId == m2.memberId)) {
            if (m1.memberValue == m2.memberValue)
                return 1; // SAME
            else
                return 2; // SAME COMPONENT, DIFFERENT VALUE
        } else if (m1.subsetId == m2.subsetId) {
            if (m1.memberId > m2.memberId)
                return 3; // ADDED
            else
                return 4; // DROPPED
        } else if (m1.subsetId > m2.subsetId) {
            return 3; // ADDED            
        } else { // m1.subsetId < m2.subsetId
            return 4; // DROPPED            
        }
    }

    private String convertMemberToArf(Rf1SubsetMember m, String date)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();

        // REFSET_UUID
        sb.append(mapSubsetIdToRefsetUuid.get(m.subsetId) + TAB_CHARACTER);
        // MEMBER_UUID ... of refset member
        UUID uuid = Type5UuidFactory.get(Rf1Dir.SUBSETMEMBER_ID_NAMESPACE_UUID_TYPE1
                + mapSubsetIdToOriginalSctId.get(m.subsetId) + m.memberId);
        sb.append(uuid.toString() + TAB_CHARACTER);
        // STATUS_UUID
        if (m.getStatus() == 0)
            sb.append(uuidCurrent + TAB_CHARACTER);
        else
            sb.append(uuidRetired + TAB_CHARACTER);
        // COMPONENT_UUID ... of member's referenced (concept, description, ...) component
        uuid = Type3UuidFactory.fromSNOMED(m.memberId);
        sb.append(uuid.toString() + TAB_CHARACTER);
        // EFFECTIVE_DATE
        sb.append(date + TAB_CHARACTER);
        // PATH_UUID
        sb.append(mapSubsetIdToPathUuid.get(m.subsetId) + TAB_CHARACTER);
        // INTEGER_EXTENSION_VALUE
        sb.append(m.memberValue + LINE_TERMINATOR);

        return sb.toString();
    }

    private void countCheck(int count1, int count2, int same, int modified, int added, int dropped) {

        // CHECK COUNTS TO MASTER FILE1 RECORD COUNT
        if ((same + modified + dropped) == count1) {
            getLog().info(
                    "PASSED1:: SAME+MODIFIED+DROPPED = " + same + "+" + modified + "+" + dropped
                            + " = " + (same + modified + dropped) + " == " + count1);
        } else {
            getLog().info(
                    "FAILED1:: SAME+MODIFIED+DROPPED = " + same + "+" + modified + "+" + dropped
                            + " = " + (same + modified + dropped) + " != " + count1);
        }

        // CHECK COUNTS TO UPDATE FILE2 RECORD COUNT
        if ((same + modified + added) == count2) {
            getLog().info(
                    "PASSED2:: SAME+MODIFIED+ADDED   = " + same + "+" + modified + "+" + added
                            + " = " + (same + modified + added) + " == " + count2);
        } else {
            getLog().info(
                    "FAILED2:: SAME+MODIFIED+ADDED   = " + same + "+" + modified + "+" + added
                            + " = " + (same + modified + added) + " != " + count2);
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

    private void logFileListList(Rf1Dir[] dirs, List<List<RF1File>> fileListList) {
        StringBuffer sb = new StringBuffer();
        for (Rf1Dir dir : dirs)
            sb.append("::: PROCESSING  " + dir.getDirName() + LINE_TERMINATOR);

        Iterator<List<RF1File>> dit = fileListList.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<RF1File> fl = dit.next(); // File List
            Iterator<RF1File> fit = fl.iterator(); // File Iterator 
            while (fit.hasNext()) {
                RF1File f2 = fit.next();
                sb.append("    " + f2.file.getName() + LINE_TERMINATOR);
            }
            sb.append("    ..." + LINE_TERMINATOR);
        }
    }

    private void parseSubsetIdToOriginalUuidMap(List<RF1File> fl) throws IOException {
        int SUBSETID = 0;
        int SUBSETORIGINALID = 1;
        // int SUBSETVERSION = 2;
        int SUBSETNAME = 3;
        // int SUBSETTYPE = 4;
        // int LANGUAGECODE = 5;
        // int REALMID = 6;
        // int CONTEXTID = 7;

        mapSubsetIdToOriginalSctId = new HashMap<Long, Long>();
        mapSubsetIdToPathUuid = new HashMap<Long, String>();
        mapSubsetIdToRefsetUuid = new HashMap<Long, String>();

        for (RF1File rf : fl) {

            BufferedReader br = new BufferedReader(new FileReader(rf.file));
            // Header row
            br.readLine();

            while (br.ready()) {
                String[] line = br.readLine().split(TAB_CHARACTER);

                // SUBSETID
                Long sctIdSubset = Long.parseLong(line[SUBSETID]);
                // SUBSETORIGINALID
                Long sctIdOriginal = Long.parseLong(line[SUBSETORIGINALID]);
                // SUBSETNAME
                String subsetName = line[SUBSETNAME];

                // FIND ORIGINALID (SCTID)
                int found = -1;
                for (int i = 0; i < rf1SubsetIds.length; i++)
                    if (sctIdOriginal == rf1SubsetIds[i].getSubsetSctIdOriginal())
                        found = i;

                if (found > -1) {
                    mapSubsetIdToOriginalSctId.put(sctIdSubset, sctIdOriginal);
                    mapSubsetIdToPathUuid.put(sctIdSubset, rf1SubsetIds[found]
                            .getSubsetPathUuidStr());
                    mapSubsetIdToRefsetUuid.put(sctIdSubset, rf1SubsetIds[found]
                            .getSubsetRefsetUuidStr());
                    getLog().info(
                            "::: MAP < " + sctIdSubset + " , " + sctIdOriginal + " (ORIGINAL) > "
                                    + rf1SubsetIds[found].getSubsetRefsetUuidStr() + " (Refset), "
                                    + rf1SubsetIds[found].getSubsetPathUuidStr() + " (Path)"
                                    + subsetName + " // "
                                    + rf1SubsetIds[found].getSubsetUuidFromName());
                } else {
                    getLog().info(
                            "::: MAP ERROR UUID not specified for SUBSETORIGINALID == "
                                    + sctIdOriginal);
                }
            }
        } // for
    } // setupSubsetIdToOriginalUuidMap

    private void parseSubsetMembers(String fName, Rf1SubsetMember[] a, int count1)
            throws IOException {
        long start = System.currentTimeMillis();

        BufferedReader br = new BufferedReader(new FileReader(fName));
        int members = 0;

        int SUBSETID = 0;
        int MEMBERID = 1;
        int MEMBERSTATUS = 2; // NOTE: status is used as a "value"
        // int LINKEDID = 3;

        // Header row
        br.readLine();

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);
            long subsetId = Long.parseLong(line[SUBSETID]);
            long memberId = Long.parseLong(line[MEMBERID]);
            int memberValue = Integer.parseInt(line[MEMBERSTATUS]);

            a[members] = new Rf1SubsetMember(subsetId, memberId, memberValue);

            members++;
        }
        Arrays.sort(a);

        getLog().info(
                "Subset Member Parse & sort time: " + members + " concepts, "
                        + (System.currentTimeMillis() - start) + " milliseconds");
    }

    private void processSubsetMembers(List<List<RF1File>> fileListList, BufferedWriter bw)
            throws MojoFailureException, IOException, NoSuchAlgorithmException {
        // :!!!: does this need to be added Collections.sort(fileListList);

        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        String yRevDateStr; // :!!!: int in processConcepts

        Rf1SubsetMember[] a1, a2, a3 = null;

        getLog().info("START RF1 SUBSETS PROCESSING...");

        Iterator<List<RF1File>> dit = fileListList.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<RF1File> fl = dit.next(); // File List
            Iterator<RF1File> fit = fl.iterator(); // File Iterator

            // READ file1 as MASTER FILE
            RF1File f1 = fit.next();
            fName1 = f1.file.getPath();
            yRevDateStr = f1.revDateStr;

            count1 = countFileLines(fName1);
            getLog().info("BASE FILE:  " + count1 + " records, " + fName1);
            a1 = new Rf1SubsetMember[count1];
            parseSubsetMembers(fName1, a1, count1);
            for (int i = 0; i < count1; i++)
                bw.write(convertMemberToArf(a1[i], yRevDateStr));

            while (fit.hasNext()) {
                // SETUP CURRENT CONCEPTS INPUT FILE
                RF1File f2 = fit.next();
                fName2 = f2.file.getPath();
                yRevDateStr = f2.revDateStr;

                count2 = countFileLines(fName2);
                getLog().info("Counted: " + count2 + " records, " + fName2);

                // Parse in file2
                a2 = new Rf1SubsetMember[count2];
                parseSubsetMembers(fName2, a2, count2);

                int r1 = 0, r2 = 0, r3 = 0; // reset record indices
                int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
                a3 = new Rf1SubsetMember[count2]; // max3
                while ((r1 < count1) && (r2 < count2)) {

                    switch (compareMember(a1[r1], a2[r2])) {
                    case 1: // SAME, skip to next
                        r1++;
                        r2++;
                        nSame++;
                        break;

                    case 2: // MODIFIED
                        // Write history
                        bw.write(convertMemberToArf(a2[r2], yRevDateStr));
                        // Update master via pointer assignment
                        a1[r1] = a2[r2];
                        r1++;
                        r2++;
                        nMod++;
                        break;

                    case 3: // ADDED
                        // Write history
                        bw.write(convertMemberToArf(a2[r2], yRevDateStr));
                        // Hold pointer to append to master
                        a3[r3] = a2[r2];
                        r2++;
                        r3++;
                        nAdd++;
                        break;

                    case 4: // DROPPED
                        // see ArchitectonicAuxiliary.getStatusFromId()
                        if (a1[r1].status != 1) { // if not RETIRED
                            a1[r1].status = 1; // set to RETIRED
                            bw.write(convertMemberToArf(a1[r1], yRevDateStr));
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
                        bw.write(convertMemberToArf(a2[r2], yRevDateStr));
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
                a2 = new Rf1SubsetMember[count1 + nAdd];
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
    }

    public void setSubsetIds(Rf1SubsetId[] subsetIds) {
        this.rf1SubsetIds = subsetIds;
    }

}
