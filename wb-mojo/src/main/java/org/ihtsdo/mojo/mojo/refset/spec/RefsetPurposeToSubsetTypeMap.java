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
package org.ihtsdo.mojo.mojo.refset.spec;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;

/**
 * This class maps between a specified refset purpose and the associated subset
 * type (integer value).
 * 
 * @author Christine Hill
 * 
 */
public class RefsetPurposeToSubsetTypeMap {

    protected enum SUBSET_TYPE {
        LANGUAGE_SUBSET(1, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE), REALM_CONCEPT_SUBSET(2, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE), REALM_DESC_SUBSET(3, RefsetAuxiliary.Concept.ANNOTATION_PURPOSE), REALM_REL_SUBSET(4, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE), CONTEXT_CONCEPT_SUBSET(5, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE), CONTEXT_DESC_SUBSET(6, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE), NAVIGATION_SUBSET(7, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE), DUPLICATE_TERMS_SUBSET(8, RefsetAuxiliary.Concept.LANGUAGE_PURPOSE);

        private SUBSET_TYPE(Integer i, I_ConceptualizeUniversally concept) {
            try {
                refsetPurposeNid = concept.localize().getNid();
            } catch (TerminologyException e) {
                throw new RuntimeException(this.toString(), e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            subsetTypeId = i;
        }

        private int refsetPurposeNid;
        private int subsetTypeId;

        private int getRefsetPurposeNid() {
            return refsetPurposeNid;
        }

        private int getSubsetTypeId() {
            return subsetTypeId;
        }
    };

    public static int convert(I_GetConceptData refsetPurpose) {
        SUBSET_TYPE subsetType = null;

        for (SUBSET_TYPE currentType : SUBSET_TYPE.values()) {
            if (currentType.getRefsetPurposeNid() == refsetPurpose.getConceptNid()) {
                subsetType = currentType;
                break;
            }
        }

        if (subsetType != null) {
            return subsetType.getSubsetTypeId();
        } else {
            return -1;
        }

    }

}
