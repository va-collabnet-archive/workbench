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
package org.dwfa.tapi.spec;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;

public class DescriptionSpec {

    private UUID[] uuids;

    private String description;

    private ConceptSpec concept;

    public DescriptionSpec(String description, String uuid, ConceptSpec concept) {
        this(description, UUID.fromString(uuid), concept);
    }

    public DescriptionSpec(String description, UUID uuid, ConceptSpec concept) {
        this(description, new UUID[] { uuid }, concept);
    }

    public DescriptionSpec(String description, UUID[] uuids, ConceptSpec concept) {
        this.uuids = uuids;
        this.description = description;
        this.concept = concept;
    }

    public I_DescribeConceptLocally localize() {
        try {
            I_ConceptualizeLocally localConcept = concept.localize();
            int dnid = LocalFixedTerminology.getStore().getNid(Arrays.asList(uuids));
            I_DescribeConceptLocally desc = LocalFixedTerminology.getStore()
                .getDescription(dnid, localConcept.getNid());
            if (description.equals(desc.getText())) {
                return desc;
            } else {
                throw new RuntimeException("Descriptions to not match. 1: " + description + " 2: " + desc.getText());
            }
        } catch (NoMappingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TerminologyException e) {
            throw new RuntimeException(e);
        }
    }

}
