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
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
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

    private int commitRecRefsetNid;
    private int adjudicateRecRefsetNid;
    private ViewCoordinate vc;
    private final int maxSap; // does not compute AuthorTimeHash from readonly database.
    private boolean ignoreNonVisibleAth; // ignore SAP not computeable from concept
    private NidBitSetBI nidSet;
    List<MultiEditorContradictionCase> contradictionCaseList;
    HashSet<Integer> watchSet;
    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MultiEditorContradictionDetector(int commitRecRefsetNid,
            int adjudicationRecRefsetNid,
            ViewCoordinate vc,
            List<MultiEditorContradictionCase> cl,
            HashSet<Integer> ws)
            throws IOException {
        this.commitRecRefsetNid = commitRecRefsetNid;
        this.adjudicateRecRefsetNid = adjudicationRecRefsetNid;
        this.vc = vc;
        this.nidSet = Ts.get().getAllConceptNids();
        this.contradictionCaseList = cl;
        this.watchSet = ws;
        this.ignoreNonVisibleAth = false;
        maxSap = Integer.MIN_VALUE;
    }

    public MultiEditorContradictionDetector(int commitRecRefsetNid,
            int adjudicationRecRefsetNid,
            ViewCoordinate vc,
            List<MultiEditorContradictionCase> cl,
            HashSet<Integer> ws,
            boolean ignoreReadOnlySap,
            boolean ignoreNonVisibleAth)
            throws IOException {
        this.commitRecRefsetNid = commitRecRefsetNid;
        this.adjudicateRecRefsetNid = adjudicationRecRefsetNid;
        this.vc = vc;
        this.nidSet = Ts.get().getAllConceptNids();
        this.contradictionCaseList = cl;
        this.watchSet = ws;
        this.ignoreNonVisibleAth = ignoreNonVisibleAth;
        if (ignoreReadOnlySap) {
            maxSap = Ts.get().getReadOnlyMaxSap(); //
        } else {
            maxSap = Integer.MIN_VALUE;
        }

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
            System.out.println("\r\n::: FOUND WATCH CONCEPT: " + conceptVersion.toUserString());
        }

        if (conceptVersion.getPrimUuid() == null) {
            return; // concept missing important data
        }

        // Add all Commit Refset Author Time Hash (Ath) Sets in a List.
        ArrayList<HashSet<UUID>> commitRefsetAthSetsList;
        commitRefsetAthSetsList = getAthSetsFromRefset(conceptVersion, commitRecRefsetNid);
        if (commitRefsetAthSetsList.isEmpty()) {
            return; // no commit records to review
        }
        // Add all Adjudication Refset Author Time Hash (Ath) Sets in a List.
        ArrayList<HashSet<UUID>> truthRefsetAthSetsList;
        truthRefsetAthSetsList = getAthSetsFromRefset(conceptVersion, adjudicateRecRefsetNid);
        // Put concept derived Author Time Hash (ATH) Sets in a Map
        HashMap<UUID, String> conceptComputedAthMap;
        conceptComputedAthMap = getComputedAthMap(conceptVersion, false);
        // Put concept missing Author Time Hash (ATH) Sets in a Map
        HashMap<UUID, String> conceptMissingAthMap;
        conceptMissingAthMap = getMissingAthMap(conceptComputedAthMap,
                commitRefsetAthSetsList, truthRefsetAthSetsList);

        // TEST FOR CONTRADICTIONS
        Set<UUID> authTimeSetMissing = conceptMissingAthMap.keySet();
        HashSet<UUID> accumContradictedAthUuidSet = new HashSet<UUID>();
        int i = 0;
        while (i < commitRefsetAthSetsList.size() - 1) {
            int j = i + 1;
            while (j < commitRefsetAthSetsList.size()) {
                HashSet<UUID> a = commitRefsetAthSetsList.get(i);
                HashSet<UUID> b = commitRefsetAthSetsList.get(j);
                if (ignoreNonVisibleAth) {
                    b.removeAll(authTimeSetMissing);
                    a.removeAll(authTimeSetMissing);
                }
                if (b.containsAll(a) == false) {
                    HashSet<UUID> aTemp = new HashSet<UUID>(a);
                    aTemp.removeAll(b);
                    accumContradictedAthUuidSet.addAll(aTemp);

                    HashSet<UUID> bTemp = new HashSet<UUID>(b);
                    bTemp.removeAll(a);
                    accumContradictedAthUuidSet.addAll(bTemp);
                }
                j++;
            }
            i++;
        }

        // REPORT ANY CONTRADICTING CONCEPTS
        if (!accumContradictedAthUuidSet.isEmpty()) {

            // List case information in time order
            ArrayList<String> caseList = new ArrayList<String>();
            for (UUID uuid : accumContradictedAthUuidSet) {
                String s = conceptComputedAthMap.get(uuid);
                if (s != null) {
                    caseList.add(s);
                }
            }
            Collections.sort(caseList, String.CASE_INSENSITIVE_ORDER);

            // FIND THE ADJUDICATION COMMIT RECORD VALUES
            ArrayList<HashSet<UUID>> truthATHSetsList = new ArrayList<HashSet<UUID>>();

            // ADD TO CASE LIST
            MultiEditorContradictionCase caseToAdd;
            caseToAdd = new MultiEditorContradictionCase(cNid, caseList);
            caseToAdd.setAuthTimeMapComputed(conceptComputedAthMap);
            caseToAdd.setAuthTimeMapMissing(conceptMissingAthMap);
            caseToAdd.setAuthTimeSetsList(commitRefsetAthSetsList);
            caseToAdd.setAuthTimeSetsTruthList(truthATHSetsList);
            contradictionCaseList.add(caseToAdd);
        }
    }

    /**
     * get Author Time Hash (ATH) sets from refset for the provided concept
     */
    private ArrayList<HashSet<UUID>> getAthSetsFromRefset(ConceptVersionBI concept, int refset)
            throws IOException {
        ArrayList<HashSet<UUID>> authTimeSetsList = new ArrayList<HashSet<UUID>>();
        Collection<? extends RefexChronicleBI<?>> rcbic;
        rcbic = concept.getRefexMembers(refset);
        if (rcbic.size() > 1) {

            // CONVERT ARRAY HASHSET OF AUTHOR_TIME_HASH_BYTES
            for (RefexChronicleBI<?> rcbi : rcbic) {
                RefexArrayOfBytearrayVersionBI raobvbi = (RefexArrayOfBytearrayVersionBI) rcbi;
                byte[][] aoba = raobvbi.getArrayOfByteArray();
                // convert array to hashset
                HashSet<UUID> athSet = new HashSet<UUID>();
                for (byte[] bs : aoba) {
                    athSet.add(UuidT5Generator.getUuidFromRawBytes(bs));
                }

                // add hashset to list
                authTimeSetsList.add(athSet);
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
        }
        return authTimeSetsList;
    }

    private HashMap<UUID, String> getComputedAthMap(ConceptVersionBI concept, boolean readOnly)
            throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        HashMap<UUID, String> conceptComputedAthMap = new HashMap<UUID, String>();
        for (Integer sap : concept.getAllSapNids()) {
            if (sap > maxSap) { // excludes read-only db
                // COMPUTE AUTHOR_TIME_UUID5
                int authorNid = Ts.get().getAuthorNidForSapNid(sap);
                ConceptChronicleBI authorConcept = Ts.get().getConcept(authorNid);
                long time = Ts.get().getTimeForSapNid(sap);
                String str = authorConcept.getPrimUuid().toString() + Long.toString(time);
                UUID type5Uuid = UuidT5Generator.get(UuidT5Generator.AUTHOR_TIME_ID, str);

                // STORE <Key = UUID, Value= data string>
                String valueStr = toStringAuthorTime(time, authorConcept, type5Uuid);
                conceptComputedAthMap.put(type5Uuid, valueStr);
            }
        }
        return conceptComputedAthMap;
    }

    private HashMap<UUID, String> getMissingAthMap(HashMap<UUID, String> computedMap,
            ArrayList<HashSet<UUID>> commitRefsetAthSetsList,
            ArrayList<HashSet<UUID>> truthRefsetAthSetsList) {
        HashMap<UUID, String> conceptMissingAthMap = new HashMap<UUID, String>();

        HashSet<UUID> allRefsetUuids = new HashSet<UUID>();
        for (HashSet<UUID> uuidSet : commitRefsetAthSetsList) {
            allRefsetUuids.addAll(uuidSet);
        }
        // :???: truthRefsetAthSetsList may already be contained in commitRefsetAthSetsList
        for (HashSet<UUID> uuidSet : truthRefsetAthSetsList) {
            allRefsetUuids.addAll(uuidSet);
        }

        for (UUID uuid : allRefsetUuids) {
            String s = computedMap.get(uuid);
            if (s == null) {
                // not present in conceptComputedAthMap
                String valueStr = toStringAuthorTimeMissing(uuid);
                conceptMissingAthMap.put(uuid, valueStr);
            }
        }

        return conceptMissingAthMap;
    }

    private String toStringAuthorTime(long time, ConceptChronicleBI author, UUID uuid) {
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

    private String toStringAuthorTimeMissing(UUID uuid) {
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
