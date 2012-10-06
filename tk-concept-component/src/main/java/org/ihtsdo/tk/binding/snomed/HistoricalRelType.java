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

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * The Class HistoricalRelType contains
 * <code>ConceptSpec</code> representations of concepts describing historical
 * relationship types. A historical relationship is given to retired concepts to
 * provide more information about
 */
public class HistoricalRelType {

    /**
     * Represents the historical relationship type: may be a.
     */
    public static ConceptSpec MAY_BE_A =
            new ConceptSpec("MAY BE A (attribute)",
            UUID.fromString("721dadc2-53a0-3ffa-8abd-80ff6aa87db2"));
    /**
     * Represents the historical relationship type: moved to.
     */
    public static ConceptSpec MOVED_TO =
            new ConceptSpec("MOVED TO (attribute)",
            UUID.fromString("c3394436-568c-327a-9d20-4a258d65a936"));
    /**
     * Represents the historical relationship type: replaced by.
     */
    public static ConceptSpec REPLACED_BY =
            new ConceptSpec("REPLACED BY (attribute)",
            UUID.fromString("0b010f24-523b-3ae4-b3a2-ec1f425c8a85"));
    /**
     * Represents the historical relationship type: same as.
     */
    public static ConceptSpec SAME_AS =
            new ConceptSpec("SAME AS (attribute)",
            UUID.fromString("87594159-50f0-3b5f-aa4f-f6061c0ce497"));
    /**
     * Represents the historical relationship type: was a.
     */
    public static ConceptSpec WAS_A =
            new ConceptSpec("WAS A (attribute)",
            UUID.fromString("a1a598c0-7988-3c8e-9ba2-342f24de7c6b"));

    /**
     * Gets an
     * <code>array</code> of the historical relationship types.
     *
     * @return an <code>array</code> of the historical relationship types
     */
    public static ConceptSpec[] getHistoricalTypes() {

        ConceptSpec[] historicalTypes = new ConceptSpec[5];

        historicalTypes[0] = MAY_BE_A;
        historicalTypes[1] = MOVED_TO;
        historicalTypes[2] = REPLACED_BY;
        historicalTypes[3] = SAME_AS;
        historicalTypes[4] = WAS_A;

        return historicalTypes;

    }
}
