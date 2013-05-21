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
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
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
    private UUID extensionPath;
    private UUID snomedCorePath;
    private long eccsTimeThreshold; // :!!!:TEMP: move to POM parameter
    private HashMap<Long, UUID> keepMap;
    private StringBuilder instancesNotKept;
    private StringBuilder descriptionsKept;
    private SctIdResolution resolution;
    private UuidUuidRemapper sctPrimorialUuidRemapper;
    private HashSet<UUID> skipUuidSet;

    void setSkipUuidSet(HashSet<UUID> skipUuidSet) {
        this.skipUuidSet = skipUuidSet;
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
                        enclosingUuid = eConcept.primordialUuid;
                        // :!!!:DEBUG:
//                        if (debugSet.contains(eConcept.getPrimordialUuid())) {
//                            System.out.println(":!!!:DEBUG: BinaryChangeSetResolveIds .IN. \n" + eConcept.toString());
//                        }

                        if (sctPrimorialUuidRemapper != null) {
                            remapUuids(eConcept);
                            enclosingUuid = eConcept.primordialUuid;
                        }

                        if (skipUuidSet != null
                                && skipUuidSet.contains(eConcept.primordialUuid)) {
                            eccsLogExceptionsWriter.append("skipped UUID :: " + enclosingUuid.toString());
                            continue;
                        }

                        if (eccsLogPreWriter != null) {
                            eccsLogPreWriter.write("\n------- ");
                            eccsLogPreWriter.write(eConcept.toString());
                            eccsLogPreWriter.write("\n");
                        }

                    } catch (EOFException e) {
                        in.close();
                        break;
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
                    if (eccsLogPostWriter != null) {
                        eccsLogPostWriter.write("\n------- ");
                        eccsLogPostWriter.write(eConcept.toString());
                        eccsLogPostWriter.write("\n");
                    }
                    if (eccsPathExceptionFoundB) {
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

    private TkConcept remapUuids(TkConcept eConcept) throws IOException {
        UUID enclosingPrimordialUuid = sctPrimorialUuidRemapper.getUuid(eConcept.primordialUuid);
        if (enclosingPrimordialUuid != null) {
            eccsLogExceptionsWriter.append("remapped UUID from :: " + eConcept.primordialUuid + " :: to :: " + enclosingPrimordialUuid);
            eConcept.primordialUuid = enclosingPrimordialUuid;
        } else {
            enclosingPrimordialUuid = eConcept.primordialUuid;
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

        return eConcept;
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
            if (this.timeFirstEditL < o.timeFirstEditL) {
                return -1; // instance less than received
            } else if (this.timeFirstEditL > o.timeFirstEditL) {
                return 1; // instance greater than received
            } else {
                return 0;
            }
        }
    }
}
