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

import org.dwfa.ace.api.I_ConceptAttributeTuple;

public class AttrTupleConflictComparator implements Comparator<I_ConceptAttributeTuple> {
    public int compare(I_ConceptAttributeTuple t1, I_ConceptAttributeTuple t2) {
        if (t1.getConceptStatus() != t2.getConceptStatus()) {
            return t1.getConceptStatus() - t2.getConceptStatus();
        }
        if (t1.isDefined() != t2.isDefined()) {
            if (t1.isDefined()) {
                return -1;
            } else {
                return +1;
            }
        }
        return 0;
    }
}
