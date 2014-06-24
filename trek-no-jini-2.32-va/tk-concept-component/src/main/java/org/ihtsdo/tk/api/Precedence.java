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
 * The Enum Precedence is used to list the type of precedence is available for
 * resolving contradictions. When a contradiction occurs the way it is resolved
 * is determined by what type of precedence is set, and therefore which of the
 * versions in contradiction to give precedence to.
 */
public enum Precedence {

    /**
     * Time based precedence. If two versions are both on a route to the
     * destination the version with the later time has higher precedence.
     */
    TIME("time precedence", "<html>If two versions are both on a route to the destination, "
    + "the version with the later time has higher precedence."),
    /**
     * Path based precedence. If two versions are both on route to the
     * destination, but one version is on a path that is closer to the
     * destination, the version on the closer path has higher precedence. If two
     * versions are on the same path, the version with the later time has higher
     * precedence.
     */
    PATH("path precedence", "<html>If two versions are both on route to the destination, "
    + "but one version is on a path that is closer to the destination, "
    + "the version on the closer path has higher precedence.<br><br>If two versions "
    + "are on the same path, the version with the later time has higher precedence."),
    /**
     * Mixed precedence. Returns a conflict if time based precedence and path
     * base precedence have different views. The conflict is resolved using the
     * conflict resolution policy.
     */
    MIXED("mixed precedence", "<html>Returns a conflict if time based "
    + "precedence and path base precedence have different views."
    + "The conflict is resolved using the conflict resolution "
    + "policy.");
    /**
     * The label for the precedence.
     */
    private String label;
    /**
     * The description of the type of precedence.
     */
    private String description;

    /**
     * Instantiates a new type of precedence.
     *
     * @param label the label
     * @param description the description
     */
    private Precedence(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /**
     * Gets the description of the precedence.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return label;
    }
}
