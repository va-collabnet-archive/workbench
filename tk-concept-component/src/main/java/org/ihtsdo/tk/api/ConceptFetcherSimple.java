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
 * The Class ConceptFetcherSimple provides methods for fetching the associated
 * concept.
 *
 */
public class ConceptFetcherSimple implements ConceptFetcherBI {

    ConceptChronicleBI cc;

    /**
     * Instantiates a new simple concept fetcher.
     *
     * @param conceptChronicle the concept to return
     */
    public ConceptFetcherSimple(ConceptChronicleBI conceptChronicle) {
        this.cc = conceptChronicle;
    }

    /**
     *
     * @return the fetched concept
     * @throws Exception indicates an exception has occurred
     */
    @Override
    public ConceptChronicleBI fetch() throws Exception {
        return cc;
    }

    /**
     * Not supported by this class.
     *
     * @param viewCoordinate
     * @return
     * @throws Exception indicates an exception has occurred
     * @throws UnsupportedOperationException
     */
    @Override
    public ConceptVersionBI fetch(ViewCoordinate viewCoordinate) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not supported by this class.
     *
     * @param conceptChronicle
     * @return
     * @throws Exception indicates an exception has occurred
     * @throws UnsupportedOperationException
     */
    @Override
    public void update(ConceptChronicleBI conceptChronicle) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
