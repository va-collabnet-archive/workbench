package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.refset.I_RefsetDefaultsTemplateForRel;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntList;

public class RefsetDefaultsTemplateForRel extends RefsetDefaults implements I_RefsetDefaultsTemplateForRel {

    public RefsetDefaultsTemplateForRel() throws TerminologyException, IOException {
        super();
        valueType = ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_CODE_VALUE_TYPE.getUids());
        valueTypePopupIds.add(valueType.getConceptId());
        valueTypePopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_NUMBER_VALUE_TYPE.getUids()).getConceptId());
        valueTypePopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_DATE_VALUE_TYPE.getUids()).getConceptId());

        semanticStatus = ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_FINAL_SEMANTIC_STATUS.getUids());
        semanticStatusPopupIds.add(semanticStatus.getConceptId());
        semanticStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_DATE_VALUE_TYPE.getUids()).getConceptId());
        semanticStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_REFINABLE_SEMANTIC_STATUS.getUids()).getConceptId());
        semanticStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_NUMERIC_QUALIFIER_REFINE_SEMANTIC_STATUS.getUids()).getConceptId());
        semanticStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_MANDATORY_TO_REFINE_SEMANTIC_STATUS.getUids()).getConceptId());
        semanticStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_CHILD_REFINE_SEMANTIC_STATUS.getUids()).getConceptId());
        semanticStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_QUALIFIER_REFINE_SEMANTIC_STATUS.getUids()).getConceptId());
        semanticStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_UNSPECIFIED_SEMANTIC_STATUS.getUids()).getConceptId());

        attributeDisplayStatus = ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_ATTRIBUTE_DISPLAYED.getUids());
        attributeDisplayStatusPopupIds.add(attributeDisplayStatus.getConceptId());
        attributeDisplayStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_ATTRIBUTE_HIDDEN.getUids()).getConceptId());
        attributeDisplayStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_ATTRIBUTE_UNSPECIFIED.getUids()).getConceptId());

        characteristicStatus = ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_CHARACTERSITIC_QUALIFIER.getUids());
        characteristicStatusPopupIds.add(characteristicStatus.getConceptId());
        characteristicStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_CHARACTERSITIC_ATOM.getUids()).getConceptId());
        characteristicStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_CHARACTERSITIC_FACT.getUids()).getConceptId());
        
    }
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private I_GetConceptData valueType;
    private I_IntList valueTypePopupIds = new IntList();
    private int cardinality = 0;
    private Integer[] cardinalityPopupItems = new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private I_GetConceptData semanticStatus;
    private I_IntList semanticStatusPopupIds = new IntList();
    private int browseAttributeOrder= 99;
    private Integer[] browseAttributeOrderPopupItems = new Integer[] {99, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private int browseValueOrder=99;
    private Integer[] browseValueOrderPopupItems = new Integer[] {99, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private int notesScreenOrder= 0;
    private Integer[] notesScreenOrderPopupItems = new Integer[] {99, 97, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
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

     @SuppressWarnings("unchecked")
     private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            
            valueType = readConcept(in);
            valueTypePopupIds = IntList.readIntListIgnoreMapErrors(in);
            
            cardinality = in.readInt();
            cardinalityPopupItems = (Integer[]) in.readObject();
            
            semanticStatus = readConcept(in);
            semanticStatusPopupIds = IntList.readIntListIgnoreMapErrors(in);
            
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

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getValueType()
     */
    public I_GetConceptData getValueType() {
        return valueType;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setValueType(org.dwfa.ace.api.I_GetConceptData)
     */
    public void setValueType(I_GetConceptData valueType) {
        this.valueType = valueType;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getValueTypePopupIds()
     */
    public I_IntList getValueTypePopupIds() {
        return valueTypePopupIds;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setValueTypePopupIds(org.dwfa.ace.api.I_IntList)
     */
    public void setValueTypePopupIds(I_IntList valueTypePopupIds) {
        this.valueTypePopupIds = valueTypePopupIds;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getCardinality()
     */
    public int getCardinality() {
        return cardinality;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setCardinality(int)
     */
    public void setCardinality(int cardinality) {
        this.cardinality = cardinality;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getCardinalityPopupItems()
     */
    public Integer[] getCardinalityPopupItems() {
        return cardinalityPopupItems;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setCardinalityPopupItems(java.lang.Integer[])
     */
    public void setCardinalityPopupItems(Integer[] cardinalityPopupItems) {
        this.cardinalityPopupItems = cardinalityPopupItems;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getSemanticStatus()
     */
    public I_GetConceptData getSemanticStatus() {
        return semanticStatus;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setSemanticStatus(org.dwfa.ace.api.I_GetConceptData)
     */
    public void setSemanticStatus(I_GetConceptData semanticStatus) {
        this.semanticStatus = semanticStatus;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getSemanticStatusPopupIds()
     */
    public I_IntList getSemanticStatusPopupIds() {
        return semanticStatusPopupIds;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setSemanticStatusPopupIds(org.dwfa.ace.api.I_IntList)
     */
    public void setSemanticStatusPopupIds(I_IntList semanticStatusPopupIds) {
        this.semanticStatusPopupIds = semanticStatusPopupIds;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getBrowseAttributeOrder()
     */
    public int getBrowseAttributeOrder() {
        return browseAttributeOrder;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setBrowseAttributeOrder(int)
     */
    public void setBrowseAttributeOrder(int browseAttributeOrder) {
        this.browseAttributeOrder = browseAttributeOrder;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getBrowseAttributeOrderPopupItems()
     */
    public Integer[] getBrowseAttributeOrderPopupItems() {
        return browseAttributeOrderPopupItems;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setBrowseAttributeOrderPopupItems(java.lang.Integer[])
     */
    public void setBrowseAttributeOrderPopupItems(Integer[] browseAttributeOrderPopupItems) {
        this.browseAttributeOrderPopupItems = browseAttributeOrderPopupItems;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getBrowseValueOrder()
     */
    public int getBrowseValueOrder() {
        return browseValueOrder;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setBrowseValueOrder(int)
     */
    public void setBrowseValueOrder(int browseValueOrder) {
        this.browseValueOrder = browseValueOrder;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getBrowseValueOrderPopupItems()
     */
    public Integer[] getBrowseValueOrderPopupItems() {
        return browseValueOrderPopupItems;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setBrowseValueOrderPopupItems(java.lang.Integer[])
     */
    public void setBrowseValueOrderPopupItems(Integer[] browseValueOrderPopupItems) {
        this.browseValueOrderPopupItems = browseValueOrderPopupItems;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getNotesScreenOrder()
     */
    public int getNotesScreenOrder() {
        return notesScreenOrder;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setNotesScreenOrder(int)
     */
    public void setNotesScreenOrder(int notesScreenOrder) {
        this.notesScreenOrder = notesScreenOrder;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getNotesScreenOrderPopupItems()
     */
    public Integer[] getNotesScreenOrderPopupItems() {
        return notesScreenOrderPopupItems;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setNotesScreenOrderPopupItems(java.lang.Integer[])
     */
    public void setNotesScreenOrderPopupItems(Integer[] notesScreenOrderPopupItems) {
        this.notesScreenOrderPopupItems = notesScreenOrderPopupItems;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getAttributeDisplayStatus()
     */
    public I_GetConceptData getAttributeDisplayStatus() {
        return attributeDisplayStatus;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setAttributeDisplayStatus(org.dwfa.ace.api.I_GetConceptData)
     */
    public void setAttributeDisplayStatus(I_GetConceptData attributeDisplayStatus) {
        this.attributeDisplayStatus = attributeDisplayStatus;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getAttributeDisplayStatusPopupIds()
     */
    public I_IntList getAttributeDisplayStatusPopupIds() {
        return attributeDisplayStatusPopupIds;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setAttributeDisplayStatusPopupIds(org.dwfa.ace.api.I_IntList)
     */
    public void setAttributeDisplayStatusPopupIds(I_IntList attributeDisplayStatusPopupIds) {
        this.attributeDisplayStatusPopupIds = attributeDisplayStatusPopupIds;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getCharacteristicStatus()
     */
    public I_GetConceptData getCharacteristicStatus() {
        return characteristicStatus;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setCharacteristicStatus(org.dwfa.ace.api.I_GetConceptData)
     */
    public void setCharacteristicStatus(I_GetConceptData characteristicStatus) {
        this.characteristicStatus = characteristicStatus;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#getCharacteristicStatusPopupIds()
     */
    public I_IntList getCharacteristicStatusPopupIds() {
        return characteristicStatusPopupIds;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplateForRel#setCharacteristicStatusPopupIds(org.dwfa.ace.api.I_IntList)
     */
    public void setCharacteristicStatusPopupIds(I_IntList characteristicStatusPopupIds) {
        this.characteristicStatusPopupIds = characteristicStatusPopupIds;
    }

}
