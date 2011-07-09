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
import java.io.UnsupportedEncodingException;
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
 * @goal sct-rf2-refset-c-to-arf
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class SctRf2RefsetCToArfMojo extends AbstractMojo implements Serializable {

    private static final String FILE_SEPARATOR = File.separator;
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
     * Default value "/classes" set programmatically due to file separator
     *
     * @parameter default-value="generated-arf"
     */
    private String outputDir;
    /**
     * Directory used to output the eConcept format files
     */
    private String[] filters;
    String uuidSourceSnomedLongStr;
    String uuidPathStr;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<Rf2File> filesIn;
        getLog().info("::: BEGIN SctRf2RefsetCToArfMojo");

        // SHOW DIRECTORIES
        String wDir = targetDirectory.getAbsolutePath();
        getLog().info("    POM Target Directory: " + targetDirectory.getAbsolutePath());
        getLog().info("    POM Target Sub Directory: " + targetSubDir);
        getLog().info("    POM Target Sub Data Directory: " + inputDir);

        try {
            // SETUP CONSTANTS
            uuidSourceSnomedLongStr =
                    ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getPrimoridalUid().toString();

            // SETUP EXCLUSIONS FILTER
            Long[] exclusions = null;
            if (filters != null && filters.length > 0) {
                exclusions = new Long[filters.length];
                for (int i = 0; i < exclusions.length; i++) {
                    exclusions[i] = Long.parseLong(filters[i]);
                }
            }

            // FILE & DIRECTORY SETUP
            // Create multiple directories
            String outDir = wDir + FILE_SEPARATOR + targetSubDir + FILE_SEPARATOR
                    + outputDir + FILE_SEPARATOR;
            boolean success = (new File(outDir)).mkdirs();
            if (success) {
                getLog().info("::: Output Directory: " + outDir);
            }

            // CONCEPT REFSET FILES
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    outDir + "concept_refsetc_rf2.refset"), "UTF-8"));
            getLog().info("::: CONCEPT REFSET FILE: " + outDir + "concept_refsetc_rf2.refset");
            filesIn = Rf2File.getFiles(wDir, targetSubDir, inputDir, "der2_cRefset_Association", ".txt");
            for (Rf2File rf2File : filesIn) {
                Rf2_RefsetCRecord[] members = Rf2_RefsetCRecord.parseRefset(rf2File, exclusions);
                for (Rf2_RefsetCRecord m : members) {
                    m.writeArf(bw);
                }
            }
            bw.flush();
            bw.close();

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SctRf2RefsetCToArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("RF2/ARF SctRf2RefsetCToArfMojo UnsupportedEncodingException", ex);
        } catch (TerminologyException ex) {
            Logger.getLogger(SctRf2ToArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("RF2/ARF SctRf2RefsetCToArfMojo Terminology error", ex);
        } catch (IOException ex) {
            Logger.getLogger(SctRf2ToArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("RF2/ARF SctRf2RefsetCToArfMojo file error", ex);
        } catch (ParseException ex) {
            Logger.getLogger(SctRf2ToArfMojo.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("RF2/ARF SctRf2RefsetCToArfMojo file name parse error", ex);
        }
        getLog().info("::: END SctRf2RefsetCToArfMojo");
    }
}
