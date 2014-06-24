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

import java.io.IOException;

/**
 * The Interface ProcessUnfetchedConceptDataBI can be implemented by classes
 * wishing to process a set of nids in a certain way. This set can be processes
 * in parallel making much more efficient. Generally used when need to processes
 * all of the nids in the database. The set of nids is processes according to
 * the implementation of the processUnfetchedConceptData method.
 * 
 * @see TerminologyStoreDI#iterateConceptDataInParallel(org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI)
 * @see TerminologyStoreDI#iterateConceptDataInSequence(org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI) 
 *
 */
public interface ProcessUnfetchedConceptDataBI extends ContinuationTrackerBI {

    /**
     * Process unfetched concept data.
     *
     * @param conceptNid the nid of the concept to process
     * @param conceptFetcher the fetcher for getting the concept version associated
     * with the <code>cNid</code> from the database
     * @throws Exception indicates an exception has occurred
     */
    void processUnfetchedConceptData(int conceptNid,
            ConceptFetcherBI conceptFetcher) throws Exception;

    /**
     * Gets the set of nids to process.
     *
     * @return the nid set to process
     * @throws IOException signals that an I/O exception has occurred
     */
    NidBitSetBI getNidSet() throws IOException;
}
