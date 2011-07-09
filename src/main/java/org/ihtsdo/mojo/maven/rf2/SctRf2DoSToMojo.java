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
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.tapi.TerminologyException;

/**
 *
 * @author marc
 * @goal sct-rf2-dos-to-arf
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class SctRf2DoSToMojo extends AbstractMojo implements Serializable {

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
     *
     * @parameter default-value="generated-arf"
     */
    private String outputDir;

        /**
     * Directory used to output the eConcept format files
     *
     * @parameter default-value="generated-arf"
     */
    private String[] filters;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<Rf2File> filesIn;
        getLog().info("::: BEGIN Rf2_RefsetCreateConceptMojo");

        String wDir = targetDirectory.getAbsolutePath();
        getLog().info("    POM Target Directory: " + targetDirectory.getAbsolutePath());
        getLog().info("    POM Target Sub Directory: " + targetSubDir);
        getLog().info("    POM Target Sub Data Directory: " + inputDir);

        try {
            // FILE & DIRECTORY SETUP
            // Create multiple directories
            String outDir = wDir + FILE_SEPARATOR + targetSubDir + FILE_SEPARATOR
                    + outputDir + FILE_SEPARATOR;
            boolean success = (new File(outDir)).mkdirs();
            if (success) {
                getLog().info("::: Output Directory: " + outDir);
            }

            // SETUP EXCLUSIONS FILTER
            Long[] exclusions = new Long[filters.length];
            for (int i = 0; i < exclusions.length; i++) {
                exclusions[i] = new Long(filters[i]);
            }

            // CONCEPT REFSET FILES
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    outDir + "concept_refsetDoS_rf2.refset"), "UTF-8"));
            getLog().info("::: DoS REFSET FILE: " + outDir + "concept_refsetDoS_rf2.refset");
            filesIn = Rf2File.getFiles(wDir, targetSubDir, inputDir, "AttributeValue", ".txt");
            for (Rf2File rf2File : filesIn) {
                Rf2_RefsetCRecord[] members = Rf2_RefsetCRecord.parseRefset(rf2File, exclusions);
                for (Rf2_RefsetCRecord m : members) {
                    m.writeArf(bw);
                }
            }
            bw.flush();
            bw.close();

            // WRITE PARENT REFSET CONCEPT :!!!:INTERIM:
            ArrayList<Rf2_RefsetId> refsetIdList = new ArrayList<Rf2_RefsetId>();
            refsetIdList.add(new Rf2_RefsetId(449613003L, /* refsetSctIdOriginal */
                    "2002.01.31", /* refsetDate */
                    "8c230474-9f11-30ce-9cad-185a96fd03a2", /* refsetPathUuidStr */
                    "Degree of Synonymy Refset (RF2)", /* refsetPrefTerm */
                    "Degree of Synonymy Refset (RF2)", /* refsetFsName */
                    "3e0cd740-2cc6-3d68-ace7-bad2eb2621da")); /* refsetParentUuid */
            Rf2_RefsetId.saveRefsetConcept(outDir, refsetIdList);

            getLog().info("::: END Rf2_RefsetCreateConceptMojo");
        } catch (TerminologyException ex) {
            Logger.getLogger(SctRf2DoSToMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SctRf2DoSToMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(SctRf2DoSToMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SctRf2DoSToMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
