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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * <b>DESCRIPTION: </b><br>
 * 
 * Rf1ToArfLanguageGbUsMojo is a maven mojo which pre-processes RF1 en-GB and en-US for the Rf1ToArfSubsetsMojo.<br>
 * <br>
 * Rf1ToArfLanguageGbUsMojo will compare en-GB subset to en-US to create a en-GB exceptions subset.  
 * The resulting en-GB subset is passed to the Rf1ToArfSubsetsMojo is <code>&lt;keepGBExceptions&gt;</code> is <code>true</code><p>
 * 
 * <b>INPUTS:</b><br>
 * The pom needs to configure the following parameters for the <code>rf1-language-gb-us-to-arf</code> goal.
 * <pre>
 * &lt;targetSub&gt; subdirectoryname -- working sub directly under build directory
 *
 * &lt;keepGB&gt;           true | false -- keep GB Dialect Subset (false)
 * &lt;keepUS&gt; true | false -- keep US Dialect Subset (false)
 * &lt;keepGBExceptions&gt; true | false -- keep GB Dialect Exceptions Subset (false)
 * 
 * &lt;dateStart&gt; yyyy.mm.dd -- filter excludes files before startDate
 * &lt;dateStop&gt;  yyyy.mm.dd -- filter excludes files after stopDate
 * 
 * &lt;rf1Dirs&gt;            -- creates list of directories to be searched 
 *    &lt;rf1Dir&gt; dir_name -- specific directory to be added to the search list 
 *    
 * &lt;rf1SubsetIds&gt;    -- list of subset id information items
 *    &lt;rf1SubsetId&gt;  -- subset id information with addition information to create refset concept 
 *       &lt;sctIdOriginal&gt;    long       -- subset original SCT ID  (must be match subsets file entry)
 *       &lt;subsetType&gt;       integer    -- subset type (must be match subsets file entry)
 *       &lt;refsetFsName&gt;     name       -- refset Fully Specified Name description
 *       &lt;refsetPrefTerm&gt;   name       -- refset Preferred Term description
 *       &lt;refsetPathUuid&gt;   uuid       -- refest path uuid
 *       &lt;refsetDate&gt;       yyyy.mm.dd -- originating date of refset concept
 *       &lt;refsetParentUuid&gt; uuid       -- taxonomy parent uuid
 * </pre>
 * 
 * Note:<br>
 * Commenting in or out &lt;rf1Dir&gt; items with enable and disable which file directories are imported.
 * <b>EVERY &lt;rf1Dir&gt; MUST HAVE A CORRESPONDING &lt;rf1SubsetId&gt; TO ACCEPT THE IMPORTED DATA, OR THE BUILD WILL FAIL.</b><br>
 * <br>
 * Commenting in or out &lt;rf1SubsetId&gt; items will affect which subsets show in the taxonomy.
 * A &lt;rf1SubsetId&gt; without a corresponding &lt;rf1Dir&gt; directory will create an empty refset concept.
 * <br>
 * <br>
 * @author Marc E. Campbell
 *
 * @goal rf1-language-gb-us-to-arf
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class Rf1ToArfLanguageGbUsMojo extends AbstractMojo implements Serializable {

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
     * @parameter default-value="false"
     */
    private boolean keepGB;

    /**
     * @parameter default-value="false"
     */
    private boolean keepUS;

    /**
     * @parameter default-value="false"
     */
    private boolean keepGBExceptions;

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
     * Input Directories Array. The directory array parameter supported
     * extensions via separate directories in the array.
     * 
     * @parameter
     * @required
     */
    private Rf1SubsetId[] rf1SubsetIds;

    /**
     * Directory used to output the eConcept format files
     * Default value "/classes" set programmatically due to file separator
     * 
     * @parameter default-value="generated-arf"
     */
    private String outputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("::: BEGIN Rf1ToArfLanguageGbUsMojo");

        // SHOW target directory from POM file
        String targetDir = targetDirectory.getAbsolutePath();
        getLog().info("    POM: Target Directory: " + targetDir);

        // SHOW input sub directory from POM file
        if (!targetSubDir.equals("")) {
            targetSubDir = FILE_SEPARATOR + targetSubDir;
            getLog().info("    POM: Target Sub Directory: " + targetSubDir);
        }

        if (rf1Dirs == null)
            throw new MojoExecutionException("Rf1ToArfLanguageGbUsMojo <rf1Dirs> not provided");

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

        executeMojo(targetDir, targetSubDir, rf1Dirs, rf1SubsetIds, outputDirectory);
        getLog().info("::: END Rf1ToArfLanguageGbUsMojo");
    }

    public void executeMojo(String tDir, String tSubDir, Rf1Dir[] inDirs, Rf1SubsetId[] subsetIds,
            String outDir) throws MojoFailureException, MojoExecutionException {
        getLog().info("::: Target Directory: " + tDir);
        getLog().info("::: Target Sub Directory:     " + tSubDir);

        String rootGxDir = "language_en-GX";
        // Create multiple directories
        boolean success = (new File(tDir + tSubDir + FILE_SEPARATOR + rootGxDir)).mkdirs();
        if (success) {
            getLog().info("OUTPUT DIRECTORY: " + tDir + outDir);
        }
        getLog().info("::: Target GX Directory:     " + tSubDir + FILE_SEPARATOR + rootGxDir);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.ss HH:mm:ss");
        if (dateStartObj != null)
            getLog().info("::: Start date (inclusive) = " + sdf.format(dateStartObj));
        if (dateStopObj != null)
            getLog().info(":::  Stop date (inclusive) = " + sdf.format(dateStopObj));

        for (int i = 0; i < inDirs.length; i++)
            getLog().info("::: Input Directory (" + i + ") = " + inDirs[i].getDirName());

        for (Rf1SubsetId idRec : subsetIds)
            getLog().info("::: SubsetId " + idRec.toString());

        getLog().info("::: Output Directory:  " + outDir);

        // Setup target (build) directory
        getLog().info("    Target Build Directory: " + tDir);

        // FIND FILES
        ArrayList<String> filter = new ArrayList<String>();
        filter.add("der1_");
        filter.add("Subsets_en-US");
        filter.add(".txt");
        List<List<RF1File>> subsetsUSListList = Rf1Dir.getRf1Files(tDir, tSubDir, inDirs, filter,
                dateStartObj, dateStopObj);
        logFileListList(inDirs, subsetsUSListList);

        filter.set(1, "Subsets_en-GB");
        List<List<RF1File>> subsetsGBListList = Rf1Dir.getRf1Files(tDir, tSubDir, inDirs, filter,
                dateStartObj, dateStopObj);
        logFileListList(inDirs, subsetsGBListList);

        filter.set(1, "SubsetMembers_en-US");
        List<List<RF1File>> membersUSListList = Rf1Dir.getRf1Files(tDir, tSubDir, inDirs, filter,
                dateStartObj, dateStopObj);
        logFileListList(inDirs, membersUSListList);

        filter.set(1, "SubsetMembers_en-GB");
        List<List<RF1File>> membersGBListList = Rf1Dir.getRf1Files(tDir, tSubDir, inDirs, filter,
                dateStartObj, dateStopObj);
        logFileListList(inDirs, membersGBListList);

        List<RF1File> subsetsUSList = new ArrayList<RF1File>();
        List<RF1File> subsetsGBList = new ArrayList<RF1File>();
        List<RF1File> membersUSList = new ArrayList<RF1File>();
        List<RF1File> membersGBList = new ArrayList<RF1File>();

        for (List<RF1File> a : subsetsUSListList)
            for (RF1File b : a)
                subsetsUSList.add(b);
        for (List<RF1File> a : subsetsGBListList)
            for (RF1File b : a)
                subsetsGBList.add(b);
        for (List<RF1File> a : membersUSListList)
            for (RF1File b : a)
                membersUSList.add(b);
        for (List<RF1File> a : membersGBListList)
            for (RF1File b : a)
                membersGBList.add(b);

        int totalFiles = subsetsUSList.size();
        if (subsetsGBList.size() != totalFiles || membersUSList.size() != totalFiles
                || membersGBList.size() != totalFiles)
            throw new MojoExecutionException("totalFiles != totalFiles");

        Collections.sort(subsetsGBList);
        Collections.sort(membersGBList);
        Collections.sort(subsetsUSList);
        Collections.sort(membersUSList);

        List<RF1File> subsetsGXList = new ArrayList<RF1File>();
        List<RF1File> membersGXList = new ArrayList<RF1File>();

        // PROCESS FILES IN DATE ORDER
        for (int i = 0; i < totalFiles; i++) {
            Date dateSubsetGB = subsetsGBList.get(i).revDate;
            Date dateMemberGB = membersGBList.get(i).revDate;
            Date dateSubsetUS = subsetsUSList.get(i).revDate;
            Date dateMemberUS = membersUSList.get(i).revDate;

            if (dateSubsetGB.equals(dateMemberGB) != true
                    || dateSubsetGB.equals(dateSubsetUS) != true
                    || dateSubsetGB.equals(dateMemberUS) != true)
                throw new MojoExecutionException("date != date");

            Calendar calGB = Calendar.getInstance();
            calGB.setTime(dateSubsetGB);

            DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            String dateStr = formatter.format(dateSubsetGB);

            // CREATE SUBSETS FILE
            String fNameSubsets = tDir + tSubDir + FILE_SEPARATOR + rootGxDir + FILE_SEPARATOR
                    + "der1_Subsets_en-GX_" + dateStr + ".txt";
            String fNameMembers = tDir + tSubDir + FILE_SEPARATOR + rootGxDir + FILE_SEPARATOR
                    + "der1_SubsetMembers_en-GX_" + dateStr + ".txt";

            try {
                // CREATE SUBSETS FILE
                BufferedWriter bwSubsets = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(fNameSubsets), "UTF-8"));
                getLog().info("::: GB EXCEPTION SUBSETS OUTPUT: " + bwSubsets);
                Rf1SubsetTable[] tmp = Rf1SubsetTable.parseSubsetIdToOriginalUuidMap(subsetsGBList
                        .get(i));
                long subsetSctId = -tmp[0].subsetId;
                long subsetSctIdOriginal = -tmp[0].subsetOriginalId;
                int subsetVersion = tmp[0].subsetVersion;
                bwSubsets.write("SUBSETID\tSUBSETORIGINALID\tSUBSETVERSION\tSUBSETNAME\t"
                        + "SUBSETTYPE\tLANGUAGECODE\tREALMID\tCONTEXTID\r\n");
                bwSubsets.write(subsetSctId + "\t" + subsetSctIdOriginal + "\t" + subsetVersion
                        + "\t" + "GB English Dialect Exceptions Subset\t1\ten-GB\t0\t0\r\n");
                bwSubsets.close();

                // CREATE MEMBERS FILE
                Rf1SubsetMember[] membersGB = Rf1SubsetMember.parseSubsetMembers(membersGBList
                        .get(i), subsetSctIdOriginal);
                Rf1SubsetMember[] membersUS = Rf1SubsetMember.parseSubsetMembers(membersUSList
                        .get(i), subsetSctIdOriginal);

                BufferedWriter bwMembersGX = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(fNameMembers), "UTF-8"));
                getLog().info("::: GB EXCEPTION MEMBERS OUTPUT: " + bwMembersGX);
                bwMembersGX.write("SUBSETID\tMEMBERID\tMEMBERSTATUS\tLINKEDID\r\n");

                int gbi = 0;
                int usi = 0;
                while (gbi < membersGB.length && usi < membersUS.length) {
                    switch (compareMember(membersGB[gbi], membersUS[usi])) {
                    case -1: // GB < US
                        bwMembersGX.write(subsetSctId + "\t" + membersGB[gbi].getMemberId() + "\t"
                                + membersGB[gbi].getMemberValue() + "\t\r\n");
                        gbi++;
                        break;

                    case 0: // GB == US
                        gbi++;
                        usi++;
                        break;

                    case 1: // GB > US
                        usi++;
                        break;
                    }
                }

                while (gbi < membersGB.length) {
                    bwMembersGX.write(subsetSctId + "\t" + membersGB[gbi].getMemberId() + "\t"
                            + membersGB[gbi].getMemberValue() + "\t\r\n");
                    gbi++;
                }

                bwMembersGX.close();

            } catch (IOException e) {
                e.printStackTrace();
                throw new MojoFailureException("ERROR with output");
            }
        }

        // RUN Rf1ToSubsetsArf
        Rf1ToArfSubsetsMojo mojo = new Rf1ToArfSubsetsMojo();
        mojo.setDateStart(dateStart);
        mojo.setDateStop(dateStop);

        // KEEP LOGIC
        if (keepUS) {
            filter = new ArrayList<String>();
            filter.add("en-US");
            mojo.executeMojo(tDir, tSubDir, inDirs, subsetIds, filter, outDir,
                    "language_subsets_en-US");
        }
        if (keepGB) {
            filter = new ArrayList<String>();
            filter.add("en-GB");
            mojo.executeMojo(tDir, tSubDir, inDirs, subsetIds, filter, outDir,
                    "language_subsets_en-GB");
        }
        if (keepGBExceptions) {
            filter = new ArrayList<String>();
            filter.add("en-GX");
            inDirs = new Rf1Dir[1];
            inDirs[0] = new Rf1Dir(FILE_SEPARATOR + rootGxDir, "en-GX");
            mojo.executeMojo(tDir, tSubDir, inDirs, subsetIds, filter, outDir,
                    "language_subsets_en-GX");
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

    private int compareMember(Rf1SubsetMember m1, Rf1SubsetMember m2) {
        if (m1.memberId < m2.memberId) {
            return -1; // m1 less than m2
        } else if (m1.memberId > m2.memberId) {
            return 1; // m1 greater than m2
        } else {
            if (m1.memberValue < m2.memberValue) {
                return -1; // m1 less than m2
            } else if (m1.memberValue > m2.memberValue) {
                return 1; // m1 greater than m2
            } else {
                return 0; // m1 == m2
            }
        }
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
