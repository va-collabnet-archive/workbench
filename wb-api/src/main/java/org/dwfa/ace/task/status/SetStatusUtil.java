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

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;

/**
 * @deprecated Use {@link TupleListUtil}
 */
@Deprecated
public class SetStatusUtil {

    public static void setStatusOfRelInfo(I_GetConceptData status, List<I_RelTuple> reltuples) {
        for (I_RelTuple rt : reltuples) {
            rt.setStatusId(status.getConceptNid());
        }
    }

    public static void setStatusOfConceptInfo(I_GetConceptData status, List<? extends I_ConceptAttributeTuple> contuples) {
        for (I_ConceptAttributeTuple cat : contuples) {
            cat.setStatusId(status.getConceptNid());
        }
    }

    public static void setStatusOfDescriptionInfo(I_GetConceptData status, List<? extends I_DescriptionTuple> desctuples) {
        for (I_DescriptionTuple dt : desctuples) {
            dt.setStatusId(status.getConceptNid());
        }
    }
}
