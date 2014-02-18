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

import java.io.BufferedOutputStream;
import org.ihtsdo.helper.rf2.UuidUuidRemapper;
import org.ihtsdo.helper.rf2.UuidUuidRecord;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.tk.uuid.UuidT3Generator;

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
    private final String idSubDir = "";
    /**
     * Input Directory.<br> The directory array parameter supported extensions via separate
     * directories in the array.
     *
     * @parameter
     */
    private final String idInputDir = "";
    /**
     * Input Directories Array.<br> The directory array parameter supported extensions via separate
     * directories in the array.
     *
     * @parameter
     */
    private String remapSubDir;
    /**
     * Input Directories Array.<br> The directory array parameter supported extensions via separate
     * directories in the array.
     *
     * @parameter
     */
    private String[] remapArfDirs;
    /**
     * Directory used for intermediate serialized sct/uuid mapping cache
     *
     * @parameter
     */
    private final String idCacheDir = "";

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
            UuidUuidRemapper idLookup = new UuidUuidRemapper(idCacheFName);
            // idLookup.setupReverseLookup();
            
            if (remapArfDirs != null) {
                RemapAllFiles(wDir, remapSubDir, remapArfDirs, idLookup);
            }
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
            parseToUuidRemapCacheFile(filesIn, idCacheFName);

        } catch (Exception ex) {
            Logger.getLogger(Rf2IdUuidRemapArfMojo.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    void RemapAllFiles(String tDir, String tSubDir, String[] inDirs, UuidUuidRemapper idLookup)
            throws IOException {
        long startTime = System.currentTimeMillis();
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

    void RemapFile(File inFile, File outFile, UuidUuidRemapper uuidUuidRemapper)
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

    /**
     *
     * @param fList
     * @param parseToUuidRemapCacheFile
     * @throws Exception
     */
    static void parseToUuidRemapCacheFile(List<Rf2File> fList, String idCacheOutputPathFnameStr)
            throws Exception {
        // SNOMED CT UUID scheme
        long sctUuidSchemeIdL = Long.parseLong("900000000000002006");
        long countNonActiveL;
        long countNonComputedIdsL;
        long nonParsableLinesL;
        long totalParsedLinesL;
        Set<Long> idSchemeSet = new HashSet<>();
        Set<Long> dateTimeSet = new HashSet<>();
        Set<Long> moduleIdSet = new HashSet<>();
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(
                new FileOutputStream(idCacheOutputPathFnameStr)))) {
            int IDENTIFIER_SCHEME_ID = 0;
            int ALTERNATE_IDENTIFIER = 1;
            int EFFECTIVE_TIME = 2;
            int ACTIVE = 3;
            int MODULE_ID = 4;
            int REFERENCED_COMPONENT_ID = 5;
            for (Rf2File f : fList) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                        new FileInputStream(f.file), "UTF-8"));
                br.readLine();
                countNonActiveL = 0;
                countNonComputedIdsL = 0;
                totalParsedLinesL = 0;
                nonParsableLinesL = 0;
                while (br.ready()) {
                    String tempLine = br.readLine();
                    String[] line = tempLine.split(TAB_CHARACTER);
                    if (line.length < REFERENCED_COMPONENT_ID + 1) {
                        System.err.println("not parsed: " + tempLine);
                        nonParsableLinesL++;
                        continue;
                    } else {
                        totalParsedLinesL++;
                    }
                    // IDENTIFIER_SCHEME_ID
                    long identifierScheme = Long.parseLong(line[IDENTIFIER_SCHEME_ID]);
                    if (identifierScheme != sctUuidSchemeIdL) {
                        idSchemeSet.add(identifierScheme);
                        continue;
                    }
                    // ACTIVE
                    int activeData = Integer.parseInt(line[ACTIVE]);
                    if (activeData != 1) {
                        countNonActiveL++;
                    }
                    // EFFECTIVE_TIME
                    String eTimeStr = Rf2x.convertEffectiveTimeToDate(line[EFFECTIVE_TIME]);
                    long eTime = Rf2x.convertDateToTime(eTimeStr);
                    dateTimeSet.add(eTime);

                    // MODULE_ID
                    Long moduleIdL = Long.parseLong(line[MODULE_ID]);
                    moduleIdSet.add(moduleIdL);

                    // ALTERNATE_IDENTIFIER and REFERENCED_COMPONENT_ID
                    // Assigned uuid
                    UUID aUuid = UUID.fromString(line[ALTERNATE_IDENTIFIER]);
                    // Computed uuid
                    long sctIdL = Long.parseLong(line[REFERENCED_COMPONENT_ID]);
                    UUID cUuid = UUID.fromString(
                            UuidT3Generator.fromSNOMED(sctIdL).toString());
                    
                    // UUID cUuid = UUID.fromString(Rf2x.convertSctIdToUuidStr(sctIdL));
                    if (identifierScheme == sctUuidSchemeIdL && aUuid.compareTo(cUuid) != 0) {
                        countNonComputedIdsL++;
                        UuidUuidRecord tempIdCompact = new UuidUuidRecord(
                                cUuid,
                                aUuid);
                        // Write to JBIN file
                        oos.writeUnshared(tempIdCompact);
                    }

                }
                StringBuilder sb = new StringBuilder();
                sb.append("\n::: parseToUuidRemapCacheFile(..) ");
                sb.append("\n::: SNOMED CT UUID Schema (900000000000002006) cached");
                if (idSchemeSet.size() > 0) {
                   sb.append("\n::: Other UUID Schemas (900000000000002006) not cached");
                }
                sb.append("\n::: PARSED & WRITTEN TO UUID ID REMAP CACHE: ");
                sb.append(f.file.toURI().toString());
                if (idSchemeSet.size() > 0) {
                    Long[] idSchemeArray = idSchemeSet.toArray(new Long[0]);
                    for (Long long1 : idSchemeArray) {
                        sb.append("\n::: ID Schema: ");
                        sb.append(long1.toString());
                    }
                } else {
                    sb.append("\n::: SNOMED CT UUID Schema (900000000000002006)");
                }
                sb.append("\n::: countNonActive=");
                sb.append(countNonActiveL);
                sb.append("\n::: countNonComputedIDs=");
                sb.append(countNonComputedIdsL);
                sb.append("\n::: totalParsedLinesL=");
                sb.append(totalParsedLinesL);
                sb.append("\n::: nonParsableLines=");
                sb.append(nonParsableLinesL);
                sb.append("\n::: dateTimeCount=");
                sb.append(dateTimeSet.size());
                Long[] moduleIdArray = moduleIdSet.toArray(new Long[0]);
                for (Long moduleIdLong : moduleIdArray) {
                    sb.append("\n::: ModuleID: ");
                    sb.append(moduleIdLong.toString());
                }
                Long[] dateTimeArray = dateTimeSet.toArray(new Long[0]);
                for (Long dateLong : dateTimeArray) {
                    sb.append("\n:::     ");
                    sb.append(Rf2x.convertTimeToDate(dateLong));
                }
                sb.append("\n::: \n");
                Logger logger = Logger.getLogger(UuidUuidRecord.class.getName());
                logger.info(sb.toString());
            }
            oos.flush();
        }
    }
}
