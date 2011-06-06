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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
 * @goal rf1-crossmaps-to-arf
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class Rf1ToArfCrossMapsMojo extends AbstractMojo implements Serializable {

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
    /**
     * Start date (inclusive)
     * 
     * @parameter
     */
    private String dateStart;
    private Date dateStartObj;
    /**
     * Stop date inclusive
     * 
     * @parameter
     */
    private String dateStop;
    private Date dateStopObj;
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
     */
    private Rf1Dir[] rf1Dirs;
    /**
     * Directory used to output the eConcept format files
     * Default value "/generated-arf" set programmatically due to file separator
     * 
     * @parameter default-value="generated-arf"
     */
    private String outputDirectory;
    /**
     * @parameter
     * @required
     */
    private String outputInfix;
    /**
     * @parameter
     * @required
     */
    private String refsetFsName;
    /**
     * @parameter
     * @required
     */
    private String refsetPrefTerm;
    /**
     * @parameter
     * @required
     */
    private String refsetPathUuid;
    /**
     * @parameter
     * @required
     */
    private String refsetDate;
    private Date refsetDateObj;
    /**
     * @parameter
     * @required
     */
    private String refsetParentUuid;
    private HashMap<Long, String> mapTargetidTargetcode; // <TARGETID, TARGETCODE>
    private String uuidCurrentStr;
    private String uuidRetiredStr;
    private UUID refsetUuid;
    private String refsetUuidStr;

    private class CrossMapRecord implements Comparable<Object> {

        long sctId;
        String targetCode;
        int priority;
        int status;

        public CrossMapRecord(long id, String code, int p) {
            sctId = id;
            priority = p;
//            if (priority > 0)
//                targetCode = code + " [" + Integer.toString(priority) + "]";
//            else
            targetCode = code;
            status = 0;
        }

        @Override
        public int compareTo(Object obj) {
            CrossMapRecord o2 = (CrossMapRecord) obj;
            int thisMore = 1;
            int thisLess = -1;

            if (this.sctId > o2.sctId) {
                return thisMore;
            } else if (this.sctId < o2.sctId) {
                return thisLess;
            } else {
                if (this.targetCode.compareTo(o2.targetCode) > 0) {
                    return thisMore;
                } else if (this.targetCode.compareTo(o2.targetCode) < 0) {
                    return thisLess;
                }
            }
            return 0; // EQUAL
        }

        @Override
        public String toString() {
            return "sctId=" + sctId + "\ttargetCode=" + targetCode
                    + "\tpriority=" + priority + "\tstatus=" + status;
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("::: BEGIN Rf1CrossMapsToArf");

        // SHOW target directory from POM file
        String targetDir = targetDirectory.getAbsolutePath();
        getLog().info("    POM: Target Directory: " + targetDir);

        // SHOW input sub directory from POM file
        if (!targetSubDir.equals("")) {
            targetSubDir = FILE_SEPARATOR + targetSubDir;
            getLog().info("    POM: Target Sub Directory: " + targetSubDir);
        }

        if (rf1Dirs == null) {
            rf1Dirs = new Rf1Dir[0];
        }
        for (int i = 0; i < rf1Dirs.length; i++) {
            rf1Dirs[i].setDirName(rf1Dirs[i].getDirName().replace('/',
                    File.separatorChar));
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

        try {
            executeMojo(targetDir, targetSubDir, rf1Dirs, outputDirectory);
        } catch (UnsupportedEncodingException e) {
            getLog().error(e);
        } catch (FileNotFoundException e) {
            getLog().error(e);
        } catch (NoSuchAlgorithmException e) {
            getLog().error(e);
        } catch (IOException e) {
            getLog().error(e);
        }
        getLog().info("::: END Rf1CrossMapsToArf");
    }

    public void executeMojo(String tDir, String tSubDir, Rf1Dir[] inDirs, String outDir)
            throws MojoFailureException, NoSuchAlgorithmException, IOException {
        getLog().info("::: Target Directory: " + tDir);
        getLog().info("::: Target Sub Directory:     " + tSubDir);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.ss HH:mm:ss");
        if (dateStartObj != null) {
            getLog().info("::: Start date (inclusive) = " + sdf.format(dateStartObj));
        }
        if (dateStopObj != null) {
            getLog().info(":::  Stop date (inclusive) = " + sdf.format(dateStopObj));
        }

        for (int i = 0; i < inDirs.length; i++) {
            getLog().info("::: Input Directory (" + i + ") = " + inDirs[i].getDirName());
        }
        getLog().info("::: Output Directory:  " + outDir);

        // Setup target (build) directory
        getLog().info("    Target Build Directory: " + tDir);

        // SETUP INPUT FILES
        ArrayList<String> filter = new ArrayList<String>();
        filter.add("der1_");
        filter.add("CrossMaps_");
        filter.add(".txt");
        List<RF1File> inCrossMapFiles = Rf1Dir.getRf1FileList(tDir, tSubDir, inDirs, filter,
                dateStartObj, dateStopObj);
        filter = new ArrayList<String>();
        filter.add("der1_");
        filter.add("CrossMapTargets_");
        filter.add(".txt");
        List<RF1File> inCrossMapTargetFiles = Rf1Dir.getRf1FileList(tDir, tSubDir, inDirs, filter,
                dateStartObj, dateStopObj);
        getLog().info(":::  Input CrossMaps: " + inCrossMapFiles.toString());
        getLog().info(":::  Input CrossMapTargets: " + inCrossMapTargetFiles.toString());

        // SETUP OUTPUT FILE
        String fPathOutDir = tDir + FILE_SEPARATOR + tSubDir + FILE_SEPARATOR + outDir;
        boolean success = (new File(fPathOutDir)).mkdirs();
        if (success) {
            getLog().info("::: OUTPUT PATH " + fPathOutDir);
        }
        String fNameSubsetConRefsetArf = fPathOutDir + FILE_SEPARATOR + "string_" + outputInfix
                + ".refset";
        BufferedWriter bwRefset = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                fNameSubsetConRefsetArf), "UTF-8"));
        getLog().info("::: OUTPUT: " + fNameSubsetConRefsetArf);

        UUID uuidCurrent = ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next();
        this.uuidCurrentStr = uuidCurrent.toString();
        UUID uuidRetired = ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next();
        this.uuidRetiredStr = uuidRetired.toString();

        this.refsetUuid = Type5UuidFactory.get(Rf1Dir.SUBSETREFSET_ID_NAMESPACE_UUID_TYPE1
                + refsetFsName);
        this.refsetUuidStr = refsetUuid.toString();

        saveRefsetConcept(fPathOutDir, outputInfix, refsetPathUuid);
        processCrossMapFiles(bwRefset, inCrossMapFiles, inCrossMapTargetFiles, refsetPathUuid);

        bwRefset.close();
    }

    private int compareCrossMapRecord(CrossMapRecord r1, CrossMapRecord r2) {
        if (r1.sctId > r2.sctId) {
            return 3; // ADDED
        } else if (r1.sctId < r2.sctId) {
            return 4; // DROPPED
        } else if (r1.sctId == r2.sctId) {
            int comp = r1.targetCode.compareTo(r2.targetCode);
            if (comp == 0) {
                if (r1.status == r2.status) {
                    return 1; // SAME
                } else {
                    return 2; // MODIFIED
                }
            } else if (comp > 0) {
                return 3; // ADDED
            } else if (comp < 0) {
                return 4; // DROPPED
            }
        }
        return 2; // MODIFIED
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

    private CrossMapRecord[] parseCrossMaps(RF1File rf1) throws IOException, MojoFailureException {
        int lineCount = RF1File.countFileLines(rf1);
        CrossMapRecord[] a = new CrossMapRecord[lineCount];

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf1.file),
                "UTF-8"));

        int MAPCONCEPTID = 1;
        int MAPPRIORITY = 3;
        int MAPTARGETID = 4;

        // Header row
        br.readLine();

        int mapsIdx = 0;
        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);
            long conceptId = Long.parseLong(line[MAPCONCEPTID]);
            int priority = 0;
            if ("".equalsIgnoreCase(line[MAPPRIORITY]) == false) {
                priority = Integer.parseInt(line[MAPPRIORITY]);
            }
            long targetId = Long.parseLong(line[MAPTARGETID]);

            String targetCode = mapTargetidTargetcode.get(targetId);

            if (targetCode != null) {
                a[mapsIdx] = new CrossMapRecord(conceptId, targetCode, priority);
                mapsIdx++;
            } else {
                getLog().info(
                        "DATA ERROR: target not present: MAPCONCEPTID=" + line[MAPCONCEPTID]
                        + " MAPPRIORITY=" + line[MAPPRIORITY] + " MAPTARGETID="
                        + line[MAPTARGETID]);
                a[mapsIdx] = new CrossMapRecord(conceptId, "TARGET_NOT_PRESENT", priority);
                mapsIdx++;
            }

        }
        br.close();

        Arrays.sort(a);

        // REMOVE DUPLICATES
        a = removeDuplCrossMapRecords(a);

        return a;
    }

    private CrossMapRecord[] removeDuplCrossMapRecords(CrossMapRecord[] a) {

        // REMOVE DUPLICATES
        int lenA = a.length;
        ArrayList<Integer> duplIdxList = new ArrayList<Integer>();
        for (int idx = 0; idx < lenA - 2; idx++) {
            if ((a[idx].sctId == a[idx + 1].sctId)
                    && (a[idx].targetCode.equalsIgnoreCase(a[idx + 1].targetCode))) {
                duplIdxList.add(Integer.valueOf(idx));
                getLog().info(
                        "::: WARNING : REMOVED -- same: sctId and targetCode" + "\r\n::: A:" + a[idx]
                        + "\r\n::: B:" + a[idx + 1]);
            }
        }
        if (duplIdxList.size() > 0) {
            CrossMapRecord[] b = new CrossMapRecord[lenA - duplIdxList.size()];
            int aPos = 0;
            int bPos = 0;
            int len;
            for (int dropIdx : duplIdxList) {
                len = dropIdx - aPos;
                System.arraycopy(a, aPos, b, bPos, len);
                bPos = bPos + len;
                aPos = aPos + len + 1;
            }
            len = lenA - aPos;
            System.arraycopy(a, aPos, b, bPos, len);
            return b;
        } else {
            return a;
        }
    }

    private void parseCrossMapTargets(RF1File rf1) throws IOException {
        mapTargetidTargetcode = new HashMap<Long, String>();

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf1.file),
                "UTF-8"));

        int TARGETID = 0;
        int TARGETCODE = 2;

        // Header row
        br.readLine();

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);
            long targetId = Long.parseLong(line[TARGETID]);
            String targetCode = line[TARGETCODE];
            mapTargetidTargetcode.put(targetId, targetCode);
        }
        br.close();
    }

    private void processCrossMapFiles(BufferedWriter out, List<RF1File> inCrossMapFiles,
            List<RF1File> inCrossMapTargetFiles, String uuidPathStr) throws MojoFailureException,
            IOException, NoSuchAlgorithmException {
        int count1, count2; // records in arrays 1 & 2
        CrossMapRecord[] a1, a2, a3 = null;

        getLog().info("START CROSSMAP PROCESSING...");

        Iterator<RF1File> fit = inCrossMapFiles.iterator(); // File Iterator
        Iterator<RF1File> fitTarget = inCrossMapTargetFiles.iterator(); // File Iterator

        // READ file1 as MASTER FILE
        RF1File f1 = fit.next();
        Date date = f1.revDate;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = formatter.format(date);
        getLog().info("::: ... " + f1.file.getName());

        parseCrossMapTargets(fitTarget.next());
        a1 = parseCrossMaps(f1);
        count1 = a1.length;

        for (int i = 0; i < a1.length; i++) {
            writeToArfFile(a1[i], dateStr, uuidPathStr, out);
        }

        while (fit.hasNext()) {
            // SETUP CURRENT CONCEPTS INPUT FILE
            RF1File f2 = fit.next();
            date = f2.revDate;
            dateStr = formatter.format(date);
            getLog().info("::: ... " + f2.file.getName());

            // Parse in file2
            parseCrossMapTargets(fitTarget.next());
            a2 = parseCrossMaps(f2);
            count2 = a2.length;

            int r1 = 0, r2 = 0, r3 = 0; // reset record indices
            int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
            a3 = new CrossMapRecord[count2];
            while ((r1 < count1) && (r2 < count2)) {

                switch (compareCrossMapRecord(a1[r1], a2[r2])) {                    
                    case 1: // SAME CROSSMAP, skip to next
                        r1++;
                        r2++;
                        nSame++;
                        break;

                    case 2: // MODIFIED CROSSMAP
                        // Retire previous
                        if (a1[r1].status != 1) { // if not RETIRED
                            a1[r1].status = 1; // set to RETIRED
                            writeToArfFile(a1[r1], dateStr, uuidPathStr, out);
                        }

                        // Add current
                        writeToArfFile(a2[r2], dateStr, uuidPathStr, out);

                        // Update master via pointer assignment
                        a1[r1] = a2[r2];
                        r1++;
                        r2++;
                        nMod++;
                        break;

                    case 3: // ADDED CROSSMAP
                        // Write history
                        writeToArfFile(a2[r2], dateStr, uuidPathStr, out);

                        // Hold pointer to append to master
                        a3[r3] = a2[r2];
                        r2++;
                        r3++;
                        nAdd++;
                        break;

                    case 4: // DROPPED CROSSMAP
                        // see ArchitectonicAuxiliary.getStatusFromId()
                        if (a1[r1].status != 1) { // if not RETIRED
                            a1[r1].status = 1; // set to RETIRED
                            writeToArfFile(a1[r1], dateStr, uuidPathStr, out);
                        }
                        r1++;
                        nDrop++;
                        break;

                }
            } // WHILE (NOT END OF EITHER A1 OR A2)

            // NOT MORE TO COMPARE, HANDLE REMAINING CONCEPTS
            if (r1 < count1) {
                while (r1 < count1) {
                    if (a1[r1].status != 1) { // if not RETIRED
                        a1[r1].status = 1; // set to RETIRED
                        writeToArfFile(a1[r1], dateStr, uuidPathStr, out);
                    }
                    r1++;
                    nDrop++;
                }
            }

            if (r2 < count2) {
                while (r2 < count2) { // ADD REMAINING CROSSMAP INPUT
                    // Write history
                    writeToArfFile(a2[r2], dateStr, uuidPathStr, out);

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
            a2 = new CrossMapRecord[count1 + nAdd];
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

        } // WHILE (EACH CROSSMAP INPUT FILE)
    }

    private void saveRefsetConcept(String arfDir, String infix, String pathStr)
            throws MojoFailureException {

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String refsetBaseDate = formatter.format(refsetDateObj);


            Writer concepts;
            concepts = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(
                    arfDir, "concepts_" + infix + ".txt")), "UTF-8"));
            Writer descriptions = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(arfDir, "descriptions_" + infix + ".txt")), "UTF-8"));
            Writer relationships = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(arfDir, "relationships_" + infix + ".txt")), "UTF-8"));

            concepts.append(refsetUuidStr); // refset concept uuid
            concepts.append("\t");
            concepts.append(uuidCurrentStr); //status uuid
            concepts.append("\t");
            concepts.append("1"); // primitive
            concepts.append("\t");
            concepts.append(refsetBaseDate); // effective date
            concepts.append("\t");
            concepts.append(pathStr); //path uuid
            concepts.append("\n");

            // DESCRIPTION - FULLY SPECIFIED
            descriptions.append(Type5UuidFactory.get(
                    Rf1Dir.SUBSETREFSET_ID_NAMESPACE_UUID_TYPE1 + "Subset Fully Specified Name"
                    + refsetUuidStr).toString()); // description uuid
            descriptions.append("\t");
            descriptions.append(uuidCurrentStr); // status uuid
            descriptions.append("\t");
            descriptions.append(refsetUuidStr); // refset concept uuid
            descriptions.append("\t");
            descriptions.append(refsetFsName); // term
            descriptions.append("\t");
            descriptions.append("1"); // primitive
            descriptions.append("\t");
            descriptions.append(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids().iterator().next().toString()); // description type uuid
            descriptions.append("\t");
            descriptions.append("en"); // language code
            descriptions.append("\t");
            descriptions.append(refsetBaseDate); // effective date
            descriptions.append("\t");
            descriptions.append(pathStr); //path uuid
            descriptions.append("\n");

            // DESCRIPTION - FULLY PREFERRED
            descriptions.append(Type5UuidFactory.get(
                    Rf1Dir.SUBSETREFSET_ID_NAMESPACE_UUID_TYPE1 + "ICD-O-3 CrossMap"
                    + refsetUuidStr).toString()); // description uuid
            descriptions.append("\t");
            descriptions.append(uuidCurrentStr); // status uuid
            descriptions.append("\t");
            descriptions.append(refsetUuidStr); // refset concept uuid
            descriptions.append("\t");
            descriptions.append(refsetPrefTerm); // term
            descriptions.append("\t");
            descriptions.append("1"); // primitive
            descriptions.append("\t");
            descriptions.append(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids().iterator().next().toString()); // description type uuid
            descriptions.append("\t");
            descriptions.append("en"); // language code
            descriptions.append("\t");
            descriptions.append(refsetBaseDate); // effective date
            descriptions.append("\t");
            descriptions.append(pathStr); //path uuid
            descriptions.append("\n");

            // RELATIONSHIP
            relationships.append(Type5UuidFactory.get(
                    Rf1Dir.SUBSETREFSET_ID_NAMESPACE_UUID_TYPE1 + "Relationship" + refsetUuidStr).toString()); // relationship uuid
            relationships.append("\t");
            relationships.append(uuidCurrentStr); // status uuid
            relationships.append("\t");
            relationships.append(refsetUuidStr); // refset source concept uuid
            relationships.append("\t");
            relationships.append(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids().iterator().next().toString()); // relationship type uuid
            relationships.append("\t");
            relationships.append(refsetParentUuid); // destination concept uuid
            relationships.append("\t");
            relationships.append(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids().iterator().next().toString()); // characteristic type uuid
            relationships.append("\t");
            relationships.append(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids().iterator().next().toString()); // refinability uuid
            relationships.append("\t");
            relationships.append("0"); // relationship group
            relationships.append("\t");
            relationships.append(refsetBaseDate); // effective date
            relationships.append("\t");
            relationships.append(pathStr); // path uuid
            relationships.append("\n");

            concepts.close();
            descriptions.close();
            relationships.close();

        } catch (IOException e) {
            getLog().error(e);
            throw new MojoFailureException("RefToArfSubsetsMojo IO Error", e);
        } catch (NoSuchAlgorithmException e) {
            getLog().error(e);
            throw new MojoFailureException("RefToArfSubsetsMojo no such algorithm", e);
        }

    }

    private void writeToArfFile(CrossMapRecord r, String date, String path, BufferedWriter bw)
            throws NoSuchAlgorithmException, IOException {
        UUID sctUuid = Type3UuidFactory.fromSNOMED(r.sctId);
        
        // REFSET_UUID
        bw.write(refsetUuidStr + TAB_CHARACTER);
        
        // MEMBER_UUID ... of refset member
       
        /*To create consistent algorithm to generated uuid in workbench*/
        UUID uuid = null;
        getLog().info("==debug====" + refsetFsName);
       
        if(refsetFsName.equals("ICD-O-3 CrossMap")){
        	getLog().info("==Refset Name====" + refsetFsName);
        	uuid = Type5UuidFactory.get("446608001" + Long.toString(r.sctId) +  r.targetCode); //public final static String ICDO_REFSET_ID = "446608001";
        }
        
        /*else{
        	uuid = Type5UuidFactory.get(Rf1Dir.SUBSETMEMBER_ID_NAMESPACE_UUID_TYPE1 + refsetFsName
                + sctUuid + ":" + r.targetCode);
        }*/
        
        bw.write(uuid.toString() + TAB_CHARACTER);
        // STATUS_UUID
        if (r.status == 0) {
            bw.write(uuidCurrentStr + TAB_CHARACTER);
        } else {
            bw.write(uuidRetiredStr + TAB_CHARACTER);
        }

        // COMPONENT_UUID ... of member's referenced (concept, description, ...) component
        bw.write(sctUuid.toString() + TAB_CHARACTER);
        // EFFECTIVE_DATE
        bw.write(date + TAB_CHARACTER);
        // PATH_UUID
        bw.write(path + TAB_CHARACTER);

        // SNOMED_STRING_VALUE
        bw.write(r.targetCode + LINE_TERMINATOR);
    }

    public String getDateStart() {
        return this.dateStart;
    }

    public void setDateStart(String sStart) throws MojoFailureException {
        this.dateStart = sStart;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        try {
            this.dateStartObj = formatter.parse(sStart + " 00:00:00");
        } catch (ParseException e) {
            getLog().error(e);
            throw new MojoFailureException("SimpleDateFormat yyyy.MM.dd dateStart parse error: "
                    + sStart);
        }
        getLog().info("::: START DATE (INCLUSIVE) " + this.dateStart);
    }

    public String getDateStop() {
        return this.dateStop;
    }

    public void setDateStop(String sStop) throws MojoFailureException {
        this.dateStop = sStop;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        try {
            this.dateStopObj = formatter.parse(sStop + " 23:59:59");
        } catch (ParseException e) {
            getLog().error(e);
            throw new MojoFailureException("SimpleDateFormat yyyy.MM.dd dateStop parse error: "
                    + sStop);
        }
        getLog().info(":::  STOP DATE (INCLUSIVE) " + this.dateStop);
    }

    public String getRefsetDate() {
        return this.refsetDate;
    }

    public void setRefsetDate(String date) throws MojoFailureException {
        this.refsetDate = date;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        try {
            this.refsetDateObj = formatter.parse(date + " 00:00:00");
        } catch (ParseException e) {
            getLog().error(e);
            throw new MojoFailureException("SimpleDateFormat yyyy.MM.dd refsetDate parse error: "
                    + date);
        }
        getLog().info("::: REFSET DATE " + this.refsetDate);
    }
}
