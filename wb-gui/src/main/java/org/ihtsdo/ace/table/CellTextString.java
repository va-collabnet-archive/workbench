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
package org.ihtsdo.ace.table;

public abstract class CellTextString <T extends I_CellText<T>> implements I_CellText<T> {

    private String cellText;

    public CellTextString (String cellText) {
        super();
        this.cellText = cellText;
    }

    public String toString() {
        String text = cellText;
            
//      text = "<html><em style=\"color:red\">" + text + "</em>";
  
        return text;
    }

    public int compareTo(T another) {
        if (another == null || another.getCellText() == null) {
            return 1;
        }
        if (cellText == null) {
            return 1;
        }
        return cellText.compareTo(another.getCellText());
    }

    public String getCellText() {
        return cellText;
    }
}
