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

import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.table.I_CellTextWithTuple;
import org.dwfa.ace.table.StringWithTuple;

public class StringWithExtTuple extends StringWithTuple implements Comparable<StringWithExtTuple>, I_CellTextWithTuple {

    private I_ThinExtByRefTuple tuple;
    private int id;

    public StringWithExtTuple(String cellText, I_ThinExtByRefTuple tuple, int id) {
        this(cellText, tuple, id, false);
    }

    public StringWithExtTuple(String cellText, I_ThinExtByRefTuple tuple, int id, boolean isInConflict) {
        super(cellText, isInConflict);
        this.tuple = tuple;
        this.id = id;
    }

    public I_ThinExtByRefTuple getTuple() {
        return tuple;
    }

    public int compareTo(StringWithExtTuple another) {
        return getCellText().compareTo(another.getCellText());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
