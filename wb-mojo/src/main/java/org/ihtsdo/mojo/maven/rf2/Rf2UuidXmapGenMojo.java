/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 *
 * Read a file of SCTID with corresponding UUIDs and determines if the UUIDs
 * need to be re-mapped.
 *
 * @author Marc E. Campbell
 *
 * @goal sct-rf2-uuid-xmap-gen
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class Rf2UuidXmapGenMojo extends AbstractMojo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * Line terminator is deliberately set to CR-LF which is DOS style
     */
    private static final String FILE_SEPARATOR = File.separator;
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
     * @parameter default-value="uuid-xmap"
     */
    private String outputDir;
    /**
     * Default value SNOMED Core
     *
     * @parameter default-value="8c230474-9f11-30ce-9cad-185a96fd03a2"
     */
    private UUID uuidPath;

    public void setUuidPath(String uuidStr) {
        uuidPath = UUID.fromString(uuidStr);
    }
    /**
     * Default value Workbench Auxiliary 'user'
     *
     * @parameter default-value="f7495b58-6630-3499-a44e-2052b5fcf06c"
     */
    private UUID uuidAuthor;

    public void setUuidAuthor(String uuidStr) {
        uuidAuthor = UUID.fromString(uuidStr);
    }

    @Override
    public void execute()
            throws MojoExecutionException, MojoFailureException {
        List<Rf2File> filesIn;
        BufferedWriter bw = null;
        getLog().info("::: BEGIN Rf2UuidXmapGenMojo");
        try {
            // SHOW DIRECTORIES
            String wDir = targetDirectory.getAbsolutePath();
            getLog().info("    POM Target Directory: " + targetDirectory.getAbsolutePath());
            getLog().info("    POM Target Sub Directory: " + targetSubDir);
            getLog().info("    POM Target Sub Data Directory: " + inputDir);
            // FILE & DIRECTORY SETUP
            // Create multiple directories
            String outDir = wDir + FILE_SEPARATOR + targetSubDir + FILE_SEPARATOR
                    + outputDir + FILE_SEPARATOR;
            boolean success = (new File(outDir)).mkdirs();
            if (success) {
                getLog().info("::: Output Directory: " + outDir);
            }
            String idCacheDir = wDir + FILE_SEPARATOR + targetSubDir + FILE_SEPARATOR
                    + "idcache" + FILE_SEPARATOR;
            success = (new File(idCacheDir)).mkdirs();
            if (success) {
                getLog().info("::: ID Cache Directory: " + idCacheDir);
            }
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    outDir + "ids_xmap.txt"), "UTF-8"));
            getLog().info("::: ID XMAP OUTPUT: " + outDir + "ids_xmap.txt");

            filesIn = Rf2File.getFiles(wDir, targetSubDir, inputDir,
                    "_Identifier_", ".txt");

            // Parse IHTSDO Terminology Identifiers
            // to Sct_CompactId serial object file.
            String idPreCacheFName = idCacheDir + "idPreCache.ser";
            Sct2_IdRecord.parseToIdPreCacheFile(filesIn, idPreCacheFName);
            // Setup id array cache object
            // idCacheDir + FILE_SEPARATOR + "idObjectCache.jbin"
            long startTime = System.currentTimeMillis();
            Sct2_IdLookUp idLookup = new Sct2_IdLookUp(idPreCacheFName);
            System.out.println((System.currentTimeMillis() - startTime) + " mS");

            // Parse IHTSDO Terminology Identifiers
            // to Sct_CompactId serial object file.
            String idAssignedArfFName = outDir + "ids_assigned.txt";
            try (BufferedWriter bwIdArf = new BufferedWriter(
                            new OutputStreamWriter(
                            new FileOutputStream(
                            idAssignedArfFName), "UTF-8"))) {
                getLog().info("::: Assigned SCTID/UUID ARF output: "
                        + idAssignedArfFName);
                Sct2_IdRecord.parseIdsToArf(filesIn, bwIdArf, idLookup,
                        uuidPath, uuidAuthor);
                bwIdArf.flush();
                bwIdArf.close();
            }

            for (Rf2File rf2File : filesIn) {
                // Sct2_IdRecord.parseIdsToArf(rf2File, false, bwArf);
//                for (Sct2_DesRecord d : textdefinitions) {
//                    d.writeArf(bw);
//                    writeSctSnomedLongId(bwIds, d.desSnoIdL, d.effDateStr, d.pathUuidStr);
//                }
            }
            bw.flush();
            bw.close();


        } catch (Exception ex) {
            Logger.getLogger(Rf2UuidXmapGenMojo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(Rf2UuidXmapGenMojo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }
}
