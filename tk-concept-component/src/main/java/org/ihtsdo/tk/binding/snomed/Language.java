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

import java.io.IOException;
import java.util.UUID;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.spec.ConceptSpec;

// TODO: Auto-generated Javadoc
/**
 * The Class Language.
 *
 * @author kec
 */
public class Language {

    /** The es. */
    public static ConceptSpec ES =
            new ConceptSpec("Spanish (language concept)",
            UUID.fromString("0fcf44fb-d0a7-3a67-bc9f-eb3065ed3c8e"));
    
    /** The en. */
    public static ConceptSpec EN =
            new ConceptSpec("English (language concept)",
            UUID.fromString("06d905ea-c647-3af9-bfe5-2514e135b558"));
    
    /** The en au. */
    public static ConceptSpec EN_AU =
            new ConceptSpec("English-Australian",
            UUID.fromString("63de00e1-df26-3623-9e24-0464e3048e1b"));
    
    /** The en ca. */
    public static ConceptSpec EN_CA =
            new ConceptSpec("English-Canadian",
            UUID.fromString("48067f1c-1ef5-3e9e-bb86-03e8a5eb325f"));
    
    /** The en nz. */
    public static ConceptSpec EN_NZ =
            new ConceptSpec("English-New Zealand",
            UUID.fromString("073da2f6-34c5-3f74-8688-b43c4e3f7a1b"));
    
    /** The en uk. */
    public static ConceptSpec EN_UK =
            new ConceptSpec("English-United Kingdom",
            UUID.fromString("2986eb1f-a853-3278-9850-c9fcf4333c41"));
    
    /** The en us. */
    public static ConceptSpec EN_US =
            new ConceptSpec("English-United States",
            UUID.fromString("ddcc2a22-f8fc-33bb-bbb0-944abb395c14"));
    
    /** The en variant text. */
    public static ConceptSpec EN_VARIANT_TEXT =
            new ConceptSpec("EN Text with dialect variants",
            UUID.fromString("c285d882-5378-396e-8db1-ad40bac27078"));
    
    /** The en au text variants. */
    public static ConceptSpec EN_AU_TEXT_VARIANTS =
            new ConceptSpec("en-AU text variants",
            UUID.fromString("0d4fd1ed-00a0-396a-9417-32477df1c669"));
    
    /** The en ca text variants. */
    public static ConceptSpec EN_CA_TEXT_VARIANTS =
            new ConceptSpec("en-CA text variants",
            UUID.fromString("f1d5859d-0e7f-38a5-a811-7912908aef56"));
    
    /** The en nz text variants. */
    public static ConceptSpec EN_NZ_TEXT_VARIANTS =
            new ConceptSpec("en-NZ text variants",
            UUID.fromString("19e8b4d2-f4c2-39ca-8601-467ac5fd14a8"));
    
    /** The en uk text variants. */
    public static ConceptSpec EN_UK_TEXT_VARIANTS =
            new ConceptSpec("en-UK text variants",
            UUID.fromString("6c920832-2208-39e7-a9ff-9362b84a1b73"));
    
    /** The en us text variants. */
    public static ConceptSpec EN_US_TEXT_VARIANTS =
            new ConceptSpec("en-US text variants",
            UUID.fromString("4a28be68-0e9c-349b-a474-4472c889345e"));

    /**
     * Gets the concept from lang.
     *
     * @param lang the lang
     * @param vc the vc
     * @return the concept from lang
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static ConceptVersionBI getConceptFromLang(
            String lang,
            ViewCoordinate vc) throws IOException {
        if (lang.toLowerCase().startsWith("en")) {
            return EN.getStrict(vc);
        }
        if (lang.toLowerCase().startsWith("es")) {
            return ES.getStrict(vc);
        }
        throw new UnsupportedOperationException("lang: " + lang);
    }
}
