/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.drools;

import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * Each array represents a pair between the refex and the type value, 
 * for use with the IsMemberOfWithType Drools evaluator. The first object in the 
 * array is a ConceptSpec representing the refex. The second value is the value associated with
 * the refex. This value can be a ConceptSpec, String, Integer, or Boolean. NOTE: the evaluator has only been
 * tested for a concept refex.
 * @author akf
 */
public class RefexTypePairsForEvaluator {
    
    public static ConceptSpec[] DK_PREFERRED = {RefsetAux.DA_REFEX, SnomedMetadataRf2.PREFERRED_RF2};
    
    public static ConceptSpec[] DK_ACCEPTABLE = {RefsetAux.DA_REFEX, SnomedMetadataRf2.ACCEPTABLE_RF2};
}
