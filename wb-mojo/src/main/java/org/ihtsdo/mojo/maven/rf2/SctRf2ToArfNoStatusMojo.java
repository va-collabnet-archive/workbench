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

import java.io.*;
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
 * @goal sct-rf2-to-arf-no-status
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class SctRf2ToArfNoStatusMojo extends AbstractMojo implements Serializable {

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
     * @parameter @required
     */
    private String inputDir;
    /**
     * Directory used to output the eConcept format files Default value
     * "/classes" set programmatically due to file separator
     *
     * @parameter default-value="generated-arf"
     */
    private String outputDir;
    /**
     * Path to import concepts on. Defaults to SNOMED Core.
     * @parameter default-value="8c230474-9f11-30ce-9cad-185a96fd03a2"
     */
    private String pathUuid;
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
            Rf2x.setupIdCache(targetDirectory.getAbsolutePath());

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
                    outDir + "ids.txt"), "UTF-8"));
            getLog().info("::: IDS OUTPUT: " + outDir + "ids_sct.txt");


            // hardcoded
            // CONCEPT FILES: parse, write
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    outDir + "concepts_rf2.txt"), "UTF-8"));
            getLog().info("::: CONCEPTS FILE: " + outDir + "concepts_rf2.txt");
            filesIn = Rf2File.getFiles(wDir, targetSubDir, inputDir, "sct2_Concept", ".txt");
            for (Rf2File rf2File : filesIn) {
                getLog().info("    ... " + rf2File.file.getName());
                Sct2_ConRecord[] concepts = Sct2_ConRecord.parseConcepts(rf2File, pathUuid);
                for (Sct2_ConRecord c : concepts) {
                    c.writeArf(bw);
                    if (Rf2x.isSctIdInUuidCache(c.conSnoIdL) == false) {
                        writeSctSnomedLongId(bwIds, c.conSnoIdL, c.effDateStr, c.pathUuidStr);
                    }
                }
            }
            bw.flush();
            bw.close();

            // DESCRIPTION FILES "sct2_Description"
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    outDir + "descriptions_rf2.txt"), "UTF-8"));
            getLog().info("::: DESCRIPTIONS FILE: " + outDir + "descriptions_rf2.txt");
            filesIn = Rf2File.getFiles(wDir, targetSubDir, inputDir, "sct2_Description", ".txt");
            for (Rf2File rf2File : filesIn) {
                getLog().info("    ... " + rf2File.file.getName());
                Sct2_DesRecord[] descriptions = Sct2_DesRecord.parseDescriptions(rf2File, pathUuid);
                for (Sct2_DesRecord d : descriptions) {
                    d.writeArf(bw);
                    if (Rf2x.isSctIdInUuidCache(d.desSnoIdL) == false) {
                        writeSctSnomedLongId(bwIds, d.desSnoIdL, d.effDateStr, d.pathUuidStr);
                    }
                }
            }
            bw.flush();
            bw.close();

            // RELATIONSHIP FILES "sct2_StatedRelationship" "sct2_Relationship"
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    outDir + "relationships_rf2.txt"), "UTF-8"));
            getLog().info("::: RELATIONSHIPS FILE: " + outDir + "relationships_rf2.txt");
            filesIn = Rf2File.getFiles(wDir, targetSubDir, inputDir, "sct2_Relationship", ".txt");
            filesIn.addAll(Rf2File.getFiles(wDir, targetSubDir, inputDir, "res2_RetiredIsaRelationship", ".txt"));
            for (Rf2File rf2File : filesIn) {
                getLog().info("    ... " + rf2File.file.getName());
                Sct2_RelRecord[] rels = Sct2_RelRecord.parseRelationships(rf2File, true, pathUuid);
                for (Sct2_RelRecord r : rels) {
                    r.writeArf(bw);
                    if (Rf2x.isSctIdInUuidCache(r.relSnoId) == false) {
                        writeSctSnomedLongId(bwIds, r.relSnoId, r.effDateStr, r.pathUuidStr);
                    }
                }
            }

            filesIn = Rf2File.getFiles(wDir, targetSubDir, inputDir, "sct2_StatedRelationship", ".txt");
            filesIn.addAll(Rf2File.getFiles(wDir, targetSubDir, inputDir, "res2_RetiredStatedIsaRelationship", ".txt"));
            for (Rf2File rf2File : filesIn) {
                getLog().info("    ... " + rf2File.file.getName());
                Sct2_RelRecord[] rels = Sct2_RelRecord.parseRelationships(rf2File, false, pathUuid);
                for (Sct2_RelRecord r : rels) {
                    r.writeArf(bw);
                    if (Rf2x.isSctIdInUuidCache(r.relSnoId) == false) {
                        writeSctSnomedLongId(bwIds, r.relSnoId, r.effDateStr, r.pathUuidStr);
                    }
                }
            }
            bw.flush();
            bw.close();

            bwIds.flush();
            bwIds.close();

        } catch (TerminologyException ex) {
            Logger.getLogger(SctRf2ToArfNoStatusMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SctRf2ToArfNoStatusMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("RF2/ARF file error", ex);
        } catch (MojoFailureException ex) {
            Logger.getLogger(SctRf2ToArfNoStatusMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (ParseException ex) {
            Logger.getLogger(SctRf2ToArfNoStatusMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("RF2/ARF file name parse error", ex);
        }
    }

    private void writeSctSnomedLongId(BufferedWriter writer, long sctId, String date, String path)
            throws IOException, TerminologyException {
        // PRIMARY_UUID = 0;
        writer.append(Rf2x.convertSctIdToUuidStr(sctId) + TAB_CHARACTER);
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
