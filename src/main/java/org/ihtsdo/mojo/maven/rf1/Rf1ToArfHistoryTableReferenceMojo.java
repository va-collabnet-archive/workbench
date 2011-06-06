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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
 * <b>DESCRIPTION: </b><br>
 * 
 * Rf1ToArfHistoryTableReferences is a maven mojo which pre-processes TextDefinitions to arf format.<br>
 * <br>
 * 
 * <b>INPUTS:</b><br>
 * The pom needs to configure the following parameters for the <code>rf1-historytablereferences-to-arf</code> goal.
 * <pre>
 * &lt;targetSub&gt; subdirectoryname -- working sub directly under build directory
 *
 * &lt;dateStart&gt; yyyy.mm.dd -- filter excludes files before startDate
 * &lt;dateStop&gt;  yyyy.mm.dd -- filter excludes files after stopDate
 * 
 * &lt;rf1Dirs&gt;            -- creates list of directories to be searched 
 *    &lt;rf1Dir&gt; dir_name -- specific directory to be added to the search list     
 * </pre>
 * 
 * @author Marc E. Campbell
 *
 * @goal rf1-historytablereferences-to-arf
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class Rf1ToArfHistoryTableReferenceMojo extends AbstractMojo implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String FILE_SEPARATOR = File.separator;

    /**
     * Line terminator is deliberately set to CR-LF which is DOS style
     */
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";

    private static String uuidCurrentStr;
    private static String uuidRetiredStr;

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
     * @parameter
     * @required
     */
    private String pathUuid;

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
     * Default value "/classes" set programmatically due to file separator
     * 
     * @parameter default-value="generated-arf"
     */
    private String outputDirectory;

    String uuidHistoryTableReferencesRefset;
    String uuidReplacedByRefset;
    String uuidDuplicatedByRefset;
    String uuidSimilarToRefset;
    String uuidAlternativeRefset;
    String uuidMovedToRefset;
    String uuidMovedFromRefset;
    String uuidRefersToRefset;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("::: BEGIN Rf1ToArfHistoryTableReferenceMojo");

        // SHOW target directory from POM file
        String targetDir = targetDirectory.getAbsolutePath();
        getLog().info("    POM: Target Directory: " + targetDir);

        // SHOW input sub directory from POM file
        if (!targetSubDir.equals("")) {
            targetSubDir = FILE_SEPARATOR + targetSubDir;
            getLog().info("    POM: Target Sub Directory: " + targetSubDir);
        }

        if (rf1Dirs == null)
            throw new MojoExecutionException(
                    "Rf1ToArfHistoryTableReferenceMojo <rf1Dirs> not provided");

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

        try {
            executeMojo(targetDir, targetSubDir, rf1Dirs, outputDirectory);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new MojoFailureException(
                    "Rf1ToArfHistoryTableReferenceMojo ... NoSuchAlgorithmException");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new MojoFailureException(
                    "Rf1ToArfHistoryTableReferenceMojo ... UnsupportedEncodingException");
        }
        getLog().info("::: END Rf1ToArfHistoryTableReferenceMojo");
    }

    public void executeMojo(String tDir, String tSubDir, Rf1Dir[] inDirs, String outDir)
            throws MojoFailureException, NoSuchAlgorithmException, UnsupportedEncodingException {
        uuidCurrentStr = ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next()
                .toString();
        uuidRetiredStr = ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next()
                .toString();

        uuidHistoryTableReferencesRefset = Type5UuidFactory.get(
                Rf1Dir.HISTORY_TABLE_REFERENCES_NAMESPACE_UUID_TYPE1 + "History Table References")
                .toString();
        uuidReplacedByRefset = Type5UuidFactory.get(
                Rf1Dir.HISTORY_TABLE_REFERENCES_NAMESPACE_UUID_TYPE1 + "Replaced By").toString();
        uuidDuplicatedByRefset = Type5UuidFactory.get(
                Rf1Dir.HISTORY_TABLE_REFERENCES_NAMESPACE_UUID_TYPE1 + "Duplicated By").toString();
        uuidSimilarToRefset = Type5UuidFactory.get(
                Rf1Dir.HISTORY_TABLE_REFERENCES_NAMESPACE_UUID_TYPE1 + "Similar To").toString();
        uuidAlternativeRefset = Type5UuidFactory.get(
                Rf1Dir.HISTORY_TABLE_REFERENCES_NAMESPACE_UUID_TYPE1 + "Alternative").toString();
        uuidMovedToRefset = Type5UuidFactory.get(
                Rf1Dir.HISTORY_TABLE_REFERENCES_NAMESPACE_UUID_TYPE1 + "Moved To").toString();
        uuidMovedFromRefset = Type5UuidFactory.get(
                Rf1Dir.HISTORY_TABLE_REFERENCES_NAMESPACE_UUID_TYPE1 + "Moved From").toString();
        uuidRefersToRefset = Type5UuidFactory.get(
                Rf1Dir.HISTORY_TABLE_REFERENCES_NAMESPACE_UUID_TYPE1 + "Refers To").toString();

        getLog().info("::: Target Directory: " + tDir);
        getLog().info("::: Target Sub Directory:     " + tSubDir);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.ss HH:mm:ss");
        if (dateStartObj != null)
            getLog().info("::: Start date (inclusive) = " + sdf.format(dateStartObj));
        if (dateStopObj != null)
            getLog().info(":::  Stop date (inclusive) = " + sdf.format(dateStopObj));

        for (int i = 0; i < inDirs.length; i++)
            getLog().info("::: Input Directory (" + i + ") = " + inDirs[i].getDirName());

        // Setup target (build) directory
        BufferedWriter bwcRefSet = null;
        String fNameHistoryTableRelationshpsArf = tDir + tSubDir + outDir + FILE_SEPARATOR
                + "concept_history_table_relationships.refset";

        try {
            // FILE & DIRECTORY SETUP
            // Create multiple directories
            boolean success = (new File(tDir + tSubDir + outDir)).mkdirs();
            if (success) {
                getLog().info("::: Output Directory: " + tDir + tSubDir + outDir);
            }

            // SETUP REFSET OUTPUT FILE
            bwcRefSet = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    fNameHistoryTableRelationshpsArf), "UTF-8"));
            getLog().info(
                    "::: REFSET HISTORY TABLE REFERENCES OUTPUT: "
                            + fNameHistoryTableRelationshpsArf);

            // WRITE REFSET CONCEPTS
            saveRefsetConcepts(tDir + tSubDir + outDir + FILE_SEPARATOR);

            // PROCESS SUBSET MEMBERS FILES FOR EACH ROOT DIRECTORY
            List<List<RF1File>> fileListList = null;

            ArrayList<String> filter = new ArrayList<String>();
            filter.add("SCT");
            filter.add("REFERENCES");
            filter.add(".TXT");
            for (Rf1Dir d : inDirs) {
                Rf1Dir[] tmpDirs = { d };
                fileListList = Rf1Dir.getRf1Files(tDir, tSubDir, tmpDirs, filter, dateStartObj,
                        dateStopObj);
                logFileListList(tmpDirs, fileListList);
                processHistoryTableReferences(fileListList, bwcRefSet);
            }

            bwcRefSet.close();

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

    private void processHistoryTableReferences(List<List<RF1File>> fileListList, BufferedWriter bwc)
            throws MojoFailureException, IOException, NoSuchAlgorithmException {
        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        String yRevDateStr;
        String tmpArf;

        Rf1HistoryTableReference[] a1, a2, a3 = null;

        getLog().info("START RF1 HISTORY TABLE PROCESSING...");

        Iterator<List<RF1File>> dit = fileListList.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<RF1File> fl = dit.next(); // File List
            Collections.sort(fl);
            Iterator<RF1File> fit = fl.iterator(); // File Iterator

            if (fit == null || fit.hasNext() == false)
                continue;

            // READ file1 as MASTER FILE
            RF1File f1 = fit.next();
            yRevDateStr = f1.revDateStr;

            a1 = Rf1HistoryTableReference.parseFile(f1);
            count1 = a1.length;
            getLog().info("BASE FILE:  " + count1 + " records, " + f1.file.getPath());

            for (int i = 0; i < count1; i++) {
                // Write history
                tmpArf = convertReferenceToArf(a1[i], yRevDateStr);
                bwc.write(tmpArf);
            }

            while (fit.hasNext()) {
                // SETUP CURRENT CONCEPTS INPUT FILE
                RF1File f2 = fit.next();
                yRevDateStr = f2.revDateStr;

                // Parse in file2
                a2 = Rf1HistoryTableReference.parseFile(f2);
                count2 = a2.length;
                getLog().info("BASE FILE:  " + count2 + " records, " + f2.file.getPath());

                int r1 = 0, r2 = 0, r3 = 0; // reset record indices
                int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
                a3 = new Rf1HistoryTableReference[count2]; // max3
                while ((r1 < count1) && (r2 < count2)) {

                    switch (compareMember(a1[r1], a2[r2])) {
                    case 1: // SAME, skip to next
                        r1++;
                        r2++;
                        nSame++;
                        break;

                    case 2: // MODIFIED
                        // Write history
                        tmpArf = convertReferenceToArf(a2[r2], yRevDateStr);
                        bwc.write(tmpArf);
                        // Update master via pointer assignment
                        a1[r1] = a2[r2];
                        r1++;
                        r2++;
                        nMod++;
                        break;

                    case 3: // ADDED
                        // Write history
                        tmpArf = convertReferenceToArf(a2[r2], yRevDateStr);
                        bwc.write(tmpArf);
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
                            tmpArf = convertReferenceToArf(a1[r1], yRevDateStr);
                            bwc.write(tmpArf);
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
                        tmpArf = convertReferenceToArf(a2[r2], yRevDateStr);
                        bwc.write(tmpArf);
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
                a2 = new Rf1HistoryTableReference[count1 + nAdd];
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

    private int compareMember(Rf1HistoryTableReference htrA, Rf1HistoryTableReference htrB) {
        if (htrA.componentSid < htrB.componentSid) {
            return 4; // DROPPED instance less than received
        } else if (htrA.componentSid > htrB.componentSid) {
            return 3; // ADDED instance greater than received
        } else {
            if (htrA.referencedSid < htrB.referencedSid) {
                return 4; // DROPPED instance less than received
            } else if (htrA.referencedSid > htrB.referencedSid) {
                return 3; // ADDED instance greater than received
            } else {
                if (htrA.referenceType < htrB.referenceType) {
                    return 4; // DROPPED instance less than received
                } else if (htrA.referenceType > htrB.referenceType) {
                    return 3; // ADDED instance greater than received
                } else {
                    if (htrA.status == htrB.status)
                        return 1; // SAME instance == received
                    else
                        return 2; // MODIFIED
                }
            }
        }
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

    private String convertReferenceToArf(Rf1HistoryTableReference htr, String date)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();

        // REFSET_UUID
        switch (htr.referenceType) {
        case 1: // REPLACED BY
            sb.append(uuidReplacedByRefset + TAB_CHARACTER);
            break;
        case 2: // DUPLICATED BY
            sb.append(uuidDuplicatedByRefset + TAB_CHARACTER);
            break;
        case 3: // SIMILAR TO
            sb.append(uuidSimilarToRefset + TAB_CHARACTER);
            break;
        case 4: // ALTERNATIVE
            sb.append(uuidAlternativeRefset + TAB_CHARACTER);
            break;
        case 5: // MOVED TO
            sb.append(uuidMovedToRefset + TAB_CHARACTER);
            break;
        case 6: // MOVED FROM
            sb.append(uuidMovedFromRefset + TAB_CHARACTER);
            break;
        case 7: // REFERS TO
            sb.append(uuidRefersToRefset + TAB_CHARACTER);
            break;
        }
        
        
        /*To create consistent algorithm to generated uuid in workbench*/
        UUID uuid = null;
        
        // MEMBER_UUID ... of refset member
        switch (htr.referenceType) {
        case 1: // REPLACED BY
        	uuid = Type5UuidFactory.get("900000000000526001" +Long.toString(htr.componentSid) + Long.toString(htr.referencedSid)); //public final static String REPLACED_REFERENCES_REFSET_ID = "900000000000526001";
            break;
        case 2: // DUPLICATED BY
        	uuid = Type5UuidFactory.get("900000000000523009" +Long.toString(htr.componentSid) + Long.toString(htr.referencedSid)); //public final static String DUPLICATE_REFERENCES_REFSET_ID = "900000000000523009";
            break;
        case 3: // SIMILAR TO
        	uuid = Type5UuidFactory.get("900000000000529008" +Long.toString(htr.componentSid) + Long.toString(htr.referencedSid)); //public final static String SIMILAR_REFERENCES_REFSET_ID = "900000000000529008";
            break;
        case 4: // ALTERNATIVE
        	uuid = Type5UuidFactory.get("900000000000530003" +Long.toString(htr.componentSid) + Long.toString(htr.referencedSid)); //public final static String ALTERNATIVE_REFERENCES_REFSET_ID = "900000000000530003";
            break;
        case 5: // MOVED TO
        	uuid = Type5UuidFactory.get("900000000000524003" +Long.toString(htr.componentSid) + Long.toString(htr.referencedSid));//public final static String MOVED_TO_REFERENCES_REFSET_ID = "900000000000524003";
            break;
        case 6: // MOVED FROM
        	uuid = Type5UuidFactory.get("900000000000525002" +Long.toString(htr.componentSid) + Long.toString(htr.referencedSid)); //public final static String MOVED_FROM_REFERENCES_REFSET_ID = "900000000000525002";
            break;
        case 7: // REFERS TO
        	uuid = Type5UuidFactory.get("900000000000531004" +Long.toString(htr.componentSid) + Long.toString(htr.referencedSid)); //public final static String REFERS_REFERENCES_REFSET_ID = "900000000000531004";
            break;
        /*default:
            uuid = Type5UuidFactory.get(Rf1Dir.HISTORY_TABLE_REFERENCES_NAMESPACE_UUID_TYPE1
                   + htr.componentSid + htr.referenceType + htr.referencedSid);*/
        }
        
        sb.append(uuid.toString() + TAB_CHARACTER);
        

        // STATUS_UUID
        if (htr.status == 0)
            sb.append(uuidCurrentStr + TAB_CHARACTER);
        else
            sb.append(uuidRetiredStr + TAB_CHARACTER);

        // COMPONENT_UUID ... of member's referenced (concept, description, ...) component
        uuid = Type3UuidFactory.fromSNOMED(htr.componentSid);
        sb.append(uuid.toString() + TAB_CHARACTER);

        // EFFECTIVE_DATE
        sb.append(date + TAB_CHARACTER);

        // PATH_UUID
        sb.append(pathUuid + TAB_CHARACTER);

        // CONCEPT_EXTENSION_VALUE
        sb.append(Type3UuidFactory.fromSNOMED(htr.referencedSid) + LINE_TERMINATOR);

        return sb.toString();
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
            e.printStackTrace();
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
            e.printStackTrace();
            throw new MojoFailureException("SimpleDateFormat yyyy.MM.dd dateStop parse error: "
                    + sStop);
        }
        getLog().info(":::  STOP DATE (INCLUSIVE) " + this.dateStop);
    }

    private void logFileListList(Rf1Dir[] dirs, List<List<RF1File>> fileListList) {
        StringBuffer sb = new StringBuffer();
        for (Rf1Dir dir : dirs)
            sb.append("::: PROCESSING  " + dir.getDirName() + LINE_TERMINATOR);

        Iterator<List<RF1File>> dit = fileListList.iterator(); // Directory Iterator
        while (dit.hasNext()) {
            List<RF1File> fl = dit.next(); // File List
            Collections.sort(fl);
            Iterator<RF1File> fit = fl.iterator(); // File Iterator 
            while (fit.hasNext()) {
                RF1File f2 = fit.next();
                sb.append("    " + f2.file.getName() + LINE_TERMINATOR);
            }
            sb.append("    ..." + LINE_TERMINATOR);
        }
    }

    private void saveRefsetConcepts(String arfDir) throws MojoFailureException {

        try {
            Writer concepts = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(arfDir, "concepts_history_table_references.txt")), "UTF-8"));
            Writer descriptions = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(arfDir, "descriptions_history_table_references.txt")), "UTF-8"));
            Writer relationships = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(arfDir, "relationships_history_table_references.txt")), "UTF-8"));

            saveRefsetConcept(uuidHistoryTableReferencesRefset,
                    "3e0cd740-2cc6-3d68-ace7-bad2eb2621da", "History Table References", concepts,
                    descriptions, relationships);

            saveRefsetConcept(uuidReplacedByRefset, uuidHistoryTableReferencesRefset,
                    "Replaced By Refset", concepts, descriptions, relationships);
            saveRefsetConcept(uuidDuplicatedByRefset, uuidHistoryTableReferencesRefset,
                    "Duplicated By Refset", concepts, descriptions, relationships);
            saveRefsetConcept(uuidSimilarToRefset, uuidHistoryTableReferencesRefset,
                    "Similar To Refset", concepts, descriptions, relationships);
            saveRefsetConcept(uuidAlternativeRefset, uuidHistoryTableReferencesRefset,
                    "Alternative Refset", concepts, descriptions, relationships);
            saveRefsetConcept(uuidMovedToRefset, uuidHistoryTableReferencesRefset,
                    "Moved To Refset", concepts, descriptions, relationships);
            saveRefsetConcept(uuidMovedFromRefset, uuidHistoryTableReferencesRefset,
                    "Moved From Refset", concepts, descriptions, relationships);
            saveRefsetConcept(uuidRefersToRefset, uuidHistoryTableReferencesRefset,
                    "Refers To Refset", concepts, descriptions, relationships);

            concepts.close();
            descriptions.close();
            relationships.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("RefToArfSubsetsMojo IO Error", e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new MojoFailureException("RefToArfSubsetsMojo no such algorithm", e);
        }

    }

    private void saveRefsetConcept(String refsetUuid, String refsetParentUuid, String name,
            Writer concepts, Writer descriptions, Writer relationships) throws IOException,
            NoSuchAlgorithmException {
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String effectiveDate = format.format(dateStartObj);

            concepts.append(refsetUuid); // refset concept uuid
            concepts.append("\t");
            concepts.append(uuidCurrentStr); //status uuid
            concepts.append("\t");
            concepts.append("1"); // primitive
            concepts.append("\t");
            concepts.append(effectiveDate); // effective date
            concepts.append("\t");
            concepts.append(pathUuid); //path uuid
            concepts.append("\n");

            descriptions.append(Type5UuidFactory.get(
                    Rf1Dir.HISTORY_TABLE_REFERENCES_NAMESPACE_UUID_TYPE1
                            + "History Table References Fully Specified Name " + name).toString()); // description uuid
            descriptions.append("\t");
            descriptions.append(uuidCurrentStr); // status uuid
            descriptions.append("\t");
            descriptions.append(refsetUuid); // refset concept uuid
            descriptions.append("\t");
            descriptions.append(name); // term
            descriptions.append("\t");
            descriptions.append("1"); // primitive
            descriptions.append("\t");
            descriptions.append(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
                    .getUids().iterator().next().toString()); // description type uuid
            descriptions.append("\t");
            descriptions.append("en"); // language code
            descriptions.append("\t");
            descriptions.append(effectiveDate); // effective date
            descriptions.append("\t");
            descriptions.append(pathUuid); //path uuid
            descriptions.append("\n");

            descriptions.append(Type5UuidFactory.get(
                    Rf1Dir.HISTORY_TABLE_REFERENCES_NAMESPACE_UUID_TYPE1
                            + "History Table References Preferred Name " + name).toString()); // description uuid
            descriptions.append("\t");
            descriptions.append(uuidCurrentStr); // status uuid
            descriptions.append("\t");
            descriptions.append(refsetUuid); // refset concept uuid
            descriptions.append("\t");
            descriptions.append(name); // term
            descriptions.append("\t");
            descriptions.append("1"); // primitive
            descriptions.append("\t");
            descriptions.append(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
                    .getUids().iterator().next().toString()); // description type uuid
            descriptions.append("\t");
            descriptions.append("en"); // language code
            descriptions.append("\t");
            descriptions.append(effectiveDate); // effective date
            descriptions.append("\t");
            descriptions.append(pathUuid); //path uuid
            descriptions.append("\n");

            relationships.append(Type5UuidFactory.get(
                    Rf1Dir.HISTORY_TABLE_REFERENCES_NAMESPACE_UUID_TYPE1 + "Relationship"
                            + refsetUuid + refsetParentUuid).toString()); // relationship uuid
            relationships.append("\t");
            relationships.append(uuidCurrentStr); // status uuid
            relationships.append("\t");
            relationships.append(refsetUuid); // refset source concept uuid
            relationships.append("\t");
            relationships.append(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids().iterator()
                    .next().toString()); // relationship type uuid
            relationships.append("\t");
            relationships.append(refsetParentUuid); // destination concept uuid
            relationships.append("\t");
            relationships.append(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()
                    .iterator().next().toString()); // characteristic type uuid
            relationships.append("\t");
            relationships.append(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids().iterator()
                    .next().toString()); // refinability uuid
            relationships.append("\t");
            relationships.append("0"); // relationship group
            relationships.append("\t");
            relationships.append(effectiveDate); // effective date
            relationships.append("\t");
            relationships.append(pathUuid); // path uuid
            relationships.append("\n");
        }
    }

}
