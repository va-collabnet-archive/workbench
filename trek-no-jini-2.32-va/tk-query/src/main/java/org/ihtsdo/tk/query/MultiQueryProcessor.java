/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.query;

import java.io.IOException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;

/**
 * Processes one or more <code>Query</code> objects using the same parallel concept iterator.
 * @author aimeefurber
 */
public class MultiQueryProcessor implements ProcessUnfetchedConceptDataBI {
    RefsetComputer[] queries;
    NidBitSetBI possibleNids;
    

    /**
     * Creates a new <code>MultiQueryProcessor</code> based on the given <code>queries</code>
     * @param queries one or more query to be processed
     * @throws IOException
     */
    public MultiQueryProcessor(RefsetComputer... queries) throws IOException {
        this.queries = queries;
        possibleNids = Ts.get().getEmptyNidSet();
        for(RefsetComputer query : queries){
            possibleNids.or(query.getNidSet());
        }
    }

    @Override
    public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
        for(RefsetComputer query : queries){
            query.processUnfetchedConceptData(conceptNid, conceptFetcher);
        }
        
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return possibleNids;
    }

    @Override
    public boolean continueWork() {
        return true;
    }
    
}
