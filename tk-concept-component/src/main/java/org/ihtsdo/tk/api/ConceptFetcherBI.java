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
package org.ihtsdo.tk.api;

import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 * The Interface ConceptFetcherBI provides methods for fetching concepts from
 * the database. Use with a parallel or sequential iterator to retrieve the
 * concepts from the database for processing. Prior to calling fetch() the
 * concept may or may not be in memory. Only need to fetch a concept if more
 * than its identifier is needed.
 */
public interface ConceptFetcherBI {

    /**
     * Fetches a concept.
     *
     * @return the fetched concept
     * @throws Exception indicates an exception has occurred
     */
    ConceptChronicleBI fetch() throws Exception;

    /**
     * Fetches a concept version based on the given
     * <code>viewCoordinate</code>.
     *
     * @param viewCoordinate the view coordinate specifying which version to
     * return
     * @return the specified concept version
     * @throws Exception indicates an exception has occurred
     */
    ConceptVersionBI fetch(ViewCoordinate viewCoordinate) throws Exception;

    /**
     * Update the currently fetched concept. Must not be used for random
     * updates.
     *
     * @param conceptChronicle the concept chronicle to update
     * @throws Exception indicates an exception has occurred
     */
    void update(ConceptChronicleBI conceptChronicle) throws Exception;
}
