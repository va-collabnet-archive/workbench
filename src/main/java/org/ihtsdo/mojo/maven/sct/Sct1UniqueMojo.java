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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * APPROACH: year to year
 * APPROACH: remove id matches, flag error if different
 * 
 * ASSUMPTION: both directories have same number of date-aligned files
 * 
 * @author marc
 *
 * @goal sct1-unique
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class Sct1UniqueMojo extends AbstractMojo implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String FILE_SEPARATOR = File.separator;
    private static final String LINE_TERMINATOR = "\r\n";

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    /**
     * Applicable input sub directory under the build directory.
     * 
     * @parameter
     */
    private String targetSubDir = "";

    /**
     * @parameter 
     * @required
     */
    private String sct1In1stDir;

    /**
     * @parameter 
     * @required
     */
    private String sct1In2ndDir;

    /**
     * @parameter 
     * @required
     */
    private String sct1Out1stDir;

    /**
     * @parameter 
     * @required
     */
    private String sct1Out2ndDir;

    /**
     * @parameter 
     */
    private String sct1Dupl2ndDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            executeMojo(targetDirectory, targetSubDir, sct1In1stDir, sct1In2ndDir, sct1Out1stDir,
                    sct1Out2ndDir, sct1Dupl2ndDir);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new MojoFailureException("MojoFailureException: Parse");
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("MojoFailureException: IOException");
        }
    }

    public void executeMojo(File tDir, String tSubDir, String in1stDir, String in2ndDir,
            String out1stDir, String out2ndDir, String dupDir) throws ParseException,
            MojoFailureException, IOException {

        String fPathIn1stDir = tDir + FILE_SEPARATOR + tSubDir + in1stDir;
        String fPathIn2ndDir = tDir + FILE_SEPARATOR + tSubDir + in2ndDir;
        String fPathOut1stDir = tDir + FILE_SEPARATOR + tSubDir + out1stDir;
        String fPathOut2ndDir = tDir + FILE_SEPARATOR + tSubDir + out2ndDir;
        String fPathDupl2ndDir = null;

        getLog().info(":::  Input 1st: " + fPathIn1stDir);
        getLog().info(":::  Input 2nd: " + fPathIn2ndDir);
        getLog().info("::: Output 1st: " + fPathOut1stDir);
        getLog().info("::: Output 2nd: " + fPathOut2ndDir);

        List<Sct1File> in1stFiles = Sct1File.getSctFiles(tDir.getAbsolutePath(), tSubDir, in1stDir,
                "descriptions", ".txt");
        List<Sct1File> in2ndFiles = Sct1File.getSctFiles(tDir.getAbsolutePath(), tSubDir, in2ndDir,
                "descriptions", ".txt");

        getLog().info(":::  Input 1st " + in1stFiles.toString());
        getLog().info(":::  Input 2nd " + in2ndFiles.toString());

        boolean success1 = (new File(fPathOut1stDir)).mkdirs();
        if (success1)
            getLog().info("::: Output 1st: " + fPathOut1stDir);
        boolean success2 = (new File(fPathOut2ndDir)).mkdirs();
        if (success2)
            getLog().info("::: Output 2nd: " + fPathOut2ndDir);

        if (dupDir != null) {
            fPathDupl2ndDir = tDir + FILE_SEPARATOR + tSubDir + dupDir;
            boolean successDupl = (new File(fPathDupl2ndDir)).mkdirs();
            if (successDupl)
                getLog().info("::: Duplicates: " + fPathDupl2ndDir);
        }

        int totalFiles = in1stFiles.size();
        if (in2ndFiles.size() != totalFiles) {
            getLog().info("::: CAUTION: Number of input files do not match");
            // ALLOW FOR 1 LESS FILE ON 2ND SIDE
            // Example case, Spanish lags the latest international release
            if (in2ndFiles.size() == totalFiles - 1)
                totalFiles = in2ndFiles.size();
            else
                throw new MojoFailureException("Number of input files do not match");
        }

        for (int i = 0; i < totalFiles; i++) {
            Sct1File f1 = in1stFiles.get(i);
            Sct1File f2 = in2ndFiles.get(i);

            SctYDesRecord[] a1 = Sct1File.parseDescriptions(f1);
            SctYDesRecord[] a2 = Sct1File.parseDescriptions(f2);

            BufferedWriter bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    fPathOut1stDir + FILE_SEPARATOR + f1.file.getName()), "UTF-8"));
            BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    fPathOut2ndDir + FILE_SEPARATOR + f2.file.getName()), "UTF-8"));

            bw1.write(SctYDesRecord.toStringHeader() + LINE_TERMINATOR);
            bw2.write(SctYDesRecord.toStringHeader() + LINE_TERMINATOR);

            BufferedWriter bwDupl = null;
            if (fPathDupl2ndDir != null) {
                bwDupl = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                        fPathDupl2ndDir + FILE_SEPARATOR + "dupl_" + f2.file.getName()), "UTF-8"));
                bwDupl.write(SctYDesRecord.toStringHeader() + LINE_TERMINATOR);
            }

            // COMPARE AND WRITE TO OUTPUT FILES
            int idx1 = 0;
            int idx2 = 0;

            int max1 = a1.length;
            int max2 = a2.length;

            int countKeep1st = 0;
            int countKeep2nd = 0;

            while (idx1 < max1 && idx2 < max2) {
                if (a1[idx1].desSnoId < a2[idx2].desSnoId) {
                    // PRIMARY DIFFERENT
                    bw1.write(a1[idx1].toString() + LINE_TERMINATOR);
                    countKeep1st++;
                    idx1++;
                } else if (a1[idx1].desSnoId == a2[idx2].desSnoId) {
                    // SAME ID, KEEP ONLY PRIMARY
                    bw1.write(a1[idx1].toString() + LINE_TERMINATOR);
                    countKeep1st++;
                    if (bwDupl != null)
                        bwDupl.write(a2[idx2].toString() + LINE_TERMINATOR);

                    idx1++;
                    idx2++;
                } else {
                    // SECONDARY DIFFERENT
                    bw2.write(a2[idx2].toString() + LINE_TERMINATOR);
                    countKeep2nd++;
                    idx2++;
                }
            }

            // WRITE REMAINDERS
            while (idx1 < max1) {
                bw1.write(a1[idx1].toString() + LINE_TERMINATOR);
                countKeep1st++;
                idx1++;
            }
            while (idx2 < max2) {
                bw2.write(a2[idx2].toString() + LINE_TERMINATOR);
                countKeep2nd++;
                idx2++;
            }

            bw1.flush();
            bw1.close();
            bw2.flush();
            bw2.close();
            if (bwDupl != null)
                bwDupl.close();

            getLog().info(
                    "::: File 1st: " + f1.file.getName() + " in=" + a1.length + " out="
                            + countKeep1st);
            getLog().info(
                    "::: File 2nd: " + f2.file.getName() + " in=" + a2.length + " out="
                            + countKeep2nd);
            getLog().info("::: ");

        }
    }

}
