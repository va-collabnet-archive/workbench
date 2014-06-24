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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
 * @goal sct-rf2-uuid-remap-arf
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class Rf2IdUuidRemapArfMojo
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
    private String idSubDir = "";
    /**
     * Input Directory.<br> The directory array parameter supported extensions
     * via separate directories in the array.
     *
     * @parameter
     */
    private String idInputDir = "";
    /**
     * Input Directories Array.<br> The directory array parameter supported
     * extensions via separate directories in the array.
     *
     * @parameter
     */
    private String remapSubDir;
    /**
     * Input Directories Array.<br> The directory array parameter supported
     * extensions via separate directories in the array.
     *
     * @parameter
     */
    private String[] remapArfDirs;
    /**
     * Directory used for intermediate serialized sct/uuid mapping cache
     *
     * @parameter
     */
    private String idCacheDir = "";

    @Override
    public void execute()
            throws MojoExecutionException, MojoFailureException {
        // SHOW DIRECTORIES
        String wDir = targetDirectory.getAbsolutePath();
        getLog().info("  POM       Target Directory:           "
                + targetDirectory.getAbsolutePath());
        getLog().info("  POM Input Target/Sub Directory:       "
                + idSubDir);
        getLog().info("  POM ID SCT/UUID Cache Directory:      "
                + idCacheDir);

        // Setup cache paths
        String cachePath = wDir + FILE_SEPARATOR + idCacheDir + FILE_SEPARATOR;
        String idCacheFName = cachePath + "uuidRemapCache.ser";
        if ((new File(cachePath)).mkdirs()) {
            getLog().info("::: UUID Remap Cache : " + idCacheFName);
        }

        CreateUuidRemapCache(wDir, idCacheFName);
        try {
            RemapAllFiles(wDir, remapSubDir, remapArfDirs, idCacheFName);
        } catch (IOException ex) {
            throw new MojoExecutionException("failed uuid remap", ex);
        }
    }

    void CreateUuidRemapCache(String wDir, String idCacheFName) {
        List<Rf2File> filesIn;
        getLog().info("::: BEGIN Rf2IdUuidRemapArfMojo");
        try {

            // Parse IHTSDO Terminology Identifiers to Sct_CompactId cache file.
            filesIn = Rf2File.getFiles(wDir, idSubDir, idInputDir,
                    "_Identifier_", ".txt");
            Sct2_UuidUuidRecord.parseToUuidRemapCacheFile(filesIn, idCacheFName);

        } catch (Exception ex) {
            Logger.getLogger(Rf2IdUuidRemapArfMojo.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    void RemapAllFiles(String tDir, String tSubDir, String[] inDirs, String idCacheFName)
            throws IOException {
        long startTime = System.currentTimeMillis();
        Sct2_UuidUuidRemapper idLookup = new Sct2_UuidUuidRemapper(idCacheFName);
        System.out.println((System.currentTimeMillis() - startTime) + " mS");

        for (String inDirName : inDirs) {
            String dirPathName = tDir + tSubDir + inDirName;
            dirPathName = dirPathName.replace('/', File.separatorChar);
            File dir = new File(dirPathName);
            // list files in directory
            for (String fileName : dir.list()) {
                if (fileName.contains(".refset")
                        || fileName.contains("concepts.txt")
                        || fileName.contains("descriptions.txt")
                        || fileName.contains("relationships.txt")
                        || fileName.contains("ids.txt")) {
                    String fPathNameIn = dirPathName + FILE_SEPARATOR + fileName;
                    String fPathNameOut = dirPathName + FILE_SEPARATOR + "tmp_" + fileName;
                    RemapFile(new File(fPathNameIn), new File(fPathNameOut), idLookup);
                }
            }
        }

    }

    void RemapFile(File inFile, File outFile, Sct2_UuidUuidRemapper uuidUuidRemapper)
            throws IOException {
        getLog().info("remap UUIDs in: " + inFile.getAbsolutePath());

        FileReader fr = new FileReader(inFile);
        try (BufferedReader br = new BufferedReader(fr)) {
            FileWriter fw = new FileWriter(outFile);
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                String eachLine = br.readLine();
                while (eachLine != null && eachLine.length() > 8) {

                    String[] line = eachLine.split(TAB_CHARACTER);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < line.length; i++) {
                        // 2bfc4102-f630-5fbe-96b8-625f2a6b3d5a
                        // 012345678901234567890123456789012345
                        if (line[i].length() == 36
                                && line[i].charAt(8) == '-'
                                && line[i].charAt(13) == '-'
                                && line[i].charAt(18) == '-'
                                && line[i].charAt(23) == '-') {
                            UUID tmpUuid = uuidUuidRemapper.getUuid(line[i]);
                            if (tmpUuid != null) {
                                sb.append(tmpUuid.toString());
                            } else {
                                sb.append(line[i]);
                            }
                        } else { // is not a UUID
                            sb.append(line[i]);
                        }
                        if (i < line.length - 1) {
                            sb.append(TAB_CHARACTER);
                        } else {
                            sb.append(LINE_TERMINATOR);
                        }
                    }
                    bw.write(sb.toString());
                    eachLine = br.readLine();
                }

                bw.flush();
                bw.close();
                br.close();

                inFile.delete();
                outFile.renameTo(inFile);
            }
        }

    }
}
