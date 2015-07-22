/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.mojo.mojo.ConceptDescriptor;
/**
 * @author Marc E. Campbell
 *
 * @goal sct-rf2-to-arf
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class SctRf2ToArfMojo extends AbstractMojo implements Serializable {

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
     * @parameter @required
     */
    private String statusDir;
    /**
     * Directory used to output the eConcept format files Default value
     * "/classes" set programmatically due to file separator
     *
     * @parameter default-value="generated-arf"
     */
    private String outputDir;
    
    /**
     * Path on which to load data. Defaults to SNOMED Core.
     *
     * @parameter 
     */
    private ConceptDescriptor pathConcept = new ConceptDescriptor("8c230474-9f11-30ce-9cad-185a96fd03a2","SNOMED Core");
    
    /**
     * Path to import concepts on. Defaults to SNOMED Core.
     * @parameter default-value="8c230474-9f11-30ce-9cad-185a96fd03a2"
     */
    private String pathUuid;
    
    /**
     * Enable storing an in-memory map from UUIDs to SCTIDs.  May not always be necessary - set to false to reduce memory usage.
     *
     * @parameter default-value=true
     */
    private boolean enableUUIDToSCTIDMap = true;
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

        String pathStr = null;
        try {
            pathStr = pathConcept.getUuid();
            // If either pathUuid is not the default and pathStr is, override with pathUuid
            if (!pathUuid.equals("8c230474-9f11-30ce-9cad-185a96fd03a2") &&
                    pathStr.equals("8c230474-9f11-30ce-9cad-185a96fd03a2"))
                pathStr = pathUuid;
        } catch (RuntimeException e) {
            getLog().error("Poorly configured path concept, at least one UUID must be specified", e);
            throw e;
        }
        getLog().info("    Path UUID: " + pathStr);

        try {
            Rf2x.setupIdCache(targetDirectory.getAbsolutePath(), enableUUIDToSCTIDMap);
            
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

            // :NYI: extended status implementation does not support multiple version years
            filesInStatus = Rf2File.getFiles(wDir, targetSubDir, statusDir, "AttributeValue", ".txt");

            ArrayList<Rf2_RefsetCRecord[]> rf2_RefsetCRecordArray = new ArrayList<>();
            int arrayCont = 0;
            for (Rf2File rf2File : filesInStatus) {
                rf2_RefsetCRecordArray.add(Rf2_RefsetCRecord.parseRefset(rf2File, null));
            }
            for (Rf2_RefsetCRecord[] rf2_RefsetCRecordTmp : rf2_RefsetCRecordArray) {
                arrayCont += rf2_RefsetCRecordTmp.length;
            }

            Rf2_RefsetCRecord[] statusRecords = new Rf2_RefsetCRecord[arrayCont];
            int index = 0;
            for (Rf2_RefsetCRecord[] rf2_RefsetCRecordTmp : rf2_RefsetCRecordArray) {
                for (int i = 0; i < rf2_RefsetCRecordTmp.length; i++) {
                    statusRecords[index] = rf2_RefsetCRecordTmp[i];
                    index++;
                }
            }

            // Rf2_RefsetCRecord[] statusRecords = Rf2_RefsetCRecord.parseRefset(filesInStatus.get(0), null);
            // hardcoded
            // CONCEPT FILES: parse, write
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    outDir + "concepts_rf2.txt"), "UTF-8"));
            getLog().info("::: CONCEPTS FILE: " + outDir + "concepts_rf2.txt");
            filesIn = Rf2File.getFiles(wDir, targetSubDir, inputDir, "sct2_Concept", ".txt");
            for (Rf2File rf2File : filesIn) {
                getLog().info("    ... " + rf2File.file.getName());
                Sct2_ConRecord[] concepts = Sct2_ConRecord.parseConcepts(rf2File, pathStr);
                concepts = Sct2_ConRecord.attachStatus(concepts, statusRecords);
                for (Sct2_ConRecord c : concepts) {
                    c.setPath(pathStr);
                    c.writeArf(bw);
                    if (Rf2x.isSctIdInUuidCache(c.conSnoIdL) == false) {
                        writeSctSnomedLongId(bwIds, c.conSnoIdL, c.effDateStr, c.pathUuidStr,
                                Rf2Defaults.getAuthorUuidStr(), c.moduleUuidStr);
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
                Sct2_DesRecord[] descriptions = Sct2_DesRecord.parseDescriptions(rf2File, pathStr);
                descriptions = Sct2_DesRecord.attachStatus(descriptions, statusRecords);
                for (Sct2_DesRecord d : descriptions) {
                    d.setPath(pathStr);
                    d.writeArf(bw);
                    if (Rf2x.isSctIdInUuidCache(d.desSnoIdL) == false) {
                        writeSctSnomedLongId(bwIds, d.desSnoIdL, d.effDateStr, d.pathUuidStr,
                                Rf2Defaults.getAuthorUuidStr(), d.moduleUuidStr);
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
                Sct2_RelRecord[] rels = Sct2_RelRecord.parseRelationships(rf2File, true, pathStr);
                rels = Sct2_RelRecord.attachStatus(rels, statusRecords);
                for (Sct2_RelRecord r : rels) {
                    r.setPath(pathStr);
                    r.writeArf(bw);
                    if (Rf2x.isSctIdInUuidCache(r.relSnoId) == false) {
                        writeSctSnomedLongId(bwIds, r.relSnoId, r.effDateStr, r.pathUuidStr,
                                Rf2Defaults.getAuthorUuidStr(), r.moduleUuidStr);
                    }
                }
            }

            filesIn = Rf2File.getFiles(wDir, targetSubDir, inputDir, "sct2_StatedRelationship", ".txt");
            filesIn.addAll(Rf2File.getFiles(wDir, targetSubDir, inputDir, "res2_RetiredStatedIsaRelationship", ".txt"));
            for (Rf2File rf2File : filesIn) {
                getLog().info("    ... " + rf2File.file.getName());
                Sct2_RelRecord[] rels = Sct2_RelRecord.parseRelationships(rf2File, false, pathStr);
                for (Sct2_RelRecord r : rels) {
                    r.setPath(pathStr);
                    r.writeArf(bw);
                    if (Rf2x.isSctIdInUuidCache(r.relSnoId) == false) {
                        writeSctSnomedLongId(bwIds, r.relSnoId, r.effDateStr, r.pathUuidStr,
                                Rf2Defaults.getAuthorUuidStr(), r.moduleUuidStr);
                    }
                }
            }
            bw.flush();
            bw.close();

            bwIds.flush();
            bwIds.close();

        } catch (TerminologyException ex) {
            Logger.getLogger(SctRf2ToArfMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SctRf2ToArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("RF2/ARF file error", ex);
        } catch (MojoFailureException ex) {
            Logger.getLogger(SctRf2ToArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (ParseException ex) {
            Logger.getLogger(SctRf2ToArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("RF2/ARF file name parse error", ex);
        }
    }

    private void writeSctSnomedLongId(BufferedWriter writer, long sctId, String date, String path,
            String author, String module)
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
        writer.append(path + TAB_CHARACTER);
        // Author UUID String --> user
        writer.append(author + TAB_CHARACTER);
        // Module UUID String
        writer.append(module + LINE_TERMINATOR);

    }
}
