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

import org.dwfa.ace.api.I_RelTuple;

public class RelTupleConflictComparator implements Comparator<I_RelTuple> {

    public int compare(I_RelTuple t1, I_RelTuple t2) {
        if (t1.getStatusId() != t2.getStatusId()) {
            return t1.getStatusId() - t2.getStatusId();
        }
        if (t1.getC1Id() != t2.getC1Id()) {
            return t1.getC1Id() - t2.getC1Id();
        }
        if (t1.getC2Id() != t2.getC2Id()) {
            return t1.getC2Id() - t2.getC2Id();
        }
        if (t1.getCharacteristicId() != t2.getCharacteristicId()) {
            return t1.getCharacteristicId() - t2.getCharacteristicId();
        }
        if (t1.getGroup() != t2.getGroup()) {
            return t1.getGroup() - t2.getGroup();
        }
        if (t1.getRefinabilityId() != t2.getRefinabilityId()) {
            return t1.getRefinabilityId() - t2.getRefinabilityId();
        }
        if (t1.getRelTypeId() != t2.getRelTypeId()) {
            return t1.getRelTypeId() - t2.getRelTypeId();
        }
        return 0;
    }
}
