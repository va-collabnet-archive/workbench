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
package org.ihtsdo.tk.contradiction;

/**
 * The Enum ContradictionResult lists the types of contradiction that could
 * occur.
 *
 */
public enum ContradictionResult {

    /**
     * No changes by any developers have been made to a concept.
     */
    NONE,
    /**
     * Single change by a single modeler on a concept, be it a edit on an
     * existing component, a new component, or the creation of a new concept.
     */
    SINGLE_MODELER_CHANGE,
    /**
     * An edit by two or more developers on a given component where all
     * developers have made the same exact modification to the component.
     */
    DUPLICATE_EDIT,
    /**
     * A creation of a new component on a given concept by two or more
     * developers where all developers have made the same component type with
     * the same values.
     */
    DUPLICATE_NEW,
    /**
     * Two or more modelers make changes to the same concept such that the
     * change isn't of type DUPLICATE_EDIT nor DUPLICATE_NEW.
     */
    CONTRADICTION,
    /**
     * An Error in detection.
     */
    ERROR;
}
