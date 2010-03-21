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
package org.dwfa.vodb.types;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartTemplateForRel;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartTemplateForRel;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartTemplateForRel extends ThinExtByRefPart implements I_ThinExtByRefPartTemplateForRel {

    int valueTypeId;
    int cardinality;
    int semanticStatusId;
    int browseAttributeOrder;
    int browseValueOrder;
    int notesScreenOrder;
    int attributeDisplayStatusId;
    int characteristicStatusId;

    public ArrayIntList getPartComponentNids() {
        ArrayIntList partComponentNids = new ArrayIntList(6);
        partComponentNids.add(getPathId());
        partComponentNids.add(getStatusId());
        partComponentNids.add(valueTypeId);
        partComponentNids.add(semanticStatusId);
        partComponentNids.add(attributeDisplayStatusId);
        partComponentNids.add(characteristicStatusId);
        return partComponentNids;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#getValueTypeId()
     */
    public int getValueTypeId() {
        return valueTypeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#setValueTypeId(int)
     */
    public void setValueTypeId(int valueTypeId) {
        this.valueTypeId = valueTypeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#getCardinality()
     */
    public int getCardinality() {
        return cardinality;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#setCardinality(int)
     */
    public void setCardinality(int cardinality) {
        this.cardinality = cardinality;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#getSemanticStatusId
     * ()
     */
    public int getSemanticStatusId() {
        return semanticStatusId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#setSemanticStatusId
     * (int)
     */
    public void setSemanticStatusId(int semanticStatusId) {
        this.semanticStatusId = semanticStatusId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#getBrowseAttributeOrder
     * ()
     */
    public int getBrowseAttributeOrder() {
        return browseAttributeOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#setBrowseAttributeOrder
     * (int)
     */
    public void setBrowseAttributeOrder(int browseAttributeOrder) {
        this.browseAttributeOrder = browseAttributeOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#getBrowseValueOrder
     * ()
     */
    public int getBrowseValueOrder() {
        return browseValueOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#setBrowseValueOrder
     * (int)
     */
    public void setBrowseValueOrder(int browseValueOrder) {
        this.browseValueOrder = browseValueOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#getNotesScreenOrder
     * ()
     */
    public int getNotesScreenOrder() {
        return notesScreenOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#setNotesScreenOrder
     * (int)
     */
    public void setNotesScreenOrder(int notesScreenOrder) {
        this.notesScreenOrder = notesScreenOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#
     * getAttributeDisplayStatusId()
     */
    public int getAttributeDisplayStatusId() {
        return attributeDisplayStatusId;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#
     * setAttributeDisplayStatusId(int)
     */
    public void setAttributeDisplayStatusId(int attributeDisplayStatusId) {
        this.attributeDisplayStatusId = attributeDisplayStatusId;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#
     * getCharacteristicStatusId()
     */
    public int getCharacteristicStatusId() {
        return characteristicStatusId;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.vodb.types.I_ThinExtByRefPartTemplateForRel#
     * setCharacteristicStatusId(int)
     */
    public void setCharacteristicStatusId(int characteristicStatusId) {
        this.characteristicStatusId = characteristicStatusId;
    }

    public I_ThinExtByRefPart duplicate() {
        return new ThinExtByRefPartTemplateForRel(this);
    }

    public ThinExtByRefPartTemplateForRel(ThinExtByRefPartTemplateForRel another) {
        super(another);
        this.valueTypeId = another.valueTypeId;
        this.cardinality = another.cardinality;
        this.semanticStatusId = another.semanticStatusId;
        this.browseAttributeOrder = another.browseAttributeOrder;
        this.browseValueOrder = another.browseValueOrder;
        this.notesScreenOrder = another.notesScreenOrder;
        this.attributeDisplayStatusId = another.attributeDisplayStatusId;
        this.characteristicStatusId = another.characteristicStatusId;
    }

    public ThinExtByRefPartTemplateForRel() {
        super();
    }

    public int compareTo(I_ThinExtByRefPart o) {
        if (this.getClass().isAssignableFrom(o.getClass())) {
            ThinExtByRefPartTemplateForRel otherPart = (ThinExtByRefPartTemplateForRel) o;
            return this.getVersion() - otherPart.getVersion() + this.getPathId() - otherPart.getPathId();
        }
        return 1;
    }

    @Override
    public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
        I_TermFactory tf = LocalVersionedTerminology.get();
        UniversalAceExtByRefPartTemplateForRel universalPart = new UniversalAceExtByRefPartTemplateForRel();
        universalPart.setValueTypeUid(tf.getUids(valueTypeId));
        universalPart.setCardinality(cardinality);
        universalPart.setSemanticStatusUid(tf.getUids(semanticStatusId));
        universalPart.setBrowseAttributeOrder(browseAttributeOrder);
        universalPart.setBrowseValueOrder(browseValueOrder);
        universalPart.setNotesScreenOrder(notesScreenOrder);
        universalPart.setAttributeDisplayStatusUid(tf.getUids(attributeDisplayStatusId));
        universalPart.setCharacteristicStatusUid(tf.getUids(characteristicStatusId));
        universalPart.setPathUid(tf.getUids(getPathId()));
        universalPart.setStatusUid(tf.getUids(getStatusId()));
        universalPart.setTime(ThinVersionHelper.convert(getVersion()));
        return universalPart;
    }

}
