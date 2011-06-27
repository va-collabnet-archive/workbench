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
public class SnomedMetadataRf1 {

    /** STATUS ***/
    // SCT ID: 900000000000003001
    // SCT Enum: ConceptStatus 0
    // SCT Enum: DescriptionStatus 0
    public static ConceptSpec CURRENT_RF1 =
            new ConceptSpec("current (active status type)",
            UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66"));

    /*** DEFINITIONS ***/
    // SCT ID: none. 
    // SCT Enum: DescriptionType 0 -- Unspecified

    // SCT ID: none. 
    // SCT Enum: DescriptionType 1 -- Preferred

    // SCT ID: none. 
    // SCT Enum: DescriptionType 2 -- FullySpecifiedName

    // SCT ID: none. 
    // SCT Enum: DescriptionType 3
    public static ConceptSpec FULLY_SPECIFIED_NAME_RF1 =
            new ConceptSpec("fully specified name (description type)",
            UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66"));

}
