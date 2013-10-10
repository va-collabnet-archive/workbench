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
package org.ihtsdo.mojo.db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.rf2.UuidUuidRemapper;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidRevision;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

/**
 *
 * @author Marc Campbell
 */
public class BinaryChangeSetResolveIds {

    private ArrayList<File> eccsInputFiles;
    private File eccsOutputFile;
    private File keepMapOutputFile;
    private UUID snomedIntUuid;
    private UUID rf1ActiveUuid;
    private UUID rf2ActiveUuid;
    private UUID userAuthorUuid;
    private UUID extensionPath;
    private UUID snomedCorePath;
    private long eccsTimeThreshold; // :!!!:TEMP: move to POM parameter
    private HashMap<Long, UUID> keepMap;
    private StringBuilder instancesNotKept;
    private StringBuilder descriptionsKept;
    private SctIdResolution resolution;
    private UuidUuidRemapper sctPrimorialUuidRemapper;
    private HashSet<UUID> skipUuidSet;
    private HashSet<UUID> skipMemberUuidSet;

    void setSkipUuidSet(HashSet<UUID> uuidSet) {
        this.skipUuidSet = uuidSet;
    }
    
    void setSkipMemberUuidSet(HashSet<UUID> uuidSet) {
        this.skipMemberUuidSet = uuidSet;
    }

    void setSctPrimorialUuidRemapper(UuidUuidRemapper uuidUuidRemapper) {
        this.sctPrimorialUuidRemapper = uuidUuidRemapper;
    }

    public enum SctIdResolution {

        KEEP_ALL_SCTID, KEEP_NO_ECCS_SCTID, KEEP_LAST_CURRENT_USE, FILTER_DESCRIPTION_SCTIDS
    };
    // DETAIL LOG FILES
    private boolean eccsLogPreB;
    private File eccsLogPreFile; // all econcepts as strings
    BufferedWriter eccsLogPreWriter;
    private boolean eccsLogPostB;
    private File eccsLogPostFile; // all econcepts as strings
    BufferedWriter eccsLogPostWriter;
    private File eccsLogExceptionsFile; // list concepts with path change
    BufferedWriter eccsLogExceptionsWriter;
    boolean eccsPathExceptionFoundB;

    public BinaryChangeSetResolveIds(String rootDirStr,
            String targetDirStr,
            SctIdResolution resolutionApproach,
            boolean logPreEccsB,
            boolean logPostEccsB,
            String extensionPathUuidStr)
            throws IOException, TerminologyException {
        this.snomedIntUuid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getPrimoridalUid();
        this.rf1ActiveUuid = SnomedMetadataRf1.CURRENT_RF1.getUuids()[0];
        this.rf2ActiveUuid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0];
        this.userAuthorUuid = UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c");
        this.eccsInputFiles = new ArrayList<>();
        File rootDir = new File(rootDirStr);
        listFilesRecursive(eccsInputFiles, rootDir, "", ".eccs");

        File artifactDirectory = new File(targetDirStr);
        artifactDirectory.mkdirs();
        this.eccsOutputFile = new File(targetDirStr + File.separator + "processed.eccs");
        this.keepMapOutputFile = new File(targetDirStr + File.separator + "keep_ids.txt");
        this.resolution = resolutionApproach;

        // VERBOSE LOG FILE
        this.eccsLogPreB = logPreEccsB;
        if (this.eccsLogPreB) {
            this.eccsLogPreFile = new File(targetDirStr + File.separator + "eccs_in.txt");
        }
        this.eccsLogPostB = logPostEccsB;
        if (this.eccsLogPostB) {
            this.eccsLogPostFile = new File(targetDirStr + File.separator + "eccs_out.txt");
        }
        this.eccsLogExceptionsFile = new File(targetDirStr + File.separator + "eccs_exception.txt");
        this.eccsPathExceptionFoundB = false;

        // EXTENSION PATH ON WHICH ALL ECCS CHANGES WILL BE PUT
        this.extensionPath = null;
        if (extensionPathUuidStr != null) {
            this.extensionPath = UUID.fromString(extensionPathUuidStr);
        }
        this.snomedCorePath = UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2");
        this.eccsTimeThreshold = 1327996800000L; // :!!!:TEMP: move to POM parameter

        this.skipUuidSet = null;
        this.skipMemberUuidSet = null;

        setupPathRemapping();
    }

    public void processFiles() throws IOException {
        instancesNotKept = new StringBuilder();
        instancesNotKept.append("\n\n**************************");
        instancesNotKept.append("\n*** INSTANCES NOT KEPT ***");
        instancesNotKept.append("\n**************************");
        instancesNotKept.append("\nSCTID \t ConceptUUID \t ComponentUUID\n");

        descriptionsKept = new StringBuilder();
        descriptionsKept.append("\n\n**************************");
        descriptionsKept.append("\n*** INSTANCES ARE KEPT ***");
        descriptionsKept.append("\n**************************");
        descriptionsKept.append("\nSCTID \t ConceptUUID \t ComponentUUID\n");

        pass1CreateKeepMap();

        if (eccsLogPreB) {
            eccsLogPreWriter = new BufferedWriter(new FileWriter(eccsLogPreFile));
        }
        if (eccsLogPostB) {
            eccsLogPostWriter = new BufferedWriter(new FileWriter(eccsLogPostFile));
        }
        eccsLogExceptionsWriter = new BufferedWriter(new FileWriter(eccsLogExceptionsFile));

        pass2CreateUpdatedEccsFile(eccsInputFiles);

        if (eccsLogPreWriter != null) {
            eccsLogPreWriter.flush();
            eccsLogPreWriter.close();
        }
        if (eccsLogPostWriter != null) {
            eccsLogPostWriter.flush();
            eccsLogPostWriter.close();
        }
        eccsLogExceptionsWriter.append(instancesNotKept.toString());
        eccsLogExceptionsWriter.append(descriptionsKept.toString());
        eccsLogExceptionsWriter.flush();
        eccsLogExceptionsWriter.close();

        // Logger logger = Logger.getLogger(BinaryChangeSetResolveIds.class.getName());
        // logger.log(Level.INFO, instancesNotKept.toString());
        // logger.log(Level.INFO, descriptionsKept.toString());
    }

    private void pass1CreateKeepMap() {
        // enclosing concept, sctidL, data, status
        ArrayList<SctIdUseInstance> keepList = new ArrayList<>();
        ArrayList<SctIdUseInstance> useList = gatherIdsUseList(eccsInputFiles);
        Collections.sort(useList);
        int i = 0;
        SctIdUseInstance prevKeep = null;
        while (i < useList.size() - 2) {
            SctIdUseInstance a = useList.get(i);
            SctIdUseInstance b = useList.get(i + 1);
            if (a.sctidL == b.sctidL) {
                if (a.enclosingConcept.compareTo(b.enclosingConcept) != 0) {
                    System.out.println(a.sctidL + "\t"
                            + a.enclosingConcept + "\t" + a.timeL + "\t" + a.activeInt + "\t"
                            + b.enclosingConcept + "\t" + b.timeL + "\t" + b.activeInt + "\t");

                    // check if a prior use instance needs to be added and closed
                    if (prevKeep != null
                            && prevKeep.sctidL != a.sctidL) {
                        keepList.add(prevKeep);
                        prevKeep = null;
                    }

                    // check if new use instance needs to be created and/or updated
                    if (a.enclosingConcept.compareTo(b.enclosingConcept) != 0) {
                        if (prevKeep == null) {
                            prevKeep = new SctIdUseInstance();
                        }
                        if (b.activeInt > 0) {
                            prevKeep.sctidL = b.sctidL;
                            prevKeep.enclosingConcept = b.enclosingConcept;
                            prevKeep.timeL = b.timeL;
                            prevKeep.activeInt = b.activeInt;
                        } else if (a.activeInt > 0) {
                            prevKeep.sctidL = a.sctidL;
                            prevKeep.enclosingConcept = a.enclosingConcept;
                            prevKeep.timeL = a.timeL;
                            prevKeep.activeInt = a.activeInt;
                        } else {
                            System.out.println("BOTH A & B NOT ACTIVE, KEPT B");
                            prevKeep.sctidL = b.sctidL;
                            prevKeep.enclosingConcept = b.enclosingConcept;
                            prevKeep.timeL = b.timeL;
                            prevKeep.activeInt = b.activeInt;
                        }
                    }

                }
            }
            i++;
        }
        if (prevKeep != null) {
            keepList.add(prevKeep);
        }

        System.out.println("\n\n************ KEEP LIST ************");
        for (SctIdUseInstance keep : keepList) {
            System.out.println(keep.sctidL + "\t"
                    + keep.enclosingConcept + "\t" + keep.timeL + "\t" + keep.activeInt);
        }

        // write keemMap to text file
        FileOutputStream fos;
        OutputStreamWriter osw;
        try {
            fos = new FileOutputStream(keepMapOutputFile);
            osw = new OutputStreamWriter(fos, "UTF-8");
            try (BufferedWriter bw = new BufferedWriter(osw)) {
                for (SctIdUseInstance k : keepList) {
                    bw.write(Long.toString(k.sctidL));
                    bw.write("\t");
                    bw.write(k.enclosingConcept.toString());
                    bw.write("\n");
                }
                bw.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(BinaryChangeSetResolveIds.class.getName()).log(Level.SEVERE, null, ex);
        }

        // convert to keepList to keepMap
        keepMap = new HashMap<>();
        for (SctIdUseInstance k : keepList) {
            keepMap.put(k.sctidL, k.enclosingConcept);
        }

    }

    private void pass2CreateUpdatedEccsFile(ArrayList<File> eccsInputFilesList) {
        List<TkIdentifier> preList;
        List<TkIdentifier> postList;
        List<TkChangeSortable> bcsList = new ArrayList<>();
        // :DEBUG:BEGIN:
//        HashSet<UUID> debugSet = new HashSet<>();
//        debugSet.add(UUID.fromString("9ef4e796-d5b9-538e-b1b4-43b4d9d7815a"));
//        debugSet.add(UUID.fromString("76ae0785-e3a7-52c6-8054-f82be4e9b8c4"));
//        debugSet.add(UUID.fromString("fe5ed2e6-ba31-5d61-828c-a017d02094de"));
//        debugSet.add(UUID.fromString("7158f3db-7e44-53e0-beee-9f82b0ffcce7"));
//        debugSet.add(UUID.fromString("07570642-1b43-31b4-9e90-4d1ebc8e21bb"));
//        debugSet.add(UUID.fromString("db8c0eba-4c1e-5659-bfc7-afb779f78062"));
//        debugSet.add(UUID.fromString("f3a36c74-9b49-57d9-a912-cdb5a9cb2b69"));
//        debugSet.add(UUID.fromString("fe94655d-f895-524a-bfa9-1aa5f588f7b0"));
//        debugSet.add(UUID.fromString("68d85b76-f5df-5403-aef1-29cba3dc3db2"));
//        debugSet.add(UUID.fromString("76ae0785-e3a7-52c6-8054-f82be4e9b8c4"));
//        debugSet.add(UUID.fromString("776b8717-5d7b-5ae7-a07a-70883733ee48"));
//        debugSet.add(UUID.fromString("3b4f0354-9424-56f7-9a1c-941d406355fa"));
//        debugSet.add(UUID.fromString("cdf6415e-c388-52f3-8944-b9157d30166c"));        
        // :DEBUG:END:
        try {
            for (File file : eccsInputFilesList) {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream in = new DataInputStream(bis);

                if (eccsLogPreWriter != null) {
                    eccsLogPreWriter.write("\n####### ");
                    eccsLogPreWriter.write(file.getName());
                    eccsLogPreWriter.write("\n");
                }
                if (eccsLogPostWriter != null) {
                    eccsLogPostWriter.write("\n####### ");
                    eccsLogPostWriter.write(file.getName());
                    eccsLogPostWriter.write("\n");
                }
                eccsLogExceptionsWriter.write("\n####### ");
                eccsLogExceptionsWriter.write(file.getName());
                eccsLogExceptionsWriter.write("\n");

                while (true) {
                    long timeStampEccsL;
                    TkConcept eConcept;
                    UUID enclosingUuid;
                    try {
                        timeStampEccsL = in.readLong();
                        eConcept = new TkConcept(in);
                        tweakTimeStamps(eConcept);
                        enclosingUuid = eConcept.primordialUuid;
                        // :!!!:DEBUG:
//                        if (debugSet.contains(eConcept.getPrimordialUuid())) {
//                            System.out.println(":!!!:DEBUG: BinaryChangeSetResolveIds .IN. \n" + eConcept.toString());
//                        }

                        if (sctPrimorialUuidRemapper != null) {
                            remapPrimordialUuids(eConcept);
                            enclosingUuid = eConcept.primordialUuid;
                        }

                        remapPathUuids(eConcept);
                        
                        removeLegacyGBDialectExceptionsAnnotation(eConcept);

                        if (eccsLogPreWriter != null) {
                            eccsLogPreWriter.write("\n#_#_#_#_# ");
                            eccsLogPreWriter.write("timeStampEccsL: ");
                            eccsLogPreWriter.write(Long.toString(timeStampEccsL));
                            eccsLogPreWriter.write("\n");
                            eccsLogPreWriter.write(eConcept.toString());
                            eccsLogPreWriter.write("\n");
                        }

                        if (skipUuidSet != null
                                && skipUuidSet.contains(eConcept.primordialUuid)) {
                            eccsLogExceptionsWriter.append("skipped UUID :: " + enclosingUuid.toString() + "\n");
                            continue;
                        }

                    } catch (EOFException e) {
                        in.close();
                        break;
                    }

                    if (eConcept.refsetMembers != null &&
                            eConcept.refsetMembers.size() > 0 &&
                            skipMemberUuidSet != null) {
                        processRefsetMembers(eConcept);
                    }
                    
                    long timeFirstEditL = timeStampEccsL;
                    eccsPathExceptionFoundB = false;

                    // Concept Attributes
                    if (eConcept.getConceptAttributes() != null) {
                        if (timeFirstEditL > eConcept.getConceptAttributes().time) {
                            timeFirstEditL = eConcept.getConceptAttributes().time;
                        }
                        // Report any extension path exception
                        if (extensionPath != null
                                && eConcept.getConceptAttributes().getPathUuid().compareTo(snomedCorePath) == 0
                                && eConcept.getConceptAttributes().getTime() > eccsTimeThreshold) {
                            eConcept.getConceptAttributes().setPathUuid(extensionPath);
                            if (!eccsPathExceptionFoundB) {
                                eccsLogExceptionsWriter.append(enclosingUuid.toString());
                                eccsPathExceptionFoundB = true;
                            }
                            eccsLogExceptionsWriter.append(" attributes");
                        }
                        // Process additional ids
                        if (eConcept.getConceptAttributes().additionalIds != null) {
                            preList = eConcept.getConceptAttributes().additionalIds;
                            postList = processIdListWithFilter(enclosingUuid, preList);
                            eConcept.getConceptAttributes().additionalIds = postList;
                            timeFirstEditL = findIdListFirstUseDate(postList, timeFirstEditL);
                        }
                    }
                    // Description
                    List<TkDescription> descriptionList = eConcept.getDescriptions();
                    if (descriptionList != null) {
                        for (TkDescription tkd : descriptionList) {
                            if (timeFirstEditL > tkd.time) {
                                timeFirstEditL = tkd.time;
                            }
                            // Report any extension path exception
                            if (extensionPath != null
                                    && tkd.getPathUuid().compareTo(snomedCorePath) == 0
                                    && tkd.getTime() > eccsTimeThreshold) {
                                tkd.setPathUuid(extensionPath);
                                if (!eccsPathExceptionFoundB) {
                                    eccsLogExceptionsWriter.append(enclosingUuid.toString());
                                    eccsPathExceptionFoundB = true;
                                }
                                eccsLogExceptionsWriter.append(" description");
                            }
                            // Process additional ids
                            if (tkd.additionalIds != null) {
                                preList = tkd.additionalIds;
                                postList = processIdListWithFilterDesc(enclosingUuid, tkd, preList);
                                tkd.additionalIds = postList;
                                timeFirstEditL = findIdListFirstUseDate(postList, timeFirstEditL);
                            }
                        }
                    }
                    // Relationships
                    List<TkRelationship> relationshipList = eConcept.getRelationships();
                    if (relationshipList != null) {
                        for (TkRelationship tkr : relationshipList) {
                            if (timeFirstEditL > tkr.time) {
                                timeFirstEditL = tkr.time;
                            }
                            // Report any extension path exception
                            if (extensionPath != null
                                    && tkr.getPathUuid().compareTo(snomedCorePath) == 0
                                    && tkr.getTime() > eccsTimeThreshold) {
                                tkr.setPathUuid(extensionPath);
                                if (!eccsPathExceptionFoundB) {
                                    eccsLogExceptionsWriter.append(enclosingUuid.toString());
                                    eccsPathExceptionFoundB = true;
                                }
                                eccsLogExceptionsWriter.append(" relationship");
                            }
                            // Process additional ids
                            if (tkr.additionalIds != null) {
                                preList = tkr.additionalIds;
                                postList = processIdListWithFilter(enclosingUuid, preList);
                                tkr.additionalIds = postList;
                                timeFirstEditL = findIdListFirstUseDate(postList, timeFirstEditL);
                            }

                        }
                    }
                    // Refset Members
                    List<TkRefexAbstractMember<?>> memberList = eConcept.getRefsetMembers();
                    if (memberList != null) {
                        for (TkRefexAbstractMember<?> tkram : memberList) {
                            if (timeFirstEditL > tkram.time) {
                                timeFirstEditL = tkram.time;
                            }
                            // Report any extension path exception
                            if (extensionPath != null
                                    && tkram.getPathUuid().compareTo(snomedCorePath) == 0
                                    && tkram.getTime() > eccsTimeThreshold) {
                                tkram.setPathUuid(extensionPath);
                                if (!eccsPathExceptionFoundB) {
                                    eccsLogExceptionsWriter.append(enclosingUuid.toString());
                                    eccsPathExceptionFoundB = true;
                                }
                                eccsLogExceptionsWriter.append(" member");
                            }
                            // Process additional ids
                            if (tkram.additionalIds != null) {
                                preList = tkram.additionalIds;
                                postList = processIdListWithFilter(enclosingUuid, preList);
                                tkram.additionalIds = postList;
                                timeFirstEditL = findIdListFirstUseDate(postList, timeFirstEditL);
                            }
                        }
                    }

//                    if (debugSet.contains(eConcept.getPrimordialUuid())) {
//                        System.out.println(":!!!:DEBUG: BinaryChangeSetResolveIds .OUT. \n" + eConcept.toString());
//                    }
                    if (eccsPathExceptionFoundB) {
                        eccsLogExceptionsWriter.append("### PATH EXCEPTION FOUND ###\n");
                        eccsLogExceptionsWriter.append(eConcept.toString());
                        eccsLogExceptionsWriter.append("\n");
                    }
                    bcsList.add(new TkChangeSortable(timeStampEccsL, timeFirstEditL, eConcept));
                }
            }
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(BinaryChangeSetResolveIds.class.getName()).log(Level.SEVERE, null, ex);
        }

        Collections.sort(bcsList);
        try {
            FileOutputStream fos = new FileOutputStream(eccsOutputFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            try (DataOutputStream out = new DataOutputStream(bos)) {
                for (TkChangeSortable tkcs : bcsList) {
                    out.writeLong(tkcs.timeStampEccsL);
                    tkcs.eConcept.writeExternal(out);
                    if (eccsLogPostWriter != null) {
                        eccsLogPostWriter.write("\n#_#_#_#_# ");
                        eccsLogPostWriter.write("timeStampEccsL: ");
                        eccsLogPostWriter.write(Long.toString(tkcs.timeStampEccsL));
                        eccsLogPostWriter.write(" ---- timeFirstEditL: ");
                        eccsLogPostWriter.write(Long.toString(tkcs.timeFirstEditL));
                        eccsLogPostWriter.write("\n");
                        eccsLogPostWriter.write(tkcs.eConcept.toString());
                        eccsLogPostWriter.write("\n");
                    }
                }
                out.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(BinaryChangeSetResolveIds.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private ArrayList<SctIdUseInstance> gatherIdsUseList(ArrayList<File> eccsInputFilesList) {
        ArrayList<SctIdUseInstance> sctIdUseList = new ArrayList<>();

        for (File file : eccsInputFilesList) {
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream in = new DataInputStream(bis);

                long firstDate;
                while (true) {
                    TkConcept eConcept;
                    try {
                        firstDate = in.readLong(); // time
                        eConcept = new TkConcept(in);
                    } catch (EOFException e) {
                        in.close();
                        break;
                    }

                    // Concept Attributes
                    if (eConcept.getConceptAttributes() != null) {
                        if (eConcept.getConceptAttributes().additionalIds != null) {
                            List<TkIdentifier> idList = eConcept.getConceptAttributes().additionalIds;
                            processIdList(eConcept.primordialUuid, idList, sctIdUseList);
                        }
                    }
                    // Description
                    List<TkDescription> descriptionList = eConcept.getDescriptions();
                    if (descriptionList != null) {
                        for (TkDescription tkd : descriptionList) {
                            if (tkd.additionalIds != null) {
                                processIdList(eConcept.primordialUuid, tkd.additionalIds, sctIdUseList);
                            }
                        }
                    }
                    // Relationships
                    List<TkRelationship> relationshipList = eConcept.getRelationships();
                    if (relationshipList != null) {
                        for (TkRelationship tkr : relationshipList) {
                            if (tkr.additionalIds != null) {
                                processIdList(eConcept.primordialUuid, tkr.additionalIds, sctIdUseList);
                            }
                        }
                    }
                    // Refset Members
                    List<TkRefexAbstractMember<?>> memberList = eConcept.getRefsetMembers();
                    if (memberList != null) {
                        for (TkRefexAbstractMember<?> tkram : memberList) {
                            if (tkram.additionalIds != null) {
                                processIdList(eConcept.primordialUuid, tkram.additionalIds, sctIdUseList);
                            }
                        }
                    }

                }

            } catch (ClassNotFoundException | IOException ex) {
                Logger.getLogger(BinaryChangeSetResolveIds.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        return sctIdUseList;
    }

    private void listFilesRecursive(ArrayList<File> list, File root, String infix,
            String postfix) {
        if (root.isFile()) {
            list.add(root);
            return;
        }
        File[] files = root.listFiles();
        if (files.length > 0) {
            Arrays.sort(files);
            for (int i = 0; i < files.length; i++) {
                String name = files[i].getName().toUpperCase();

                if (files[i].isFile() && name.endsWith(postfix.toUpperCase())
                        && name.contains(infix.toUpperCase())) {
                    list.add(files[i]);
                }
                if (files[i].isDirectory()) {
                    listFilesRecursive(list, files[i], infix, postfix);
                }
            }
        }
    }

    private void processIdList(UUID enclosingConceptUuid,
            List<TkIdentifier> idList,
            List<SctIdUseInstance> inUseList) {
        if (resolution.compareTo(SctIdResolution.KEEP_ALL_SCTID) == 0) {
            return; // no special cases to add to use list
        }
        if (resolution.compareTo(SctIdResolution.KEEP_NO_ECCS_SCTID) == 0) {
            return; // no special cases to add to use list
        }
        if (resolution.compareTo(SctIdResolution.FILTER_DESCRIPTION_SCTIDS) == 0) {
            return; // no special cases to add to use list
        }
        for (TkIdentifier tki : idList) {
            if (tki.authorityUuid.compareTo(snomedIntUuid) == 0) {
                boolean status = false;
                if (tki.statusUuid.compareTo(rf1ActiveUuid) == 0
                        || tki.statusUuid.compareTo(rf2ActiveUuid) == 0) {
                    status = true;
                }
                SctIdUseInstance sctIdUseInstance = new SctIdUseInstance(enclosingConceptUuid,
                        (Long) tki.getDenotation(), tki.time, status);
                inUseList.add(sctIdUseInstance);
            }
        }
    }

    private List<TkIdentifier> processIdListWithFilter(UUID enclosingConceptUuid,
            List<TkIdentifier> idList) throws IOException {
        if (resolution.compareTo(SctIdResolution.KEEP_ALL_SCTID) == 0) {
            return idList; // do not filter list
        }
        if (resolution.compareTo(SctIdResolution.FILTER_DESCRIPTION_SCTIDS) == 0) {
            return idList; // do not filter list of non-descriptions
        }
        ArrayList<TkIdentifier> filteredIdList = new ArrayList<>();
        for (TkIdentifier tki : idList) {
            // Report any extension path exception
            if (extensionPath != null
                    && tki.getPathUuid().compareTo(snomedCorePath) == 0
                    && tki.getTime() > eccsTimeThreshold) {
                tki.setPathUuid(extensionPath);
                if (!eccsPathExceptionFoundB) {
                    eccsLogExceptionsWriter.append(enclosingConceptUuid.toString());
                    eccsPathExceptionFoundB = true;
                }
                eccsLogExceptionsWriter.append(" id");
            }
            // Process additional ids
            if (tki.authorityUuid.compareTo(snomedIntUuid) == 0) {
                if (resolution.compareTo(SctIdResolution.KEEP_NO_ECCS_SCTID) != 0) {
                    if (keepMap.containsKey((Long) tki.getDenotation())) {
                        UUID keepUuid = keepMap.get((Long) tki.getDenotation());
                        if (enclosingConceptUuid.compareTo(keepUuid) == 0) {
                            filteredIdList.add(tki); // match, keep this instance
                        } else {
                            instancesNotKept.append((Long) tki.getDenotation());
                            instancesNotKept.append("\t");
                            instancesNotKept.append(enclosingConceptUuid.toString());
                            instancesNotKept.append("\n");
                        }
                    } else {
                        filteredIdList.add(tki); // not a filtered case
                    }
                }
            } else {
                filteredIdList.add(tki); // not a filtered authority
            }
        }
        return filteredIdList;
    }

    private List<TkIdentifier> processIdListWithFilterDesc(UUID enclosingConceptUuid, TkDescription tkd, List<TkIdentifier> idList)
            throws IOException {
        if (resolution.compareTo(SctIdResolution.KEEP_ALL_SCTID) == 0) {
            return idList; // do not filter list
        }
        ArrayList<TkIdentifier> filteredIdList = new ArrayList<>();
        for (TkIdentifier tki : idList) {
            // Report any extension path exception
            if (extensionPath != null
                    && tki.getPathUuid().compareTo(snomedCorePath) == 0
                    && tki.getTime() > eccsTimeThreshold) {
                tki.setPathUuid(extensionPath);
                if (!eccsPathExceptionFoundB) {
                    eccsLogExceptionsWriter.append(enclosingConceptUuid.toString());
                    eccsPathExceptionFoundB = true;
                }
                eccsLogExceptionsWriter.append(" id");
            }
            // Process additional ids
            if (tki.authorityUuid.compareTo(snomedIntUuid) == 0) {
                if (resolution.compareTo(SctIdResolution.FILTER_DESCRIPTION_SCTIDS) == 0) {
                    if (tki.pathUuid.compareTo(extensionPath) == 0
                            || tki.pathUuid.compareTo(snomedCorePath) == 0) {
                        // only keep SCTID on extension path or SNOMED Core path
                        filteredIdList.add(tki);
                        descriptionsKept.append((Long) tki.getDenotation());
                        descriptionsKept.append("\t");
                        descriptionsKept.append(enclosingConceptUuid.toString());
                        descriptionsKept.append("\t");
                        descriptionsKept.append(tkd.primordialUuid.toString());
                        descriptionsKept.append("\t");
                        descriptionsKept.append(tkd.pathUuid.toString());
                        descriptionsKept.append("\tKEEP\n");
                    } else {
                        instancesNotKept.append((Long) tki.getDenotation());
                        instancesNotKept.append("\t");
                        instancesNotKept.append(enclosingConceptUuid.toString());
                        instancesNotKept.append("\t");
                        instancesNotKept.append(tkd.primordialUuid.toString());
                        instancesNotKept.append("\t");
                        instancesNotKept.append(tkd.pathUuid.toString());
                        instancesNotKept.append("\tDROP\n");
                    }
                } else if (resolution.compareTo(SctIdResolution.KEEP_NO_ECCS_SCTID) != 0) {
                    if (keepMap.containsKey((Long) tki.getDenotation())) {
                        UUID keepUuid = keepMap.get((Long) tki.getDenotation());
                        if (enclosingConceptUuid.compareTo(keepUuid) == 0) {
                            filteredIdList.add(tki); // match, keep this instance
                        } else {
                            instancesNotKept.append((Long) tki.getDenotation());
                            instancesNotKept.append("\t");
                            instancesNotKept.append(enclosingConceptUuid.toString());
                            instancesNotKept.append("\n");
                        }
                    } else {
                        filteredIdList.add(tki); // not a filtered case
                    }
                }
            } else {
                filteredIdList.add(tki); // not a filtered authority
            }
        }
        return filteredIdList;
    }

    private void processRefsetMembers(TkConcept eConcept) throws IOException {
        ArrayList<TkRefexAbstractMember<?>> memberList = new ArrayList<>();
        for (TkRefexAbstractMember<?> member : eConcept.refsetMembers) {
            if (skipMemberUuidSet.contains(member.primordialUuid)) {
                eccsLogExceptionsWriter.append("skipped refset member :: \t");
                eccsLogExceptionsWriter.append(member.primordialUuid.toString());
                eccsLogExceptionsWriter.append("\t of component : \t");
                eccsLogExceptionsWriter.append(member.componentUuid.toString());
                eccsLogExceptionsWriter.append("\n");
            } else {
                memberList.add(member); // keep this member
            }
        }
        eConcept.refsetMembers = memberList;
    }

//    private void printPathAuthor(UUID authorUuid, UUID pathUuid) {
//        if (authorUuid != null && pathUuid != null) {
//            UUID theAuthor = pathToAuthorMap.get(pathUuid);
//            if (theAuthor != null && userAuthorUuid.compareTo(authorUuid) == 0) {
//                System.out.println("SORT_THIS " + pathUuid.toString() + " :: " + authorUuid.toString());
//            }
//
//        }
//    }
    private UUID remapAuthor(UUID authorUuid, UUID pathUuid) {
        if (authorUuid == null || pathUuid == null) {
            // return null;
            throw new UnsupportedOperationException("null in remapAuthor (authorUuid == null || pathUuid == null)");
        }
        UUID mappedAuthor = pathToAuthorMap.get(pathUuid);
        if (mappedAuthor == null) {
            return authorUuid;
        } else {
            return mappedAuthor;
        }
    }

    private UUID remapPath(UUID authorUuid, UUID pathUuid) {
        if (authorUuid == null || pathUuid == null) {
            // return null;
            throw new UnsupportedOperationException("null in remapPath (authorUuid == null || pathUuid == null)");
        }
        UUID mappedAuthor = pathToAuthorMap.get(pathUuid);
        if (mappedAuthor == null) {
            if (pathsToNotChange.contains(pathUuid)) {
                return pathUuid;
            } else {
                throw new UnsupportedOperationException("case not supported");
            }
        } else {
            return developmentPath;
        }
    }

    private void remapPathUuids(TkConcept eConcept) throws IOException {
        TkConceptAttributes attributes = eConcept.conceptAttributes;
        if (attributes != null) {
            // printPathAuthor(attributes.authorUuid, attributes.pathUuid);
            attributes.authorUuid = remapAuthor(attributes.authorUuid, attributes.pathUuid);
            attributes.pathUuid = remapPath(attributes.authorUuid, attributes.pathUuid);
            // IDS
            if (attributes.additionalIds != null) {
                List<TkIdentifier> ids = attributes.additionalIds;
                for (TkIdentifier tki : ids) {
                    // printPathAuthor(tki.authorUuid, tki.pathUuid);
                    tki.authorUuid = remapAuthor(tki.authorUuid, tki.pathUuid);
                    tki.pathUuid = remapPath(tki.authorUuid, tki.pathUuid);
                }
            }
            // ANNOTATIONS
            if (attributes.annotations != null) {
                List<TkRefexAbstractMember<?>> annot = attributes.annotations;
                for (TkRefexAbstractMember<?> tkram : annot) {
                    // printPathAuthor(tkram.authorUuid, tkram.pathUuid);
                    tkram.authorUuid = remapAuthor(tkram.authorUuid, tkram.pathUuid);
                    tkram.pathUuid = remapPath(tkram.authorUuid, tkram.pathUuid);
                    List<?> revisions = tkram.revisions;
                    if (revisions != null) {
                        for (Object o : revisions) {
                            if (TkRevision.class.isAssignableFrom(o.getClass())) {
                                TkRevision trur = (TkRevision) o;
                                trur.authorUuid = remapAuthor(trur.authorUuid, trur.pathUuid);
                                trur.pathUuid = remapPath(trur.authorUuid, trur.pathUuid);
                            } else {
                                throw new UnsupportedOperationException("remapPathUuids attributes revision" + o.getClass());
                            }
                        }
                    }
                }
            }
            // REVISIONS
            if (attributes.revisions != null) {
                List<TkConceptAttributesRevision> revisions = attributes.revisions;
                for (TkConceptAttributesRevision tkcar : revisions) {
                    // printPathAuthor(tkcar.authorUuid, tkcar.pathUuid);
                    tkcar.authorUuid = remapAuthor(tkcar.authorUuid, tkcar.pathUuid);
                    tkcar.pathUuid = remapPath(tkcar.authorUuid, tkcar.pathUuid);
                }
            }
        }

        List<TkDescription> descriptions = eConcept.descriptions;
        if (descriptions != null) {
            for (TkDescription tkd : descriptions) {
                // printPathAuthor(tkd.authorUuid, tkd.pathUuid);
                tkd.authorUuid = remapAuthor(tkd.authorUuid, tkd.pathUuid);
                tkd.pathUuid = remapPath(tkd.authorUuid, tkd.pathUuid);
                // IDS
                if (tkd.additionalIds != null) {
                    List<TkIdentifier> ids = tkd.additionalIds;
                    for (TkIdentifier tki : ids) {
                        // printPathAuthor(tki.authorUuid, tki.pathUuid);
                        tki.authorUuid = remapAuthor(tki.authorUuid, tki.pathUuid);
                        tki.pathUuid = remapPath(tki.authorUuid, tki.pathUuid);
                    }
                }
                // ANNOTATIONS
                if (tkd.annotations != null) {
                    List<TkRefexAbstractMember<?>> annotations = tkd.annotations;
                    for (TkRefexAbstractMember<?> tkram : annotations) {
                        // printPathAuthor(tkram.authorUuid, tkram.pathUuid);
                        tkram.authorUuid = remapAuthor(tkram.authorUuid, tkram.pathUuid);
                        tkram.pathUuid = remapPath(tkram.authorUuid, tkram.pathUuid);
                        List<?> revisions = tkram.revisions;
                        if (revisions != null) {
                            for (Object o : revisions) {
                                if (TkRevision.class.isAssignableFrom(o.getClass())) {
                                    TkRevision trur = (TkRevision) o;
                                    trur.authorUuid = remapAuthor(trur.authorUuid, trur.pathUuid);
                                    trur.pathUuid = remapPath(trur.authorUuid, trur.pathUuid);
                                } else {
                                    throw new UnsupportedOperationException("remapPathUuids description revision" + o.getClass());
                                }
                            }
                        }
                    }
                }
                // REVISIONS
                if (tkd.revisions != null) {
                    List<TkDescriptionRevision> revisions = tkd.revisions;
                    for (TkDescriptionRevision tkdr : revisions) {
                        // printPathAuthor(tkdr.authorUuid, tkdr.pathUuid);
                        tkdr.authorUuid = remapAuthor(tkdr.authorUuid, tkdr.pathUuid);
                        tkdr.pathUuid = remapPath(tkdr.authorUuid, tkdr.pathUuid);
                    }
                }
            }
        }

        List<TkRelationship> relationships = eConcept.relationships;
        if (relationships != null) {
            for (TkRelationship tkr : relationships) {
                // printPathAuthor(tkr.authorUuid, tkr.pathUuid);
                tkr.authorUuid = remapAuthor(tkr.authorUuid, tkr.pathUuid);
                tkr.pathUuid = remapPath(tkr.authorUuid, tkr.pathUuid);
                // IDS
                if (tkr.additionalIds != null) {
                    List<TkIdentifier> ids = tkr.additionalIds;
                    for (TkIdentifier tki : ids) {
                        // printPathAuthor(tki.authorUuid, tki.pathUuid);
                        tki.authorUuid = remapAuthor(tki.authorUuid, tki.pathUuid);
                        tki.pathUuid = remapPath(tki.authorUuid, tki.pathUuid);
                    }
                }
                // ANNOTATIONS
                if (tkr.annotations != null) {
                    List<TkRefexAbstractMember<?>> annotations = tkr.annotations;
                    for (TkRefexAbstractMember<?> tkram : annotations) {
                        // printPathAuthor(tkram.authorUuid, tkram.pathUuid);
                        tkram.authorUuid = remapAuthor(tkram.authorUuid, tkram.pathUuid);
                        tkram.pathUuid = remapPath(tkram.authorUuid, tkram.pathUuid);
                        List<?> revisions = tkram.revisions;
                        if (revisions != null) {
                            for (Object o : revisions) {
                                if (TkRevision.class.isAssignableFrom(o.getClass())) {
                                    TkRevision trur = (TkRevision) o;
                                    trur.authorUuid = remapAuthor(trur.authorUuid, trur.pathUuid);
                                    trur.pathUuid = remapPath(trur.authorUuid, trur.pathUuid);
                                } else {
                                    throw new UnsupportedOperationException("remapPathUuids relationship revision" + o.getClass());
                                }
                            }
                        }
                    }
                }
                // REVISIONS
                if (tkr.revisions != null) {
                    List<TkRelationshipRevision> revisions = tkr.revisions;
                    for (TkRelationshipRevision tkrr : revisions) {
                        // printPathAuthor(tkrr.authorUuid, tkrr.pathUuid);
                        tkrr.authorUuid = remapAuthor(tkrr.authorUuid, tkrr.pathUuid);
                        tkrr.pathUuid = remapPath(tkrr.authorUuid, tkrr.pathUuid);
                    }
                }
            }
        }


        List<TkRefexAbstractMember<?>> refsetMembers = eConcept.refsetMembers;
        if (refsetMembers != null) {
            for (TkRefexAbstractMember<?> tkram : refsetMembers) {
                // printPathAuthor(tkram.authorUuid, tkram.pathUuid);
                tkram.authorUuid = remapAuthor(tkram.authorUuid, tkram.pathUuid);
                tkram.pathUuid = remapPath(tkram.authorUuid, tkram.pathUuid);
                // IDS
                List<TkIdentifier> ids = tkram.additionalIds;
                if (ids != null) {
                    for (TkIdentifier tki : ids) {
                        // printPathAuthor(tki.authorUuid, tki.pathUuid);
                        tki.authorUuid = remapAuthor(tki.authorUuid, tki.pathUuid);
                        tki.pathUuid = remapPath(tki.authorUuid, tki.pathUuid);
                    }
                }
                // ANNOTATIONS
                List<TkRefexAbstractMember<?>> annotations = tkram.annotations;
                if (tkram.annotations != null) {
                    for (TkRefexAbstractMember<?> annot : annotations) {
                        throw new UnsupportedOperationException("remapPathUuids tkram.annotations");
                    }
                }
                // REVISIONS
                if (tkram.revisions != null) {
                    List<?> revisions = tkram.revisions;
                    for (Object o : revisions) {
                        throw new UnsupportedOperationException("remapPathUuids tkram.revisions");
                    }
                }
            }
        }
    }

    private void remapPrimordialUuids(TkConcept eConcept) throws IOException {
        UUID enclosingPrimordialUuid = sctPrimorialUuidRemapper.getUuid(eConcept.primordialUuid);
        if (enclosingPrimordialUuid != null) {
            eccsLogExceptionsWriter.append("remapped UUID from :: " + eConcept.primordialUuid + " :: to :: " + enclosingPrimordialUuid + "\n");
            eConcept.primordialUuid = enclosingPrimordialUuid;
        } else {
            enclosingPrimordialUuid = eConcept.primordialUuid; // keep original
        }

        if (eConcept.conceptAttributes != null) {
            TkConceptAttributes attributes = eConcept.conceptAttributes;
            attributes.primordialUuid = enclosingPrimordialUuid;
            List<TkRefexAbstractMember<?>> conceptAnnotations = attributes.annotations;
            if (conceptAnnotations != null) {
                for (TkRefexAbstractMember<?> tkram : conceptAnnotations) {
                    tkram.componentUuid = enclosingPrimordialUuid;
                    if (tkram.annotations != null) {
                        throw new UnsupportedOperationException("tkram.annotations != null" + tkram.toString());
                    }
                    List<?> revisions = tkram.revisions;
                    if (revisions != null) {
                        for (Object o : revisions) {
                            if (TkRefexUuidRevision.class.isAssignableFrom(o.getClass())) {
                                TkRefexUuidRevision trur = (TkRefexUuidRevision) o;
                                UUID uuid1 = sctPrimorialUuidRemapper.getUuid(trur.uuid1);
                                if (uuid1 != null) {
                                    trur.uuid1 = uuid1;
                                }
                            } else {
                                throw new UnsupportedOperationException("conceptAnnotations revision" + o.getClass());
                            }
                        }
                    }
                }
            }
        }

        if (eConcept.descriptions != null) {
            List<TkDescription> descriptions = eConcept.descriptions;
            for (TkDescription tkd : descriptions) {
                tkd.conceptUuid = enclosingPrimordialUuid;
                UUID uuid = sctPrimorialUuidRemapper.getUuid(tkd.primordialUuid);
                if (uuid != null) {
                   tkd.primordialUuid = uuid;
                }
                // description revisions do not have any remappable uuids
            }
        }

        if (eConcept.relationships != null) {
            List<TkRelationship> relationships = eConcept.relationships;
            for (TkRelationship tkr : relationships) {
                tkr.c1Uuid = enclosingPrimordialUuid;
                // 
                UUID relUuid = sctPrimorialUuidRemapper.getUuid(tkr.primordialUuid);
                if (relUuid != null) {
                    tkr.primordialUuid = relUuid;
                }
                UUID c2Uuid = sctPrimorialUuidRemapper.getUuid(tkr.c2Uuid);
                if (c2Uuid != null) {
                    tkr.c2Uuid = c2Uuid;
                }
                UUID typeUuid = sctPrimorialUuidRemapper.getUuid(tkr.typeUuid);
                if (typeUuid != null) {
                    tkr.typeUuid = typeUuid;
                }
                // REVISIONS
                if (tkr.revisions != null) {
                    List<TkRelationshipRevision> revisions = tkr.revisions;
                    for (TkRelationshipRevision tkrr : revisions) {
                        UUID revTypeUuid = sctPrimorialUuidRemapper.getUuid(tkrr.typeUuid);
                        if (revTypeUuid != null) {
                            tkrr.typeUuid = revTypeUuid;
                        }
                    }
                }                
            }
        }

        if (eConcept.refsetMembers != null) {
            List<TkRefexAbstractMember<?>> refsetMembers = eConcept.refsetMembers;
            for (TkRefexAbstractMember<?> tkram : refsetMembers) {
                UUID componentUuid = sctPrimorialUuidRemapper.getUuid(tkram.componentUuid);
                if (componentUuid != null) {
                    tkram.componentUuid = componentUuid;
                }
                List<?> revisions = tkram.revisions;
                if (revisions != null) {
                    for (Object o : revisions) {
                        throw new UnsupportedOperationException(":eConcept.refsetMembers revisions" + o.getClass());
                    }
                }
            }
        }
    }

    private final UUID legacyGbDialectExceptionsRefsetUuid = UUID.fromString("e8191494-ce3c-5bd8-803e-31d31c831f8a");
    private void removeLegacyGBDialectExceptionsAnnotation(TkConcept eConcept) {
        if (eConcept.primordialUuid.compareTo(UUID.fromString("c7b73676-d9fb-322d-a721-1c0bdf3f11c2")) == 0 ||
                eConcept.primordialUuid.compareTo(UUID.fromString("123f303c-9c8a-5092-81d5-db0d2ffc4d62")) == 0) {
            // System.out.println(":!!!:DEBUG:");
        }
        if (eConcept.descriptions != null) {
            for (TkDescription tkd : eConcept.descriptions) {
                if (tkd.annotations != null) {
                    List<TkRefexAbstractMember<?>> keepAnnotations = new ArrayList<>();
                    for (TkRefexAbstractMember<?> tkram : tkd.annotations) {
                        if (tkram.getRefexUuid().compareTo(legacyGbDialectExceptionsRefsetUuid) != 0) {
                            keepAnnotations.add(tkram);
                        } else {
                            // System.out.println(":!!!:DEBUG: ... dropped legacyGbDialectExceptionsRefsetUuid");
                        }
                    }
                    tkd.annotations = keepAnnotations;
                }
            }
        }
    }

    private Long findIdListFirstUseDate(List<TkIdentifier> idList, Long firstDate) {
        Long aDate = firstDate;
        for (TkIdentifier tki : idList) {
            if (aDate > tki.time) {
                aDate = tki.time;
            }
        }
        return aDate;
    }

    private class SctIdUseInstance implements Comparable<SctIdUseInstance> {

        long sctidL;
        long timeL;
        UUID enclosingConcept;
        int activeInt;

        private SctIdUseInstance() {
            this.enclosingConcept = null;
            this.sctidL = Long.MAX_VALUE;
            this.timeL = Long.MAX_VALUE;
            this.activeInt = 0;
        }

        private SctIdUseInstance(UUID enclosingConceptUuid, long sctidL, long time, boolean status) {
            this.enclosingConcept = enclosingConceptUuid;
            this.sctidL = sctidL;
            this.timeL = time;
            this.activeInt = 0;
            if (status) {
                this.activeInt = 1;
            }
        }

        @Override
        public int compareTo(SctIdUseInstance o) {
            if (this.sctidL < o.sctidL) {
                return -1; // instance less than received
            } else if (this.sctidL > o.sctidL) {
                return 1; // instance greater than received
            } else {
                if (this.timeL < o.timeL) {
                    return -1; // instance less than received
                } else if (this.timeL > o.timeL) {
                    return 1; // instance greater than received
                } else {
                    if (this.activeInt < o.activeInt) {
                        return -1; // instance less than received
                    } else if (this.activeInt > o.activeInt) {
                        return 1; // instance greater than received
                    } else {
                        if (this.enclosingConcept.compareTo(o.enclosingConcept) < 0) {
                            return -1; // instance less than received
                        } else if (this.enclosingConcept.compareTo(o.enclosingConcept) > 0) {
                            return 1; // instance greater than received
                        } else {
                            return 0; // equal
                        }
                    }
                }
            }
        }
    }

    private class TkChangeSortable implements Comparable<TkChangeSortable> {

        Long timeStampEccsL;
        Long timeFirstEditL;
        TkConcept eConcept;

        public TkChangeSortable(Long timeEccsL, Long timeFirstEditL, TkConcept eConcept) {
            this.timeStampEccsL = timeEccsL;
            this.timeFirstEditL = timeFirstEditL;
            this.eConcept = eConcept;
        }

        @Override
        public int compareTo(TkChangeSortable o) {
            if (this.timeStampEccsL < o.timeStampEccsL) {
                return -1; // instance less than received
            } else if (this.timeStampEccsL > o.timeStampEccsL) {
                return 1; // instance greater than received
            } else {
                return 0;
            }
        }
//        public int compareTo(TkChangeSortable o) {
//            if (this.timeFirstEditL < o.timeFirstEditL) {
//                return -1; // instance less than received
//            } else if (this.timeFirstEditL > o.timeFirstEditL) {
//                return 1; // instance greater than received
//            } else {
//                return 0;
//            }
//        }
    }
    //################## :TODO: generalize these as parameters ##########################
    HashSet<UUID> pathsToNotChange;
    HashMap<UUID, UUID> pathToAuthorMap; // <pathUuid, authorUuid>
    UUID developmentPath;

    private void setupPathRemapping() {
        this.pathsToNotChange = new HashSet<>();
        // KPET CMT Project release candidate path
        this.pathsToNotChange.add(UUID.fromString("ded44500-d486-59ab-9749-68aa719e74a4"));
        // KPET CMT Project development path
        this.pathsToNotChange.add(UUID.fromString("3770e517-7adc-5a24-a447-77a9daa3eedf"));
        // KPET CMT Project development origin path
        this.pathsToNotChange.add(UUID.fromString("098eed03-204c-5bf0-91c0-3c9610beec6b"));
        // 2bfc4102-f630-5fbe-96b8-625f2a6b3d5a KPET Extension Path
        this.pathsToNotChange.add(UUID.fromString("2bfc4102-f630-5fbe-96b8-625f2a6b3d5a"));
        // 7b6ace72-4604-5ff1-b8c0-48e8f6204e3d source baseline
        this.pathsToNotChange.add(UUID.fromString("7b6ace72-4604-5ff1-b8c0-48e8f6204e3d"));
        // 8c230474-9f11-30ce-9cad-185a96fd03a2 SNOMED Core
        this.pathsToNotChange.add(UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2"));
        // Workbench Auxiliary
        this.pathsToNotChange.add(UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66"));

        this.pathToAuthorMap = new HashMap<>();
        // Peter Hender dev path
        pathToAuthorMap.put(UUID.fromString("0d6f7564-7fd0-5b5b-8266-323f1afdbcd5"), UUID.fromString("5aaf8f31-65e4-5eac-85db-bf83333547e0"));
        // Alan Abilla M dev path
        pathToAuthorMap.put(UUID.fromString("1d4bb71f-849c-5b4d-9e72-e818794bf7e4"), UUID.fromString("9b09b43b-ee48-57f3-bcd8-ac134a850c5d"));
        // Sarah Albo dev path
        pathToAuthorMap.put(UUID.fromString("6a8348da-7d95-5640-8ba2-4d20c440bd54"), UUID.fromString("3b463a64-ccf0-5957-a49d-2f1c48e85995"));
        // Robert Clements dev path
        pathToAuthorMap.put(UUID.fromString("41bfa888-2d01-5b5d-9344-38ad0f704ab2"), UUID.fromString("260b19f2-f152-5d45-ab84-c3d9084d8ea1"));
        // Denny Cordy dev path
        pathToAuthorMap.put(UUID.fromString("67cb4ddc-321b-5976-881f-7bd3c3897bd8"), UUID.fromString("b025678a-e524-5a3b-9762-8bdbb0f8f06f"));
        // Moon Hee Lee dev path
        pathToAuthorMap.put(UUID.fromString("75f06c8b-c18f-5a77-92f9-daa6bfac0ec5"), UUID.fromString("30d445cb-5c76-5f9a-bcc4-99ca2103704e"));
        // Ellen Torres dev path
        pathToAuthorMap.put(UUID.fromString("159c33ec-818e-5889-a130-be23aa467043"), UUID.fromString("162b0ef8-2daf-5713-aa3f-6d6248c8fa85"));
        // Andrew Kim dev path
        pathToAuthorMap.put(UUID.fromString("311c12c3-d901-5fa7-afd4-715e885dc40a"), UUID.fromString("f54e3a41-5d59-50a9-9649-d7316c501cc6"));
        // Bruce Goldberg dev path
        pathToAuthorMap.put(UUID.fromString("673ffe19-9da3-5b8e-ba08-45e813bc350a"), UUID.fromString("3de4c63a-74c7-5675-8895-8230d803ed64"));
        // Gerry Lazzraschi dev path
        pathToAuthorMap.put(UUID.fromString("1136c0b8-572c-5e3d-9fd1-2dabbb4a6851"), UUID.fromString("8c398123-36c4-5e7b-9387-5686e7964121"));
        // Jonathan Lukoff dev path
        pathToAuthorMap.put(UUID.fromString("141452fa-1c2c-5fa9-af07-aa4ec5fec623"), UUID.fromString("ace60f65-6a6d-54f0-a514-6e31ea052c43"));
        // Kathleen Schwarz dev path
        pathToAuthorMap.put(UUID.fromString("a3cac37a-0964-5750-a8ea-addd90a848a2"), UUID.fromString("ca477462-6390-5737-bcec-e280da403066"));
        // Rita Barsoum M dev path
        pathToAuthorMap.put(UUID.fromString("a860a481-c06a-5655-9e90-fb8e1f1cdd6b"), UUID.fromString("11a8781e-b5cd-52a1-99d7-f7382f4c77e8"));
        // Ross Hansen dev path
        pathToAuthorMap.put(UUID.fromString("aeaf539c-7c59-57fd-a7e1-2a296a0a9ca8"), UUID.fromString("fd6e21e0-718e-56f0-a52e-f0a7a3c4bf46"));
        // Xiang Mack dev path
        pathToAuthorMap.put(UUID.fromString("bf4692ca-00df-5f1b-a90e-b095fbc66e62"), UUID.fromString("07efce48-0738-5be2-bca8-fdf9dee6c663"));
        // Mary Gerard dev path
        pathToAuthorMap.put(UUID.fromString("e6fb5c3e-5327-589e-8bbc-8f05bb6ceb37"), UUID.fromString("fc1ade4c-9646-5291-8006-1a174207cd39"));
        ///////////////
        // Alisa Papotto dev path
        pathToAuthorMap.put(UUID.fromString("2e2a2614-e80b-5f16-90d5-b743b9448e16"), UUID.fromString("8fa1be59-6899-52d1-8a5b-2a02a86d58af"));
        // Diane Carter dev path
        pathToAuthorMap.put(UUID.fromString("33fc12fd-0f1d-5bfa-9362-09d097cca05b"), UUID.fromString("7b37e356-a4ee-5e54-ac3e-40da8d3a9d73"));
        // Esther Straus dev path
        pathToAuthorMap.put(UUID.fromString("54735cdf-2577-5a88-b607-f3664877ba5f"), UUID.fromString("9cd9f981-8f4e-56a1-a640-7f2b2f4f8fab"));
        // Gail Danelius dev path
        pathToAuthorMap.put(UUID.fromString("2cdad18c-7e90-575c-9a9a-8bea2a418f9d"), UUID.fromString("56e83fe2-2a4e-5fda-9580-30b29e32ded9"));
        // Georgina Kurtovich dev path
        pathToAuthorMap.put(UUID.fromString("175f1530-9046-5c19-ace3-cd3e21e9daa2"), UUID.fromString("943a411c-ec36-5ec0-a3b2-d30f93465e9d"));
        // Harry Abilla dev path
        pathToAuthorMap.put(UUID.fromString("255ccf60-ec14-5775-b9da-b6a6f52cbf47"), UUID.fromString("c23c87aa-b874-5d09-b269-f26dc62419c6"));
        // Karen Jahn dev path
        pathToAuthorMap.put(UUID.fromString("c657a7f1-bc8b-561b-a9fc-82c8724e9b70"), UUID.fromString("43c61474-7490-56a5-8d97-f40071f95fc5"));
        // Karen Vournas dev path
        pathToAuthorMap.put(UUID.fromString("d2cb5eb4-a717-5ffb-8adc-f8fbba3b0e7f"), UUID.fromString("9bd23a9d-8ab3-5588-894a-64258764a3d7"));
        // Kate Christensen dev path
        pathToAuthorMap.put(UUID.fromString("e95c4d35-a241-59c0-a50f-a211efc67664"), UUID.fromString("da6aa7d7-f189-5bcb-9ff3-82edf2cf0197"));
        // Lonette McCauley dev path
        pathToAuthorMap.put(UUID.fromString("53ae63b6-7258-5517-8d44-5836b0c9980d"), UUID.fromString("2aa8ebf5-5708-569c-8deb-9c981d3b1fd7"));
        // Margo X Imel dev path
        pathToAuthorMap.put(UUID.fromString("544aeb09-6bfd-5484-8f6b-8e7696d8ee03"), UUID.fromString("f201b7cc-fa78-5dc9-b8f0-79e03da111bb"));
        // Mark Groshek dev path
        pathToAuthorMap.put(UUID.fromString("faf553f7-bd4e-5717-a3fd-5b2ac7186189"), UUID.fromString("a99607e3-d0ec-5ffd-bb93-971f788b6a78"));
        // Michael B. Smith dev path
        pathToAuthorMap.put(UUID.fromString("28f904d6-7180-5283-b83c-0d46c8c265cf"), UUID.fromString("832ff735-dd6a-5ddc-a2eb-8974d09a826c"));
        // Michael Madden dev path
        pathToAuthorMap.put(UUID.fromString("5688036b-36f3-59f2-a220-7240ee80056f"), UUID.fromString("3622c55c-a33f-560a-b769-e4723374df57"));
        // Mirzet Halilovic dev path
        pathToAuthorMap.put(UUID.fromString("1d4d02b7-4255-51c2-845f-349740436e11"), UUID.fromString("f83d2307-e72b-5bec-b9f1-efe9e1be091a"));
        // Nancy Dirgo dev path
        pathToAuthorMap.put(UUID.fromString("41214b5e-4f4b-5e16-a1d2-101be39eb94f"), UUID.fromString("cd44a00d-3758-5d87-9fc5-871ddeff1c72"));
        // Tim Hearvy dev path
        pathToAuthorMap.put(UUID.fromString("da88e272-3f90-51b2-a0f5-fecd0ef233e4"), UUID.fromString("5d069252-3928-5fa2-9e50-118cdef69d61"));
        // Yasmeen Wengrow dev path
        pathToAuthorMap.put(UUID.fromString("43f67b8d-2b94-5919-81e9-416a158bb5df"), UUID.fromString("f637752b-10f9-527f-8ba4-ef7690d655bd"));
        // kp_admin dev path <> kp_admin
        pathToAuthorMap.put(UUID.fromString("cc8c1851-1981-52b7-b19c-22941710171d"), UUID.fromString("4239726b-588c-5c0a-b622-605ea9824d78"));
        // edit template path <> user
        pathToAuthorMap.put(UUID.fromString("8a6447b8-4a57-56b0-960f-075f430cd02f"), UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c"));
        /////
        developmentPath = UUID.fromString("3770e517-7adc-5a24-a447-77a9daa3eedf"); // destination development path :: KPET CMT Project development path
    }
    
    private class TimeStamp implements Comparable<TimeStamp> {
        long timePrimordial;
        long time;
        UUID status;

        public TimeStamp(long timePrimordial, long time, UUID status) {
            this.timePrimordial = timePrimordial;
            this.time = time;
            this.status = status;
        }

        @Override
        public int compareTo(TimeStamp o) {
            if (this.time > o.time) {
                return 1; // this is greater than received
            } else if (this.time < o.time) {
                return -1; // this is less than received
            } else {
                return 0; // this == received
            }
        }
    }
    
    private void tweakTimeStamps(TkConcept eConcept) throws IOException {
        if (eConcept.conceptAttributes != null) {
            TkConceptAttributes attributes = eConcept.conceptAttributes;
            UUID uuidStatus = attributes.getStatusUuid();
            if (attributes.revisions != null) {
                List<TkConceptAttributesRevision> r = attributes.revisions;
                TimeStamp[] tsa = new TimeStamp[r.size() + 1];
                int i = 0;
                long timePrimordial = attributes.time;
                tsa[i++] = new TimeStamp(timePrimordial, timePrimordial, uuidStatus);
                for (TkConceptAttributesRevision tkConceptAttributesRevision : r) {
                    uuidStatus = tkConceptAttributesRevision.statusUuid;
                    long time = tkConceptAttributesRevision.time;
                    tsa[i++] = new TimeStamp(timePrimordial, time, uuidStatus);
                }
                Arrays.sort(tsa);
                for (int j = 0; j < tsa.length - 1; j++) {
                    if (tsa[j].time == tsa[j + 1].time) {
                        eccsLogExceptionsWriter.append(":WARNING: found duplicate concept attributes time.");
                        eccsLogExceptionsWriter.append(" Concept: ");
                        eccsLogExceptionsWriter.append(eConcept.primordialUuid.toString());
                        eccsLogExceptionsWriter.append(" revisions.size=");
                        eccsLogExceptionsWriter.append(Integer.toString(r.size()));
                        eccsLogExceptionsWriter.append(" Status: ");
                        eccsLogExceptionsWriter.append(tsa[j].status.toString());
                        eccsLogExceptionsWriter.append(" ");
                        eccsLogExceptionsWriter.append(tsa[j+1].status.toString());
                        eccsLogExceptionsWriter.append("\r\n");
                    }
                }
            }
        }
                
        if (eConcept.descriptions != null) {
            HashSet<UUID> dropDescriptionEccsSet = new HashSet();
            List<TkDescription> descriptionList = eConcept.descriptions;
            for (TkDescription tkDescription : descriptionList) {
                if (tkDescription != null) {
                    UUID uuidStatus = tkDescription.getStatusUuid();
                    if (tkDescription.revisions != null) {
                        List<TkDescriptionRevision> r = tkDescription.revisions;
                        TimeStamp[] tsa = new TimeStamp[r.size() + 1];
                        int i = 0;
                        long timePrimordial = tkDescription.time;
                        tsa[i++] = new TimeStamp(timePrimordial, timePrimordial, uuidStatus);
                        for (TkDescriptionRevision tkDescriptionRevision : r) {
                            uuidStatus = tkDescriptionRevision.statusUuid;
                            long time = tkDescriptionRevision.time;
                            tsa[i++] = new TimeStamp(timePrimordial, time, uuidStatus);
                        }
                        Arrays.sort(tsa);
                        for (int j = 0; j < tsa.length - 1; j++) {
                            if (tsa[j].time == tsa[j + 1].time) {
                                dropDescriptionEccsSet.add(tkDescription.primordialUuid);
                                eccsLogExceptionsWriter.append(":WARNING: found duplicate description revision time.");
                                eccsLogExceptionsWriter.append(" Concept: ");
                                eccsLogExceptionsWriter.append(eConcept.primordialUuid.toString());
                                eccsLogExceptionsWriter.append(" Description: ");
                                eccsLogExceptionsWriter.append(tkDescription.primordialUuid.toString());
                                eccsLogExceptionsWriter.append(" revisions.size=");
                                eccsLogExceptionsWriter.append(Integer.toString(r.size()));
                                eccsLogExceptionsWriter.append(" Status: ");
                                eccsLogExceptionsWriter.append(tsa[j].status.toString());
                                eccsLogExceptionsWriter.append(" ");
                                eccsLogExceptionsWriter.append(tsa[j + 1].status.toString());
                                eccsLogExceptionsWriter.append("\r\n");
                            }
                        }
                    }
                }
                if (dropDescriptionEccsSet.size() > 0) {
                    eccsLogExceptionsWriter.append("...... dropping whole description(s) with revision containing the same timestamp.\r\n");
                    ArrayList<TkDescription> tempDescriptionList = new ArrayList();
                    for (TkDescription tkd : eConcept.descriptions) {
                        if (!dropDescriptionEccsSet.contains(tkd.primordialUuid)) {
                            tempDescriptionList.add(tkd); // just keep the descriptions without the ambiguous duplicate timestamps
                        } else {
                            eccsLogExceptionsWriter.append("...... dropped: ");
                            eccsLogExceptionsWriter.append(tkd.primordialUuid.toString());
                            eccsLogExceptionsWriter.append("\r\n");
                        }
                    }
                    eConcept.descriptions = tempDescriptionList;
                }
            }
        }
        
        if (eConcept.relationships != null) {
            List<TkRelationship> relationshipList = eConcept.relationships;
            for (TkRelationship tkRelationship : relationshipList) {
                if (tkRelationship != null) {
                    UUID uuidStatus = tkRelationship.getStatusUuid();
                    if (tkRelationship.revisions != null) {
                        List<TkRelationshipRevision> r = tkRelationship.revisions;
                        TimeStamp[] tsa = new TimeStamp[r.size() + 1];
                        int i = 0;
                        long timePrimordial = tkRelationship.time;
                        tsa[i++] = new TimeStamp(timePrimordial, timePrimordial, uuidStatus);
                        for (TkRelationshipRevision tkRelationshipRevision : r) {
                            uuidStatus = tkRelationshipRevision.statusUuid;
                            long time = tkRelationshipRevision.time;
                            tsa[i++] = new TimeStamp(timePrimordial, time, uuidStatus);
                        }
                        Arrays.sort(tsa);
                        boolean rejectRevision = false;
                        for (int j = 0; j < tsa.length - 1; j++) {
                            if (tsa[j].time == tsa[j + 1].time) {
                                eccsLogExceptionsWriter.append(":WARNING: found relationship with same revision timestamp.");
                                eccsLogExceptionsWriter.append(" Concept: ");
                                eccsLogExceptionsWriter.append(eConcept.primordialUuid.toString());
                                eccsLogExceptionsWriter.append(" Relationship: ");
                                eccsLogExceptionsWriter.append(tkRelationship.primordialUuid.toString());
                                eccsLogExceptionsWriter.append(" revisions.size=");
                                eccsLogExceptionsWriter.append(Integer.toString(r.size()));
                                eccsLogExceptionsWriter.append(" Status: ");
                                eccsLogExceptionsWriter.append(tsa[j].status.toString());
                                eccsLogExceptionsWriter.append(" ");
                                eccsLogExceptionsWriter.append(tsa[j + 1].status.toString());
                                eccsLogExceptionsWriter.append("\r\n");
                                rejectRevision = true;
                            }
                        }
                        if (rejectRevision && r.size() == 1) {
                            tkRelationship.revisions = null;
                            tkRelationship.statusUuid = rf2ActiveUuid;
                            eccsLogExceptionsWriter.append("...... dropped relationship revision with same timestamp. made primordial active\r\n");
                        } else if (rejectRevision) {
                            throw new UnsupportedOperationException("relationship with other than one duplicate timestamp not supported");
                        }
                    } // tkRelationship.revisions != null
                }
            }
        } 
        
        if (eConcept.refsetMembers != null) {
            List<TkRefexAbstractMember<?>> refsetMemberList = eConcept.refsetMembers;
            for (TkRefexAbstractMember member : refsetMemberList) {
                if (member != null) {
                    UUID uuidStatus = member.getStatusUuid();
                    if (member.revisions != null) {
                        List<TkRelationshipRevision> r = member.revisions;
                        TimeStamp[] tsa = new TimeStamp[r.size() + 1];
                        int i = 0;
                        long timePrimordial = member.time;
                        tsa[i++] = new TimeStamp(timePrimordial, timePrimordial, uuidStatus);
                        for (TkRelationshipRevision tkRelationshipRevision : r) {
                            uuidStatus = tkRelationshipRevision.statusUuid;
                            long time = tkRelationshipRevision.time;
                            tsa[i++] = new TimeStamp(timePrimordial, time, uuidStatus);
                        }
                        Arrays.sort(tsa);
                        for (int j = 0; j < tsa.length - 1; j++) {
                            if (tsa[j].time == tsa[j + 1].time) {
                                eccsLogExceptionsWriter.append(":WARNING: found relationship with same revision timestamp.");
                                eccsLogExceptionsWriter.append(" Concept: ");
                                eccsLogExceptionsWriter.append(eConcept.primordialUuid.toString());
                                eccsLogExceptionsWriter.append(" Refset member: ");
                                eccsLogExceptionsWriter.append(member.primordialUuid.toString());
                                eccsLogExceptionsWriter.append(" revisions.size=");
                                eccsLogExceptionsWriter.append(Integer.toString(r.size()));
                                eccsLogExceptionsWriter.append(" Status: ");
                                eccsLogExceptionsWriter.append(tsa[j].status.toString());
                                eccsLogExceptionsWriter.append(" ");
                                eccsLogExceptionsWriter.append(tsa[j + 1].status.toString());
                                eccsLogExceptionsWriter.append("\r\n");
                            }
                        }
                    }
                }
            }
        }
    } // tweakTimeStamps()

}
