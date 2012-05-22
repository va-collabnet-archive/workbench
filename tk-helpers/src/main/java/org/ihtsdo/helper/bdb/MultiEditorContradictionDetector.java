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
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author marc
 */
public class MultiEditorContradictionDetector implements ProcessUnfetchedConceptDataBI {

    private int commitRecRefsetNid;
    private int adjudicateRecRefsetNid;
    private ViewCoordinate vc;
    private final boolean ignoreNonVisibleAth; // ignore SAP not computeable from concept
    private final boolean ignoreReadOnlySap;
    private final int maxSap; // does not compute AuthorTimeHash from readonly database.
    private NidBitSetBI nidSet;
    private HashMap<UUID, Collection<Integer>> sapNidTimeAuthMap = new HashMap<UUID, Collection<Integer>>();
    private HashSet<Integer> conflictSaps = new HashSet<Integer>();
    List<MultiEditorContradictionCase> contradictionCaseList;
    HashSet<Integer> watchSet;
    List<MultiEditorContradictionCase> watchCaseList;
    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    List<Integer> componentsMissingCommitRecord;
    int snorocketAuthorNid;
    int userAuthorNid;

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
        this.nidSet = Ts.get().getAllConceptNids(); //
        this.contradictionCaseList = cl;
        this.watchSet = ws;
        this.watchCaseList = null;
        if (this.watchSet != null) {
            this.watchCaseList = new ArrayList<MultiEditorContradictionCase>();
        }
        this.ignoreNonVisibleAth = ignoreNonVisibleAth;
        this.ignoreReadOnlySap = ignoreReadOnlySap;
        this.maxSap = Ts.get().getReadOnlyMaxSap();
        this.componentsMissingCommitRecord = new ArrayList<Integer>();

        snorocketAuthorNid = Ts.get().getNidForUuids(UUID.fromString("7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9"));
        userAuthorNid = Ts.get().getNidForUuids(UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c"));
    }

    public MultiEditorContradictionDetector(int commitRecRefsetNid,
            int adjudicationRecRefsetNid,
            ViewCoordinate vc,
            List<MultiEditorContradictionCase> cl,
            HashSet<Integer> ws,
            NidBitSetBI nidSet,
            boolean ignoreReadOnlySap,
            boolean ignoreNonVisibleAth)
            throws IOException {
        this.commitRecRefsetNid = commitRecRefsetNid;
        this.adjudicateRecRefsetNid = adjudicationRecRefsetNid;
        this.vc = vc;
        this.nidSet = nidSet; //
        this.contradictionCaseList = cl;
        this.watchSet = ws;
        this.watchCaseList = null;
        if (this.watchSet != null) {
            this.watchCaseList = new ArrayList<MultiEditorContradictionCase>();
        }
        this.ignoreNonVisibleAth = ignoreNonVisibleAth;
        this.ignoreReadOnlySap = ignoreReadOnlySap;
        this.maxSap = Ts.get().getReadOnlyMaxSap();
        this.componentsMissingCommitRecord = new ArrayList<Integer>();

        snorocketAuthorNid = Ts.get().getNidForUuids(UUID.fromString("7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9"));
        userAuthorNid = Ts.get().getNidForUuids(UUID.fromString("7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9"));
    }

    @Override
    public boolean continueWork() {
        return true;
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return nidSet;
    }

    public List<MultiEditorContradictionCase> getWatchCaseList() {
        return watchCaseList;
    }

    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
        ConceptVersionBI conceptVersion = fetcher.fetch(vc);
        Boolean watchConcept = false;
        if (watchSet != null && watchSet.contains(Integer.valueOf(cNid))) {
            watchConcept = true;
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
        HashMap<UUID, String> conceptComputedAthMap = getComputedAthMap(conceptVersion, false);
        HashMap<UUID, String> conceptComputedAthDiffMap = getComputedAthMap(conceptVersion, true);
        // Add all Adjudication Refset Author Time Hash (Ath) Sets in a List.
        ArrayList<HashSet<UUID>> replacementSet;
        replacementSet = getAthSetsFromRefset(conceptVersion, adjudicateRecRefsetNid);

        for (HashSet<UUID> truthSet : replacementSet) {
            truthSet.retainAll(conceptComputedAthMap.keySet());
        }
        ArrayList<HashSet<UUID>> truthRefsetAthSetsList = new ArrayList<HashSet<UUID>>();
        for (HashSet<UUID> truthSet : replacementSet) {
            if (!truthSet.isEmpty()) {
                truthRefsetAthSetsList.add(truthSet);
            }
        }
        // SORT BY HASHSET LENGTH
        Collections.sort(truthRefsetAthSetsList, new SizeComparator());

        // Put concept derived Author Time Hash (ATH) Sets in a Map

        // Put concept missing Author Time Hash (ATH) Sets in a Map
        HashMap<UUID, String> conceptMissingAthMap;
        conceptMissingAthMap = getMissingAthMap(conceptComputedAthMap,
                commitRefsetAthSetsList, truthRefsetAthSetsList);

        // TEST FOR CONTRADICTIONS
        Set<UUID> authTimeSetMissing = null;
        if (ignoreNonVisibleAth) {
            authTimeSetMissing = conceptMissingAthMap.keySet();
        }

        // Check for computed commit without CommitRecord
        for (HashSet<UUID> hs : commitRefsetAthSetsList) {
            for (UUID uuid : hs) {
                conceptComputedAthDiffMap.remove(uuid); // removed actual commits from computed
            }
        }
        if (conceptComputedAthDiffMap.isEmpty() == false) {
            componentsMissingCommitRecord.add(conceptVersion.getNid());
        }

        // REMOVE EXCLUDED VALUES
        if (authTimeSetMissing != null && authTimeSetMissing.size() > 0) {
            for (Set<UUID> uset : commitRefsetAthSetsList) {
                uset.removeAll(authTimeSetMissing);
            }
            Collections.sort(commitRefsetAthSetsList, new SizeComparator());
        }

        // TEST FOR CONTRADICTIONS
        HashSet<UUID> lesser;
        HashSet<UUID> greater;
        HashSet<UUID> diff;
        HashSet<UUID> accumDiffSet = new HashSet<UUID>();
        // Last truth supercedes all previous truth
        HashSet<UUID> lastTruth = null;
        if (truthRefsetAthSetsList.size() > 0) {
            lastTruth = truthRefsetAthSetsList.get(truthRefsetAthSetsList.size() - 1);
        }
        int editorIdx = 0;
        int editorSize = commitRefsetAthSetsList.size();
        while (editorIdx < editorSize) {
            lesser = commitRefsetAthSetsList.get(editorIdx);
            if (lastTruth != null && lesser.size() <= lastTruth.size()) {
                // compare editor commits with adjudication
                diff = lesserDiffFromGreater(lesser, lastTruth, false);
                accumDiffSet.addAll(diff);
                editorIdx++;
            } else {
                // compare editor commits with next editor commit
                if (editorIdx + 1 < editorSize) {
                    greater = commitRefsetAthSetsList.get(editorIdx + 1);
                    diff = lesserDiffFromGreater(lesser, greater, true);
                    accumDiffSet.addAll(diff);
                }
                editorIdx++;
            }
        }

        // REPORT ANY CONTRADICTING CONCEPTS
        if (!accumDiffSet.isEmpty() || watchConcept) {

            // List case information in time order
            ArrayList<String> caseList = new ArrayList<String>();
            for (UUID uuid : accumDiffSet) {
                String s = conceptComputedAthMap.get(uuid);
                if (s != null) {
                    caseList.add(s);
                }
            }
            Collections.sort(caseList, String.CASE_INSENSITIVE_ORDER);
            HashSet<Integer> componentNids = getComponentNidsInConflict(accumDiffSet, cNid);

            // ADD TO CASE LIST
            MultiEditorContradictionCase caseToAdd;
            caseToAdd = new MultiEditorContradictionCase(cNid, caseList,
                    componentNids, conflictSaps);
            caseToAdd.setAuthTimeMapComputed(conceptComputedAthMap);
            caseToAdd.setAuthTimeMapMissing(conceptMissingAthMap);
            caseToAdd.setAuthTimeSetsList(commitRefsetAthSetsList);
            caseToAdd.setAuthTimeSetsTruthList(truthRefsetAthSetsList);
            if (!accumDiffSet.isEmpty()) {
                contradictionCaseList.add(caseToAdd);
                //:: System.out.println("\r\n** CONFLICT **" + caseToAdd.toStringLong());
            } else {
                watchCaseList.add(caseToAdd);
                //:: System.out.println("\r\n** WATCH **" + caseToAdd.toStringLong());
            }
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
        if (!rcbic.isEmpty()) {

            // CONVERT ARRAY HASHSET OF AUTHOR_TIME_HASH_BYTES
            for (RefexChronicleBI<?> rcbi : rcbic) {
                for (RefexVersionBI<?> rcv : rcbi.getVersions()) {
                    RefexArrayOfBytearrayVersionBI raobvbi = (RefexArrayOfBytearrayVersionBI) rcv;
                    byte[][] aoba = raobvbi.getArrayOfByteArray();
                    // convert array to hashset
                    HashSet<UUID> athSet = new HashSet<UUID>();
                    for (byte[] bs : aoba) {
                        athSet.add(UuidT5Generator.getUuidFromRawBytes(bs));
                    }

                    // add hashset to list
                    authTimeSetsList.add(athSet);
                }
            }

            // SORT BY HASHSET LENGTH
            Collections.sort(authTimeSetsList, new SizeComparator());
        }
        return authTimeSetsList;
    }

    private static class SizeComparator implements Comparator<HashSet<UUID>> {

        @Override
        public int compare(HashSet<UUID> o1, HashSet<UUID> o2) {
            if (o1.size() > o2.size()) { // larger set first
                return 1;
            } else if ((o1.size() < o2.size())) {
                return -1;
            }
            return 0;
        }
    }

    private HashMap<UUID, String> getComputedAthMap(ConceptVersionBI concept, boolean skipExtra)
            throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        HashMap<UUID, String> conceptComputedAthMap = new HashMap<UUID, String>();
        for (Integer sap : concept.getAllSapNids()) {
            if (sap == Integer.MIN_VALUE) {
                continue;
            }
            // SKIP READ ONLY DATABASE
            if (ignoreReadOnlySap && sap <= maxSap) {
                continue;
            }
            // COMPUTE AUTHOR_TIME_UUID5
            int authorNid = Ts.get().getAuthorNidForSapNid(sap);
            // SKIP EXTRA AUTHORS WHICH ARE NOT SYNCHRONIZED
            if (skipExtra && (authorNid == snorocketAuthorNid
                    || authorNid == userAuthorNid
                    || sap <= maxSap)) {
                continue;
            }
            ConceptChronicleBI authorConcept = Ts.get().getConcept(authorNid);
            long time = Ts.get().getTimeForSapNid(sap);
            String str = authorConcept.getPrimUuid().toString() + Long.toString(time);
            UUID type5Uuid = UuidT5Generator.get(UuidT5Generator.AUTHOR_TIME_ID, str);

            // STORE <Key = UUID, Value= data string>
            String valueStr = toStringAuthorTime(time, authorConcept, type5Uuid);
            conceptComputedAthMap.put(type5Uuid, valueStr);
            if (!sapNidTimeAuthMap.containsKey(type5Uuid)) {
                sapNidTimeAuthMap.put(type5Uuid, new ArrayList<Integer>());
            }
            sapNidTimeAuthMap.get(type5Uuid).add(sap);
        }
        return conceptComputedAthMap;
    }

    public boolean hasComponentsMissingCommitRecord() {
        if (componentsMissingCommitRecord != null && componentsMissingCommitRecord.size() > 0) {
            return true;
        } else {
            return false;
        }
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

    private HashSet<Integer> getComponentNidsInConflict(HashSet<UUID> accumDiffSet, int cNid) throws IOException {
        HashSet<Integer> componentNids = new HashSet<Integer>();
        for (UUID timeAuthHash : accumDiffSet) {
            if (sapNidTimeAuthMap.containsKey(timeAuthHash)) {
                for (int sap : sapNidTimeAuthMap.get(timeAuthHash)) {
                    conflictSaps.add(sap);
                }
            }
        }
        ConceptChronicleBI concept = Ts.get().getConcept(cNid);
        componentNids.addAll(concept.getAllNidsForSaps(conflictSaps));
        return componentNids;
    }

    private HashSet<UUID> lesserDiffFromGreater(HashSet<UUID> lesser, HashSet<UUID> greater,
            boolean checkEqualSize) {
        HashSet<UUID> diffSet = new HashSet<UUID>();
        if (greater.containsAll(lesser) == false) {
            HashSet<UUID> lesserTemp = new HashSet<UUID>(lesser);
            lesserTemp.removeAll(greater);
            diffSet.addAll(lesserTemp);

            if (checkEqualSize && (greater.size() == lesser.size())) {
                HashSet<UUID> greaterTemp = new HashSet<UUID>(greater);
                greaterTemp.removeAll(lesser);
                diffSet.addAll(greaterTemp);
            }
        }
        return diffSet;
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
        sb.append("\tComputedHash:\t");
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
        sb.append("\tComputedHash:\t");
        sb.append(uuid.toString());

        return sb.toString();
    }

    public String toStringMissingCommitRecords() {
        StringBuilder sb = new StringBuilder();
        for (Integer nid : componentsMissingCommitRecord) {
            try {
                sb.append(Ts.get().getUuidPrimordialForNid(nid));
                sb.append("\t");
                sb.append(Ts.get().getComponent(nid).toUserString());
                sb.append("\r\n");
            } catch (IOException ex) {
                sb.append(nid);
                sb.append("\tERROR: nid NOT FOUND WITH Ts\r\n");
            }
        }
        return sb.toString();
    }
}
