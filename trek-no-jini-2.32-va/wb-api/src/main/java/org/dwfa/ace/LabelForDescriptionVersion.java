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

import org.ihtsdo.tk.api.description.DescriptionVersionBI;

public class LabelForDescriptionVersion extends LabelForTuple {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private DescriptionVersionBI desc;

    public LabelForDescriptionVersion(DescriptionVersionBI desc, boolean longForm, boolean showStatus) {
        super(longForm, showStatus);
        if (desc == null) {
            throw new NullPointerException("desc cannot be null...");
        }
        this.desc = desc;
    }

    public I_ImplementActiveLabel copy() throws IOException {
        return TermLabelMaker.newLabel(desc, isLongForm(), getShowStatus());
    }

    public DescriptionVersionBI getDesc() {
        return desc;
    }

    @Override
    protected boolean tupleEquals(Object obj) {
        if (LabelForDescriptionVersion.class.isAssignableFrom(obj.getClass())) {
            LabelForDescriptionVersion another = (LabelForDescriptionVersion) obj;
            return desc.equals(another.desc);
        }
        return false;
    }

    @Override
    protected int tupleHash() {
        return this.desc.hashCode();
    }

    @Override
    protected String getTupleString() {
        return desc.toString();
    }

}
