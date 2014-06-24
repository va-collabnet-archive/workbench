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
package org.dwfa.ace;

import java.io.IOException;
import java.util.Comparator;

import org.dwfa.ace.api.I_GetConceptData;

public class CompareConceptBeanInitialText implements Comparator<I_GetConceptData> {

    public int compare(I_GetConceptData cb1, I_GetConceptData cb2) {
        try {
            int comparison = cb1.getInitialText().compareTo(cb2.getInitialText());
            if (comparison == 0) {
                comparison = cb1.getConceptNid() - cb2.getConceptNid();
            }
            return comparison;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
