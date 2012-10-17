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
package org.ihtsdo.tk.api.contradiction;

//~--- non-JDK imports --------------------------------------------------------
import java.util.ArrayList;
import java.util.List;
import org.ihtsdo.tk.api.ComponentVersionBI;


/**
 * The Class EditPathWinsStrategy implements resolution that suppresses the
 * members that are NOT on the edit path(s) from participating in the potential
 * contradiction.
 */
public class EditPathWinsStrategy extends ContradictionManagementStrategy {

    private static final long serialVersionUID = 1L;

    //~--- methods -------------------------------------------------------------
    /**
     *
     * @param <T> the generic type of component version
     * @param versions the versions to resolve
     * @return the component versions resolved according to this edit path wins
     * strategy
     */
    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(List<T> versions) {
        List returnValues = new ArrayList(2);

        for (T v : versions) {
            if (ec.getEditPathsSet().contains(v.getPathNid())) {
                returnValues.add(v);
            }
        }

        if (returnValues.isEmpty()) {
            for (T part : versions) {
                if (part.isBaselineGeneration()) {
                    returnValues.add(part);
                }
            }
        }

        return returnValues;
    }

    /**
     *
     * @param <T> the generic type of component versions
     * @param part1 the first part
     * @param part2 the second part
     * @return parts resolved according to this edit path wins strategy
     */
    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(T part1, T part2) {
        assert part1 != null;
        assert part2 != null;
        assert ec != null;

        List returnValues = new ArrayList(2);

        if (ec.getEditPathsSet().contains(part1.getPathNid())) {
            returnValues.add(part1);
        }

        if (ec.getEditPathsSet().contains(part2.getPathNid())) {
            returnValues.add(part2);
        }

        if (returnValues.isEmpty()) {
            if (part1.isBaselineGeneration()) {
                returnValues.add(part1);
            }

            if (part2.isBaselineGeneration()) {
                returnValues.add(part2);
            }
        }

        return returnValues;
    }

    //~--- get methods ---------------------------------------------------------
    /**
     *
     * @return a description of this edit path wins conflict resolution strategy
     */
    @Override
    public String getDescription() {
        return "<html>This resolution strategy implements resolution that"
                + "<li>suppresses the members that are NOT on the edit path(s) from </li>"
                + "<li>participating in the potential contradiction.</ul>" + "</html>";
    }

    /**
     *
     * @return the display name of this edit path wins conflict resolution
     * strategy
     */
    @Override
    public String getDisplayName() {
        return "Suppress versions NOT on a edit path from contradictions";
    }
}
