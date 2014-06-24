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
package org.ihtsdo.tk.api.cs;

import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;


/**
 * The Enum ChangeSetPolicy represents available changeset policies.
 */
public enum ChangeSetPolicy {

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
    ;

    
    String displayString;

    /**
     * Instantiates a new change set policy.
     *
     * @param displayString the string to display to the user
     */
    private ChangeSetPolicy(String displayString) {
        this.displayString = displayString;
    }

    /**
     * Returns the display string of this changeset policy.
     *
     * @return the display string of this changeset policy
     */
    @Override
    public String toString() {
        return displayString;
    }

    /**
     * Gets the changeset policy for the given
     * <code>changeSetGenerationPolicy</code>. Supports: comprehensive,
     * incremental, mutable only, and off.
     *
     * @param changeSetGenerationPolicy the change set generation policy to use
     * to find the changeset policy
     * @return the specified change set policy
     */
    public static ChangeSetPolicy get(ChangeSetGenerationPolicy changeSetGenerationPolicy) {
        switch (changeSetGenerationPolicy) {
            case COMPREHENSIVE:
                return COMPREHENSIVE;
            case INCREMENTAL:
                return INCREMENTAL;
            case MUTABLE_ONLY:
                return MUTABLE_ONLY;
            case OFF:
                return OFF;
            default:
                throw new UnsupportedOperationException("Can't handle csgp: " + changeSetGenerationPolicy);
        }
    }

    /**
     * Converts this changeset policy to a changeset generation policy.
     * Supports: comprehensive, incremental, mutable only, and off.
     *
     * @return the associated change set generation policy
     */
    public ChangeSetGenerationPolicy convert() {
        switch (this) {
            case COMPREHENSIVE:
                return ChangeSetGenerationPolicy.COMPREHENSIVE;
            case INCREMENTAL:
                return ChangeSetGenerationPolicy.INCREMENTAL;
            case MUTABLE_ONLY:
                return ChangeSetGenerationPolicy.MUTABLE_ONLY;
            case OFF:
                return ChangeSetGenerationPolicy.OFF;
            default:
                throw new UnsupportedOperationException("Can't handle csgp: " + this);
        }
    }
}
