/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.binding.snomed;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * The Class RefsetAux contains
 * <code>ConceptSpec</code> representations of concepts representing refsets.
 */
public class RefsetAux {

    /** Represents the language refset for: Dutch. */
    public static ConceptSpec NL_REFEX =
            new ConceptSpec("Dutch [International Organization for Standardization 639-1 code nl] language reference set (foundation metadata concept)",
            UUID.fromString("592fe43f-f07e-568b-90e1-96de4c33b2a8"));
    
    /** Represents the refset concept for the GMDN review status.. */
    public static ConceptSpec GMDN_REFEX =
            new ConceptSpec("GMDN review status reference set (foundation metadata concept)",
            UUID.fromString("c5994e33-21d3-327c-ab25-06d2356d2d68"));
    
    /** Represents the language refset for: Swedish.. */
    public static ConceptSpec SV_REFEX =
            new ConceptSpec("Swedish [International Organization for Standardization 639-1 code sv] language reference set (foundation metadata concept)",
            UUID.fromString("e57ec728-742f-56b3-9b53-9613670fb24d"));
    
    /** Represents the language dialect refset for: US English. */
    public static ConceptSpec EN_US_REFEX =
            new ConceptSpec("United States of America English language reference set (foundation metadata concept)",
            UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad"));
    
    /** Represents the language refset for: GB Engish. */
    public static ConceptSpec EN_GB_REFEX =
            new ConceptSpec("Great Britain English language reference set",
            UUID.fromString("eb9a5e42-3cba-356d-b623-3ed472e20b30"));
}
