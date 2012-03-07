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
package org.ihtsdo.helper.bdb;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author marc
 */
public class MultiEditorContradictionDetector implements ProcessUnfetchedConceptDataBI {
    // I_ProcessConcepts
    // ProcessUnfetchedConceptDataBI

    private int refsetNid;
    private ViewCoordinate vc;
    NidBitSetBI nidSet;
    List<MultiEditorContradictionCase> contradictionCaseList;
    HashSet<Integer> watchSet;
    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MultiEditorContradictionDetector(int refsetNid, ViewCoordinate vc,
            List<MultiEditorContradictionCase> cl, HashSet<Integer> ws)
            throws IOException {
        this.refsetNid = refsetNid;
        this.vc = vc;
        this.nidSet = Ts.get().getAllConceptNids();
        this.contradictionCaseList = cl;
        this.watchSet = ws;
    }

    @Override
    public boolean continueWork() {
        return true;
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return nidSet;
    }

    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
        ConceptVersionBI conceptVersion = fetcher.fetch(vc);
        if (watchSet != null && watchSet.contains(Integer.valueOf(cNid))) { // :!!!:
            System.out.println("::: FOUND WATCH CONCEPT: " + conceptVersion.toUserString());
        }

        if (conceptVersion.getPrimUuid() != null) {
            Collection<? extends RefexChronicleBI<?>> rcbic;
            rcbic = conceptVersion.getRefexMembers(refsetNid);
            if (rcbic.size() > 1) {

                // CONVERT ARRAY HASHSET OF AUTHOR_TIME_HASH_BYTES
                ArrayList<HashSet<UUID>> authTimeSetsList = new ArrayList<HashSet<UUID>>();
                for (RefexChronicleBI<?> rcbi : rcbic) {
                    RefexArrayOfBytearrayVersionBI raobvbi = (RefexArrayOfBytearrayVersionBI) rcbi;
                    byte[][] aoba = raobvbi.getArrayOfByteArray();
                    // convert array to hashset
                    HashSet<UUID> authorTimeHashSet = new HashSet<UUID>();
                    for (byte[] bs : aoba) {
                        authorTimeHashSet.add(UuidT5Generator.getUuidFromRawBytes(bs));
                    }

                    // add hashset to list
                    authTimeSetsList.add(authorTimeHashSet);
                }

                // SORT BY HASHSET LENGTH
                Comparator<HashSet<UUID>> comp = new Comparator<HashSet<UUID>>() {

                    @Override
                    public int compare(HashSet<UUID> o1, HashSet<UUID> o2) {
                        if (o1.size() > o2.size()) { // larger set first
                            return 1;
                        } else if ((o1.size() < o2.size())) {
                            return -1;
                        }
                        return 0;
                    }
                };
                Collections.sort(authTimeSetsList, comp);

                // CHECK FOR CONTRADICTION -- by smaller set not contained in larger set.
                boolean contradictionByContainmentNotFound = true;
                boolean contradictionByMonotonicNotFound = true;
                int i = 0;
                while (contradictionByContainmentNotFound && i < authTimeSetsList.size() - 1) {
                    int j = i + 1;
                    while (contradictionByContainmentNotFound && j < authTimeSetsList.size()) {
                        HashSet<UUID> a = authTimeSetsList.get(i);
                        HashSet<UUID> b = authTimeSetsList.get(j);
                        if (b.containsAll(a) == false) {
                            contradictionByContainmentNotFound = false;
                        }
                        j++;
                    }
                    i++;
                }

                //

                // REPORT ANY CONTRADICTING CONCEPTS
                if (contradictionByContainmentNotFound == false) {
                    // CREATE HashMap of AuthorTimeUuid
                    HashMap<UUID, String> authTimeMapComputed = new HashMap<UUID, String>();
                    HashMap<UUID, String> authTimeMapMissing = new HashMap<UUID, String>();
                    for (Integer sap : conceptVersion.getAllSapNids()) {
                        // if (sap > Bdb.getSapDb().getReadOnlyMax()) { // excludes read-only db
                        // COMPUTE AUTHOR_TIME_UUID5
                        int authorNid = Ts.get().getAuthorNidForSapNid(sap);
                        ConceptChronicleBI authorConcept = Ts.get().getConcept(authorNid);
                        long time = Ts.get().getTimeForSapNid(sap);
                        String atStr = authorConcept.getPrimUuid().toString() + Long.toString(time);
                        UUID type5Uuid = UuidT5Generator.get(UuidT5Generator.AUTHOR_TIME_ID, atStr);

                        // STORE <Key = UUID, Value= data string>
                        String valueStr = toAuthorTimeString(time, authorConcept, type5Uuid);
                        authTimeMapComputed.put(type5Uuid, valueStr);
                        //}
                    }

                    // :!!!:DEBUG:BEGIN
                    int setCounter = 0;
                    System.out.println("\r\n!!! CONCEPT: " + conceptVersion.toUserString());
                    for (HashSet<UUID> hs : authTimeSetsList) {
                        System.out.println("\r\n AuthorTime HashSet #" + setCounter++);
                        for (UUID uuid : hs) {
                            System.out.println(" UUID Entry: " + uuid.toString());
                        }

                    }
                    // :!!!:DEBUG:END:

                    // determine which hash codes are in contradiction
                    // ACCUMULATE CONTRADICTIONS -- smaller set not contained in larger set.
                    HashSet<UUID> accumContradictionsHashSet = new HashSet<UUID>();
                    i = 0;
                    while (i < authTimeSetsList.size() - 1) {
                        int j = i + 1;
                        while (j < authTimeSetsList.size()) {
                            HashSet<UUID> a = authTimeSetsList.get(i);
                            HashSet<UUID> b = authTimeSetsList.get(j);
                            if (b.containsAll(a) == false) {
                                HashSet<UUID> aTemp = new HashSet<UUID>(a);
                                aTemp.removeAll(b);
                                accumContradictionsHashSet.addAll(aTemp);

                                HashSet<UUID> bTemp = new HashSet<UUID>(b);
                                bTemp.removeAll(a);
                                accumContradictionsHashSet.addAll(bTemp);
                            }
                            j++;
                        }
                        i++;
                    }

                    ArrayList<String> caseList = new ArrayList<String>();
                    for (UUID uuid : accumContradictionsHashSet) {
                        String s = authTimeMapComputed.get(uuid);
                        if (s != null) {
                            caseList.add(s);
                        } else {
                            // not present in computed authorTimeMapComputed
                            String valueStr = toAuthorTimeMissingString(uuid);
                            authTimeMapMissing.put(uuid, valueStr);
                        }
                    }
                    //if (testLevel > 0) {
                    Set<UUID> authTimeSetMissing = authTimeMapMissing.keySet();
                    HashSet<UUID> accumContraKnownHashSet = new HashSet<UUID>();
                    i = 0;
                    while (i < authTimeSetsList.size() - 1) {
                        int j = i + 1;
                        while (j < authTimeSetsList.size()) {
                            HashSet<UUID> a = authTimeSetsList.get(i);
                            HashSet<UUID> b = authTimeSetsList.get(j);
                            b.removeAll(authTimeSetMissing);
                            a.removeAll(authTimeSetMissing);
                            if (b.containsAll(a) == false) {
                                HashSet<UUID> aTemp = new HashSet<UUID>(a);
                                aTemp.removeAll(b);
                                accumContraKnownHashSet.addAll(aTemp);

                                HashSet<UUID> bTemp = new HashSet<UUID>(b);
                                bTemp.removeAll(a);
                                accumContraKnownHashSet.addAll(bTemp);
                            }
                            j++;
                        }
                        i++;
                    }

                    //}

                    // ADD TO CASE LIST
                    MultiEditorContradictionCase caseToAdd;
                    caseToAdd = new MultiEditorContradictionCase(cNid, caseList);
                    caseToAdd.setAuthTimeMapComputed(authTimeMapComputed);
                    caseToAdd.setAuthTimeMapMissing(authTimeMapMissing);
                    contradictionCaseList.add(caseToAdd);
                }

            } // if rcbic.size() > 1
        }
    }

    private String toAuthorTimeString(long time, ConceptChronicleBI author, UUID uuid) {
        StringBuilder sb = new StringBuilder();

        sb.append("Time:\t");
        Date d = new Date(time);
        sb.append(formatter.format(d));
        sb.append("\tAuthor:\t");
        sb.append(author.getPrimUuid().toString());
        sb.append("\t");
        sb.append(author.toUserString());
        sb.append("\tCommitRecordHash:\t");
        sb.append(uuid);

        return sb.toString();
    }

    private String toAuthorTimeMissingString(UUID uuid) {
        StringBuilder sb = new StringBuilder();

        sb.append("Time:\t");
        sb.append("????-??-?? ??:??:??");
        sb.append("\tAuthor:\t");
        sb.append("........-....-....-....-............");
        sb.append("\t");
        sb.append("UNKNOWN");
        sb.append("\tCommitRecordHash:\t");
        sb.append(uuid.toString());

        return sb.toString();
    }
}
