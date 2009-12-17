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

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartTemplate;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartTemplate;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartTemplate extends ThinExtByRefPartTemplateForRel implements I_ThinExtByRefPartTemplate {

    int attributeId;
    int targetCodeId;

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartTemplate#getAttributeId()
     */
    public int getAttributeId() {
        return attributeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartTemplate#setAttributeId(int)
     */
    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartTemplate#getTargetCodeId()
     */
    public int getTargetCodeId() {
        return targetCodeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartTemplate#setTargetCodeId(int)
     */
    public void setTargetCodeId(int targetCodeId) {
        this.targetCodeId = targetCodeId;
    }

    public ThinExtByRefPartTemplate(ThinExtByRefPartTemplate another) {
        super(another);
        this.attributeId = another.attributeId;
        this.targetCodeId = another.targetCodeId;
    }

    public I_ThinExtByRefPart duplicate() {
        return new ThinExtByRefPartTemplate(this);
    }

    public ThinExtByRefPartTemplate() {
        super();
    }

    @Override
    public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
        I_TermFactory tf = LocalVersionedTerminology.get();
        UniversalAceExtByRefPartTemplate universalPart = new UniversalAceExtByRefPartTemplate();
        universalPart.setAttributeUid(tf.getUids(attributeId));
        universalPart.setTargetCodeUid(tf.getUids(targetCodeId));
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
