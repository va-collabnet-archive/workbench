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

import java.util.Collection;

import org.ihtsdo.tk.api.ComponentVersionBI;

/**
 * The Class FoundContradictionVersions represents the contradicting versions of
 * a component and the associated contradiction type.
 */
public class FoundContradictionVersions {

    /**
     * The result of testing for contradictions.
     */
    private ContradictionResult result;
    /**
     * The contradicting versions.
     */
    private Collection<? extends ComponentVersionBI> versions;

    /**
     * Instantiates a new
     * <code>FoundContradictionVersions</code> object based on the given
     * <code>contradictionResult</code> and
     * <code>componentVersions</code>.
     *
     * @param contradictionResult the result of testing for a contradiction that
     * specifies what type of contradiction was found
     * @param componentVersions the contradicting component versions
     */
    public FoundContradictionVersions(ContradictionResult contradictionResult, Collection<? extends ComponentVersionBI> componentVersions) {
        result = contradictionResult;
        versions = componentVersions;
    }

    /**
     * Gets the contradiction result specifying which type of contradiction was
     * found.
     *
     * @return the contradiction result
     */
    public ContradictionResult getResult() {
        return result;
    }

    /**
     * Gets conflicting component versions.
     *
     * @return the component versions in contradiction, and empty <code>Collection</code> if no contradictions
     */
    public Collection<? extends ComponentVersionBI> getVersions() {
        return versions;
    }

    /**
     * Creates a String representation of the <code>FoundContradictionVersions</code> object.
     * 
     * @return a String representation of the contradiction finding and the contradicting versions
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("ContradictionFinder findings: ");

        if (versions.size() == 0 && result == ContradictionResult.NONE) {
            buffer.append("\nThere are no versions in conflict");
        } else if (result == ContradictionResult.ERROR
                || versions.size() == 0
                || result == ContradictionResult.NONE) {
            buffer.append("\nThere was an odd result that requires investigation for this concept");
        } else {
            buffer.append("\nThe result of the inspection was: ");

            switch (result) {
                case CONTRADICTION:
                    buffer.append("Contradcition");
                    break;
                case DUPLICATE_EDIT:
                    buffer.append("Duplicate edit of a concept");
                    break;
                case DUPLICATE_NEW:
                    buffer.append("Duplicate newly created concepts");
                    break;
                case SINGLE_MODELER_CHANGE:
                    buffer.append("Single modeler change");
                    break;
            }

            buffer.append("\nWith the following Versions: ");

            int counter = 0;
            for (ComponentVersionBI v : versions) {
                buffer.append("\n" + counter++ + ": " + v.toString());
            }
        }

        return buffer.toString();
    }
}
