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
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

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
    private SctIdResolution resolution;
    private UuidUuidRemapper relLogicalUuidRemapper;

    void setRelUuidRemap(UuidUuidRemapper idLookup) {
        relLogicalUuidRemapper = idLookup;
    }

    public enum SctIdResolution {

        KEEP_ALL_SCTID, KEEP_NO_ECCS_SCTID, KEEP_LAST_CURRENT_USE
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

        this.relLogicalUuidRemapper = null;
    }

    public void processFiles() throws IOException {
        instancesNotKept = new StringBuilder("\r\n SCTID-UUID instance pairs not kept\r\n");

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
        eccsLogExceptionsWriter.flush();
        eccsLogExceptionsWriter.close();

        Logger logger = Logger.getLogger(BinaryChangeSetResolveIds.class.getName());
        logger.log(Level.INFO, instancesNotKept.toString());
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

        System.out.println("\n\n############ KEEP LIST ############");
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
                    bw.write("\r\n");
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
                        // :!!!:DEBUG:
                        if (eConcept.getPrimordialUuid().compareTo(UUID.fromString("d439284a-21d1-3ef9-842c-27f017c9f042")) == 0) {
                            System.out.println(":!!!:DEBUG: BinaryChangeSetResolveIds .IN. \n" + eConcept.toString());
                        }
                        if (eccsLogPreWriter != null) {
                            eccsLogPreWriter.write("\n------- ");
                            eccsLogPreWriter.write(eConcept.toString());
                            eccsLogPreWriter.write("\n");
                        }

                        enclosingUuid = eConcept.primordialUuid;
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
                                postList = processIdListWithFilter(enclosingUuid, preList);
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

                            // 
                            if (relLogicalUuidRemapper != null) {
                                UUID uuid = relLogicalUuidRemapper.getUuid(tkr.primordialUuid);
                                if (uuid != null) {
                                    tkr.primordialUuid = uuid;
                                }
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

                    if (eConcept.getPrimordialUuid().compareTo(UUID.fromString("d439284a-21d1-3ef9-842c-27f017c9f042")) == 0) {
                        System.out.println(":!!!:DEBUG: BinaryChangeSetResolveIds .OUT. \n" + eConcept.toString());
                    }
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
                            instancesNotKept.append("\r\n");
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
