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
 * The Class WbDescType contains
 * <code>ConceptSpec</code> representations Release Format 1 description types.
 */
public class WbDescType {

    /**
     * Represents the description type: fully specified name.
     */
    public static ConceptSpec FULLY_SPECIFIED =
            new ConceptSpec("RF1 fully specified name (description type)",
            UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66"));
    /**
     * Represents the description type: preferred term.
     */
    public static ConceptSpec PREFERRED =
            new ConceptSpec("RF1 preferred term (description type)",
            UUID.fromString("d8e3b37d-7c11-33ef-b1d0-8769e2264d44"));
    /**
     * Represents the description type: synonym.
     */
    public static ConceptSpec SYNONYM =
            new ConceptSpec("RF1 synonym (description type)",
            UUID.fromString("d6fad981-7df6-3388-94d8-238cc0465a79"));
}
