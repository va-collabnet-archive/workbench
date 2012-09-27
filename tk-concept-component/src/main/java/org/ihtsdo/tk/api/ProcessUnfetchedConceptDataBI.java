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

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Interface ProcessUnfetchedConceptDataBI.
 *
 * @author kec
 */
public interface ProcessUnfetchedConceptDataBI extends ContinuationTrackerBI {

    /**
     * Process unfetched concept data.
     *
     * @param conceptNid the concept nid
     * @param conceptFetcher the concept fetcher
     * @throws Exception the exception
     */
    void processUnfetchedConceptData(int conceptNid,
            ConceptFetcherBI conceptFetcher) throws Exception;

    /**
     * Gets the native id set.
     *
     * @return the native id set
     * @throws IOException Signals that an I/O exception has occurred.
     */
    NidBitSetBI getNidSet() throws IOException;
    
}

