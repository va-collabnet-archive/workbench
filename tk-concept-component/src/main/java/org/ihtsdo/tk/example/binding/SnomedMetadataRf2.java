/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.tk.example.binding;

import java.util.UUID;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 *
 * @author marc
 */
public class SnomedMetadataRf2 {

    /** STATUS ***/
    // SCT ID: 900000000000545005
    public static ConceptSpec ACTIVE_VALUE_RF2 =
            new ConceptSpec("Active value (foundation metadata concept)",
            UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f"));

    /*** DEFINITIONS ***/
    // SCT ID: 900000000000003001
    public static ConceptSpec FULLY_SPECIFIED_NAME_RF2 =
            new ConceptSpec("Fully specified name (core metadata concept)",
            UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf"));


}
