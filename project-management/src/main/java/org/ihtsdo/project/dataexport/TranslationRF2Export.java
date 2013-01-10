/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.project.dataexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.refset.members.RefsetUtilImpl;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidSetBI;
//import org.ihtsdo.rf2.constant.I_Constants;
//import org.ihtsdo.rf2.identifier.factory.RF2IdListGeneratorFactory;
//import org.ihtsdo.rf2.identifier.mojo.Key;
//import org.ihtsdo.rf2.identifier.mojo.RF2IdentifierFile;
//import org.ihtsdo.rf2.identifier.mojo.SctIDParam;
//import org.ihtsdo.rf2.util.Config;
//import org.ihtsdo.rf2.util.ExportUtil;
//import org.ihtsdo.rf2.util.JAXBUtil;
//import org.ihtsdo.rf2.util.WriteUtil;

/**
 * The Class TranslationRF2Export.
 */
public class TranslationRF2Export extends RF2DataExport {

    /**
     * The language refset.
     */
    private I_GetConceptData languageRefset;
    /**
     * The source refset.
     */
    private I_GetConceptData sourceRefset;
    /**
     * The formatter.
     */
    private SimpleDateFormat formatter;
    /**
     * The CONCEP t_ retired.
     */
    private int CONCEPT_RETIRED;
    /**
     * The LIMITED.
     */
    private int LIMITED;
    /**
     * The FSN.
     */
    private int FSN;
    /**
     * The PREFERRED.
     */
    private int PREFERRED;
    /**
     * The snomed root.
     */
    private I_GetConceptData snomedRoot;
    /**
     * The report file writer.
     */
    private BufferedWriter reportFileWriter;
    /**
     * The desc file writer.
     */
    private BufferedWriter descFileWriter;
    /**
     * The lang file writer.
     */
    private BufferedWriter langFileWriter;
    /**
     * The beg end.
     */
    private String begEnd;
    /**
     * The sep.
     */
    private String sep;
    /**
     * The snomed int id.
     */
    private Object snomedIntId;
    /**
     * The r util.
     */
    private RefsetUtilImpl rUtil;
    /**
     * The module concept.
     */
    private I_GetConceptData moduleConcept;
    /**
     * The exp folder.
     */
    private String expFolder;
    /**
     * The allowed dest rel types.
     */
    private I_IntSet allowedDestRelTypes;
    /**
     * The active value.
     */
    private I_GetConceptData activeValue;
    /**
     * The inactive value.
     */
    private I_GetConceptData inactiveValue;
    /**
     * The all snomed status.
     */
    private NidSetBI allSnomedStatus;
    /**
     * The module sctid.
     */
    private String moduleSCTID;
    /**
     * The bwi.
     */
    private BufferedWriter bwi;
    /**
     * The bwl.
     */
    private BufferedWriter bwl;
    /**
     * The bwd.
     */
    private BufferedWriter bwd;
    /**
     * The refset sctid.
     */
    private String refsetSCTID;
    /**
     * The Idgenerate.
     */
    private boolean Idgenerate;
    /**
     * The b create refset concept.
     */
    private boolean bCreateRefsetConcept;
    /**
     * The b create module concept.
     */
    private boolean bCreateModuleConcept;
    /**
     * The previous r f2 folder.
     */
    private String previousRF2Folder;
    /**
     * The concept tmp file.
     */
    private File conceptTmpFile;
    /**
     * The description tmp file.
     */
    private File descriptionTmpFile;
    /**
     * The relationship tmp file.
     */
    private File relationshipTmpFile;
    /**
     * The stated relationship tmp file.
     */
    private File statedRelationshipTmpFile;
    /**
     * The language tmp file.
     */
    private File languageTmpFile;
    /**
     * The inact desc tmp file.
     */
    private File inactDescTmpFile;
    /**
     * The exp folder file.
     */
    private File expFolderFile;
    /**
     * The previous release date.
     */
    private String previousReleaseDate;
    /**
     * The endpoint url.
     */
    private String endpointURL;
    /**
     * The password.
     */
    private String password;
    /**
     * The username.
     */
    private String username;
    /**
     * The nsp nr.
     */
    private Integer nspNr;
    /**
     * The hash id map.
     */
    private HashMap<String, File> hashIdMap;
    /**
     * The id map path.
     */
    private String idMapPath;
    /**
     * The complete fsn not translated.
     */
    private boolean completeFSNNotTranslated;
    /**
     * The tgt lang code.
     */
    private String tgtLangCode;
    /**
     * The report file.
     */
    private File reportFile;
    private String countryNamespace;

    /**
     * Instantiates a new translation r f2 export.
     *
     * @param releaseConfig the release config
     * @param moduleConcept the module concept
     * @param nspNr the nsp nr
     * @param expFolder the exp folder
     * @param reportFile the report file
     * @param languageRefset the language refset
     * @param releaseDate the release date
     * @param sourceRefset the source refset
     * @param completeFSNNotTranslated the complete fsn not translated
     * @param Idgenerate the idgenerate
     * @param previousRF2Folder the previous r f2 folder
     * @param previousReleaseDate the previous release date
     * @param endpointURL the endpoint url
     * @param password the password
     * @param username the username
     * @throws Exception the exception
     */
    public TranslationRF2Export(I_ConfigAceFrame releaseConfig,
            I_GetConceptData moduleConcept, Integer nspNr, String expFolder,
            File reportFile, I_GetConceptData languageRefset,
            String releaseDate, I_GetConceptData sourceRefset,
            boolean completeFSNNotTranslated, boolean Idgenerate,
            String previousRF2Folder, String previousReleaseDate,
            String endpointURL, String password, String username) throws Exception {

//
        super(releaseConfig, releaseDate, Terms.get().getActiveAceFrameConfig());
//
//        this.releaseConfig = releaseConfig;
//        this.sourceRefset = sourceRefset;
//        this.moduleConcept = moduleConcept;
//        this.expFolder = expFolder;
//        this.Idgenerate = Idgenerate;
//        this.nspNr = nspNr;
//        if (nspNr == 0) {
//            countryNamespace = "INT";
//        } else {
//            countryNamespace = String.valueOf(nspNr);
//        }
//        this.reportFile = reportFile;
//        this.previousRF2Folder = previousRF2Folder;
//        this.previousReleaseDate = previousReleaseDate;
//        this.baseConfig = Terms.get().getActiveAceFrameConfig();
//        this.endpointURL = endpointURL;
//        this.username = username;
//        this.password = password;
//        this.completeFSNNotTranslated = completeFSNNotTranslated;
//        //		Terms.get().setActiveAceFrameConfig(releaseConfig);
//        formatter = new SimpleDateFormat("yyyyMMdd");
//        this.languageRefset = languageRefset;
//        hashIdMap = new HashMap<String, File>();
//
//        CONCEPT_RETIRED = SnomedMetadataRf2.CONCEPT_NON_CURRENT_RF2.getLenient().getNid();
//        LIMITED = SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getNid();
//        FSN = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid();
//        //TODO change logic for detect preferred from language refset
//        PREFERRED = SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid();
//        snomedIntId = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());
//
//        snomedRoot = Terms.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
//
//        allowedDestRelTypes = Terms.get().newIntSet();
//        allowedDestRelTypes.add(Terms.get().uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
//        activeValue = Terms.get().getConcept(UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f"));
//        inactiveValue = Terms.get().getConcept(UUID.fromString("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"));
//
//        allSnomedStatus = getSnomedStatuses();
//        //			String exportDescFile=	expFolder.trim() + File.separator + "sct2_Description_Ful"  + releaseDate + ".txt" ;
//        //			String exportLangFile=expFolder.trim() + File.separator + "der1_SubsetMembers_"  + releaseDate + ".txt" ;
//
//        reportFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile), "UTF8"));
//        //			descFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportDescFile),"UTF8"));
//        //			langFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportLangFile),"UTF8"));
//
//        LanguageMembershipRefset tgtLangRefset = new LanguageMembershipRefset(languageRefset, baseConfig);
//        tgtLangCode = tgtLangRefset.getLangCode(baseConfig);
//        Set<? extends I_GetConceptData> promRefsets = Terms.get().getRefsetHelper(releaseConfig).getPromotionRefsetForRefset(languageRefset, baseConfig);
//        I_GetConceptData promoRefset = promRefsets.iterator().next();
//        if (promoRefset == null) {
//            reportFileWriter.append("The promotion refset concept for target language refset " + languageRefset + " doesn't exists." + "\r\n");
//            throw new Exception("The promotion refset concept for target language refset " + languageRefset + " doesn't exists.");
//        } else {
//
//            //refset id and module id verify
//
//            rUtil = new RefsetUtilImpl();
//            bCreateRefsetConcept = false;
//            refsetSCTID = rUtil.getSnomedId(languageRefset.getNid(), Terms.get()).toString();
//            if (refsetSCTID == null) {
//                bCreateRefsetConcept = true;
//            } else {
//                try {
//                    Long.parseLong(refsetSCTID);
//                } catch (NumberFormatException e) {
//                    bCreateRefsetConcept = true;
//                }
//            }
//
//            bCreateModuleConcept = false;
//            moduleSCTID = rUtil.getSnomedId(moduleConcept.getNid(), Terms.get()).toString();
//            if (moduleSCTID == null) {
//                bCreateModuleConcept = true;
//            } else {
//                try {
//                    Long.parseLong(moduleSCTID);
//                } catch (NumberFormatException e) {
//                    bCreateModuleConcept = true;
//                }
//            }
//            expFolderFile = new File(expFolder);
//            if (!expFolderFile.exists()) {
//                expFolderFile.mkdirs();
//            }
//
//            idMapPath = expFolderFile.getAbsolutePath() + "/idMap";
//
//            // create concepts if need
//            String PartitionID = null;
//            if (nspNr == 0) {
//                PartitionID = "00";
//            } else {
//                PartitionID = "10";
//            }
//
//            IdAssignmentBI idAssignment = null;
//            if (bCreateRefsetConcept) {
//                if (Idgenerate) {
//                    //"http://mgr.servers.aceworkspace.net:50040/axis2/services/id_generator"
//                    idAssignment = new IdAssignmentImpl(endpointURL, username, password);
//
//                    Long newSctId = idAssignment.createSCTID(languageRefset.getUids().iterator().next(), nspNr, PartitionID, releaseDate, releaseDate, "");
//                    System.out.println("New refset SCTID: " + newSctId);
//
//                    refsetSCTID = newSctId.toString();
//
//                } else {
//                    refsetSCTID = languageRefset.getUids().iterator().next().toString();
//                }
//            }
//
//            if (bCreateModuleConcept) {
//                if (Idgenerate) {
//                    if (idAssignment == null) {
//                        idAssignment = new IdAssignmentImpl(endpointURL, username, password);
//                    }
//
//                    Long newSctId = idAssignment.createSCTID(moduleConcept.getUids().iterator().next(), nspNr, PartitionID, releaseDate, releaseDate, "");
//                    System.out.println("New module SCTID: " + newSctId);
//
//                    moduleSCTID = newSctId.toString();
//                } else {
//                    moduleSCTID = moduleConcept.getUids().iterator().next().toString();
//                }
//            }
//            //open file and write headers
//
//            File tmpFolder = new File(expFolderFile, "tmp");
//            tmpFolder.mkdir();
//
//            conceptTmpFile = null;
//            descriptionTmpFile = null;
//            relationshipTmpFile = null;
//            statedRelationshipTmpFile = null;
//            languageTmpFile = null;
//            inactDescTmpFile = null;
//            BufferedWriter bwc = null;
//            bwd = null;
//            BufferedWriter bwr = null;
//            bwl = null;
//            bwi = null;
//            BufferedWriter bws = null;
//
//            descriptionTmpFile = File.createTempFile("des", ".txt", tmpFolder);
//            bwd = WriteUtil.createWriter(descriptionTmpFile.getAbsolutePath());
//            writeHeader(bwd, getDescriptionHeader());
//
//            if (bCreateRefsetConcept || bCreateModuleConcept) {
//                conceptTmpFile = File.createTempFile("con", ".txt", tmpFolder);
//                bwc = WriteUtil.createWriter(conceptTmpFile.getAbsolutePath());
//                writeHeader(bwc, getConceptHeader());
//
//
//                relationshipTmpFile = File.createTempFile("rel", ".txt", tmpFolder);
//                bwr = WriteUtil.createWriter(relationshipTmpFile.getAbsolutePath());
//                writeHeader(bwr, getRelationshipHeader());
//
//
//                statedRelationshipTmpFile = File.createTempFile("statedrel", ".txt", tmpFolder);
//                bws = WriteUtil.createWriter(statedRelationshipTmpFile.getAbsolutePath());
//                writeHeader(bws, getRelationshipHeader());
//            }
//
//            languageTmpFile = File.createTempFile("lan", ".txt", tmpFolder);
//            bwl = WriteUtil.createWriter(languageTmpFile.getAbsolutePath());
//            writeHeader(bwl, getLanguageHeader());
//
//
//            inactDescTmpFile = File.createTempFile("inac", ".txt", tmpFolder);
//            bwi = WriteUtil.createWriter(inactDescTmpFile.getAbsolutePath());
//            writeHeader(bwi, getInactdescriptionHeader());
//
//            //write raw export process
//            if (bCreateRefsetConcept) {
//                exportConcept(languageRefset, refsetSCTID, moduleSCTID, bwc);
//                HashMap<I_DescriptionTuple, RefexChronicleBI> descs = getDescriptions(languageRefset, languageRefset.getNid());
//                if (descs.size() == 0) {
//
//                    reportFileWriter.append("The  refset concept " + languageRefset.toUserString() + " has not descriptions on language refset.\r\n");
//
//                } else {
//                    boolean FSNExists = false;
//                    for (I_DescriptionTuple description : descs.keySet()) {
//                        exportDescription(description, languageRefset,
//                                refsetSCTID, moduleSCTID, tgtLangCode, bwd, reportFileWriter);
//
//                        if (description.getTypeNid() == FSN) {
//                            FSNExists = true;
//                        }
//                        I_ExtendByRef extension = Terms.get().getExtension(descs.get(description).getNid());
//                        I_ExtendByRefPartCid lastPart = getLastLangExtensionPart(extension);
//
//                        exportLanguage(lastPart, description, languageRefset, tgtLangCode, moduleSCTID, refsetSCTID, bwl, reportFileWriter);
//                    }
//                    if (!FSNExists && completeFSNNotTranslated) {
//                        //description will be exported with its decriptionid from source and moduleid from extension g
//                        descs = getDescriptions(languageRefset, sourceRefset.getNid());
//                        if (descs.size() > 0) {
//                            for (I_DescriptionTuple desc : descs.keySet()) {
//                                if (desc.getTypeNid() == FSN && desc.getStatusNid() == activeValue.getNid()) {
//
//                                    exportDescription(desc, languageRefset, refsetSCTID, moduleSCTID, tgtLangCode, bwd, reportFileWriter);
//
//                                    I_ExtendByRef extension = Terms.get().getExtension(descs.get(desc).getNid());
//                                    I_ExtendByRefPartCid lastPart = getLastLangExtensionPart(extension);
//
//                                    exportLanguage(lastPart, desc, languageRefset, tgtLangCode, moduleSCTID, refsetSCTID, bwl, reportFileWriter);
//                                }
//                            }
//                        }
//                    }
//                }
//                if (exportStatedRelationship(languageRefset, refsetSCTID, moduleSCTID, bws)) {
//                    if (!exportRelationship(languageRefset, refsetSCTID, moduleSCTID, bwr)) {
//                        reportFileWriter.append("The refset concept " + languageRefset.toUserString() + " has not inferred relationships.\r\n");
//                    }
//                } else {
//                    reportFileWriter.append("The refset concept " + languageRefset.toUserString() + " has not stated relationships.\r\n");
//                }
//
//            }
//            if (bCreateModuleConcept) {
//                exportConcept(moduleConcept, moduleSCTID, moduleSCTID, bwc);
//                HashMap<I_DescriptionTuple, RefexChronicleBI> descs = getDescriptions(moduleConcept, languageRefset.getNid());
//                if (descs.size() == 0) {
//                    reportFileWriter.append("The  module concept " + moduleConcept.toUserString() + " has not descriptions on language refset.\r\n");
//
//                } else {
//                    boolean FSNExists = false;
//                    for (I_DescriptionTuple description : descs.keySet()) {
//                        exportDescription(description, moduleConcept,
//                                moduleSCTID, moduleSCTID, tgtLangCode, bwd, reportFileWriter);
//
//                        if (description.getTypeNid() == FSN) {
//                            FSNExists = true;
//                        }
//                        I_ExtendByRef extension = Terms.get().getExtension(descs.get(description).getNid());
//                        I_ExtendByRefPartCid lastPart = getLastLangExtensionPart(extension);
//
//                        exportLanguage(lastPart, description, moduleConcept, tgtLangCode, moduleSCTID, refsetSCTID, bwl, reportFileWriter);
//                    }
//                    if (!FSNExists && completeFSNNotTranslated) {
//                        //description will be exported with its decriptionid from source and moduleid from extension g
//                        descs = getDescriptions(moduleConcept, sourceRefset.getNid());
//                        if (descs.size() > 0) {
//                            for (I_DescriptionTuple desc : descs.keySet()) {
//                                if (desc.getTypeNid() == FSN && desc.getStatusNid() == activeValue.getNid()) {
//
//                                    exportDescription(desc, moduleConcept, refsetSCTID, moduleSCTID, tgtLangCode, bwd, reportFileWriter);
//
//                                    I_ExtendByRef extension = Terms.get().getExtension(descs.get(desc).getNid());
//                                    I_ExtendByRefPartCid lastPart = getLastLangExtensionPart(extension);
//
//                                    exportLanguage(lastPart, desc, moduleConcept, tgtLangCode, moduleSCTID, refsetSCTID, bwl, reportFileWriter);
//                                }
//                            }
//                        }
//                    }
//                }
//                if (exportStatedRelationship(moduleConcept, moduleSCTID, moduleSCTID, bws)) {
//                    if (!exportRelationship(moduleConcept, moduleSCTID, moduleSCTID, bwr)) {
//                        reportFileWriter.append("The module concept " + moduleConcept.toUserString() + " has not inferred relationships.\r\n");
//                    }
//                } else {
//                    reportFileWriter.append("The module concept " + moduleConcept.toUserString() + " has not stated relationships.\r\n");
//                }
//
//            }
//            if (bCreateRefsetConcept || bCreateModuleConcept) {
//                bwc.close();
//                bwr.close();
//                bws.close();
//                bwc = null;
//                bwr = null;
//                bws = null;
//            }
//            System.gc();


            //write final files


//        }

    }

    /**
     * Gets the descriptions.
     *
     * @param concept the concept
     * @param refsetNid the refset nid
     * @return the descriptions
     * @throws IOException Signals that an I/O exception has occurred.
     */
//    @SuppressWarnings("unchecked")
//    private HashMap<I_DescriptionTuple, RefexChronicleBI> getDescriptions(
//            I_GetConceptData concept, int refsetNid) throws IOException {
//        List<? extends I_DescriptionTuple> descriptions = concept.getDescriptionTuples(allSnomedStatus,
//                allDescTypes, releaseConfig.getViewPositionSetReadOnly(),
//                Precedence.TIME, releaseConfig.getConflictResolutionStrategy());
//
//        HashMap<I_DescriptionTuple, RefexChronicleBI> descTMap = new HashMap<I_DescriptionTuple, RefexChronicleBI>();
//        for (I_DescriptionTuple descT : descriptions) {
//            for (RefexVersionBI desc : descT.getDescVersioned().getAnnotationsActive(releaseConfig.getViewCoordinate())) {
//                if (desc.getRefexNid() == refsetNid) {
//                    descTMap.put(descT, desc);
//                    break;
//                }
//            }
//        }
//        return descTMap;
//    }
//
//    /**
//     * Post export process.
//     *
//     * @param nextStep
//     *
//     * @throws Exception the exception
//     */
//    public void postExportProcess(boolean nextStep) throws Exception {
//
//
//        File outFolder = new File(expFolderFile, "preAssign");
//        if (!outFolder.exists()) {
//            outFolder.mkdirs();
//        }
//        File rf2FullFolder = new File(previousRF2Folder);
//        if (!rf2FullFolder.exists()) {
//            rf2FullFolder.mkdirs();
//        }
//
//        File rf2SnapshotOutputFolder = null;
//        File rf2DeltaOutputFolder = null;
//        File rf2FullOutputFolder = null;
//        Rf2FileProvider fProv = new Rf2FileProvider(tgtLangCode, countryNamespace);
//        if (!nextStep) {
//
//
//            rf2FullOutputFolder = new File(expFolderFile.getAbsolutePath() + "/" + fProv.getFullOutputFolder());
//            if (!rf2FullOutputFolder.exists()) {
//                rf2FullOutputFolder.mkdir();
//            }
//            rf2DeltaOutputFolder = new File(expFolderFile.getAbsolutePath() + "/" + fProv.getDeltaOutputFolder());
//            if (!rf2DeltaOutputFolder.exists()) {
//                rf2DeltaOutputFolder.mkdir();
//            }
//            rf2SnapshotOutputFolder = new File(expFolderFile.getAbsolutePath() + "/" + fProv.getSnapshotOutputFolder());
//            if (!rf2SnapshotOutputFolder.exists()) {
//                rf2SnapshotOutputFolder.mkdir();
//            }
//        }
//        RF2ArtifactPostExportImpl pExp = null;
//        File deltaPrevFile;
//        File fullPrevFile;
//        File snapshotPrevFile;
//        File fullFinalFile;
//        File deltaFinalFile;
//        File snapshotFinalFile;
//        if (bCreateRefsetConcept || bCreateModuleConcept) {
//            pExp = new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_CONCEPT, rf2FullFolder,
//                    conceptTmpFile, outFolder, new File(expFolder),
//                    previousReleaseDate, releaseDate, tgtLangCode, countryNamespace);
//            pExp.postProcess();
//            pExp = null;
//
//            if (!nextStep) {
//                fullPrevFile = fProv.getFullOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_CONCEPT, releaseDate);
//                deltaPrevFile = fProv.getDeltaOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_CONCEPT, releaseDate, previousReleaseDate);
//                snapshotPrevFile = fProv.getSnapshotOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_CONCEPT, releaseDate);
//                fullFinalFile = fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_CONCEPT, releaseDate);
//                deltaFinalFile = fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_CONCEPT, releaseDate, previousReleaseDate);
//                snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_CONCEPT, releaseDate);
//
//
//                fullPrevFile.renameTo(fullFinalFile);
//                deltaPrevFile.renameTo(deltaFinalFile);
//                snapshotPrevFile.renameTo(snapshotFinalFile);
//
//            }
//            System.gc();
//
//            pExp = new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_RELATIONSHIP, rf2FullFolder,
//                    relationshipTmpFile, outFolder, new File(expFolder),
//                    previousReleaseDate, releaseDate, tgtLangCode, countryNamespace);
//            pExp.postProcess();
//            pExp = null;
//
//            if (!nextStep) {
//                fullPrevFile = fProv.getFullOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP, releaseDate);
//                deltaPrevFile = fProv.getDeltaOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP, releaseDate, previousReleaseDate);
//                snapshotPrevFile = fProv.getSnapshotOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP, releaseDate);
//                fullFinalFile = fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP, releaseDate);
//                deltaFinalFile = fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP, releaseDate, previousReleaseDate);
//                snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP, releaseDate);
//
//
//                fullPrevFile.renameTo(fullFinalFile);
//                deltaPrevFile.renameTo(deltaFinalFile);
//                snapshotPrevFile.renameTo(snapshotFinalFile);
//
//            }
//            System.gc();
//
//            pExp = new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_STATED_RELATIONSHIP, rf2FullFolder,
//                    statedRelationshipTmpFile, outFolder, new File(expFolder),
//                    previousReleaseDate, releaseDate, tgtLangCode, countryNamespace);
//            pExp.postProcess();
//            pExp = null;
//
//            if (!nextStep) {
//                fullPrevFile = fProv.getFullOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP, releaseDate);
//                deltaPrevFile = fProv.getDeltaOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP, releaseDate, previousReleaseDate);
//                snapshotPrevFile = fProv.getSnapshotOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP, releaseDate);
//                fullFinalFile = fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP, releaseDate);
//                deltaFinalFile = fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP, releaseDate, previousReleaseDate);
//                snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP, releaseDate);
//
//
//                fullPrevFile.renameTo(fullFinalFile);
//                deltaPrevFile.renameTo(deltaFinalFile);
//                snapshotPrevFile.renameTo(snapshotFinalFile);
//
//            }
//            System.gc();
//        }
//
//        pExp = new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_DESCRIPTION, rf2FullFolder,
//                descriptionTmpFile, outFolder, new File(expFolder),
//                previousReleaseDate, releaseDate, tgtLangCode, countryNamespace);
//        pExp.postProcess();
//        pExp = null;
//
//        if (!nextStep) {
//            fullPrevFile = fProv.getFullOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION, releaseDate);
//            deltaPrevFile = fProv.getDeltaOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION, releaseDate, previousReleaseDate);
//            snapshotPrevFile = fProv.getSnapshotOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION, releaseDate);
//            fullFinalFile = fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION, releaseDate);
//            deltaFinalFile = fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION, releaseDate, previousReleaseDate);
//            snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION, releaseDate);
//
//
//            fullPrevFile.renameTo(fullFinalFile);
//            deltaPrevFile.renameTo(deltaFinalFile);
//            snapshotPrevFile.renameTo(snapshotFinalFile);
//
//        }
//        System.gc();
//
//        pExp = new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_LANGUAGE_REFSET, rf2FullFolder,
//                languageTmpFile, outFolder, new File(expFolder),
//                previousReleaseDate, releaseDate, tgtLangCode, countryNamespace);
//        pExp.postProcess();
//        pExp = null;
//
//        if (!nextStep) {
//            fullPrevFile = fProv.getFullOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET, releaseDate);
//            deltaPrevFile = fProv.getDeltaOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET, releaseDate, previousReleaseDate);
//            snapshotPrevFile = fProv.getSnapshotOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET, releaseDate);
//            fullFinalFile = fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET, releaseDate);
//            deltaFinalFile = fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET, releaseDate, previousReleaseDate);
//            snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET, releaseDate);
//
//
//            fullPrevFile.renameTo(fullFinalFile);
//            deltaPrevFile.renameTo(deltaFinalFile);
//            snapshotPrevFile.renameTo(snapshotFinalFile);
//
//        }
//        System.gc();
//
//        pExp = new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_ATTRIBUTE_VALUE, rf2FullFolder,
//                inactDescTmpFile, outFolder, new File(expFolder),
//                previousReleaseDate, releaseDate, tgtLangCode, countryNamespace);
//        pExp.postProcess();
//        pExp = null;
//        if (!nextStep) {
//            fullPrevFile = fProv.getFullOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_ATTRIBUTE_VALUE, releaseDate);
//            deltaPrevFile = fProv.getDeltaOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_ATTRIBUTE_VALUE, releaseDate, previousReleaseDate);
//            snapshotPrevFile = fProv.getSnapshotOutputFile(outFolder.getAbsolutePath(), FILE_TYPE.RF2_ATTRIBUTE_VALUE, releaseDate);
//            fullFinalFile = fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_ATTRIBUTE_VALUE, releaseDate);
//            deltaFinalFile = fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_ATTRIBUTE_VALUE, releaseDate, previousReleaseDate);
//            snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_ATTRIBUTE_VALUE, releaseDate);
//
//
//            fullPrevFile.renameTo(fullFinalFile);
//            deltaPrevFile.renameTo(deltaFinalFile);
//            snapshotPrevFile.renameTo(snapshotFinalFile);
//
//        }
//
//        System.gc();
//
//    }
//
//    /**
//     * Id assignment process.
//     *
//     * @param idInsert the id insert
//     * @throws Exception the exception
//     */
//    public void idAssignmentProcess(boolean idInsert) throws Exception {
//
//        Rf2FileProvider fProv = new Rf2FileProvider(tgtLangCode, countryNamespace);
//        Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/idGenerator.xml");
//        config.setReleaseDate(releaseDate);
//        config.setFlushCount(10000);
//        config.setInvokeDroolRules("false");
//        config.setFileExtension("txt");
//        config.setUsername(username);
//        config.setPassword(password);
//        config.setEndPoint(endpointURL);
//        config.setDestinationFolder(expFolder);
//        ArrayList<RF2IdentifierFile> rf2Files = new ArrayList<RF2IdentifierFile>();
//
//
//        File preFolder = new File(expFolderFile, "preAssign");
//        if (!preFolder.exists()) {
//            preFolder.mkdirs();
//        }
//        File rf2FullPrevFolder = new File(preFolder.getAbsolutePath() + "/" + fProv.getFullOutputFolder());
//        if (!rf2FullPrevFolder.exists()) {
//            rf2FullPrevFolder.mkdir();
//        }
//        File rf2DeltaPrevFolder = new File(preFolder.getAbsolutePath() + "/" + fProv.getDeltaOutputFolder());
//        if (!rf2DeltaPrevFolder.exists()) {
//            rf2DeltaPrevFolder.mkdir();
//        }
//        File rf2SnapshotPrevFolder = new File(preFolder.getAbsolutePath() + "/" + fProv.getSnapshotOutputFolder());
//        if (!rf2SnapshotPrevFolder.exists()) {
//            rf2SnapshotPrevFolder.mkdir();
//        }
//
//        File rf2FullOutputFolder = new File(expFolderFile.getAbsolutePath() + "/" + fProv.getFullOutputFolder());
//        if (!rf2FullOutputFolder.exists()) {
//            rf2FullOutputFolder.mkdir();
//        }
//        File rf2DeltaOutputFolder = new File(expFolderFile.getAbsolutePath() + "/" + fProv.getDeltaOutputFolder());
//        if (!rf2DeltaOutputFolder.exists()) {
//            rf2DeltaOutputFolder.mkdir();
//        }
//        File rf2SnapshotOutputFolder = new File(expFolderFile.getAbsolutePath() + "/" + fProv.getSnapshotOutputFolder());
//        if (!rf2SnapshotOutputFolder.exists()) {
//            rf2SnapshotOutputFolder.mkdir();
//        }
//
//        File fullPrevFile;
//        File deltaPrevFile;
//        File snapshotPrevFile;
//
//        File fullFinalFile;
//        File deltaFinalFile;
//        File snapshotFinalFile;
//
//        Date et = new Date();
//        String execId = "WB-" + Terms.get().getActiveAceFrameConfig().getDbConfig().getUsername().substring(0, 5) + "-" + ExportUtil.DATEFORMAT.format(et);
//        int etOrd = 1;
//        ArrayList<String> ordin = null;
//
//        String componentType = "";
//        String idSaveTolist = "";
//        String idType = "";
//        String idColumnIndex = "";
//        String idMapFile = "";
//        String partitionId = "";
//        RF2IdentifierFile idFile = null;
//        if (bCreateRefsetConcept || bCreateModuleConcept) {
//
//            fullPrevFile = fProv.getFullOutputFile(rf2FullPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_CONCEPT, releaseDate);
//            deltaPrevFile = fProv.getDeltaOutputFile(rf2DeltaPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_CONCEPT, releaseDate, previousReleaseDate);
//            snapshotPrevFile = fProv.getSnapshotOutputFile(rf2SnapshotPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_CONCEPT, releaseDate);
//
//            fullFinalFile = fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_CONCEPT, releaseDate);
//            deltaFinalFile = fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_CONCEPT, releaseDate, previousReleaseDate);
//            snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_CONCEPT, releaseDate);
//
//            fullPrevFile.renameTo(fullFinalFile);
//            deltaPrevFile.renameTo(deltaFinalFile);
//            snapshotPrevFile.renameTo(snapshotFinalFile);
//
//
//            fullPrevFile = fProv.getFullOutputFile(rf2FullPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP, releaseDate);
//            deltaPrevFile = fProv.getDeltaOutputFile(rf2DeltaPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP, releaseDate, previousReleaseDate);
//            snapshotPrevFile = fProv.getSnapshotOutputFile(rf2SnapshotPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP, releaseDate);
//
//            fullFinalFile = fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP, releaseDate);
//            deltaFinalFile = fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP, releaseDate, previousReleaseDate);
//            snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP, releaseDate);
//
//            ordin = new ArrayList<String>();
//            ordin.add("0");
//            componentType = "Relationship full";
//            idSaveTolist = "true";
//            idType = "RF2_RELATIONSHIP";
//            idColumnIndex = "0";
//            idMapFile = idMapPath + "/Relationships_Uuid_Id.txt";
//
//            hashIdMap.put("RF2_RELATIONSHIP", new File(idMapFile));
//            partitionId = "";
//            if (nspNr == 0) {
//                partitionId = "02";
//            } else {
//                partitionId = "12";
//            }
//
//            idFile = getIdentifierFile(fullPrevFile, fullFinalFile, etOrd, ordin, execId,
//                    componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
//                    partitionId);
//            rf2Files.add(idFile);
//
//
//            ordin = new ArrayList<String>();
//            ordin.add("0");
//            componentType = "Relationship delta";
//            idSaveTolist = "false";
//            idType = "RF2_RELATIONSHIP";
//            idColumnIndex = "-1";
//            idMapFile = "";
//            partitionId = "";
//            if (nspNr == 0) {
//                partitionId = "02";
//            } else {
//                partitionId = "12";
//            }
//
//            idFile = getIdentifierFile(deltaPrevFile, deltaFinalFile, etOrd, ordin, execId,
//                    componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
//                    partitionId);
//
//            rf2Files.add(idFile);
//
//            ordin = new ArrayList<String>();
//            ordin.add("0");
//            componentType = "Relationship snapshot";
//            idSaveTolist = "false";
//            idType = "RF2_RELATIONSHIP";
//            idColumnIndex = "-1";
//            idMapFile = "";
//            partitionId = "";
//            if (nspNr == 0) {
//                partitionId = "02";
//            } else {
//                partitionId = "12";
//            }
//
//            idFile = getIdentifierFile(snapshotPrevFile, snapshotFinalFile, etOrd, ordin, execId,
//                    componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
//                    partitionId);
//
//            rf2Files.add(idFile);
//
//            fullPrevFile = fProv.getFullOutputFile(rf2FullPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP, releaseDate);
//            deltaPrevFile = fProv.getDeltaOutputFile(rf2DeltaPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP, releaseDate, previousReleaseDate);
//            snapshotPrevFile = fProv.getSnapshotOutputFile(rf2SnapshotPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP, releaseDate);
//
//            fullFinalFile = fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP, releaseDate);
//            deltaFinalFile = fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP, releaseDate, previousReleaseDate);
//            snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP, releaseDate);
//
//
//            ordin = new ArrayList<String>();
//            ordin.add("0");
//            componentType = "Stated Relationship full";
//            idSaveTolist = "true";
//            idType = "RF2_STATED_RELATIONSHIP";
//            idColumnIndex = "0";
//            idMapFile = idMapPath + "/StatedRelationships_Uuid_Id.txt";
//
//            hashIdMap.put("RF2_STATED_RELATIONSHIP", new File(idMapFile));
//            partitionId = "";
//            if (nspNr == 0) {
//                partitionId = "02";
//            } else {
//                partitionId = "12";
//            }
//
//            idFile = getIdentifierFile(fullPrevFile, fullFinalFile, etOrd, ordin, execId,
//                    componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
//                    partitionId);
//            rf2Files.add(idFile);
//
//
//            ordin = new ArrayList<String>();
//            ordin.add("0");
//            componentType = "Stated Relationship delta";
//            idSaveTolist = "false";
//            idType = "RF2_STATED_RELATIONSHIP";
//            idColumnIndex = "-1";
//            idMapFile = "";
//            partitionId = "";
//            if (nspNr == 0) {
//                partitionId = "02";
//            } else {
//                partitionId = "12";
//            }
//
//            idFile = getIdentifierFile(deltaPrevFile, deltaFinalFile, etOrd, ordin, execId,
//                    componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
//                    partitionId);
//
//            rf2Files.add(idFile);
//
//            ordin = new ArrayList<String>();
//            ordin.add("0");
//            componentType = "Stated Relationship snapshot";
//            idSaveTolist = "false";
//            idType = "RF2_STATED_RELATIONSHIP";
//            idColumnIndex = "-1";
//            idMapFile = "";
//            partitionId = "";
//            if (nspNr == 0) {
//                partitionId = "02";
//            } else {
//                partitionId = "12";
//            }
//
//            idFile = getIdentifierFile(snapshotPrevFile, snapshotFinalFile, etOrd, ordin, execId,
//                    componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
//                    partitionId);
//
//            rf2Files.add(idFile);
//        }
//        fullPrevFile = fProv.getFullOutputFile(rf2FullPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION, releaseDate);
//        deltaPrevFile = fProv.getDeltaOutputFile(rf2DeltaPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION, releaseDate, previousReleaseDate);
//        snapshotPrevFile = fProv.getSnapshotOutputFile(rf2SnapshotPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION, releaseDate);
//
//        fullFinalFile = fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION, releaseDate);
//        deltaFinalFile = fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION, releaseDate, previousReleaseDate);
//        snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION, releaseDate);
//
//        ordin = new ArrayList<String>();
//        ordin.add("0");
//        componentType = "Description full";
//        idSaveTolist = "true";
//        idType = "RF2_DESCRIPTION";
//        idColumnIndex = "0";
//        idMapFile = idMapPath + "/Descriptions_Uuid_Id.txt";
//        hashIdMap.put("RF2_DESCRIPTION", new File(idMapFile));
//        partitionId = "";
//        if (nspNr == 0) {
//            partitionId = "01";
//        } else {
//            partitionId = "11";
//        }
//
//        idFile = getIdentifierFile(fullPrevFile, fullFinalFile, etOrd, ordin, execId,
//                componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
//                partitionId);
//        rf2Files.add(idFile);
//
//
//        ordin = new ArrayList<String>();
//        ordin.add("0");
//        componentType = "Description delta";
//        idSaveTolist = "false";
//        idType = "RF2_DESCRIPTION";
//        idColumnIndex = "-1";
//        idMapFile = "";
//        partitionId = "";
//        if (nspNr == 0) {
//            partitionId = "01";
//        } else {
//            partitionId = "11";
//        }
//
//        idFile = getIdentifierFile(deltaPrevFile, deltaFinalFile, etOrd, ordin, execId,
//                componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
//                partitionId);
//
//        rf2Files.add(idFile);
//
//        ordin = new ArrayList<String>();
//        ordin.add("0");
//        componentType = "Description snapshot";
//        idSaveTolist = "false";
//        idType = "RF2_DESCRIPTION";
//        idColumnIndex = "-1";
//        idMapFile = "";
//        partitionId = "";
//        if (nspNr == 0) {
//            partitionId = "01";
//        } else {
//            partitionId = "11";
//        }
//
//        idFile = getIdentifierFile(snapshotPrevFile, snapshotFinalFile, etOrd, ordin, execId,
//                componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
//                partitionId);
//
//        rf2Files.add(idFile);
//
//        fullPrevFile = fProv.getFullOutputFile(rf2FullPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET, releaseDate);
//        deltaPrevFile = fProv.getDeltaOutputFile(rf2DeltaPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET, releaseDate, previousReleaseDate);
//        snapshotPrevFile = fProv.getSnapshotOutputFile(rf2SnapshotPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET, releaseDate);
//
//        fullFinalFile = fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET, releaseDate);
//        deltaFinalFile = fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET, releaseDate, previousReleaseDate);
//        snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET, releaseDate);
//
//        ordin = new ArrayList<String>();
//        ordin.add("5");
//        componentType = "Language full";
//        idSaveTolist = "false";
//        idType = "RF2_DESCRIPTION";
//        idColumnIndex = "-1";
//        idMapFile = "";
//        partitionId = "";
//        if (nspNr == 0) {
//            partitionId = "01";
//        } else {
//            partitionId = "11";
//        }
//
//        idFile = getIdentifierFile(fullPrevFile, fullFinalFile, etOrd, ordin, execId,
//                componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
//                partitionId);
//        rf2Files.add(idFile);
//
//
//        ordin = new ArrayList<String>();
//        ordin.add("5");
//        componentType = "Language delta";
//        idSaveTolist = "false";
//        idType = "RF2_DESCRIPTION";
//        idColumnIndex = "-1";
//        idMapFile = "";
//        partitionId = "";
//        if (nspNr == 0) {
//            partitionId = "01";
//        } else {
//            partitionId = "11";
//        }
//
//        idFile = getIdentifierFile(deltaPrevFile, deltaFinalFile, etOrd, ordin, execId,
//                componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
//                partitionId);
//
//        rf2Files.add(idFile);
//
//        ordin = new ArrayList<String>();
//        ordin.add("5");
//        componentType = "Language snapshot";
//        idSaveTolist = "false";
//        idType = "RF2_DESCRIPTION";
//        idColumnIndex = "-1";
//        idMapFile = "";
//        partitionId = "";
//        if (nspNr == 0) {
//            partitionId = "01";
//        } else {
//            partitionId = "11";
//        }
//
//        idFile = getIdentifierFile(snapshotPrevFile, snapshotFinalFile, etOrd, ordin, execId,
//                componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
//                partitionId);
//
//        rf2Files.add(idFile);
//
//
//        config.setRf2Files(rf2Files);
//        //		config.setUpdateWbSctId(updateWbSctId);
//
//        RF2IdListGeneratorFactory factory = new RF2IdListGeneratorFactory(config);
//        factory.export();
//
//        factory = null;
//        System.gc();
//
//        fullPrevFile = fProv.getFullOutputFile(rf2FullPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_ATTRIBUTE_VALUE, releaseDate);
//        deltaPrevFile = fProv.getDeltaOutputFile(rf2DeltaPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_ATTRIBUTE_VALUE, releaseDate, previousReleaseDate);
//        snapshotPrevFile = fProv.getSnapshotOutputFile(rf2SnapshotPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_ATTRIBUTE_VALUE, releaseDate);
//
//        fullFinalFile = fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_ATTRIBUTE_VALUE, releaseDate);
//        deltaFinalFile = fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_ATTRIBUTE_VALUE, releaseDate, previousReleaseDate);
//        snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_ATTRIBUTE_VALUE, releaseDate);
//
//        fullPrevFile.renameTo(fullFinalFile);
//        deltaPrevFile.renameTo(deltaFinalFile);
//        snapshotPrevFile.renameTo(snapshotFinalFile);
//        if (idInsert) {
//            updateWb();
//        }
//    }
//
//    /**
//     * Update wb.
//     *
//     * @throws Exception
//     */
//    private void updateWb() throws Exception {
//
//        try {
//            if (bCreateRefsetConcept) {
//                insertConceptPair(languageRefset.getUids().iterator().next().toString(), refsetSCTID);
//
//                Terms.get().commit();
//            }
//            if (bCreateModuleConcept) {
//                insertConceptPair(moduleConcept.getUids().iterator().next().toString(), moduleSCTID);
//
//                Terms.get().commit();
//            }
//
//        } catch (IOException e) {
//            AceLog.getAppLog().alertAndLogException(e);
//        }
//        for (String key : hashIdMap.keySet()) {
//            File fMap = hashIdMap.get(key);
//            if (fMap.exists()) {
//                if (key.equals("RF2_CONCEPT")) {
//                    insertConceptIds(fMap);
//
//                    Terms.get().commit();
//                    break;
//                }
//
//                if (key.equals("RF2_DESCRIPTION")) {
//                    insertDescriptionIds(fMap);
//
//                    Terms.get().commit();
//                    break;
//                }
//
//                if (key.equals("RF2_RELATIONSHIP")) {
//                    insertRelationshipIds(fMap);
//
//                    Terms.get().commit();
//                    break;
//                }
//
//                if (key.equals("RF2_STATED_RELATIONSHIP")) {
//                    insertRelationshipIds(fMap);
//
//                    Terms.get().commit();
//                    break;
//                }
//            }
//        }
//
//    }
//
//    /**
//     * Insert relationship ids.
//     *
//     * @param fMap the f map
//     */
//    private void insertRelationshipIds(File fMap) {
//        FileInputStream ifis;
//        try {
//            ifis = new FileInputStream(fMap);
//            InputStreamReader iisr = new InputStreamReader(ifis, "UTF-8");
//            BufferedReader ibr = new BufferedReader(iisr);
//            String line;
//            String[] splitLine;
//            UUID uuid;
//            String sctid;
//            while ((line = ibr.readLine()) != null) {
//                splitLine = line.split("\t", -1);
//                uuid = UUID.fromString(splitLine[0]);
//                sctid = splitLine[1];
//
//                int componentNid = Terms.get().uuidToNative(uuid);
//                I_Identify i_Identify = Terms.get().getId(componentNid);
//                I_GetConceptData commitedConcept = Terms.get().getConceptForNid(componentNid);
//                i_Identify.addLongId(Long.parseLong(sctid),
//                        ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid(),
//                        activeValue.getNid(),
//                        Long.MAX_VALUE,
//                        releaseConfig.getDbConfig().getUserConcept().getNid(),
//                        releaseConfig.getEditCoordinate().getModuleNid(),
//                        releaseConfig.getViewPositionSet().iterator().next().getPath().getConceptNid());
//                Terms.get().addUncommittedNoChecks(commitedConcept);
//
//            }
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            AceLog.getAppLog().alertAndLogException(e);
//        } catch (UnsupportedEncodingException e) {
//            // TODO Auto-generated catch block
//            AceLog.getAppLog().alertAndLogException(e);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            AceLog.getAppLog().alertAndLogException(e);
//        } catch (TerminologyException e) {
//            // TODO Auto-generated catch block
//            AceLog.getAppLog().alertAndLogException(e);
//        }
//    }
//
//    /**
//     * Insert description ids.
//     *
//     * @param fMap the f map
//     */
//    private void insertDescriptionIds(File fMap) {
//
//        FileInputStream ifis;
//        try {
//            ifis = new FileInputStream(fMap);
//            InputStreamReader iisr = new InputStreamReader(ifis, "UTF-8");
//            BufferedReader ibr = new BufferedReader(iisr);
//            String line;
//            String[] splitLine;
//            UUID uuid;
//            String sctid;
//            while ((line = ibr.readLine()) != null) {
//                splitLine = line.split("\t", -1);
//                uuid = UUID.fromString(splitLine[0]);
//                sctid = splitLine[1];
//
//                I_Identify i_Identify = Terms.get().getId(uuid);
//                i_Identify.addLongId(Long.parseLong(sctid),
//                        ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid(),
//                        activeValue.getNid(),
//                        Long.MAX_VALUE,
//                        releaseConfig.getDbConfig().getUserConcept().getNid(),
//                        releaseConfig.getEditCoordinate().getModuleNid(),
//                        releaseConfig.getViewPositionSet().iterator().next().getPath().getConceptNid());
//                I_DescriptionVersioned description = Terms.get().getDescription(Terms.get().uuidToNative(uuid));
//                I_GetConceptData concept = Terms.get().getConcept(description.getConceptNid());
//                Terms.get().addUncommittedNoChecks(concept);
//
//            }
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            AceLog.getAppLog().alertAndLogException(e);
//        } catch (UnsupportedEncodingException e) {
//            // TODO Auto-generated catch block
//            AceLog.getAppLog().alertAndLogException(e);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            AceLog.getAppLog().alertAndLogException(e);
//        } catch (TerminologyException e) {
//            // TODO Auto-generated catch block
//            AceLog.getAppLog().alertAndLogException(e);
//        }
//
//    }
//
//    /**
//     * Insert concept ids.
//     *
//     * @param fMap the f map
//     */
//    private void insertConceptIds(File fMap) {
//
//        FileInputStream ifis;
//        try {
//            ifis = new FileInputStream(fMap);
//            InputStreamReader iisr = new InputStreamReader(ifis, "UTF-8");
//            BufferedReader ibr = new BufferedReader(iisr);
//            String line;
//            String[] splitLine;
//            String uuid;
//            String sctid;
//            while ((line = ibr.readLine()) != null) {
//                splitLine = line.split("\t", -1);
//                uuid = splitLine[0];
//                sctid = splitLine[1];
//
//                insertConceptPair(uuid, sctid);
//            }
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            AceLog.getAppLog().alertAndLogException(e);
//        } catch (UnsupportedEncodingException e) {
//            // TODO Auto-generated catch block
//            AceLog.getAppLog().alertAndLogException(e);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            AceLog.getAppLog().alertAndLogException(e);
//        }
//    }
//
//    /**
//     * Insert concept pair.
//     *
//     * @param uuid the uuid
//     * @param sctid the sctid
//     */
//    private void insertConceptPair(String uuid, String sctid) {
//        I_GetConceptData concept;
//        try {
//            concept = Terms.get().getConcept(UUID.fromString(uuid));
//            I_Identify i_Identify = concept.getIdentifier();
//
//            i_Identify.addLongId(Long.parseLong(sctid),
//                    ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid(),
//                    activeValue.getNid(),
//                    Long.MAX_VALUE,
//                    releaseConfig.getDbConfig().getUserConcept().getNid(),
//                    releaseConfig.getEditCoordinate().getModuleNid(),
//                    releaseConfig.getViewPositionSet().iterator().next().getPath().getConceptNid());
//
//            Terms.get().addUncommittedNoChecks(concept);
//        } catch (TerminologyException e) {
//            // TODO Auto-generated catch block
//            AceLog.getAppLog().alertAndLogException(e);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            AceLog.getAppLog().alertAndLogException(e);
//        }
//
//
//    }
//
//    /**
//     * Gets the identifier file.
//     *
//     * @param fullPrevFile the full prev file
//     * @param fullFinalFile the full final file
//     * @param etOrd the et ord
//     * @param ordin the ordin
//     * @param execId the exec id
//     * @param componentType the component type
//     * @param idSaveTolist the id save tolist
//     * @param idType the id type
//     * @param idColumnIndex the id column index
//     * @param idMapFile the id map file
//     * @param partitionId the partition id
//     * @return the identifier file
//     */
//    private RF2IdentifierFile getIdentifierFile(File fullPrevFile, File fullFinalFile,
//            int etOrd, ArrayList<String> ordin, String execId,
//            String componentType, String idSaveTolist, String idType,
//            String idColumnIndex, String idMapFile, String partitionId) {
//        RF2IdentifierFile ident = new RF2IdentifierFile();
//        Key key = new Key();
//        key.effectiveTimeOrdinal = etOrd;
//        key.keyOrdinals = ordin;
//        ident.key = key;
//        ident.fileName = fullPrevFile.getAbsolutePath();
//        ident.sctIdFileName = fullFinalFile.getAbsolutePath();
//        SctIDParam spar = new SctIDParam();
//        spar.namespaceId = String.valueOf(nspNr);
//        spar.partitionId = partitionId;
//        spar.releaseId = releaseDate;
//        spar.executionId = execId;
//        spar.moduleId = moduleSCTID;
//        spar.componentType = componentType;
//        spar.idSaveTolist = idSaveTolist;
//        spar.idType = idType;
//        spar.idColumnIndex = idColumnIndex;
//        spar.idMapFile = idMapFile;
//        ident.sctidparam = spar;
//        return ident;
//    }
//
//    /**
//     * Write header.
//     *
//     * @param bw the bw
//     * @param header the header
//     * @throws IOException Signals that an I/O exception has occurred.
//     */
//    private void writeHeader(BufferedWriter bw, String header) throws IOException {
//        bw.append(header);
//        bw.append("\r\n");
//
//    }
//
//    /* (non-Javadoc)
//     * @see org.ihtsdo.project.dataexport.RF2DataExport#processUnfetchedConceptData(int, org.ihtsdo.tk.api.ConceptFetcherBI)
//     */
    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher)
            throws Exception {
//
//        I_GetConceptData concept = Terms.get().getConcept(cNid);
//        Long sctid = getExistentConceptSCTID(concept);
//        String uuid = concept.getUUIDs().iterator().next().toString();
//        if (sctid != null) {
//            String conceptSCTID = String.valueOf(sctid);
//            boolean FSNExists = false;
//            HashMap<I_DescriptionTuple, RefexChronicleBI> descriptions = getDescriptions(concept, languageRefset.getNid());
//            if (descriptions.size() > 0) {
//                for (I_DescriptionTuple desc : descriptions.keySet()) {
//
//                    if (desc.getTypeNid() == FSN) {
//                        FSNExists = true;
//                    }
//                    exportDescription(desc, concept, conceptSCTID, moduleSCTID, tgtLangCode, bwd, reportFileWriter);
//
//                    exportInactDescription(desc, moduleSCTID, I_Constants.DESCRIPTION_INACTIVATION_REFSET_ID, bwi);
//
//                    I_ExtendByRef extension = Terms.get().getExtension(descriptions.get(desc).getNid());
//                    I_ExtendByRefPartCid lastPart = getLastLangExtensionPart(extension);
//
//                    exportLanguage(lastPart, desc, concept, tgtLangCode, moduleSCTID, refsetSCTID, bwl, reportFileWriter);
//                }
//                if (!FSNExists && completeFSNNotTranslated) {
//                    //description will be exported with its decriptionid from source and moduleid from extension g
//                    descriptions = getDescriptions(concept, sourceRefset.getNid());
//                    if (descriptions.size() > 0) {
//                        for (I_DescriptionTuple desc : descriptions.keySet()) {
//                            if (desc.getTypeNid() == FSN && desc.getStatusNid() == activeValue.getNid()) {
//
//                                exportDescription(desc, concept, conceptSCTID, moduleSCTID, tgtLangCode, bwd, reportFileWriter);
//
//                                I_ExtendByRef extension = Terms.get().getExtension(descriptions.get(desc).getNid());
//                                I_ExtendByRefPartCid lastPart = getLastLangExtensionPart(extension);
//
//                                exportLanguage(lastPart, desc, concept, tgtLangCode, moduleSCTID, refsetSCTID, bwl, reportFileWriter);
//                            }
//                        }
//                    }
//                }
//            }
//        } else {
//            HashMap<I_DescriptionTuple, RefexChronicleBI> descriptions = getDescriptions(concept, languageRefset.getNid());
//            if (descriptions.size() > 0) {
//                reportFileWriter.append("The concept " + concept.toUserString() + " has not SCTID and it cannot be exported." + "\r\n");
//            }
//        }
    }
//
//    /**
//     * Gets the last lang extension part.
//     *
//     * @param extension the extension
//     * @return the last lang extension part
//     * @throws TerminologyException the terminology exception
//     * @throws IOException Signals that an I/O exception has occurred.
//     */
//    public I_ExtendByRefPartCid getLastLangExtensionPart(I_ExtendByRef extension) throws TerminologyException, IOException {
//        long lastVersion = Long.MIN_VALUE;
//        I_ExtendByRefPartCid extensionPart = null;
//        for (I_ExtendByRefVersion loopTuple : extension.getTuples(allStatusSet, releaseConfig.getViewPositionSetReadOnly(),
//                Precedence.TIME, releaseConfig.getConflictResolutionStrategy())) {
//
//            if (loopTuple.getTime() >= lastVersion) {
//                lastVersion = loopTuple.getTime();
//                extensionPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
//            }
//        }
//        return extensionPart;
//    }
//
//    /**
//     * The Class Rf2FileProvider.
//     */
//    class Rf2FileProvider extends RF2ArtifactPostExportAbst {
//
//        public Rf2FileProvider(String langCode, String countryNamespace) {
//            super(langCode, countryNamespace);
//            // TODO Auto-generated constructor stub
//        }
//    }
//
//    /**
//     * Close files.
//     *
//     * @param nextStep
//     */
//    public void closeFiles(boolean nextStep) {
//
//        try {
//            bwd.close();
//            bwi.close();
//            bwl.close();
//            bwd = null;
//            bwi = null;
//            bwl = null;
//            if (!nextStep) {
//
//                File rf2SnapshotOutputFolder = null;
//                Rf2FileProvider fProv = new Rf2FileProvider(tgtLangCode, countryNamespace);
//
//                rf2SnapshotOutputFolder = new File(expFolderFile.getAbsolutePath() + "/" + fProv.getSnapshotOutputFolder());
//                if (!rf2SnapshotOutputFolder.exists()) {
//                    rf2SnapshotOutputFolder.mkdir();
//                }
//
//                File snapshotFinalFile;
//                if (bCreateModuleConcept || bCreateRefsetConcept) {
//                    snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_CONCEPT, releaseDate);
//                    conceptTmpFile.renameTo(snapshotFinalFile);
//
//                    snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP, releaseDate);
//                    relationshipTmpFile.renameTo(snapshotFinalFile);
//
//                    snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP, releaseDate);
//                    statedRelationshipTmpFile.renameTo(snapshotFinalFile);
//                }
//                snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION, releaseDate);
//                descriptionTmpFile.renameTo(snapshotFinalFile);
//
//                snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET, releaseDate);
//                languageTmpFile.renameTo(snapshotFinalFile);
//
//                snapshotFinalFile = fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_ATTRIBUTE_VALUE, releaseDate);
//                inactDescTmpFile.renameTo(snapshotFinalFile);
//
//            }
//        } catch (IOException e) {
//            AceLog.getAppLog().alertAndLogException(e);
//        }
//    }
//
//    /**
//     * Gets the log.
//     *
//     * @return the log
//     */
//    public String getLog() {
//        try {
//            reportFileWriter.flush();
//
//            reportFileWriter.close();
//            FileInputStream fis = new FileInputStream(reportFile);
//
//            return readStream(fis);
//        } catch (IOException e) {
//            AceLog.getAppLog().alertAndLogException(e);
//        }
//        return null;
//    }
//
//    /**
//     * Read stream.
//     *
//     * @param is the is
//     * @return the string
//     */
//    public static String readStream(FileInputStream is) {
//        StringBuilder sb = new StringBuilder(1024);
//        try {
//            Reader r = new InputStreamReader(is, "UTF-8");
//            int c = 0;
//            while (c != -1) {
//                c = r.read();
//                sb.append((char) c);
//            }
//            r.close();
//            System.gc();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return sb.toString();
//    }

	@Override
	public NidBitSetBI getNidSet() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean continueWork() {
		// TODO Auto-generated method stub
		return false;
	}
}