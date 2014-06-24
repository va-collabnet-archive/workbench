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
package org.ihtsdo.tk.api.changeset;

/**
 * The Enum ChangeSetGenerationPolicy representing the available changeset
 * generation policies.
 */
public enum ChangeSetGenerationPolicy {

    /**
     * Don't generate change sets.
     */
    OFF("no changeset"),
    /**
     * Only include changes that represent the sapNids from the current commit.
     */
    INCREMENTAL("incremental changeset"),
    /**
     * Only include sapNids that are written to the mutable database.
     */
    MUTABLE_ONLY("mutable-only changeset"),
    /**
     * Include all changes.
     */
    COMPREHENSIVE("comprehensive changeset");
    String displayString;

    /**
     * Instantiates a new change set generation policy.
     *
     * @param displayString the string to display for the policy
     */
    private ChangeSetGenerationPolicy(String displayString) {
        this.displayString = displayString;
    }

    /**
     * Returns the display string for this changeset generation policy type.
     * @return the display string
     */
    @Override
    public String toString() {
        return displayString;
    }
}
