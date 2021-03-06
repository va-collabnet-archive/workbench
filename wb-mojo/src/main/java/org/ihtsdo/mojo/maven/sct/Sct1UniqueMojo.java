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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
 * &lt;dateStart&gt; yyyy.mm.dd -- filter excludes files before startDate
 * &lt;dateStop&gt;  yyyy.mm.dd -- filter excludes files after stopDate

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
     * Start date (inclusive)
     * @parameter
     */
    private String dateStart;
    private Date dateStartObj;

    /**
     * Stop date (inclusive)
     * @parameter
     */
    private String dateStop;
    private Date dateStopObj;

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
     * @required
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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.ss HH:mm:ss");
        if (dateStartObj != null)
            getLog().info("::: Start date (inclusive) = " + sdf.format(dateStartObj));
        if (dateStopObj != null)
            getLog().info(":::  Stop date (inclusive) = " + sdf.format(dateStopObj));
        
        String fPathIn1stDir = tDir + FILE_SEPARATOR + tSubDir + in1stDir;
        String fPathIn2ndDir = tDir + FILE_SEPARATOR + tSubDir + in2ndDir;
        String fPathOut1stDir = tDir + FILE_SEPARATOR + tSubDir + out1stDir;
        String fPathOut2ndDir = tDir + FILE_SEPARATOR + tSubDir + out2ndDir;
        String fPathDupl2ndDir = null;

        getLog().info(":::  Input 1st: " + fPathIn1stDir);
        getLog().info(":::  Input 2nd: " + fPathIn2ndDir);
        getLog().info("::: Output 1st: " + fPathOut1stDir);
        getLog().info("::: Output 2nd: " + fPathOut2ndDir);

        List<Sct1File> in1stFiles = null;
        List<Sct1File> in2ndFiles = null;
        if (dateStartObj == null && dateStopObj == null) {
            in1stFiles = Sct1File.getSctFiles(tDir.getAbsolutePath(), tSubDir, in1stDir,
                    "descriptions", ".txt");
            in2ndFiles = Sct1File.getSctFiles(tDir.getAbsolutePath(), tSubDir, in2ndDir,
                    "descriptions", ".txt");
        } else {
            in1stFiles = Sct1File.getSctFiles(tDir.getAbsolutePath(), tSubDir, in1stDir,
                    "descriptions", ".txt", dateStartObj, dateStopObj);
            in2ndFiles = Sct1File.getSctFiles(tDir.getAbsolutePath(), tSubDir, in2ndDir,
                    "descriptions", ".txt", dateStartObj, dateStopObj);
        }

        getLog().info(":::  Input 1st " + in1stFiles.toString());
        getLog().info(":::  Input 2nd " + in2ndFiles.toString());

        boolean success1 = (new File(fPathOut1stDir)).mkdirs();
        if (success1)
            getLog().info("::: Output 1st: " + fPathOut1stDir);
        boolean success2 = (new File(fPathOut2ndDir)).mkdirs();
        if (success2)
            getLog().info("::: Output 2nd: " + fPathOut2ndDir);
        
        fPathDupl2ndDir = tDir + FILE_SEPARATOR + tSubDir + dupDir;
        boolean successDupl = (new File(fPathDupl2ndDir)).mkdirs();
        if (successDupl)
            getLog().info("::: Duplicates: " + fPathDupl2ndDir);

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

            Sct1_DesRecord[] a1 = Sct1_DesRecord.parseDescriptions(f1);
            Sct1_DesRecord[] a2 = Sct1_DesRecord.parseDescriptions(f2);

            BufferedWriter bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    fPathOut1stDir + FILE_SEPARATOR + f1.file.getName()), "UTF-8"));
            BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    fPathOut2ndDir + FILE_SEPARATOR + f2.file.getName()), "UTF-8"));

            bw1.write(Sct1_DesRecord.toStringHeader() + LINE_TERMINATOR);
            bw2.write(Sct1_DesRecord.toStringHeader() + LINE_TERMINATOR);

            BufferedWriter bwDupl = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    fPathDupl2ndDir + FILE_SEPARATOR + "dupl_" + f2.file.getName()), "UTF-8"));
            bwDupl.write(Sct1_DesRecord.toStringHeader() + LINE_TERMINATOR);

            BufferedWriter bwExcept = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    fPathDupl2ndDir + FILE_SEPARATOR + "exceptions_" + f2.file.getName()), "UTF-8"));
            bwExcept.write(Sct1_DesRecord.toStringHeader() + LINE_TERMINATOR);

            // COMPARE AND WRITE TO OUTPUT FILES
            int idx1 = 0;
            int idx2 = 0;

            int max1 = a1.length;
            int max2 = a2.length;

            int countKeep1st = 0;
            int countKeep2nd = 0;

            while (idx1 < max1 && idx2 < max2) {
                if (a1[idx1].desUuidMsb == a2[idx2].desUuidMsb
                        && a1[idx1].desUuidLsb == a2[idx2].desUuidLsb) {
                    // SAME ID, KEEP ONLY PRIMARY
                    bw1.write(a1[idx1].toString() + LINE_TERMINATOR);
                    countKeep1st++;
                    
                    // RECORD DUPLICATES
                    bwDupl.write(a2[idx2].toString() + LINE_TERMINATOR);
                    
                    // RECORD EXCEPTIONS
                    if (a1[idx1].languageCode.compareTo(a2[idx2].languageCode) != 0)
                        bwExcept.write(a2[idx2].toString() + LINE_TERMINATOR);
                        
                    idx1++;
                    idx2++;
                } else if (a1[idx1].desUuidMsb < a2[idx2].desUuidMsb) {
                    // PRIMARY DIFFERENT, NOT IN LANGUAGE EDITION
                    bw1.write(a1[idx1].toString() + LINE_TERMINATOR);
                    countKeep1st++;
                    idx1++;
                } else if (a1[idx1].desUuidMsb == a2[idx2].desUuidMsb
                        && a1[idx1].desUuidLsb < a2[idx2].desUuidLsb) {
                    // PRIMARY DIFFERENT
                    bw1.write(a1[idx1].toString() + LINE_TERMINATOR);
                    countKeep1st++;
                    idx1++;
                    
                } else {
                    // SECONDARY DIFFERENT, UNIQUE TO LANGUAGE EDITION
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
            if (bwExcept != null)
                bwExcept.close();

            getLog().info(
                    "::: File 1st: " + f1.file.getName() + " in=" + a1.length + " out="
                            + countKeep1st);
            getLog().info(
                    "::: File 2nd: " + f2.file.getName() + " in=" + a2.length + " out="
                            + countKeep2nd);
            getLog().info("::: ");

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
