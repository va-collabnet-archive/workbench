/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.api.search;

import java.io.IOException;
import org.ihtsdo.tk.Ts;

// TODO: Auto-generated Javadoc
/**
 * The Class ScoredComponentReference.
 *
 * @author kec
 */
public class ScoredComponentReference {

    /** The component nid. */
    private int componentNid;
    
    /** The score. */
    private float score;

    /**
     * Instantiates a new scored component reference.
     *
     * @param componentNid the component nid
     * @param searchScore the search score
     */
    public ScoredComponentReference(int componentNid, float searchScore) {
        this.componentNid = componentNid;
        this.score = searchScore;
    }

    /**
     * Gets the component nid.
     *
     * @return the component nid
     */
    public int getComponentNid() {
        return componentNid;
    }
    
    /**
     * Gets the concept nid.
     *
     * @return the concept nid
     */
    public int getConceptNid() {
        try {
            return Ts.get().getConceptNidForNid(componentNid);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the score.
     *
     * @return the score
     */
    public float getScore() {
        return score;
    }
}
