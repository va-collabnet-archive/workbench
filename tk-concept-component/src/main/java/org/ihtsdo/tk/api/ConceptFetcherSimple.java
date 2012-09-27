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
package org.ihtsdo.tk.api;

import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

// TODO: Auto-generated Javadoc
/**
 * The Class ConceptFetcherSimple.
 *
 * @author kec
 */
public class ConceptFetcherSimple implements ConceptFetcherBI {
    
    /** The cc. */
    ConceptChronicleBI cc;

    /**
     * Instantiates a new concept fetcher simple.
     *
     * @param cc the cc
     */
    public ConceptFetcherSimple(ConceptChronicleBI cc) {
        this.cc = cc;
    }
    
    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ConceptFetcherBI#fetch()
     */
    @Override
    public ConceptChronicleBI fetch() throws Exception {
        return cc;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ConceptFetcherBI#fetch(org.ihtsdo.tk.api.coordinate.ViewCoordinate)
     */
    @Override
    public ConceptVersionBI fetch(ViewCoordinate viewCoordinate) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ConceptFetcherBI#update(org.ihtsdo.tk.api.concept.ConceptChronicleBI)
     */
    @Override
    public void update(ConceptChronicleBI conceptChronicle) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
