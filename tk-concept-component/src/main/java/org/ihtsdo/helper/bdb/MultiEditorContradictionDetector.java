/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.helper.bdb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 * The Class MultiEditorContradictionDetector processes concepts and determines
 * if a contradiction exists. This class implements
 * <code>ProcessUnfetchedConceptDataBI</code> and the contradiction detector can
 * be "run" using the terminology store method iterateConceptDataInParallel.
 *
 * @see
 * TerminologyStoreDI#iterateConceptDataInParallel(org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI)
 *
 */
public class MultiEditorContradictionDetector implements ProcessUnfetchedConceptDataBI {

    private int commitRecRefsetNid;
    private int adjudicateRecRefsetNid;
    private ViewCoordinate vc;
    private final boolean ignoreNonVisibleAth; // ignore SAP not computable from concept
    private final boolean ignoreReadOnlySap; // does not compute AuthorTimeHash from readonly database.
    private final int maxSap;
    private NidBitSetBI nidSet;
    private ConcurrentHashMap<UUID, ConcurrentSkipListSet<Integer>> timeAthStampNidMap = new ConcurrentHashMap<UUID, ConcurrentSkipListSet<Integer>>();
    private ConcurrentHashMap<Integer, ConcurrentSkipListSet<UUID>> projectPathNidTimeAthMap = new ConcurrentHashMap<Integer, ConcurrentSkipListSet<UUID>>();
    private ConcurrentSkipListSet<Integer> conflictSaps = new ConcurrentSkipListSet<Integer>();
    List<MultiEditorContradictionCase> contradictionCaseList;
    HashSet<Integer> watchSet;
    List<MultiEditorContradictionCase> watchCaseList;
    ConcurrentSkipListSet<Integer> componentsMissingCommitRecord;
    int snorocketAuthorNid;
    int userAuthorNid;
    int userProjectPathNid;

    /**
     * Instantiates a new multi editor contradiction detector which will process
     * every concept in the database. This can be "run" using the terminology
     * store method iterateConceptDataInParallel.
     *
     * @see
     * TerminologyStoreDI#iterateConceptDataInParallel(org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI)
     *
     * @param commitRecRefsetNid the nid associated with the refset storing
     * commit records
     * @param adjudicationRecRefsetNid the nid associated with the refset
     * sorting adjudication records
     * @param viewCoordinate the view coordinate representing which version of
     * the concept to test for contradictions
     * @param caseList the list of * * * *      * type <code>MultiEditorContradictionCase</code> which will contain
     * any found contradiction
     * @param watchSet a set of concept nids to watch for debugging purposes
     * @param ignoreReadOnlySap set to <code>true</code> to not compute
     * author-time hash for stamps from read-only database
     * @param ignoreNonVisibleAth set to <code>true</code> to ignore stamp nids
     * not computable from the concept
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public MultiEditorContradictionDetector(int commitRecRefsetNid,
            int adjudicationRecRefsetNid,
            ViewCoordinate viewCoordinate,
            List<MultiEditorContradictionCase> caseList,
            HashSet<Integer> watchSet,
            boolean ignoreReadOnlySap,
            boolean ignoreNonVisibleAth)
            throws IOException {
        this.commitRecRefsetNid = commitRecRefsetNid;
        this.adjudicateRecRefsetNid = adjudicationRecRefsetNid;
        this.vc = viewCoordinate;
        this.nidSet = Ts.get().getAllConceptNids();
        this.contradictionCaseList = caseList;
        this.watchSet = watchSet;
        this.watchCaseList = null;
        if (this.watchSet != null) {
            this.watchCaseList = new ArrayList<MultiEditorContradictionCase>();
        }
        this.ignoreNonVisibleAth = ignoreNonVisibleAth;
        this.ignoreReadOnlySap = ignoreReadOnlySap;
        this.maxSap = Ts.get().getReadOnlyMaxStamp();
        this.componentsMissingCommitRecord = new ConcurrentSkipListSet<Integer>();

        snorocketAuthorNid = Ts.get().getNidForUuids(UUID.fromString("7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9"));
        userAuthorNid = Ts.get().getNidForUuids(UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c"));

        int[] viewPathNids = viewCoordinate.getPositionSet().getViewPathNidSet().getSetValues();
        if (viewPathNids.length > 1) {
            throw new UnsupportedOperationException("User must only have one view path. View position set size is: " + viewPathNids.length);
        }
        userProjectPathNid = viewPathNids[0];
    }

    /**
     * Instantiates a new multi editor contradiction detector which will process
     * every concept represented in the
     * <code>nidSet</code>. This can be "run" using the terminology store method
     * iterateConceptDataInParallel.
     *
     * @see
     * TerminologyStoreDI#iterateConceptDataInParallel(org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI)
     *
     * @param commitRecRefsetNid the nid associated with the refset storing
     * commit records
     * @param adjudicationRecRefsetNid the nid associated with the refset
     * sorting adjudication records
     * @param viewCoordinate the view coordinate representing which version of
     * the concept to test for contradictions
     * @param caseList the list of * * * *      * type <code>MultiEditorContradictionCase</code> which will contain
     * any found contradiction
     * @param watchSet a set of concept nids to watch for debugging purposes,
     * creates a list of contradiction cases for just these concepts
     * @param nidSet the set of nids representing the concepts to test for
     * contradictions
     * @param ignoreReadOnlySap set to <code>true</code> to not compute
     * author-time hash for stamps from read-only database
     * @param ignoreNonVisibleAth set to <code>true</code> to ignore stamp nids
     * not computable from the concept
     * @throws IOException Signals that an I/O exception has occurred.
     */
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
        this.nidSet = nidSet;
        this.contradictionCaseList = cl;
        this.watchSet = ws;
        this.watchCaseList = null;
        if (this.watchSet != null) {
            this.watchCaseList = new ArrayList<MultiEditorContradictionCase>();
        }
        this.ignoreNonVisibleAth = ignoreNonVisibleAth;
        this.ignoreReadOnlySap = ignoreReadOnlySap;
        this.maxSap = Ts.get().getReadOnlyMaxStamp();
        this.componentsMissingCommitRecord = new ConcurrentSkipListSet<Integer>();

        snorocketAuthorNid = Ts.get().getNidForUuids(UUID.fromString("7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9"));
        userAuthorNid = Ts.get().getNidForUuids(UUID.fromString("7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9"));

        int[] viewPathNids = vc.getPositionSet().getViewPathNidSet().getSetValues();
        if (viewPathNids.length > 1) {
            throw new UnsupportedOperationException("User must only have one view path. View position set size is: " + viewPathNids.length);
        }
        userProjectPathNid = viewPathNids[0];
    }

    /**
     *
     * @return <code>true</code>
     */
    @Override
    public boolean continueWork() {
        return true;
    }

    /**
     *
     * @return the set of nids representing the concepts to test for
     * contradictions
     * @throws IOException
     */
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return nidSet;
    }

    /**
     * Gets the list contradictions generated from the concepts represented in
     * the
     * <code>watchSet</code>.
     *
     * @return the list of contradictions from the watch concepts
     */
    public List<MultiEditorContradictionCase> getWatchCaseList() {
        return watchCaseList;
    }

    /**
     * Processes the given concept to see if contradictions exist.
     *
     * @param cNid the nid of the concept for processing
     * @param fetcher the fetcher for getting the concept version associated
     * with the <code>cNid</code> from the database
     * @throws Exception indicates an exception has occurred
     */
    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
        ConceptVersionBI conceptVersion = fetcher.fetch(vc);
        Boolean watchConcept = false;

        //SET UP DATA FOR CONTRADICTION DETECTION
        if (watchSet != null && watchSet.contains(Integer.valueOf(cNid))) {
            watchConcept = true;
        }

        if (conceptVersion.getPrimUuid() == null) {
            return; // concept missing important data
        }

        // Add all Commit Refset Author Time Hash (Ath) Sets in a List.
        ArrayList<HashSet<UUID>> commitRefsetAthSetsList = getAthSetsFromRefset(conceptVersion, commitRecRefsetNid);
        // Add all Adjudication Refset Author Time Hash (Ath) Sets in a List.
        ArrayList<HashSet<UUID>> replacementSet = getAthSetsFromRefset(conceptVersion, adjudicateRecRefsetNid);
        HashMap<UUID, String> conceptComputedAthAllMap = getComputedAthMap(conceptVersion, false);
        HashMap<UUID, String> conceptComputedAthDiffAllMap = getComputedAthMap(conceptVersion, true);

        //are populated from contradiciton detection algorithm
        HashMap<UUID, String> conceptComputedAthMap = new HashMap<UUID, String>();
        HashMap<UUID, String> conceptMissingAthMap = new HashMap<UUID, String>();
        ArrayList<HashSet<UUID>> truthRefsetAthSetsList = new ArrayList<HashSet<UUID>>();


        //RUN CONTRADICTION DETECTION ALGORITHM
        HashSet<UUID> accumDiffSet = contradictionDetectionAlgorithm(
                cNid,
                userProjectPathNid,
                ignoreNonVisibleAth,
                projectPathNidTimeAthMap,
                conflictSaps,
                timeAthStampNidMap,
                componentsMissingCommitRecord,
                commitRefsetAthSetsList,
                replacementSet,
                conceptComputedAthAllMap,
                conceptComputedAthDiffAllMap,
                conceptComputedAthMap, //empty
                conceptMissingAthMap, //empty
                truthRefsetAthSetsList); //empty

        // REPORT ANY CONTRADICTING CONCEPTS
        if (accumDiffSet != null) {
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
                HashSet<Integer> componentNids = getComponentNidsInContradiction(accumDiffSet,
                        cNid,
                        timeAthStampNidMap,
                        conflictSaps);

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

    }

    /**
     * The algorithm for detecting contradictions. Used in the
     * processUnfetchedConceptData method, but separated for testing purposes.
     * Allows specified author-time sets to be passed in which can include the
     * sets found from a given concept or mock sets generated for testing and
     * doesn't necessitate a database. Use processUnfetchedConceptData to test
     * for a contradiction unless in a testing scenario.
     *
     * @param cNid the nid representing the concept to test
     * @param ignoreReadOnlySap set to <code>true</code> to not compute
     * author-time hash for stamps from read-only database
     * @param ignoreNonVisibleAth set to <code>true</code> to ignore stamp nids
     * not computable from the concept
     * @param projectPathNidTimeAthMap the time-author maps for the specified
     * project path
     * @param conflictStamps the set of stamp nids in conflict, empty--will be
     * populated
     * @param timeAthStampNidMap the map of suthor-times to stamp nids
     * @param componentsMissingCommitRecord the set of components missing commit
     * record, empty--will be populated
     * @param commitRefsetAthSetsList the list of author-times sets for commit
     * @param replacementSet the list of author-times for adjudication commit
     * @param conceptComputedAthAllMap the map of all component uuids to
     * computed author-times for the concept
     * @param conceptComputedAthDiffAllMap the map of all component uuids to
     * computed author-times for the concept without components with
     * non-synchronized authors (classifier)
     * @param conceptComputedAthMap the map of component uuids to computed
     * author-times for the concept on the project path, empty--will be
     * populated
     * @param conceptMissingAthMap the map of missing computed author-times on
     * the concept, empty--will be populated
     * @param truthRefsetAthSetsList the list of author-times for adjudication
     * commit, empty--will be populated
     * @return the set of of contradicting author-time hashes
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     */
    public static HashSet<UUID> contradictionDetectionAlgorithm(
            int cNid,
            int userProjectPathNid,
            boolean ignoreNonVisibleAth,
            ConcurrentHashMap<Integer, ConcurrentSkipListSet<UUID>> projectPathNidTimeAthMap,
            ConcurrentSkipListSet<Integer> conflictStamps,
            ConcurrentHashMap<UUID, ConcurrentSkipListSet<Integer>> timeAthStampNidMap,
            ConcurrentSkipListSet<Integer> componentsMissingCommitRecord,
            ArrayList<HashSet<UUID>> commitRefsetAthSetsList,
            ArrayList<HashSet<UUID>> replacementSet,
            HashMap<UUID, String> conceptComputedAthAllMap,
            HashMap<UUID, String> conceptComputedAthDiffAllMap,
            HashMap<UUID, String> conceptComputedAthMap,
            HashMap<UUID, String> conceptMissingAthMap,
            ArrayList<HashSet<UUID>> truthRefsetAthSetsList) throws IOException, NoSuchAlgorithmException {

        if (commitRefsetAthSetsList.isEmpty()) {
            return null; // no commit records to review
        }

        //filter by path
        Collection<UUID> authTimeForProjectPath = projectPathNidTimeAthMap.get(userProjectPathNid);

        HashMap<UUID, String> conceptComputedAthDiffMap = new HashMap<UUID, String>();

        if (authTimeForProjectPath != null) {
            for (UUID authTime : authTimeForProjectPath) {
                if (conceptComputedAthAllMap.containsKey(authTime)) {
                    conceptComputedAthMap.put(authTime, conceptComputedAthAllMap.get(authTime));
                    if (conceptComputedAthDiffAllMap.containsKey(authTime)) {
                        conceptComputedAthDiffMap.put(authTime, conceptComputedAthDiffAllMap.get(authTime));
                    }
                }
            }
        }

        for (HashSet<UUID> truthSet : replacementSet) {
            truthSet.retainAll(conceptComputedAthMap.keySet());
        }

        for (HashSet<UUID> truthSet : replacementSet) {
            if (!truthSet.isEmpty()) {
                truthRefsetAthSetsList.add(truthSet);
            }
        }

        // SORT BY HASHSET LENGTH
        Collections.sort(truthRefsetAthSetsList, new SizeComparator());

        // Put concept missing Author Time Hash (ATH) Sets in a Map
        conceptMissingAthMap = getMissingAthMap(conceptComputedAthMap,
                commitRefsetAthSetsList, truthRefsetAthSetsList);

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
            componentsMissingCommitRecord.add(cNid);
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

        return accumDiffSet;
    }

    /**
     * Gets Author Time Hash (ATH) sets from the specified
     * <code>refset<code> for the provided
     * <code>concept<code>.
     *
     * @param concept the concept in question
     * @param refset the nid representing the refset (either commit or
     * adjudication)
     * @return the list of sets of author-time hashes
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private ArrayList<HashSet<UUID>> getAthSetsFromRefset(ConceptVersionBI concept, int refset)
            throws IOException {
        ArrayList<HashSet<UUID>> authTimeSetsList = new ArrayList<HashSet<UUID>>();
        Collection<? extends RefexChronicleBI<?>> rcbic;
        rcbic = concept.getRefexMembersActive(vc, refset);
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

    /**
     * The Class SizeComparator compares the size of two
     * <code>HashSets</code>.
     */
    private static class SizeComparator implements Comparator<HashSet<UUID>> {

        /**
         * Compares the size of two
         * <code>HashSets</code>,
         * <code>o1</code> and
         * <code>o2</code>.
         *
         * @param o1 the first <code>HashSet</code> to compare
         * @param o2 the second <code>HashSet</code> to compare
         * @return 1 if <code>o1</code> is larger than <code>o2</code>, -1 * *
         * if <code>o2</code> is larger than <code>o1</code>
         * @see java.util.Comparator#compare
         *
         */
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

    /**
     * Gets a map of computed author-times for the given
     * <code>concept</code>. This is computed based on the stamp nids associated
     * with the version of the concept.
     *
     * @param concept the concept version to use for computing the author-times
     * @param skipExtra set to <code>true</code> to exclude the classifier and
     * "user" authors
     * @return the computed author-times mapped to a String representing the
     * time, author concept, and a string representation of the author-time hash
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private HashMap<UUID, String> getComputedAthMap(ConceptVersionBI concept, boolean skipExtra)
            throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        HashMap<UUID, String> conceptComputedAthMap = new HashMap<UUID, String>();
        for (Integer sap : concept.getAllStampNids()) {
            if (sap == Integer.MIN_VALUE) {
                continue;
            }
            if (Ts.get().getTimeForStampNid(sap) == Long.MIN_VALUE) {
                continue;
            }
            // SKIP READ ONLY DATABASE
            if (ignoreReadOnlySap && sap <= maxSap) {
                continue;
            }
            // COMPUTE AUTHOR_TIME_UUID5
            int authorNid = Ts.get().getAuthorNidForStampNid(sap);
            // SKIP EXTRA AUTHORS WHICH ARE NOT SYNCHRONIZED
            if (skipExtra && (authorNid == snorocketAuthorNid
                    || authorNid == userAuthorNid
                    || sap <= maxSap)) {
                continue;
            }
            ConceptChronicleBI authorConcept = Ts.get().getConcept(authorNid);
            long time = Ts.get().getTimeForStampNid(sap);
            String str = authorConcept.getPrimUuid().toString() + Long.toString(time);
            UUID type5Uuid = UuidT5Generator.get(UuidT5Generator.AUTHOR_TIME_ID, str);

            // STORE <Key = UUID, Value= data string>
            String valueStr = toStringAuthorTime(time, authorConcept, type5Uuid);
            conceptComputedAthMap.put(type5Uuid, valueStr);
            if (!timeAthStampNidMap.containsKey(type5Uuid)) {
                timeAthStampNidMap.put(type5Uuid, new ConcurrentSkipListSet<Integer>());
            }
            timeAthStampNidMap.get(type5Uuid).add(sap);

            int projectPathNid = Ts.get().getPathNidForStampNid(sap);
            if (!projectPathNidTimeAthMap.containsKey(projectPathNid)) {
                projectPathNidTimeAthMap.put(projectPathNid, new ConcurrentSkipListSet<UUID>());
            }
            projectPathNidTimeAthMap.get(projectPathNid).add(type5Uuid);
        }
        return conceptComputedAthMap;
    }

    /**
     * Checks if this contradiction detector has found components missing commit
     * records.
     *
     * @return true, if there are components missing commit records
     */
    public boolean hasComponentsMissingCommitRecord() {
        if (componentsMissingCommitRecord != null && componentsMissingCommitRecord.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates a set of the adjudication and commit author-times and checks to
     * make sure all of the uuids in the
     * <code>computedMap</code> are present.
     *
     * @param computedMap the map containing the computed author-times
     * @param commitRefsetAthSetsList the commit author-times
     * @param truthRefsetAthSetsList the adjudication author-times
     * @return the a map of any computed author-times that were missing from the
     * commit or adjudication maps
     */
    private static HashMap<UUID, String> getMissingAthMap(HashMap<UUID, String> computedMap,
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

    /**
     * Gets the nids of the components in contradiction.
     *
     * @param accumDiffSet the set of author-times found by the contradiction
     * detection algorithm to be in contradiction
     * @param cNid the nid of the enclosing concept
     * @param timeAthStampNidMap a map of author-times mapped to the stamp nids
     * they represent
     * @param contradictionStamps the stamp nids associated with components in
     * contradiction
     * @return the nids of the components in contradiction
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static HashSet<Integer> getComponentNidsInContradiction(HashSet<UUID> accumDiffSet,
            int cNid,
            ConcurrentHashMap<UUID, ConcurrentSkipListSet<Integer>> timeAthStampNidMap,
            ConcurrentSkipListSet<Integer> contradictionStamps) throws IOException {
        HashSet<Integer> componentNids = new HashSet<Integer>();
        for (UUID timeAuthHash : accumDiffSet) {
            if (timeAthStampNidMap.containsKey(timeAuthHash)) {
                for (int sap : timeAthStampNidMap.get(timeAuthHash)) {
                    contradictionStamps.add(sap);
                }
            }
        }
        ConceptChronicleBI concept = Ts.get().getConcept(cNid);
        componentNids.addAll(concept.getAllNidsForStamps(contradictionStamps));
        return componentNids;
    }

    /**
     * Returns a set of the author-times from the
     * <code>lesser</code> set which are not represented in the
     * <code>greater</code>.
     *
     * @param lesser the lesser sized set of author-times
     * @param greater the greater sized set of author-times
     * @param checkEqualSize set to <code>true</code> to check of the sets are
     * the same size
     * @return set of the author-times from the <code>lesser</code> set which
     * are not represented in the <code>greater</code>
     */
    private static HashSet<UUID> lesserDiffFromGreater(HashSet<UUID> lesser, HashSet<UUID> greater,
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

    /**
     * Creates a
     * <code>String</code> representation of the author-time. Includes the time
     * in a yyyy-MM-dd HH:mm:ss format, the author uuid, and the author-time
     * hash.
     *
     * @param time a <code>long</code> representing the time
     * @param author the author concept
     * @param uuid the author-time uuid
     * @return a string representing the specified author-time
     */
    private static String toStringAuthorTime(long time, ConceptChronicleBI author,
            UUID uuid) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

    /**
     * Creates a
     * <code>String</code> representation of a missing author-time. Includes the
     * author-time, but has "unknown" for the time and component.
     *
     * @param uuid the author-time uuid
     * @return a string representing the missing author-time
     */
    private static String toStringAuthorTimeMissing(UUID uuid) {
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

    /**
     * Generates a string representing any missing commit records, including the
     * uuid and a string representation of the components.
     *
     * @return a string representing any missing commit records
     */
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
