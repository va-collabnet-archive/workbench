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
package org.ihtsdo.mojo.maven.transform;

import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.ihtsdo.mojo.maven.I_ReadAndTransform;
import org.ihtsdo.mojo.maven.Transform;

/**
 * Converts a initial capitalisation status from the original SNOMED definition
 * to
 * the UUID of the concept enumeration equivalent
 * 
 * @author Dion McMurtrie
 * 
 */
public class CaseSensitivityToUuidTransform extends AbstractTransform implements I_ReadAndTransform {

    public void setupImpl(Transform transformer) {

    }

    public String transform(String input) throws Exception {

        ArchitectonicAuxiliary.Concept concept;

        if (input.equals("0")) {
            concept = Concept.INITIAL_CHARACTER_NOT_CASE_SENSITIVE;
        } else if (input.equals("1")) {
            concept = Concept.ALL_CHARACTERS_CASE_SENSITIVE;
        } else {
            throw new Exception("Failed converting input " + input + " not one of the expected values 0 and 1");
        }

        UUID uuid = concept.getUids().iterator().next();

        return setLastTransform(uuid.toString());
    }
}
