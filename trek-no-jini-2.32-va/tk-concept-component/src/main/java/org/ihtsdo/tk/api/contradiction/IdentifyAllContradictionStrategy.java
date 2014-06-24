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
/**
 *
 */
package org.ihtsdo.tk.api.contradiction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.ihtsdo.tk.api.ComponentVersionBI;


/**
 * Implements the original ACE conflict resolution strategy. This is also used
 * as the default value. <p> Essentially this considers conflict to exist when
 * there is more than one state for any given entity on the user's configured
 * view paths. <p> Conflict resolution is not performed, so the result of
 * resolution is the same as the passed parameter.
 *
 *
 */
public class IdentifyAllContradictionStrategy extends ContradictionManagementStrategy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *
     * @return a description of this identify all contradiction strategy
     */
    @Override
    public String getDescription() {
        return "<html>This resolution strategy has two main characteristics"
                + "<ul><li>contradiction is considered to exist when there is more than one state for an entity on the user's configured view paths</li>"
                + "<li>resolution is not performed, therefore the content displayed is not filtered or altered</li></ul>"
                + "This strategy is useful for expert users or for independant authoring.</html>";
    }

    /**
     *
     * @return a display name of this identify all contradiction strategy
     */
    @Override
    public String getDisplayName() {
        return "Identify all conflicts";
    }

    /**
     *
     * @param <T> the generic type of component version
     * @param versions the versions to resolve
     * @return the component versions resolved according to this identify all
     * contradiction strategy
     */
    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(List<T> versions) {
        return versions;
    }

    /**
     * 
     * @param <T> the generic type of component versions
     * @param part1 the first part
     * @param part2 the second part
     * @return parts resolved according to this identify all contradiction strategy
     */
    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(T part1, T part2) {
        ArrayList<T> values = new ArrayList();
        values.add(part1);
        values.add(part2);
        return values;
    }
}
