/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.refset.I_RefsetDefaultsCrossMapForRel;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntList;

public class RefsetDefaultsCrossMapForRel extends RefsetDefaults implements I_RefsetDefaultsCrossMapForRel {

    public RefsetDefaultsCrossMapForRel() throws TerminologyException, IOException {
        super();
        refineFlag = ConceptBean.get(RefsetAuxiliary.Concept.MANDATORY_REFINABILITY_FLAG.getUids());
        refineFlagPopupIds.add(refineFlag.getConceptId());
        refineFlagPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.COMPLETE_REFINABILITY_FLAG.getUids())
            .getConceptId());
        refineFlagPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.POSSIBLE_REFINABILITY_FLAG.getUids())
            .getConceptId());

        additionalCode = ConceptBean.get(RefsetAuxiliary.Concept.MANDATORY_ADDITIONAL_CODE_FLAG.getUids());
        additionalCodePopupIds.add(additionalCode.getConceptId());
        additionalCodePopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.COMPLETE_ADDITIONAL_CODE_FLAG.getUids())
            .getConceptId());
        additionalCodePopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.POSSIBLE_ADDITIONAL_CODE_FLAG.getUids())
            .getConceptId());

    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private I_GetConceptData refineFlag;
    private I_IntList refineFlagPopupIds = new IntList();
    private I_GetConceptData additionalCode;
    private I_IntList additionalCodePopupIds = new IntList();
    private int defaultElementNo = 0;
    private Integer[] elementNoPopupItems = new Integer[] { 1, 2, 3 };
    private int defaultBlockNo = 0;
    private Integer[] blockNoPopupItems = new Integer[] { 1, 2, 3 };

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(refineFlag.getUids());
        IntList.writeIntList(out, refineFlagPopupIds);
        out.writeObject(additionalCode.getUids());
        IntList.writeIntList(out, additionalCodePopupIds);

        out.writeInt(defaultElementNo);
        out.writeObject(elementNoPopupItems);
        out.writeInt(defaultBlockNo);
        out.writeObject(blockNoPopupItems);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            refineFlag = readConcept(in);
            refineFlagPopupIds = IntList.readIntListIgnoreMapErrors(in);
            additionalCode = readConcept(in);
            additionalCodePopupIds = IntList.readIntListIgnoreMapErrors(in);
            defaultElementNo = in.readInt();
            elementNoPopupItems = (Integer[]) in.readObject();
            defaultBlockNo = in.readInt();
            blockNoPopupItems = (Integer[]) in.readObject();

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#getRefineFlag()
     */
    public I_GetConceptData getRefineFlag() {
        return refineFlag;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#setRefineFlag
     * (org.dwfa.ace.api.I_GetConceptData)
     */
    public void setRefineFlag(I_GetConceptData refineFlag) {
        this.refineFlag = refineFlag;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#
     * getRefineFlagPopupIds()
     */
    public I_IntList getRefineFlagPopupIds() {
        return refineFlagPopupIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#
     * setRefineFlagPopupIds(org.dwfa.ace.api.I_IntList)
     */
    public void setRefineFlagPopupIds(I_IntList refineFlagPopupIds) {
        this.refineFlagPopupIds = refineFlagPopupIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * 
     * 
     * 
     * 
     * 
     * org.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#getAdditionalCode
     * ()
     */
    public I_GetConceptData getAdditionalCode() {
        return additionalCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * 
     * 
     * 
     * 
     * 
     * org.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#setAdditionalCode
     * (org.dwfa.ace.api.I_GetConceptData)
     */
    public void setAdditionalCode(I_GetConceptData additionalCode) {
        this.additionalCode = additionalCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#
     * getAdditionalCodePopupIds()
     */
    public I_IntList getAdditionalCodePopupIds() {
        return additionalCodePopupIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#
     * setAdditionalCodePopupIds(org.dwfa.ace.api.I_IntList)
     */
    public void setAdditionalCodePopupIds(I_IntList additionalCodePopupIds) {
        this.additionalCodePopupIds = additionalCodePopupIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * 
     * 
     * 
     * 
     * 
     * org.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#getDefaultElementNo
     * ()
     */
    public int getDefaultElementNo() {
        return defaultElementNo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * 
     * 
     * 
     * 
     * 
     * org.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#setDefaultElementNo
     * (int)
     */
    public void setDefaultElementNo(int defaultElementNo) {
        this.defaultElementNo = defaultElementNo;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#
     * getElementNoPopupItems()
     */
    public Integer[] getElementNoPopupItems() {
        return elementNoPopupItems;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#
     * setElementNoPopupItems(java.lang.Integer[])
     */
    public void setElementNoPopupItems(Integer[] elementNoPopupItems) {
        this.elementNoPopupItems = elementNoPopupItems;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * 
     * 
     * 
     * 
     * 
     * org.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#getDefaultBlockNo
     * ()
     */
    public int getDefaultBlockNo() {
        return defaultBlockNo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * 
     * 
     * 
     * 
     * 
     * org.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#setDefaultBlockNo
     * (int)
     */
    public void setDefaultBlockNo(int defaultBlockNo) {
        this.defaultBlockNo = defaultBlockNo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * 
     * 
     * 
     * 
     * 
     * org.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#getBlockNoPopupItems
     * ()
     */
    public Integer[] getBlockNoPopupItems() {
        return blockNoPopupItems;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * 
     * 
     * 
     * 
     * 
     * org.dwfa.ace.table.refset.I_RefsetDefaultsCrossMapForRel#setBlockNoPopupItems
     * (java.lang.Integer[])
     */
    public void setBlockNoPopupItems(Integer[] blockNoPopupItems) {
        this.blockNoPopupItems = blockNoPopupItems;
    }

}
