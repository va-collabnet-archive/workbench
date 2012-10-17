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
 * The Enum ChangeSetGenerationThreadingPolicy represents the available
 * changeset generation threading policies.
 *
 */
public enum ChangeSetGenerationThreadingPolicy {

    /**
     * Use single threaded.
     */
    SINGLE_THREAD("single threaded"),
    /**
     * Use multi-threaded.
     */
    MULTI_THREAD("multi-threaded");
    
    String displayString;

    /**
     * Instantiates a new change set generation threading policy.
     *
     * @param displayString the string to display for the policy
     */
    private ChangeSetGenerationThreadingPolicy(String displayString) {
        this.displayString = displayString;
    }

    /**
     * Returns the display string for this changeset threading policy type.
     * @return the display string
     */
    @Override
    public String toString() {
        return displayString;
    }
}
