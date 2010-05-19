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
package org.dwfa.ace.task.conflict.detector;

import java.util.Comparator;

import org.dwfa.ace.api.I_DescriptionTuple;

public class DescriptionTupleConflictComparator implements Comparator<I_DescriptionTuple> {

    public int compare(I_DescriptionTuple t1, I_DescriptionTuple t2) {
        if (t1.getStatusId() != t2.getStatusId()) {
            return t1.getStatusId() - t2.getStatusId();
        }
        if (t1.getConceptId() != t2.getConceptId()) {
            return t1.getConceptId() - t2.getConceptId();
        }
        if (t1.isInitialCaseSignificant() != t2.isInitialCaseSignificant()) {
            if (t1.isInitialCaseSignificant()) {
                return -1;
            } else {
                return +1;
            }
        }
        if (t1.getLang().equals(t2.getLang()) == false) {
            return t1.getLang().compareTo(t2.getLang());
        }
        if (t1.getText().equals(t2.getText()) == false) {
            return t1.getText().compareTo(t2.getText());
        }
        if (t1.getTypeId() != t2.getTypeId()) {
            return t1.getTypeId() - t2.getTypeId();
        }
        return 0;
    }
}
