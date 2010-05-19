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
package org.dwfa.ace.task.status;

import java.util.List;

import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;

/**
 * Utility functions to set an attribute on multiple tuples at once
 */
public class TupleListUtil {

	
    public static void setStatus(I_GetConceptData status, List<? extends I_AmTuple> tuples) {
        for (I_AmTuple t : tuples) {
            t.setStatusId(status.getConceptId());
        }
    }

    public static void setVersion(int version, List<? extends I_AmTuple> tuples) {
        for (I_AmTuple t : tuples) {
            t.setTime(Terms.get().convertToThickVersion(version));
        }
    }

    public static void setPath(int pathId, List<? extends I_AmTuple> tuples) {
        for (I_AmTuple t : tuples) {
            t.setPathId(pathId);
        }
    }
}
