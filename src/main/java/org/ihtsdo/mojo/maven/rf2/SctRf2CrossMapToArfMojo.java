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
 *
 * @author marc
 *
 * @goal sct-rf2-crossmap-to-arf
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class SctRf2CrossMapToArfMojo extends AbstractMojo implements Serializable {

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
     * Directory used to output the eConcept format files
     *
     * @parameter default-value="generated-arf"
     */
    private String outputDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        BufferedWriter bwIds = null;
        List<Rf2File> filesIn;
        getLog().info("::: BEGIN SctRf2CrossMapToArfMojo");
        // SHOW DIRECTORIES
        String wDir = targetDirectory.getAbsolutePath();
        getLog().info("    POM Target Directory: " + targetDirectory.getAbsolutePath());
        getLog().info("    POM Target Sub Directory: " + targetSubDir);
        getLog().info("    POM Target Sub Data Directory: " + inputDir);

        try {
            // SETUP CONSTANTS
            Rf2_CrossmapRecord.uuidSourceSnomedRtStr =
                    ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.getPrimoridalUid().toString();
            Rf2_CrossmapRecord.uuidSourceCtv3Str =
                    ArchitectonicAuxiliary.Concept.CTV3_ID.getPrimoridalUid().toString();

            // FILE & DIRECTORY SETUP
            // Create multiple directories
            String outDir = wDir + FILE_SEPARATOR + targetSubDir + FILE_SEPARATOR
                    + outputDir + FILE_SEPARATOR;
            boolean success = (new File(outDir)).mkdirs();
            if (success) {
                getLog().info("::: Output Directory: " + outDir);
            }
            bwIds = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    outDir + "ids.txt"), "UTF-8"));
            getLog().info("::: IDS OUTPUT: " + outDir + "ids_crossmap.txt");

            // LANGUAGE REFSET FILES "der2_cRefset_Language"
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    outDir + "concept_language_rf2.refset"), "UTF-8"));
            getLog().info("::: CROSSMAP REFSET FILE: " + outDir + "string_simplemap_rf2.refset");
            filesIn = Rf2File.getFiles(wDir, targetSubDir, inputDir, "SimpleMap", ".txt");
            for (Rf2File rf2File : filesIn) {
                Rf2_CrossmapRecord[] members = Rf2_CrossmapRecord.parseCrossmapFile(rf2File);
                for (Rf2_CrossmapRecord m : members) {
                    // 446608001 ICD-O
                    // 900000000000498005 SNOMED RT
                    // 900000000000497000 CTV3
                    if (m.refsetIdL == 900000000000498005L) {
                        m.writeArfId(bwIds);
                    } else if (m.refsetIdL == 900000000000497000L) {
                        m.writeArfId(bwIds);
                    } else if (m.refsetIdL == 446608001L) {
                        m.writeArfRefset(bw);
                    } else {
                        throw new UnsupportedOperationException();
                    }
                }
            }

            bw.flush();
            bw.close();
            
            bwIds.flush();
            bwIds.close();
        } catch (TerminologyException ex) {
            Logger.getLogger(SctRf2ToArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("RF2/ARF SctRf2LrsToArfMojo Terminology error", ex);
        } catch (ParseException ex) {
            Logger.getLogger(SctRf2CrossMapToArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("RF2/ARF SctRf2CrossMapToArfMojo parse error", ex);
        } catch (IOException ex) {
            Logger.getLogger(SctRf2ToArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("RF2/ARF SctRf2CrossMapToArfMojo file error", ex);
        }
        getLog().info("::: END SctRf2CrossMapToArfMojo");
    }
}
