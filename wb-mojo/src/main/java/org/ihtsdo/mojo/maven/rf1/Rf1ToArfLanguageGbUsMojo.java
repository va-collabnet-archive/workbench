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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * 
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
     * @parameter default-value="classes"
     */
    private String outputDirectory;

    private String scratchDirectory = FILE_SEPARATOR + "tmp_steps";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("::: BEGIN Rf1LanguageGbUsToArf");

        // SHOW target directory from POM file
        String targetDir = targetDirectory.getAbsolutePath();
        getLog().info("    POM: Target Directory: " + targetDir);

        // SHOW input sub directory from POM file
        if (!targetSubDir.equals("")) {
            targetSubDir = FILE_SEPARATOR + targetSubDir;
            getLog().info("    POM: Target Sub Directory: " + targetSubDir);
        }

        if (rf1Dirs == null)
            throw new MojoExecutionException("Rf1LanguageGbUsToArf <rf1Dirs> not provided");

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
        getLog().info("::: END Rf1LanguageGbUsToArf");
    }

    public void executeMojo(String tDir, String tSubDir, Rf1Dir[] inDirs, Rf1SubsetId[] subsetIds,
            String outDir) throws MojoFailureException, MojoExecutionException {
        getLog().info("::: Target Directory: " + tDir);
        getLog().info("::: Target Sub Directory:     " + tSubDir);
        getLog().info("::: Start date (inclusive) = " + dateStart);
        getLog().info("::: Stop date (inclusive) =  " + dateStop);

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
                dateStart, dateStop);
        filter.set(1, "Subsets_en-GB");
        List<List<RF1File>> subsetsGBListList = Rf1Dir.getRf1Files(tDir, tSubDir, inDirs, filter,
                dateStart, dateStop);
        filter.set(1, "SubsetMembers_en-US");
        List<List<RF1File>> membersUSListList = Rf1Dir.getRf1Files(tDir, tSubDir, inDirs, filter,
                dateStart, dateStop);
        filter.set(1, "SubsetMembers_en-GB");
        List<List<RF1File>> membersGBListList = Rf1Dir.getRf1Files(tDir, tSubDir, inDirs, filter,
                dateStart, dateStop);

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

            String dateStr = dateSubsetGB.toString();

            // CREATE SUBSETS FILE
            String fNameSubsets = tDir + tSubDir + FILE_SEPARATOR + "der1_Subsets_en-GX" + dateStr
                    + ".txt";
            String fNameMembers = tDir + tSubDir + FILE_SEPARATOR + "der1_SubsetMembers_en-GX"
                    + dateStr + ".txt";

            try {
                // CREATE SUBSETS FILE
                BufferedWriter bwSubsets = new BufferedWriter(new FileWriter(fNameSubsets));
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
                        .get(i));
                Rf1SubsetMember[] membersUS = Rf1SubsetMember.parseSubsetMembers(membersUSList
                        .get(i));

                BufferedWriter bwMembersGX = new BufferedWriter(new FileWriter(fNameMembers));
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
            mojo.executeMojo(tDir, tSubDir, inDirs, subsetIds, filter, outDir,
                    "language_subsets_en-GX");
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

}
