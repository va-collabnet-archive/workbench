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
package org.ihtsdo.db.bdb.id;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.util.NidPairForRefex;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.concept.component.relationship.Relationship.Version;
import org.ihtsdo.helper.version.RelativePositionComputerBI;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 * Stores cross-reference information for origin relationships, destination
 * relationship origins, and refex referenced components in a integer array,
 * minimizing the object allocation burden that would otherwise be associated
 * with this information. This class interprets and manages the contents of that
 * array. <br> <h2>Implementation notes</h2> See the class
 * <code>RelationshipIndexRecord</code> for documentation of the structure of
 * the relationship index data.
 *
 * @see RelationshipIndexRecord
 * @author kec
 */
public class IndexCacheRecord {

    private static final int DESTINATION_OFFSET_INDEX = 0;
    private static final int REFEX_OFFSET_INDEX = 1;
    private static final int RELATIONSHIP_OFFSET = 2;
    //~--- fields --------------------------------------------------------------
    private int[] data;

    //~--- constructors --------------------------------------------------------
    public IndexCacheRecord() {
        this.data = new int[]{2, 2};
    }

    public IndexCacheRecord(int[] data) {
        this.data = data;

        if (data == null) {
            this.data = new int[]{2, 2};
        }
    }

    public boolean destinationRelOriginAlreadyThere(int originNid) {
        int arrayLength = data[REFEX_OFFSET_INDEX] - data[DESTINATION_OFFSET_INDEX];
        int index = Arrays.binarySearch(data, data[DESTINATION_OFFSET_INDEX],
                data[DESTINATION_OFFSET_INDEX] + arrayLength, originNid);

        if (index >= 0) {
            return true;    // origin already there...
        }
        return false;
    }
    //~--- methods -------------------------------------------------------------

    public void addDestinationOriginNid(int originNid) {
        if (!destinationRelOriginAlreadyThere(originNid)) {
            int arrayLength = data[REFEX_OFFSET_INDEX] - data[DESTINATION_OFFSET_INDEX];
            int[] destinationOriginNids = new int[arrayLength + 1];

            destinationOriginNids[arrayLength] = originNid;
            System.arraycopy(data, data[DESTINATION_OFFSET_INDEX], destinationOriginNids, 0,
                    destinationOriginNids.length - 1);
            Arrays.sort(destinationOriginNids);
            updateData(getRelationshipOutgoingArray(), destinationOriginNids, getRefexIndexArray());
        }
    }

    public boolean refexAlreadyThere(int memberNid) {
        int arrayLength = data.length - data[REFEX_OFFSET_INDEX];
        int start = data.length - arrayLength;

        for (int i = start; i < data.length; i++) {
            if (data[i] == memberNid) {
                return true;
            }
        }
        return false;
    }

    public void addNidPairForRefex(int refexNid, int memberNid) {
        if (!refexAlreadyThere(memberNid)) {
            int arrayLength = data.length - data[REFEX_OFFSET_INDEX];
            int[] nidPairForRefexArray = new int[arrayLength + 2];

            nidPairForRefexArray[arrayLength] = refexNid;
            nidPairForRefexArray[arrayLength + 1] = memberNid;
            System.arraycopy(data, data[REFEX_OFFSET_INDEX], nidPairForRefexArray, 0,
                    nidPairForRefexArray.length - 2);
            updateData(getRelationshipOutgoingArray(), getDestinationOriginNids(), nidPairForRefexArray);
        }
    }

    public void forgetNidPairForRefex(int refexNid, int memberNid) {
        int arrayLength = data.length - data[REFEX_OFFSET_INDEX];
        int start = data.length - arrayLength;

        for (int i = start; i < data.length; i++) {
            if (data[i] == memberNid) {
                int[] nidPairForRefexArray = new int[arrayLength - 2];
                int partOneLength = i - 1 - start;
                if (partOneLength > 0) {
                    System.arraycopy(data, data[REFEX_OFFSET_INDEX], nidPairForRefexArray, 0, partOneLength);
                }

                int partTwoLength = nidPairForRefexArray.length - partOneLength;
                if (partTwoLength > partOneLength) {
                    System.arraycopy(data, i + 1, nidPairForRefexArray, partOneLength, partTwoLength);
                }


                updateData(getRelationshipOutgoingArray(), getDestinationOriginNids(), nidPairForRefexArray);

                return;
            }
        }
    }

    public int[] updateData(int[] relationshipOutgoingData, int[] destinationOriginData, int[] refexData) {
        int length = relationshipOutgoingData.length + destinationOriginData.length + refexData.length
                + RELATIONSHIP_OFFSET;

        data = new int[length];
        data[DESTINATION_OFFSET_INDEX] = relationshipOutgoingData.length + RELATIONSHIP_OFFSET;
        data[REFEX_OFFSET_INDEX] = data[DESTINATION_OFFSET_INDEX] + destinationOriginData.length;
        System.arraycopy(relationshipOutgoingData, 0, data, RELATIONSHIP_OFFSET,
                relationshipOutgoingData.length);
        System.arraycopy(destinationOriginData, 0, data, data[DESTINATION_OFFSET_INDEX],
                destinationOriginData.length);
        System.arraycopy(refexData, 0, data, data[REFEX_OFFSET_INDEX], refexData.length);

        return data;
    }

    //~--- get methods ---------------------------------------------------------
    public int[] getData() {
        if (data.length == 2) {
            return null;
        }

        return data;
    }

    /**
     *
     * @return int[] of relationship nids that point to this component
     */
    public int[] getDestRelNids(int cNid) throws IOException {
        HashSet<Integer> returnValues = new HashSet<>();
        int[] originCNids = getDestinationOriginNids();

        for (int originCNid : originCNids) {
            ConceptChronicleBI c = Ts.get().getConcept(originCNid);

            for (RelationshipChronicleBI r : c.getRelationshipsOutgoing()) {
                if (r.getTargetNid() == cNid) {
                    returnValues.add(r.getNid());
                }
            }
        }

        int[] returnValueArray = new int[returnValues.size()];
        int i = 0;

        for (Integer nid : returnValues) {
            returnValueArray[i++] = nid;
        }

        return returnValueArray;
    }

    /**
     *
     * @param relTypes
     * @return int[] of relationship nids that point to this component
     */
    public int[] getDestRelNids(int cNid, NidSetBI relTypes) throws IOException {
        HashSet<Integer> returnValues = new HashSet<>();
        int[] originCNids = getDestinationOriginNids();

        for (int originCNid : originCNids) {
            ConceptChronicleBI c = Ts.get().getConcept(originCNid);

            for (RelationshipChronicleBI r : c.getRelationshipsOutgoing()) {
                if (r.getTargetNid() == cNid) {
                    for (RelationshipVersionBI rv : r.getVersions()) {
                        if (relTypes.contains(rv.getTypeNid())) {
                            returnValues.add(r.getNid());

                            break;
                        }
                    }
                }
            }
        }

        int[] returnValueArray = new int[returnValues.size()];
        int i = 0;

        for (Integer nid : returnValues) {
            returnValueArray[i++] = nid;
        }

        return returnValueArray;
    }

    /**
     *
     * @param vc
     * @return int[] of relationship nids that point to this component
     */
    public int[] getDestRelNids(int cNid, ViewCoordinate vc) throws IOException {
        Collection<Relationship> destinationRels = getDestRels(cNid);
        ArrayList<Integer> destRelNids = new ArrayList<>();
        for (Relationship rel : destinationRels) {
            List<Version> versions = rel.getVersions(vc);
            if (!versions.isEmpty()) {
                destRelNids.add(rel.getNid());
            }
        }
        int[] destRelNidsArray = new int[destRelNids.size()];
        for (int i = 0; i < destRelNids.size(); i++) {
            destRelNidsArray[i] = destRelNids.get(i);
        }

        return destRelNidsArray;
    }

    public Collection<Relationship> getDestRels(int cNid) throws IOException {
        HashSet<Relationship> returnValues = new HashSet<>();
        int[] originCNids = getDestinationOriginNids(); //is 0

        for (int originCNid : originCNids) {
            Concept c = Concept.get(originCNid);

            for (Relationship r : c.getSourceRels()) {
                if (r.getTargetNid() == cNid) {
                    returnValues.add(r);
                }
            }
        }

        return returnValues;
    }

    /**
     *
     * @return int[] of concept nids with relationships that point to this
     * component
     */
    public int[] getDestinationOriginNids() {
        int arrayLength = data[REFEX_OFFSET_INDEX] - data[DESTINATION_OFFSET_INDEX];
        int[] destinationOriginNids = new int[arrayLength];

        System.arraycopy(data, data[DESTINATION_OFFSET_INDEX], destinationOriginNids, 0, arrayLength);

        return destinationOriginNids;
    }

    public NidPairForRefex[] getNidPairsForRefsets() {
        int arrayLength = data.length - data[REFEX_OFFSET_INDEX];

        assert arrayLength % 2 == 0;

        if (arrayLength < 2) {
            return new NidPairForRefex[0];
        }

        NidPairForRefex[] returnValues = new NidPairForRefex[arrayLength / 2];
        int start = data[REFEX_OFFSET_INDEX];
        int returnIndex = 0;

        for (int i = start; i < data.length; i = i + 2) {
            returnValues[returnIndex++] = NidPairForRefex.getRefexNidMemberNidPair(data[i], data[i + 1]);
        }

        return returnValues;
    }

    public int[] getRefexIndexArray() {
        int arrayLength = data.length - data[REFEX_OFFSET_INDEX];
        int[] relationshipOutgoingArray = new int[arrayLength];

        if (arrayLength > 0) {
            System.arraycopy(data, data[REFEX_OFFSET_INDEX], relationshipOutgoingArray, 0, arrayLength);
        }

        return relationshipOutgoingArray;
    }

    public int[] getRelationshipOutgoingArray() {
        int arrayLength = data[DESTINATION_OFFSET_INDEX] - RELATIONSHIP_OFFSET;
        int[] relationshipOutgoingArray = new int[arrayLength];

        if (arrayLength > 0) {
            System.arraycopy(data, RELATIONSHIP_OFFSET, relationshipOutgoingArray, 0, arrayLength);
        }

        return relationshipOutgoingArray;
    }

    /**
     *
     * @return a <code>RelationshipIndexRecord</code> backed by the data in this
     * array.
     */
    public RelationshipIndexRecord getRelationshipsRecord() {
        return new RelationshipIndexRecord(data, RELATIONSHIP_OFFSET, data[DESTINATION_OFFSET_INDEX]);
    }

    boolean isKindOf(int parentNid, ViewCoordinate vc, RelativePositionComputerBI computer)
            throws IOException, ContradictionException {
        HashSet<Integer> visitedSet = new HashSet<>();

        return isKindOfWithVisitedSet(parentNid, vc, computer, visitedSet);
    }

    boolean isKindOfWithVisitedSet(int parentNid, ViewCoordinate vc, RelativePositionComputerBI computer,
            HashSet<Integer> visitedSet)
            throws IOException, ContradictionException {
        if (data[DESTINATION_OFFSET_INDEX] > RELATIONSHIP_OFFSET) {
            for (RelationshipIndexRecord record : getRelationshipsRecord()) {
                if (!visitedSet.contains(record.getDestinationNid())) {
                    if (record.isActiveTaxonomyRelationship(vc, computer)) {
                        visitedSet.add(record.getDestinationNid());

                        if (record.getDestinationNid() == parentNid) {
                            return true;
                        } else {
                            IndexCacheRecord possibleParentRecord =
                                    Bdb.getNidCNidMap().getIndexCacheRecord(record.getDestinationNid());

                            if (possibleParentRecord.isKindOfWithVisitedSet(parentNid, vc, computer, visitedSet)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    boolean isChildOf(int parentNid, ViewCoordinate vc, RelativePositionComputerBI computer)
            throws IOException, ContradictionException {
        if (data[DESTINATION_OFFSET_INDEX] > RELATIONSHIP_OFFSET) {
            for (RelationshipIndexRecord record : getRelationshipsRecord()) {
                if (record.isActiveTaxonomyRelationship(vc, computer)) {
                    if (record.getDestinationNid() == parentNid) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Relationships:\n");
        if (data[DESTINATION_OFFSET_INDEX] > RELATIONSHIP_OFFSET) {
            for (RelationshipIndexRecord record : getRelationshipsRecord()) {
                try {
                    sb.append("  ").append(Concept.get(record.getTypeNid()).toString()).append(" [").
                            append(record.getTypeNid()).append("]: ").
                            append(Concept.get(record.getDestinationNid()).toString()).append(" [").
                            append(record.getDestinationNid()).append("]\n");
                    for(RelationshipIndexVersion version : record.getVersions()){
                        sb.append("            VERSION: ");
                        sb.append(Concept.get(version.getCharacteristicNid()).toString()).append(" ").
                                append(version.stamp).append(" \n");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(IndexCacheRecord.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        sb.append("\nRelationship origins:\n");
        for (int destinationOrigin : getDestinationOriginNids()) {
            try {
                sb.append("  ").append(Concept.get(destinationOrigin).toString()).append(" [").
                        append(destinationOrigin).append("]\n");;
            } catch (IOException ex) {
                Logger.getLogger(IndexCacheRecord.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        sb.append("\nRefsets:\n");
        for (NidPairForRefex pair : getNidPairsForRefsets()) {
            try {
                sb.append("  ").append(Concept.get(pair.getRefexNid()).toString()).append(" [").
                        append(pair.getRefexNid()).append("], memberNid: ").
                        append(pair.getMemberNid()).append("\n");
            } catch (IOException ex) {
                Logger.getLogger(IndexCacheRecord.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return sb.toString();
    }
}
