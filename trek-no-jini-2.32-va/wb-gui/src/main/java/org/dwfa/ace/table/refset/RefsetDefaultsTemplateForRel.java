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
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.I_RefsetDefaultsTemplateForRel;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntList;

public class RefsetDefaultsTemplateForRel extends RefsetDefaults implements I_RefsetDefaultsTemplateForRel {

    public RefsetDefaultsTemplateForRel() throws TerminologyException, IOException {
        super();
        valueType = Terms.get().getConcept(RefsetAuxiliary.Concept.TEMPLATE_CODE_VALUE_TYPE.getUids());
        valueTypePopupIds.add(valueType.getConceptNid());
        valueTypePopupIds.add(Terms.get().getConcept(RefsetAuxiliary.Concept.TEMPLATE_NUMBER_VALUE_TYPE.getUids())
            .getConceptNid());
        valueTypePopupIds.add(Terms.get().getConcept(RefsetAuxiliary.Concept.TEMPLATE_DATE_VALUE_TYPE.getUids())
            .getConceptNid());

        semanticStatus = Terms.get().getConcept(RefsetAuxiliary.Concept.TEMPLATE_FINAL_SEMANTIC_STATUS.getUids());
        semanticStatusPopupIds.add(semanticStatus.getConceptNid());
        semanticStatusPopupIds.add(Terms.get().getConcept(RefsetAuxiliary.Concept.TEMPLATE_DATE_VALUE_TYPE.getUids())
            .getConceptNid());
        semanticStatusPopupIds.add(Terms.get().getConcept(RefsetAuxiliary.Concept.TEMPLATE_REFINABLE_SEMANTIC_STATUS.getUids())
            .getConceptNid());
        semanticStatusPopupIds.add(Terms.get().getConcept(
            RefsetAuxiliary.Concept.TEMPLATE_NUMERIC_QUALIFIER_REFINE_SEMANTIC_STATUS.getUids()).getConceptNid());
        semanticStatusPopupIds.add(Terms.get().getConcept(
            RefsetAuxiliary.Concept.TEMPLATE_MANDATORY_TO_REFINE_SEMANTIC_STATUS.getUids()).getConceptNid());
        semanticStatusPopupIds.add(Terms.get().getConcept(
            RefsetAuxiliary.Concept.TEMPLATE_CHILD_REFINE_SEMANTIC_STATUS.getUids()).getConceptNid());
        semanticStatusPopupIds.add(Terms.get().getConcept(
            RefsetAuxiliary.Concept.TEMPLATE_QUALIFIER_REFINE_SEMANTIC_STATUS.getUids()).getConceptNid());
        semanticStatusPopupIds.add(Terms.get().getConcept(
            RefsetAuxiliary.Concept.TEMPLATE_UNSPECIFIED_SEMANTIC_STATUS.getUids()).getConceptNid());

        attributeDisplayStatus = Terms.get().getConcept(RefsetAuxiliary.Concept.TEMPLATE_ATTRIBUTE_DISPLAYED.getUids());
        attributeDisplayStatusPopupIds.add(attributeDisplayStatus.getConceptNid());
        attributeDisplayStatusPopupIds.add(Terms.get().getConcept(RefsetAuxiliary.Concept.TEMPLATE_ATTRIBUTE_HIDDEN.getUids())
            .getConceptNid());
        attributeDisplayStatusPopupIds.add(Terms.get().getConcept(
            RefsetAuxiliary.Concept.TEMPLATE_ATTRIBUTE_UNSPECIFIED.getUids()).getConceptNid());

        characteristicStatus = Terms.get().getConcept(RefsetAuxiliary.Concept.TEMPLATE_CHARACTERSITIC_QUALIFIER.getUids());
        characteristicStatusPopupIds.add(characteristicStatus.getConceptNid());
        characteristicStatusPopupIds.add(Terms.get().getConcept(RefsetAuxiliary.Concept.TEMPLATE_CHARACTERSITIC_ATOM.getUids())
            .getConceptNid());
        characteristicStatusPopupIds.add(Terms.get().getConcept(RefsetAuxiliary.Concept.TEMPLATE_CHARACTERSITIC_FACT.getUids())
            .getConceptNid());

    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private I_GetConceptData valueType;
    private I_IntList valueTypePopupIds = new IntList();
    private int cardinality = 0;
    private Integer[] cardinalityPopupItems = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private I_GetConceptData semanticStatus;
    private I_IntList semanticStatusPopupIds = new IntList();
    private int browseAttributeOrder = 99;
    private Integer[] browseAttributeOrderPopupItems = new Integer[] { 99, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private int browseValueOrder = 99;
    private Integer[] browseValueOrderPopupItems = new Integer[] { 99, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private int notesScreenOrder = 0;
    private Integer[] notesScreenOrderPopupItems = new Integer[] { 99, 97, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private I_GetConceptData attributeDisplayStatus;
    private I_IntList attributeDisplayStatusPopupIds = new IntList();
    private I_GetConceptData characteristicStatus;
    private I_IntList characteristicStatusPopupIds = new IntList();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);

        out.writeObject(valueType.getUids());
        IntList.writeIntList(out, valueTypePopupIds);

        out.writeInt(cardinality);
        out.writeObject(cardinalityPopupItems);

        out.writeObject(semanticStatus.getUids());
        IntList.writeIntList(out, semanticStatusPopupIds);

        out.writeInt(browseAttributeOrder);
        out.writeObject(browseAttributeOrderPopupItems);

        out.writeInt(browseValueOrder);
        out.writeObject(browseValueOrderPopupItems);

        out.writeInt(notesScreenOrder);
        out.writeObject(notesScreenOrderPopupItems);

        out.writeObject(attributeDisplayStatus.getUids());
        IntList.writeIntList(out, attributeDisplayStatusPopupIds);

        out.writeObject(characteristicStatus.getUids());
        IntList.writeIntList(out, characteristicStatusPopupIds);

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {

            valueType = readConcept(in);
            valueTypePopupIds = IntList.readIntListIgnoreMapErrors(in);

            cardinality = in.readInt();
            cardinalityPopupItems = (Integer[]) in.readObject();

            semanticStatus = readConcept(in);
            semanticStatusPopupIds = IntList.readIntListIgnoreMapErrors(in);

            browseAttributeOrder = in.readInt();
            browseAttributeOrderPopupItems = (Integer[]) in.readObject();

            browseValueOrder = in.readInt();
            browseValueOrderPopupItems = (Integer[]) in.readObject();

            notesScreenOrder = in.readInt();
            notesScreenOrderPopupItems = (Integer[]) in.readObject();

            attributeDisplayStatus = readConcept(in);
            attributeDisplayStatusPopupIds = IntList.readIntListIgnoreMapErrors(in);

            characteristicStatus = readConcept(in);
            characteristicStatusPopupIds = IntList.readIntListIgnoreMapErrors(in);

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getValueType()
     */
    public I_GetConceptData getValueType() {
        return valueType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setValueType
     * (org.dwfa.ace.api.I_GetConceptData)
     */
    public void setValueType(I_GetConceptData valueType) {
        this.valueType = valueType;
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
     * org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getValueTypePopupIds
     * ()
     */
    public I_IntList getValueTypePopupIds() {
        return valueTypePopupIds;
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
     * org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setValueTypePopupIds
     * (org.dwfa.ace.api.I_IntList)
     */
    public void setValueTypePopupIds(I_IntList valueTypePopupIds) {
        this.valueTypePopupIds = valueTypePopupIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getCardinality()
     */
    public int getCardinality() {
        return cardinality;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setCardinality
     * (int)
     */
    public void setCardinality(int cardinality) {
        this.cardinality = cardinality;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * getCardinalityPopupItems()
     */
    public Integer[] getCardinalityPopupItems() {
        return cardinalityPopupItems;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * setCardinalityPopupItems(java.lang.Integer[])
     */
    public void setCardinalityPopupItems(Integer[] cardinalityPopupItems) {
        this.cardinalityPopupItems = cardinalityPopupItems;
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
     * org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getSemanticStatus
     * ()
     */
    public I_GetConceptData getSemanticStatus() {
        return semanticStatus;
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
     * org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setSemanticStatus
     * (org.dwfa.ace.api.I_GetConceptData)
     */
    public void setSemanticStatus(I_GetConceptData semanticStatus) {
        this.semanticStatus = semanticStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * getSemanticStatusPopupIds()
     */
    public I_IntList getSemanticStatusPopupIds() {
        return semanticStatusPopupIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * setSemanticStatusPopupIds(org.dwfa.ace.api.I_IntList)
     */
    public void setSemanticStatusPopupIds(I_IntList semanticStatusPopupIds) {
        this.semanticStatusPopupIds = semanticStatusPopupIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * getBrowseAttributeOrder()
     */
    public int getBrowseAttributeOrder() {
        return browseAttributeOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * setBrowseAttributeOrder(int)
     */
    public void setBrowseAttributeOrder(int browseAttributeOrder) {
        this.browseAttributeOrder = browseAttributeOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * getBrowseAttributeOrderPopupItems()
     */
    public Integer[] getBrowseAttributeOrderPopupItems() {
        return browseAttributeOrderPopupItems;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * setBrowseAttributeOrderPopupItems(java.lang.Integer[])
     */
    public void setBrowseAttributeOrderPopupItems(Integer[] browseAttributeOrderPopupItems) {
        this.browseAttributeOrderPopupItems = browseAttributeOrderPopupItems;
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
     * org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getBrowseValueOrder
     * ()
     */
    public int getBrowseValueOrder() {
        return browseValueOrder;
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
     * org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setBrowseValueOrder
     * (int)
     */
    public void setBrowseValueOrder(int browseValueOrder) {
        this.browseValueOrder = browseValueOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * getBrowseValueOrderPopupItems()
     */
    public Integer[] getBrowseValueOrderPopupItems() {
        return browseValueOrderPopupItems;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * setBrowseValueOrderPopupItems(java.lang.Integer[])
     */
    public void setBrowseValueOrderPopupItems(Integer[] browseValueOrderPopupItems) {
        this.browseValueOrderPopupItems = browseValueOrderPopupItems;
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
     * org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getNotesScreenOrder
     * ()
     */
    public int getNotesScreenOrder() {
        return notesScreenOrder;
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
     * org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setNotesScreenOrder
     * (int)
     */
    public void setNotesScreenOrder(int notesScreenOrder) {
        this.notesScreenOrder = notesScreenOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * getNotesScreenOrderPopupItems()
     */
    public Integer[] getNotesScreenOrderPopupItems() {
        return notesScreenOrderPopupItems;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * setNotesScreenOrderPopupItems(java.lang.Integer[])
     */
    public void setNotesScreenOrderPopupItems(Integer[] notesScreenOrderPopupItems) {
        this.notesScreenOrderPopupItems = notesScreenOrderPopupItems;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * getAttributeDisplayStatus()
     */
    public I_GetConceptData getAttributeDisplayStatus() {
        return attributeDisplayStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * setAttributeDisplayStatus(org.dwfa.ace.api.I_GetConceptData)
     */
    public void setAttributeDisplayStatus(I_GetConceptData attributeDisplayStatus) {
        this.attributeDisplayStatus = attributeDisplayStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * getAttributeDisplayStatusPopupIds()
     */
    public I_IntList getAttributeDisplayStatusPopupIds() {
        return attributeDisplayStatusPopupIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * setAttributeDisplayStatusPopupIds(org.dwfa.ace.api.I_IntList)
     */
    public void setAttributeDisplayStatusPopupIds(I_IntList attributeDisplayStatusPopupIds) {
        this.attributeDisplayStatusPopupIds = attributeDisplayStatusPopupIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * getCharacteristicStatus()
     */
    public I_GetConceptData getCharacteristicStatus() {
        return characteristicStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * setCharacteristicStatus(org.dwfa.ace.api.I_GetConceptData)
     */
    public void setCharacteristicStatus(I_GetConceptData characteristicStatus) {
        this.characteristicStatus = characteristicStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * getCharacteristicStatusPopupIds()
     */
    public I_IntList getCharacteristicStatusPopupIds() {
        return characteristicStatusPopupIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#
     * setCharacteristicStatusPopupIds(org.dwfa.ace.api.I_IntList)
     */
    public void setCharacteristicStatusPopupIds(I_IntList characteristicStatusPopupIds) {
        this.characteristicStatusPopupIds = characteristicStatusPopupIds;
    }

}
