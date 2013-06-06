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
package org.ihtsdo.tk.api;

/**
 * The Enum RelAssertionType lists the relationship assertion types for display.
 * Changing the relationship assertion type in the <code>viewCoordinate</code> will change the type of data
 * displayed.
 *
 * @see org.ihtsdo.tk.api.coordinate.ViewCoordinate
 */
public enum RelAssertionType {

    /**
     * Displays stated data.
     */
    STATED("stated"),
    /**
     * Displays inferred data from the classifier set in the <code>ViewCoordinate</code>
     */
    INFERRED("inferred"),
    /**
     * Displays the inferred data if it exists on the concept. If not, the displays the stated.
     */
    INFERRED_THEN_STATED("inferred then stated"),
    /**
     * Displays the short normal form of a concept.
     */
    SHORT_NORMAL_FORM("short normal form"),
    /**
     * Displays the long normal form of a concept.
     */
    LONG_NORMAL_FORM("long normal form");
    /**
     * The string to display.
     */
    String displayName;

    /**
     * Instantiates a new relationship assertion type.
     *
     * @param displayName the display name
     */
    private RelAssertionType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the display name of the relationship assertion type.
     * 
     * @return the display name
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return displayName;
    }
}
