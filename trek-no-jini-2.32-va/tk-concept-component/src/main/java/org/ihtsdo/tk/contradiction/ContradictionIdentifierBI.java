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
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

/**
 * The Interface ContradictionIdentifierBI provides methods for identifying
 * contradictions in the terminology.
 *
 */
public interface ContradictionIdentifierBI {

    /**
     * Determines if the specified <code>conceptChronicle</code> is in contradiction.
     *
     * @param conceptChronicle the concept in question
     * @return the type of contradiction found
     * @throws Exception indicates an exception has occurred
     */
    ContradictionResult isConceptInConflict(ConceptChronicleBI conceptChronicle) throws Exception;

    /**
     * Gets versions of the components found to be in contradiction.
     * 
     * @return the contradicting component versions
     */
    Collection<? extends ComponentVersionBI> getContradictingVersions();
}
