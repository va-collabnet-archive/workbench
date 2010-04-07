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
package org.dwfa.ace.table;

public abstract class StringWithTuple<T extends I_CellTextWithTuple<T>> implements I_CellTextWithTuple<T> {

    private String cellText;
    private boolean isInConflict;

    public StringWithTuple(String cellText, boolean isInConflict) {
        super();
        this.cellText = cellText;
        this.isInConflict = isInConflict;
    }

    public String toString() {
        String text = cellText;
        if (isInConflict()) {
            if (text != null && text.startsWith("<html>")) {
                text = text.substring(5);
            }
            text = "<html><em style=\"color:red\">" + text + "</em>";
        }
        return text;
    }

    public int compareTo(T another) {
        if (another == null) {
            return 1;
        }
        return cellText.compareTo(another.getCellText());
    }

    public String getCellText() {
        return cellText;
    }

    public boolean isInConflict() {
        return isInConflict;
    }

}
