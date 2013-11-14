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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.etypes.EIdentifierUuid;
import org.ihtsdo.helper.rf2.UuidUuidRecord;
import org.ihtsdo.helper.rf2.UuidUuidRemapper;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierUuid;
import org.ihtsdo.tk.uuid.UuidT3Generator;

/**
 *
 * @goal add-legacy-uuids
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class AddLegacyUuidsMojo extends AbstractMojo {

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
    private final String generatedSourcesDir = "generated-sources";
    /**
     * Input Directory.<br> The directory array parameter supported extensions
     * via separate directories in the array.
     *
     * @parameter
     */
    private final String snomedIdInputDir = "full";
    /**
     * Directory used for intermediate serialized sct/uuid mapping cache
     *
     * @parameter
     */
    private final String idCacheDir = "id-cache";
    /**
     * effective time
     *
     * @parameter
     */
    String effectiveTime = "2002-01-31 00:00:01";
    long effectiveTimeL;
    /**
     * authorUuid defaults to user
     *
     * @parameter
     */
    private final String authorUuid = "f7495b58-6630-3499-a44e-2052b5fcf06c";
    private UUID aUuid;
    /**
     * defaults to Module (core metadata concept)
     *
     * @parameter
     * @required
     */
    private final String moduleUuid = "40d1c869-b509-32f8-b735-836eac577a67";
    private UUID mUuid;
    /**
     * defaults to SNOMED Core
     *
     * @parameter
     * @required
     */
    private final String pathUuid = "8c230474-9f11-30ce-9cad-185a96fd03a2";
    private UUID pUuid;
    /**
     * defaults to SNOMED Core
     *
     * @parameter
     */
    String addUuidFromDir = "pre";
    /**
     * defaults to SNOMED Core
     *
     * @parameter
     */
    String addUuidFromFileName = "eConcepts.jbin";
    /**
     * defaults to SNOMED Core
     *
     * @parameter
     */
    String addUuidToDir = "pre";
    /**
     * defaults to SNOMED Core
     *
     * @parameter
     */
    String addUuidToFileName = "eConceptsWithLegacyUuids.jbin";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.aUuid = UUID.fromString(authorUuid);
        this.mUuid = UUID.fromString(moduleUuid);
        this.pUuid = UUID.fromString(pathUuid);
        getLog().info("AddLegacyUuidsMojo : adds legacy computed uuids ");
        // SHOW DIRECTORIES
        String wDir = targetDirectory.getAbsolutePath();
        getLog().info("  POM       Target Directory:           "
                + targetDirectory.getAbsolutePath());
        getLog().info("  POM generated-source Directory:       "
                + generatedSourcesDir);
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
            idLookup.setupReverseLookup();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            effectiveTimeL = formatter.parse(effectiveTime).getTime();

            addLegacyUuids(idLookup);

        } catch (IOException ex) {
            throw new MojoExecutionException("AddLegacyUuidsMojo: IOException", ex);
        } catch (ClassNotFoundException ex) {
            throw new MojoExecutionException("AddLegacyUuidsMojo: ClassNoteFound", ex);
        } catch (ParseException ex) {
            throw new MojoExecutionException("AddLegacyUuidsMojo: failed date parse", ex);
        }

    }

    void CreateUuidRemapCache(String wDir, String idCacheFName) {
        List<Rf2File> filesIn;
        getLog().info("::: BEGIN Rf2IdUuidRemapArfMojo");
        try {

            // Parse IHTSDO Terminology Identifiers to Sct_CompactId cache file.
            filesIn = Rf2File.getFiles(wDir, generatedSourcesDir, snomedIdInputDir,
                    "_Identifier_", ".txt");
            parseToUuidRemapCacheFile(filesIn, idCacheFName);

        } catch (Exception ex) {
            getLog().error("AddLegacyUuidsMojo :: CreateUuidRemapCache", ex);
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
                                UuidUuidRecord tempIdCompact = new UuidUuidRecord(
                                        cUuid,
                                        aUuid);
                                // Write to JBIN file
                                oos.writeUnshared(tempIdCompact);
                            }

                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("\n::: parseToUuidRemapCacheFile(..) ");
                        sb.append("\n::: PARSED & WRITTEN TO UUID ID REMAP CACHE: ");
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
                        Logger logger = Logger.getLogger(AddLegacyUuidsMojo.class.getName());
                        logger.info(sb.toString());
                    }
                    oos.flush();
                }
    } // parseToUuidRemapCacheFile

    private void addLegacyUuids(UuidUuidRemapper idLookup)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        String fromPath = targetDirectory + FILE_SEPARATOR
                + addUuidFromDir + FILE_SEPARATOR;
        String fromFileName = fromPath + addUuidFromFileName;
        if ((new File(fromPath)).mkdirs()) {
            getLog().info("::: eConcept from path : " + fromPath);
        }
        getLog().info("::: eConcept From File : " + fromFileName);

        String toPath = targetDirectory + FILE_SEPARATOR
                + addUuidToDir + FILE_SEPARATOR;
        String toFileName = toPath + addUuidToFileName;
        if ((new File(toPath)).mkdirs()) {
            getLog().info("::: eConcept to path : " + toPath);
        }
        getLog().info("::: eConcept To File : " + toFileName);

        FileInputStream fis = new FileInputStream(fromFileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        FileOutputStream fos = new FileOutputStream(toFileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DataOutputStream dos = new DataOutputStream(bos);

        while (true) {
            TkConcept tkConcept;
            try {
                tkConcept = new TkConcept(dis);
            } catch (EOFException e) {
                dis.close();
                dos.flush();
                dos.close();
                getLog().info("::: AddLegacyUuids: output file closed");
                break;
            }
            addLegacyUuidsToEConcept(tkConcept, idLookup);
            tkConcept.writeExternal(dos);
        }

    }

    /*
     * if primordial_uuid had remap_uuid
     * then check for remap_uuid in additional_uuids
     * if remap_uuid not in additional_uuids
     * then add remap_uuid to additional_uuids 
     * ... allocate additional_uuids if needed
     */

    private TkConcept addLegacyUuidsToEConcept(TkConcept tkConcept,
            UuidUuidRemapper idLookup) {
        // Concept Attributes
        TkConceptAttributes attribs = tkConcept.getConceptAttributes();
        if (attribs != null) {
            attribs.additionalIds = processIdList(tkConcept.primordialUuid,
                    attribs.additionalIds, idLookup);
        } else {
            getLog().info("AddLegacyUuidsMojo: warning concept without attributes: "
                    + tkConcept.primordialUuid.toString());
        }

        // Descriptions
        List<TkDescription> descriptionList = tkConcept.getDescriptions();
        if (descriptionList != null) {
            for (TkDescription tkd : descriptionList) {
                tkd.additionalIds = processIdList(tkd.primordialUuid, tkd.additionalIds, idLookup);
            }
        }

        // Relationships
//        List<TkRelationship> relationshipList = tkConcept.getRelationships();
//        if (relationshipList != null) {
//            for (TkRelationship tkr : relationshipList) {
//                processIdList(tkConcept.primordialUuid, tkr.additionalIds, idLookup);
//            }
//        }

        // Refset Members
//        List<TkRefexAbstractMember<?>> memberList = tkConcept.getRefsetMembers();
//        if (memberList != null) {
//            for (TkRefexAbstractMember<?> tkram : memberList) {
//                processIdList(tkConcept.primordialUuid, tkram.additionalIds, idLookup);
//            }
//        }

        return tkConcept;
    }

    private EIdentifierUuid createNewEIdentifierUuid(UUID uuid) {
        EIdentifierUuid tmpEIdentifierUuid = new EIdentifierUuid();
        tmpEIdentifierUuid.denotation = uuid;
        tmpEIdentifierUuid.authorityUuid = TkIdentifierUuid.generatedUuid;
        tmpEIdentifierUuid.statusUuid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0];
        tmpEIdentifierUuid.time = effectiveTimeL;
        tmpEIdentifierUuid.authorUuid = aUuid;
        tmpEIdentifierUuid.moduleUuid = this.mUuid;
        tmpEIdentifierUuid.pathUuid = this.pUuid;
        return tmpEIdentifierUuid;
    }

    private List<TkIdentifier> processIdList(UUID primordialUuid,
            List<TkIdentifier> additionalIds,
            UuidUuidRemapper idLookup) {
        UUID remapUuid = idLookup.getComputedUuid(primordialUuid);
        if (remapUuid != null) {
            EIdentifierUuid tmpId = createNewEIdentifierUuid(remapUuid);
            if (additionalIds == null) {
                ArrayList<TkIdentifier> tmpAdditionalIds = new ArrayList<>();
                tmpAdditionalIds.add(tmpId);
                return tmpAdditionalIds;
            } else {
                Boolean foundInList = false;
                for (TkIdentifier tkIdentifier : additionalIds) {
                    if (tkIdentifier.authorityUuid.compareTo(TkIdentifierUuid.generatedUuid) == 0) {
                        UUID denotation = ((TkIdentifierUuid)tkIdentifier).denotation;
                        if (denotation.compareTo(remapUuid) == 0) {
                            foundInList = true;
                        }
                    }
                }
                if (!foundInList) {
                    additionalIds.add(tmpId);
                }
                return additionalIds;
            }
        }
        return additionalIds;
    }

}
