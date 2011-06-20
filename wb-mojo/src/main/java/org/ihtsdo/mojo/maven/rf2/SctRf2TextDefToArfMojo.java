/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.mojo.maven.rf2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * @author Marc E. Campbell
 *
 * @goal sct-rf2-text-definition-to-arf
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class SctRf2TextDefToArfMojo extends AbstractMojo implements Serializable {

    private static final String FILE_SEPARATOR = File.separator;
    /**
     * Line terminator is deliberately set to CR-LF which is DOS style
     */
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
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
    private String inputDir;
    /**
     * @parameter
     * @required
     */
    private String statusDir;
    /**
     * Directory used to output the eConcept format files
     * Default value "/classes" set programmatically due to file separator
     *
     * @parameter default-value="generated-arf"
     */
    private String outputDir;
    String uuidSourceSnomedLongStr;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<Rf2File> filesIn;
        List<Rf2File> filesInStatus;
        getLog().info("::: BEGIN SctRf2ToArf");

        // SHOW DIRECTORIES
        String wDir = targetDirectory.getAbsolutePath();
        getLog().info("    POM Target Directory: " + targetDirectory.getAbsolutePath());
        getLog().info("    POM Target Sub Directory: " + targetSubDir);
        getLog().info("    POM Target Sub Data Directory: " + inputDir);

        try {
            // SETUP CONSTANTS
            uuidSourceSnomedLongStr =
                    ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getPrimoridalUid().toString();

            // FILE & DIRECTORY SETUP
            // Create multiple directories
            String outDir = wDir + FILE_SEPARATOR + targetSubDir + FILE_SEPARATOR
                    + outputDir + FILE_SEPARATOR;
            boolean success = (new File(outDir)).mkdirs();
            if (success) {
                getLog().info("::: Output Directory: " + outDir);
            }
            BufferedWriter bwIds = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    outDir + "ids_textdefinitions.txt"), "UTF-8"));
            getLog().info("::: IDS OUTPUT: " + outDir + "ids.txt");

            // :NYI: extended status implementation does not multiple version years
            filesInStatus = Rf2File.getFiles(wDir, targetSubDir, statusDir, "AttributeValue", ".txt");
            Rf2_RefsetCRecord[] statusRecords = Rf2_RefsetCRecord.parseRefset(filesInStatus.get(0)); // hardcoded

            // TEXTDEFINITION FILES "sct2_TextDefinition"
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    outDir + "descriptions_textdefinitions_rf2.txt"), "UTF-8"));
            getLog().info("::: TEXTDEFINITIONS FILE: " + outDir + "textdefinitions_rf2.txt");
            filesIn = Rf2File.getFiles(wDir, targetSubDir, inputDir, "sct2_TextDefinition", ".txt");
            for (Rf2File rf2File : filesIn) {
                Sct2_DesRecord[] textdefinitions = Sct2_DesRecord.parseDescriptions(rf2File);
                textdefinitions = Sct2_DesRecord.attachStatus(textdefinitions, statusRecords);
                for (Sct2_DesRecord d : textdefinitions) {
                    d.writeArf(bw);
                    writeSctSnomedLongId(bwIds, d.desSnoIdL, d.effDateStr, d.pathStr);
                }
            }
            bw.flush();
            bw.close();

            bwIds.flush();
            bwIds.close();

        } catch (TerminologyException ex) {
            Logger.getLogger(SctRf2TextDefToArfMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SctRf2TextDefToArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("RF2/ARF file error", ex);
        } catch (ParseException ex) {
            Logger.getLogger(SctRf2TextDefToArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("RF2/ARF file name parse error", ex);
        }
    }

    private void writeSctSnomedLongId(BufferedWriter writer, long sctId, String date, String path)
            throws IOException, TerminologyException {
        // PRIMARY_UUID = 0;
        writer.append(Rf2x.convertIdToUuidStr(sctId) + TAB_CHARACTER);
        // SOURCE_SYSTEM_UUID = 1;
        writer.append(uuidSourceSnomedLongStr + TAB_CHARACTER);
        // ID_FROM_SOURCE_SYSTEM = 2;
        writer.append(Long.toString(sctId) + TAB_CHARACTER);
        // STATUS_UUID = 3;
        writer.append(Rf2x.convertActiveToStatusUuid(true) + TAB_CHARACTER);
        // EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
        writer.append(date + TAB_CHARACTER);
        // PATH_UUID = 5;
        writer.append(path + LINE_TERMINATOR);
    }
}
