/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.binding.snomed;

import java.io.IOException;
import java.util.UUID;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * The Class Language contains
 * <code>ConceptSpec</code> representations of concepts describing language
 * concepts or concepts representing language refsets. Used by the
 * <code>DialectHelper</code> for determining the language of a description
 * text.
 *
 * @author kec
 */
public class Language {

    /**
     * Represents the language concept: Spanish.
     */
    public static ConceptSpec ES =
            new ConceptSpec("Spanish (language concept)",
            UUID.fromString("0fcf44fb-d0a7-3a67-bc9f-eb3065ed3c8e"));
    /**
     * Represents the language dialect concept: English.
     */
    public static ConceptSpec EN =
            new ConceptSpec("English (language concept)",
            UUID.fromString("06d905ea-c647-3af9-bfe5-2514e135b558"));
    /**
     * Represents the language dialect concept: English-Australian.
     */
    public static ConceptSpec EN_AU =
            new ConceptSpec("English-Australian",
            UUID.fromString("63de00e1-df26-3623-9e24-0464e3048e1b"));
    /**
     * Represents the language dialect concept: English-Canadian.
     */
    public static ConceptSpec EN_CA =
            new ConceptSpec("English-Canadian",
            UUID.fromString("48067f1c-1ef5-3e9e-bb86-03e8a5eb325f"));
    /**
     * TRepresents the language dialect concept: English-New Zealand.
     */
    public static ConceptSpec EN_NZ =
            new ConceptSpec("English-New Zealand",
            UUID.fromString("073da2f6-34c5-3f74-8688-b43c4e3f7a1b"));
    /**
     * Represents the language dialect concept: English-United Kingdom.
     */
    public static ConceptSpec EN_UK =
            new ConceptSpec("English-United Kingdom",
            UUID.fromString("2986eb1f-a853-3278-9850-c9fcf4333c41"));
    /**
     * Represents the language dialect concept: English-United States.
     */
    public static ConceptSpec EN_US =
            new ConceptSpec("English-United States",
            UUID.fromString("ddcc2a22-f8fc-33bb-bbb0-944abb395c14"));
    /**
     * Represents the language refset concept: EN Text with dialect variants.
     * This refset contains the English language words that have dialect
     * variants.
     */
    public static ConceptSpec EN_VARIANT_TEXT =
            new ConceptSpec("EN Text with dialect variants",
            UUID.fromString("c285d882-5378-396e-8db1-ad40bac27078"));
    /**
     * Represents the language refset concept: en-AU text variants. This refset
     * contains the Australian English dialect variants.
     */
    public static ConceptSpec EN_AU_TEXT_VARIANTS =
            new ConceptSpec("en-AU text variants",
            UUID.fromString("0d4fd1ed-00a0-396a-9417-32477df1c669"));
    /**
     * Represents the language refset concept: en-CA text variants. This refset
     * contains the Canadian English dialect variants.
     */
    public static ConceptSpec EN_CA_TEXT_VARIANTS =
            new ConceptSpec("en-CA text variants",
            UUID.fromString("f1d5859d-0e7f-38a5-a811-7912908aef56"));
    /**
     * Represents the language refset concept: en-NZ text variants. This refset
     * contains the New Zealand English dialect variants.
     */
    public static ConceptSpec EN_NZ_TEXT_VARIANTS =
            new ConceptSpec("en-NZ text variants",
            UUID.fromString("19e8b4d2-f4c2-39ca-8601-467ac5fd14a8"));
    /**
     * Represents the language refset concept: en-UK text variants. This refset
     * contains the UK English dialect variants.
     */
    public static ConceptSpec EN_UK_TEXT_VARIANTS =
            new ConceptSpec("en-UK text variants",
            UUID.fromString("6c920832-2208-39e7-a9ff-9362b84a1b73"));
    /**
     * Represents the language refset concept: en-US text variants. This refset
     * contains the US English dialect variants.
     */
    public static ConceptSpec EN_US_TEXT_VARIANTS =
            new ConceptSpec("en-US text variants",
            UUID.fromString("4a28be68-0e9c-349b-a474-4472c889345e"));

    /**
     * Gets a language concept version from the two character
     * <code>String</code> abbreviation of the language, based on the <code>viewCoordinate</code>.
     * Only "en" and "es" are supported.
     *
     * @param lang two character <code>String</code> abbreviation of the
     * language
     * @param viewCoordinate the view coordiante specifying which version to return
     * @return the spcified concept version
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ConceptVersionBI getConceptFromLang(
            String lang,
            ViewCoordinate viewCoordinate) throws IOException {
        if (lang.toLowerCase().startsWith("en")) {
            return EN.getStrict(viewCoordinate);
        }
        if (lang.toLowerCase().startsWith("es")) {
            return ES.getStrict(viewCoordinate);
        }
        throw new UnsupportedOperationException("lang: " + lang);
    }
}
