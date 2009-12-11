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

import org.dwfa.ace.table.I_CellTextWithTuple;
import org.dwfa.vodb.types.ThinExtByRefTuple;

public class StringWithExtTuple implements Comparable, I_CellTextWithTuple {
    String cellText;

    ThinExtByRefTuple tuple;

    public StringWithExtTuple(String cellText, ThinExtByRefTuple tuple) {
        super();
        this.cellText = cellText;
        this.tuple = tuple;
    }

    public String getCellText() {
        return cellText;
    }

    public ThinExtByRefTuple getTuple() {
        return tuple;
    }

    public String toString() {
        return cellText;
    }

    public int compareTo(Object o) {
        StringWithExtTuple another = (StringWithExtTuple) o;
        return cellText.compareTo(another.cellText);
    }
}
