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

import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;

/**
 * The Enum ChangeSetWriterThreading represents the available changeset
 * generation threading.
 */
public enum ChangeSetWriterThreading {

    /**
     * USe single threaded.
     */
    SINGLE_THREAD("single threaded"),
    /**
     * Use multi threaded.
     */
    MULTI_THREAD("multi-threaded");
    String displayString;

    /**
     * Instantiates a new change set writer threading.
     *
     * @param displayString the string to display for the threading type
     */
    private ChangeSetWriterThreading(String displayString) {
        this.displayString = displayString;
    }

    /**
     * Returns the display string for this changeset writer threading type.
     *
     * @return the display string
     */
    @Override
    public String toString() {
        return displayString;
    }

    /**
     * Gets the changeset policy for the given
     * <code>changeSetGenerationThreadingPolicy</code>. Supports:
     * single-threaded and multi-threaded.
     *
     * @param changeSetGenerationThreadingPolicy the change set generation
     * threading policy to use to find the changeset writer threading
     * @return the change set writer threading
     */
    public static ChangeSetWriterThreading get(ChangeSetGenerationThreadingPolicy changeSetGenerationThreadingPolicy) {
        switch (changeSetGenerationThreadingPolicy) {
            case SINGLE_THREAD:
                return SINGLE_THREAD;
            case MULTI_THREAD:
                return MULTI_THREAD;
            default:
                throw new UnsupportedOperationException("Can't handle csgtp: " + changeSetGenerationThreadingPolicy);
        }
    }

    /**
     * Converts this changeset writer threading policy to a changeset generation
     * threading policy. Supports: single-threaded and multi-threaded.
     *
     * @return the associated change set generation threading policy
     */
    public ChangeSetGenerationThreadingPolicy convert() {
        switch (this) {
            case SINGLE_THREAD:
                return ChangeSetGenerationThreadingPolicy.SINGLE_THREAD;
            case MULTI_THREAD:
                return ChangeSetGenerationThreadingPolicy.MULTI_THREAD;
            default:
                throw new UnsupportedOperationException("Can't handle csgtp: " + this);
        }
    }
}
