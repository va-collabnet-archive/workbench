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
package org.ihtsdo.tk.api.constraint;

/**
 * The Enum ConstraintCheckType lists constraint type to be used with a
 * <code>ConstraintBI</code>. Each constraint specifies a
 * subject-property-value. This enumeration lists the check types which can be
 * specified for each.
 */
public enum ConstraintCheckType {

    /**
     * Ignore - x.
     */
    IGNORE,
    /**
     * Equals - e.
     */
    EQUALS,
    /**
     * Kind of - k.
     */
    KIND_OF,
    /**
     * Regular expression - r.
     */
    REGEX;

    /**
     * Gets the type for the given string.
     *
     * @param type the one character abbreviation of the type. 
     * @return the specified constraint check type
     */
    public static ConstraintCheckType get(String type) {
        if (type.equals("x")) {
            return IGNORE;
        }
        if (type.equals("e")) {
            return EQUALS;
        }
        if (type.equals("k")) {
            return KIND_OF;
        }
        if (type.equals("r")) {
            return REGEX;
        }
        throw new UnsupportedOperationException("Can't handle type: " + type);
    }
}
