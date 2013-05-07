/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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

import edu.emory.mathcs.backport.java.util.Collections;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 *
 * @author Marc E. Campbell
 *
 * @goal rf2-rel-logical-uuid-cache-gen
 * @requiresDependencyResolution compile
 */
public class Rf2IdUuidCacheGenRelLogicalMojo
        extends AbstractMojo
        implements Serializable {

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
    private String inputSubDir = "";
    /**
     * @parameter
     */
    private String inputSctDir = "ids";
    /**
     * Directory used for intermediate serialized sct/uuid mapping cache
     *
     * @parameter
     */
    private String idCacheDir = "";
    /**
     * Applicable input sub directory under the build directory.
     *
     * @parameter default-value="generated-arf"
     */
    private String outputSubDir = "";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<Rf2File> filesIn;
        getLog().info("::: BEGIN Rf2IdUuidCacheGenRelLogicalMojo");

        try {
            // CREATE COMPUTED TO ASSIGNED MAP
            // SHOW DIRECTORIES
            String wDir = targetDirectory.getAbsolutePath();
            getLog().info("  POM       Target Directory:           "
                    + targetDirectory.getAbsolutePath());
            getLog().info("  POM Input Target/Sub Directory:       "
                    + inputSubDir);
            getLog().info("  POM Input Target/Sub/SCTID Directory: "
                    + inputSctDir);
            getLog().info("  POM ID SCT/UUID Cache Directory:      "
                    + idCacheDir);
            getLog().info("  POM Output Target/Sub Directory:      "
                    + outputSubDir);

            // Setup directory paths
            getLog().info("::: Input Sct Path: " + wDir + FILE_SEPARATOR
                    + inputSubDir + FILE_SEPARATOR + inputSctDir);
            String cachePath = wDir + FILE_SEPARATOR + idCacheDir + FILE_SEPARATOR;
            String idCacheFName = cachePath + "uuidRemapCache.ser";
            if ((new File(cachePath)).mkdirs()) {
                getLog().info("ID Cache directory created ... ");
            }

            // create declared to assithe from identifiers file
            filesIn = Rf2File.getFiles(wDir, inputSubDir, inputSctDir,
                    "_Identifier_", ".txt");
            Sct2_UuidUuidRecord.parseToUuidRemapCacheFile(filesIn, idCacheFName);

            // Parse IHTSDO Terminology Identifiers to Sct_CompactId cache file.
            filesIn = Rf2File.getFiles(wDir, inputSubDir, inputSctDir, "Stated", ".txt");
            long startTime = System.currentTimeMillis();

            ArrayList<Sct2_RelLogicalRecord> rels;
            rels = Sct2_RelLogicalRecord.parseRelationships(filesIn);

            Sct2_RelLogicalRecord.checkRelSctIdTimeErrors(rels);
            Sct2_RelLogicalRecord.checkRelGroupTime(rels);
            Sct2_RelLogicalRecord[] relArray = rels.toArray(new Sct2_RelLogicalRecord[]{});

            // create logical-rel-uuid to sctid-computed-uuid list
            ArrayList<Sct2_UuidUuidRecord> uuidUuidList;
            uuidUuidList = SctRelLogicalUuidComputer.createSctUuidToLogicalUuidList(relArray);
            // look up is sct uuid is computed from sctid or assigned
            Sct2_UuidUuidRemapper idLookup = new Sct2_UuidUuidRemapper(idCacheFName);

            String idRelLogicalCacheFName = cachePath + "uuidRemapRelLogicalCache.ser";
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(
                    new FileOutputStream(idRelLogicalCacheFName)))) {
                for (Sct2_UuidUuidRecord relIdRec : uuidUuidList) {
                    // remember "uuidDeclared" is from rel sctid
                    UUID uuid = idLookup.getUuid(relIdRec.uuidDeclared);
                    if (uuid != null) {
                        // then uuid is assigned and not computed from sctid
                        relIdRec.uuidDeclared = uuid; // swap for uuid assigned from snomed
                    }
                    oos.writeUnshared(relIdRec);
                }
                UUID[] c = idLookup.uuidComputedArray;
                UUID[] d = idLookup.uuidDeclaredArray;
                for (int i = 0; i < c.length; i++) {
                    oos.writeUnshared(new Sct2_UuidUuidRecord(c[i], d[i]));
                }
            }

            System.out.println((System.currentTimeMillis() - startTime) + " mS");
        } catch (Exception ex) {
            getLog().error(ex);
            throw new MojoFailureException("Rf2IdUuidCacheGenRelLogicalMojo: ", ex);
        }


    }
}
