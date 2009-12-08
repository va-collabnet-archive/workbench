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
import java.util.List;

import org.dwfa.ace.api.I_RelTuple;

public class LabelForGroupTuple extends LabelForTuple {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private List<I_RelTuple> relGroup;

    public LabelForGroupTuple(List<I_RelTuple> rel, boolean longForm, boolean showStatus) {
        super(longForm, showStatus);
        this.relGroup = rel;
    }

    public I_ImplementActiveLabel copy() throws IOException {
        return TermLabelMaker.newLabel(relGroup, isLongForm(), getShowStatus());
    }

    public List<I_RelTuple> getRelGroup() {
        return relGroup;
    }

    @Override
    protected boolean tupleEquals(Object obj) {
        if (LabelForGroupTuple.class.isAssignableFrom(obj.getClass())) {
            LabelForGroupTuple another = (LabelForGroupTuple) obj;
            return relGroup.equals(another.relGroup);
        }
        return false;
    }

    @Override
    protected int tupleHash() {
        return this.relGroup.hashCode();
    }

    @Override
    protected String getTupleString() {
        return relGroup.toString();
    }

}
