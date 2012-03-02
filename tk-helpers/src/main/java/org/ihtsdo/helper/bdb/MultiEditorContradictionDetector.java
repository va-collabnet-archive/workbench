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
import java.util.*;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
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
            Collection<? extends RefexChronicleBI<?>> rcbic = conceptVersion.getRefexMembers(refsetNid);
            if (rcbic.size() > 1) {

                // CONVERT ARRAY HASHSET OF AUTHOR_TIME_HASH_BYTES
                ArrayList<HashSet<UUID>> authorTimeHashList = new ArrayList<HashSet<UUID>>();
                for (RefexChronicleBI<?> rcbi : rcbic) {
                    RefexArrayOfBytearrayVersionBI raobvbi = (RefexArrayOfBytearrayVersionBI) rcbi;
                    byte[][] aoba = raobvbi.getArrayOfByteArray();
                    // convert array to hashset
                    HashSet<UUID> authorTimeHash = new HashSet<UUID>();
                    for (byte[] bs : aoba) {
                        authorTimeHash.add(UuidT5Generator.getUuidFromRawBytes(bs));
                    }

                    // add hashset to list
                    authorTimeHashList.add(authorTimeHash);
                }

                // SORT BY HASHSET LENGTH
                Comparator<HashSet<UUID>> comp = new Comparator<HashSet<UUID>>() {

                    @Override
                    public int compare(HashSet<UUID> o1, HashSet<UUID> o2) {
                        if (o1.size() < o2.size()) { // larger set first
                            return 1;
                        } else if ((o1.size() > o2.size())) {
                            return -1;
                        }
                        return 0;
                    }
                };
                Collections.sort(authorTimeHashList, comp);

                // CHECK FOR CONTRADICTION -- smaller set not contained in larger set.
                boolean contradictionNotFound = true;
                int i = 0;
                while (contradictionNotFound && i < authorTimeHashList.size() - 1) {
                    int j = i + 1;
                    while (contradictionNotFound && j < authorTimeHashList.size()) {
                        HashSet<UUID> a = authorTimeHashList.get(i);
                        HashSet<UUID> b = authorTimeHashList.get(j);
                        authorTimeHashList.get(i).containsAll(rcbic);
                        if (a.containsAll(b) == false) {
                            contradictionNotFound = false;
                        }
                        j++;
                    }
                    i++;
                }

                // REPORT ANY CONTRADICTING CONCEPTS
                if (contradictionNotFound == false) {
                    contradictionCaseList.add(new MultiEditorContradictionCase(cNid));
                }

            } // if rcbic.size() > 1
        }
    }
}
