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
package org.dwfa.ace.api;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.tapi.TerminologyException;

public interface I_SupportClassifier extends I_TermFactory {

    public void writeRel(I_RelVersioned rel) throws IOException;

    public I_RelVersioned newRelationship(UUID relUuid, int uuidType, int conceptNid, int relDestinationNid,
            int pathNid, int version, int relStatusNid, int relTypeNid, int relCharacteristicNid,
            int relRefinabilityNid, int relGroup) throws TerminologyException, IOException;

}
