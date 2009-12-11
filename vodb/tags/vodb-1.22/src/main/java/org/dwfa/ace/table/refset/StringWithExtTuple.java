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

public class StringWithExtTuple implements Comparable<StringWithExtTuple>, I_CellTextWithTuple {
    String cellText;

    I_ThinExtByRefTuple tuple;

    public StringWithExtTuple(String cellText, I_ThinExtByRefTuple tuple) {
       super();
       this.cellText = cellText;
       this.tuple = tuple;
    }

    public String getCellText() {
       return cellText;
    }

    public I_ThinExtByRefTuple getTuple() {
       return tuple;
    }

    public String toString() {
       return cellText;
    }

    public int compareTo(StringWithExtTuple another) {
       return cellText.compareTo(another.cellText);
    }
 }
