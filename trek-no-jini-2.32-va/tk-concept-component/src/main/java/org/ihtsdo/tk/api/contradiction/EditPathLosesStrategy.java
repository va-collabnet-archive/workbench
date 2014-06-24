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

import java.util.ArrayList;
import java.util.List;
import org.ihtsdo.tk.api.ComponentVersionBI;

/**
 * The Class EditPathLosesStrategy implements resolution that checks if
 * conflicting members are present on the users edit path(s), and if so,
 * suppresses members on the edit path(s) from participating in the potential
 * contradiction.
 */
public class EditPathLosesStrategy extends ContradictionManagementStrategy {

    private static final long serialVersionUID = 1L;

    /**
     *
     * @return a description of this edit path loses conflict resolution
     * strategy
     */
    @Override
    public String getDescription() {
        return "<html>This resolution strategy implements resolution that"
                + "<ul><li>checks if conflicting members are present on the users edit path(s),</li>"
                + "<li>and if so, suppresses members on the edit path(s) from participating in the</li>"
                + "<li>potential contradiction.</ul>"
                + "</html>";
    }

    /**
     *
     * @return the display name of this edit path loses conflict resolution
     * strategy
     */
    @Override
    public String getDisplayName() {
        return "Suppress edit path versions from contradictions";
    }

    /**
     *
     * @param <T> the generic type of component versions
     * @param part1 the first part
     * @param part2 the second part
     * @return parts resolved according to this edit path losses strategy
     */
    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(T part1, T part2) {
        List<T> returnValues = new ArrayList(2);
        if (!ec.getEditPathsSet().contains(part1.getPathNid())) {
            returnValues.add(part1);
        }
        if (!ec.getEditPathsSet().contains(part2.getPathNid())) {
            returnValues.add(part2);
        }
        if (returnValues.size() == 0) {
            returnValues.add(part1);
            returnValues.add(part2);
        }
        return returnValues;
    }

    /**
     *
     * @param <T> the generic type of component version
     * @param versions the versions to resolve
     * @return the component versions resolved according to this edit path
     * losses strategy
     */
    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(List<T> versions) {
        List<T> returnValues = new ArrayList(2);
        for (T v : versions) {
            if (!ec.getEditPathsSet().contains(v.getPathNid())) {
                returnValues.add(v);
            }
        }
        if (returnValues.size() == 0) {
            returnValues.addAll(versions);
        }
        return returnValues;
    }
}
