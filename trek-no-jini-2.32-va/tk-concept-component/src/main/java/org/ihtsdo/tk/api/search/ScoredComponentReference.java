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
package org.ihtsdo.tk.api.search;

import java.io.IOException;
import org.ihtsdo.tk.Ts;

/**
 * The Class ScoredComponentReference represents a scored search result. The
 * found component nid is associated with a search score indicating the
 * relevancy of the result. This score is used to order the search results.
 *
 */
public class ScoredComponentReference {

    private int componentNid;
    private float score;

    /**
     * Instantiates a new scored component reference based on the given
     * <code>componentNid</code> and
     * <code>searchScore</code>.
     *
     * @param componentNid the nid representing the found component
     * @param searchScore the search score associated with the component
     */
    public ScoredComponentReference(int componentNid, float searchScore) {
        this.componentNid = componentNid;
        this.score = searchScore;
    }

    /**
     * Gets the nid of the found component.
     *
     * @return the component nid
     */
    public int getComponentNid() {
        return componentNid;
    }

    /**
     * Gets the nid of the enclosing concept for the found component.
     *
     * @return the enclosing concept nid
     */
    public int getConceptNid() {
        try {
            return Ts.get().getConceptNidForNid(componentNid);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the search score associated with the found component. This score is
     * used to order the search results based on relevancy.
     *
     * @return the search score
     */
    public float getScore() {
        return score;
    }
}
