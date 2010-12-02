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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.id.Type3UuidFactory;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * <b>DESCRIPTION: </b><br>
 * 
 * Rf1ToArfTextDef is a maven mojo which pre-processes TextDefinitions to arf format.<br>
 * <br>
 * Rf1ToArfTextDef will compare en-GB subset to en-US to create a en-GB exceptions subset.  
 * The resulting en-GB subset is passed to the Rf1ToArfSubsetsMojo is <code>&lt;keepGBExceptions&gt;</code> is <code>true</code><p>
 * 
 * <b>INPUTS:</b><br>
 * The pom needs to configure the following parameters for the <code>rf1-language-gb-us-to-arf</code> goal.
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
 * <br>
 * <br>
 * @author Marc E. Campbell
 *
 * @goal rf1-textdefinitions-to-arf
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class Rf1ToArfTextDef extends AbstractMojo implements Serializable {

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("::: BEGIN Rf1ToArfTextDef");

        // SHOW target directory from POM file
        String targetDir = targetDirectory.getAbsolutePath();
        getLog().info("    POM: Target Directory: " + targetDir);

        // SHOW input sub directory from POM file
        if (!targetSubDir.equals("")) {
            targetSubDir = FILE_SEPARATOR + targetSubDir;
            getLog().info("    POM: Target Sub Directory: " + targetSubDir);
        }

        if (rf1Dirs == null)
            throw new MojoExecutionException("Rf1ToArfTextDef <rf1Dirs> not provided");

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

        executeMojo(targetDir, targetSubDir, rf1Dirs, outputDirectory);
        getLog().info("::: END Rf1ToArfTextDef");
    }

    public void executeMojo(String tDir, String tSubDir, Rf1Dir[] inDirs, String outDir)
            throws MojoFailureException, MojoExecutionException {
        uuidCurrentStr = ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next()
                .toString();
        uuidRetiredStr = ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next()
                .toString();

        getLog().info("::: Target Directory: " + tDir);
        getLog().info("::: Target Sub Directory:     " + tSubDir);

        // Create multiple directories
        boolean success = (new File(tDir + tSubDir + FILE_SEPARATOR + outDir)).mkdirs();
        if (success) {
            getLog().info("OUTPUT DIRECTORY: " + tDir + outDir);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.ss HH:mm:ss");
        if (dateStartObj != null)
            getLog().info("::: Start date (inclusive) = " + sdf.format(dateStartObj));
        if (dateStopObj != null)
            getLog().info(":::  Stop date (inclusive) = " + sdf.format(dateStopObj));

        for (int i = 0; i < inDirs.length; i++)
            getLog().info("::: Input Directory (" + i + ") = " + inDirs[i].getDirName());

        getLog().info("::: Output Directory:  " + outDir);

        // Setup target (build) directory
        getLog().info("    Target Build Directory: " + tDir);

        // FIND FILES
        ArrayList<String> filter = new ArrayList<String>();
        filter.add("sct1_");
        filter.add("TextDefinitions");
        filter.add(".txt");
        List<List<RF1File>> textDefListList = Rf1Dir.getRf1Files(tDir, tSubDir, inDirs, filter,
                dateStartObj, dateStopObj);
        logFileListList(inDirs, textDefListList);

        // SORT FILES INTO DATE ORDER
        List<RF1File> textDefList = new ArrayList<RF1File>();
        for (List<RF1File> a : textDefListList)
            for (RF1File b : a)
                textDefList.add(b);
        Collections.sort(textDefList);

        String fNameOutDescr = tDir + tSubDir + FILE_SEPARATOR + outDir + FILE_SEPARATOR
                + "descriptions_TextDefinitions.txt";

        try {
            // CREATE DESCRIPTIONS ARF FILE
            BufferedWriter bwOutDescr = new BufferedWriter(new FileWriter(fNameOutDescr));
           getLog().info("::: TextDefinitions DESCRIPTION ARF OUTPUT: " + bwOutDescr.toString());

            processTextDefinitions(textDefList, bwOutDescr);

            bwOutDescr.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("ERROR with output");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new MojoFailureException("ERROR now such algorithm");
        }
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

    private void processTextDefinitions(List<RF1File> fileList, BufferedWriter bwc)
            throws MojoFailureException, IOException, NoSuchAlgorithmException {
        // :!!!: does this need to be added Collections.sort(fileListList);

        int count1, count2; // records in arrays 1 & 2
        String fName1, fName2; // file path name
        String yRevDateStr; // :!!!: int in processConcepts

        Rf1TextDef[] a1;
        Rf1TextDef[] a2, a3 = null;

        getLog().info("START TEXT DEFINITION PROCESSING...");

        Iterator<RF1File> fit = fileList.iterator(); // File Iterator

        if (fit == null || fit.hasNext() == false) {
            getLog().info("processTextDefinitions: exiting, no files");
            return;
        }
        
        // READ file1 as MASTER FILE
        RF1File f1 = fit.next();
        fName1 = f1.file.getPath();
        yRevDateStr = f1.revDateStr;

        a1 = Rf1TextDef.parseFile(f1);
        count1 = a1.length;
        getLog().info("Counted: " + count1 + " records, " + fName1);

        for (int i = 0; i < count1; i++)
            // Write history baseline
            writeArf(a1[i], yRevDateStr, bwc);

        while (fit.hasNext()) {
            // SETUP CURRENT CONCEPTS INPUT FILE
            RF1File f2 = fit.next();
            fName2 = f2.file.getPath();
            yRevDateStr = f2.revDateStr;

            // Parse in file2
            a2 = Rf1TextDef.parseFile(f2);
            count2 = a2.length;
            getLog().info("Counted: " + count2 + " records, " + fName2);

            int r1 = 0, r2 = 0, r3 = 0; // reset record indices
            int nSame = 0, nMod = 0, nAdd = 0, nDrop = 0; // counters
            a3 = new Rf1TextDef[count2]; // max3
            while ((r1 < count1) && (r2 < count2)) {

                switch (compareVersion(a1[r1], a2[r2])) {
                case 1: // SAME, skip to next
                    r1++;
                    r2++;
                    nSame++;
                    break;

                case 2: // MODIFIED
                    // Write history
                    writeArf(a2[r2], yRevDateStr, bwc);
                    // Update master via pointer assignment
                    a1[r1] = a2[r2];
                    r1++;
                    r2++;
                    nMod++;
                    break;

                case 3: // ADDED
                    // Write history
                    writeArf(a2[r2], yRevDateStr, bwc);
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
                        writeArf(a1[r1], yRevDateStr, bwc);
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
                    writeArf(a2[r2], yRevDateStr, bwc);
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
            a2 = new Rf1TextDef[count1 + nAdd];
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

    private int compareVersion(Rf1TextDef a1, Rf1TextDef a2) {
        if (a1.conceptSid < a2.conceptSid) {
            return 4; // DROPPED instance less than received
        } else if (a1.conceptSid > a2.conceptSid) {
            return 3; // ADDED instance greater than received

        } else {    
            int test = a1.snomedId.compareToIgnoreCase(a2.snomedId);
            
            if (test > 1) {
                return 4; // DROPPED instance less than received
            } else if (test < 1) {
                return 3; // ADDED instance greater than received
            } else {
                return 1; // SAME
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

    private void writeArf(Rf1TextDef a1, String yRevDateStr, BufferedWriter descriptions)
            throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        // fully_specified_name - done
        descriptions.append(Type5UuidFactory.get(
                "org.ihstdo.textdefinition" + Long.toString(a1.conceptSid) + a1.definition)
                .toString()); // description uuid
        descriptions.append("\t");
        descriptions.append(uuidCurrentStr); // status uuid
        descriptions.append("\t");
        descriptions.append(Type3UuidFactory.fromSNOMED(a1.conceptSid).toString()); // concept uuid
        descriptions.append("\t");
        descriptions.append(a1.definition); // text term
        descriptions.append("\t");
        descriptions.append("1"); // primitive
        descriptions.append("\t");
        descriptions.append(ArchitectonicAuxiliary.Concept.TEXT_DEFINITION_TYPE.getUids()
                .iterator().next().toString()); // description type uuid
        descriptions.append("\t");
        descriptions.append("en"); // language code
        descriptions.append("\t");
        descriptions.append(yRevDateStr); // effective date
        descriptions.append("\t");
        descriptions.append(pathUuid); //path uuid
        descriptions.append("\n");
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

}
