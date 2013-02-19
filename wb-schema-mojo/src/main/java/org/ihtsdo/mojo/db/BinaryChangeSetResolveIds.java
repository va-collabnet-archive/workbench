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
    private HashMap<Long, UUID> keepMap;
    private StringBuilder instancesNotKept;
    private SctIdResolution resolution;

    public enum SctIdResolution {

        KEEP_ALL_SCTID, KEEP_NO_ECCS_SCTID, KEEP_LAST_CURRENT_USE
    };

    public BinaryChangeSetResolveIds(String rootDirStr, String targetDirStr,
            SctIdResolution resolutionApproach)
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
    }

    public void processFiles() {
        instancesNotKept = new StringBuilder("\r\n SCTID-UUID instance pairs not kept\r\n");

        pass1CreateKeepMap();
        pass2CreateUpdatedEccsFile(eccsInputFiles);

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
            // FileOutputStream fos = new FileOutputStream(eccsOutputFile);
            // BufferedOutputStream bos = new BufferedOutputStream(fos);
            // try (DataOutputStream out = new DataOutputStream(bos)) {
            for (File file : eccsInputFilesList) {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream in = new DataInputStream(bis);

                while (true) {
                    long timeL;
                    TkConcept eConcept;
                    UUID enclosingUuid;
                    try {
                        timeL = in.readLong();
                        eConcept = new TkConcept(in);
                        enclosingUuid = eConcept.primordialUuid;
                    } catch (EOFException e) {
                        in.close();
                        break;
                    }

                    long firstDate = timeL;

                    // Concept Attributes
                    if (eConcept.getConceptAttributes() != null) {
                        if (firstDate > eConcept.getConceptAttributes().time) {
                            firstDate = eConcept.getConceptAttributes().time;
                        }
                        if (eConcept.getConceptAttributes().additionalIds != null) {
                            preList = eConcept.getConceptAttributes().additionalIds;
                            postList = processIdListWithFilter(enclosingUuid, preList);
                            eConcept.getConceptAttributes().additionalIds = postList;
                            firstDate = findIdListFirstUseDate(postList, firstDate);
                        }
                    }
                    // Description
                    List<TkDescription> descriptionList = eConcept.getDescriptions();
                    if (descriptionList != null) {
                        for (TkDescription tkd : descriptionList) {
                            if (firstDate > tkd.time) {
                                firstDate = tkd.time;
                            }
                            if (tkd.additionalIds != null) {
                                preList = tkd.additionalIds;
                                postList = processIdListWithFilter(enclosingUuid, preList);
                                tkd.additionalIds = postList;
                                firstDate = findIdListFirstUseDate(postList, firstDate);
                            }
                        }
                    }
                    // Relationships
                    List<TkRelationship> relationshipList = eConcept.getRelationships();
                    if (relationshipList != null) {
                        for (TkRelationship tkr : relationshipList) {
                            if (firstDate > tkr.time) {
                                firstDate = tkr.time;
                            }
                            if (tkr.additionalIds != null) {
                                preList = tkr.additionalIds;
                                postList = processIdListWithFilter(enclosingUuid, preList);
                                tkr.additionalIds = postList;
                                firstDate = findIdListFirstUseDate(postList, firstDate);
                            }
                        }
                    }
                    // Refset Members
                    List<TkRefexAbstractMember<?>> memberList = eConcept.getRefsetMembers();
                    if (memberList != null) {
                        for (TkRefexAbstractMember<?> tkram : memberList) {
                            if (firstDate > tkram.time) {
                                firstDate = tkram.time;
                            }
                            if (tkram.additionalIds != null) {
                                preList = tkram.additionalIds;
                                postList = processIdListWithFilter(enclosingUuid, preList);
                                tkram.additionalIds = postList;
                                firstDate = findIdListFirstUseDate(postList, firstDate);
                            }
                        }
                    }

                    // out.writeLong(timeL);
                    // eConcept.writeExternal(out);
                    bcsList.add(new TkChangeSortable(timeL, firstDate, eConcept));
                }
            }
            // out.flush();
            // }
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(BinaryChangeSetResolveIds.class.getName()).log(Level.SEVERE, null, ex);
        }

        Collections.sort(bcsList);
        try {
            FileOutputStream fos = new FileOutputStream(eccsOutputFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            try (DataOutputStream out = new DataOutputStream(bos)) {
                for (TkChangeSortable tkcs : bcsList) {
                    out.writeLong(tkcs.time);
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
            List<TkIdentifier> idList) {
        if (resolution.compareTo(SctIdResolution.KEEP_ALL_SCTID) == 0) {
            return idList; // do not filter list
        }
        ArrayList<TkIdentifier> filteredIdList = new ArrayList<>();
        for (TkIdentifier tki : idList) {
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

        Long time;
        Long firstDate;
        TkConcept eConcept;

        public TkChangeSortable(Long time, Long firstDate, TkConcept eConcept) {
            this.time = time;
            this.firstDate = firstDate;
            this.eConcept = eConcept;
        }

        @Override
        public int compareTo(TkChangeSortable o) {
            if (this.firstDate < o.firstDate) {
                return -1; // instance less than received
            } else if (this.firstDate > o.firstDate) {
                return 1; // instance greater than received
            } else {
                return 0;
            }
        }
    }
}
