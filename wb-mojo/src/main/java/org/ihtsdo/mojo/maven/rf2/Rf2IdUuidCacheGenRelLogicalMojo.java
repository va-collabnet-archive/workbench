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

import org.ihtsdo.helper.rf2.UuidUuidRemapper;
import org.ihtsdo.helper.rf2.UuidUuidRecord;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.tk.uuid.UuidT3Generator;

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
            parseToUuidRemapCacheFile(filesIn, idCacheFName);

            // Parse IHTSDO Terminology Identifiers to Sct_CompactId cache file.
            filesIn = Rf2File.getFiles(wDir, inputSubDir, inputSctDir, "Stated", ".txt");
            long startTime = System.currentTimeMillis();

            ArrayList<Sct2_RelLogicalRecord> rels;
            rels = Sct2_RelLogicalRecord.parseRelationships(filesIn);

            Sct2_RelLogicalRecord.checkRelSctIdTimeErrors(rels);
            Sct2_RelLogicalRecord.checkRelGroupTime(rels);
            Sct2_RelLogicalRecord[] relArray = rels.toArray(new Sct2_RelLogicalRecord[]{});

            // create logical-rel-uuid to sctid-computed-uuid list
            ArrayList<UuidUuidRecord> uuidUuidList;
            uuidUuidList = SctRelLogicalUuidComputer.createSctUuidToLogicalUuidList(relArray);
            // look up is sct uuid is computed from sctid or assigned
            UuidUuidRemapper idLookup = new UuidUuidRemapper(idCacheFName);

            String idRelLogicalCacheFName = cachePath + "uuidRemapRelLogicalCache.ser";
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(
                    new FileOutputStream(idRelLogicalCacheFName)))) {
                for (UuidUuidRecord relIdRec : uuidUuidList) {
                    // remember "uuidDeclared" is from rel sctid
                    UUID uuid = idLookup.getUuid(relIdRec.uuidDeclared); //
                    if (uuid != null) {
                        // then uuid is assigned and not computed from sctid
                        relIdRec.uuidDeclared = uuid; // swap for uuid assigned from snomed
                    }
                    oos.writeUnshared(relIdRec);
                }
                UUID[] c = idLookup.uuidComputedArray;
                UUID[] d = idLookup.uuidDeclaredArray;
                for (int i = 0; i < c.length; i++) {
                    oos.writeUnshared(new UuidUuidRecord(c[i], d[i]));
                }
            }

            System.out.println((System.currentTimeMillis() - startTime) + " mS");
        } catch (Exception ex) {
            getLog().error(ex);
            throw new MojoFailureException("Rf2IdUuidCacheGenRelLogicalMojo: ", ex);
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
                    if (aUuid.compareTo(cUuid) != 0) {
                        countNonComputedIdsL++;
                    }

                    UuidUuidRecord tempIdCompact = new UuidUuidRecord(
                            cUuid,
                            aUuid);
                    // Write to JBIN file
                    oos.writeUnshared(tempIdCompact);
                }
                StringBuilder sb = new StringBuilder();
                sb.append("\n::: parseToUuidRemapCacheFile(..) ");
                sb.append("\n::: PARSED & WRITTEN TO ID CACHE: ");
                sb.append(f.file.toURI().toString());
                if (idSchemeSet.size() > 0) {
                    Long[] idSchemeArray = idSchemeSet.toArray(new Long[0]);
                    for (Long long1 : idSchemeArray) {
                        sb.append("\n::: WARNING unsupported id scheme: ");
                        sb.append(long1.toString());
                    }
                } else {
                    sb.append("\n::: Schema OK (900000000000002006)");
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
