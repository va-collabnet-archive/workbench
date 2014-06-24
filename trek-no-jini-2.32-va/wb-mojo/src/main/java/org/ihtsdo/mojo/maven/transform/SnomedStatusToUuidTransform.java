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
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.ihtsdo.mojo.maven.I_ReadAndTransform;
import org.ihtsdo.mojo.maven.Transform;

/**
 * Creates a type 5 uuid for a path given a fully specified name.
 * 
 * @author kec
 * 
 */
public class SnomedStatusToUuidTransform extends AbstractTransform implements I_ReadAndTransform {

    public void setupImpl(Transform transformer) {
    }

    public String transform(String input) throws Exception {
        I_ConceptualizeUniversally status = ArchitectonicAuxiliary.getStatusFromId(Integer.parseInt(input));
        UUID statusUuid = status.getUids().iterator().next();
        return setLastTransform(statusUuid.toString());
    }

}
