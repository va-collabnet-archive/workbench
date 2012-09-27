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
package org.dwfa.ace;

import java.io.IOException;

import org.dwfa.ace.api.I_ConceptAttributeTuple;

public class LabelForConceptAttributeTuple extends LabelForTuple {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private I_ConceptAttributeTuple conAttr;

    public LabelForConceptAttributeTuple(I_ConceptAttributeTuple conAttr, boolean longForm, boolean showStatus) {
        super(longForm, showStatus);
        this.conAttr = conAttr;
    }

    public I_ImplementActiveLabel copy() throws IOException {
        return TermLabelMaker.newLabel(conAttr, isLongForm(), getShowStatus());
    }

    public I_ConceptAttributeTuple getConAttr() {
        return conAttr;
    }

    @Override
    protected boolean tupleEquals(Object obj) {
        if (LabelForConceptAttributeTuple.class.isAssignableFrom(obj.getClass())) {
            LabelForConceptAttributeTuple another = (LabelForConceptAttributeTuple) obj;
            return conAttr.equals(another.conAttr);
        }
        return false;
    }

    @Override
    protected int tupleHash() {
        return this.conAttr.hashCode();
    }

    @Override
    protected String getTupleString() {
        return conAttr.toString();
    }

}
