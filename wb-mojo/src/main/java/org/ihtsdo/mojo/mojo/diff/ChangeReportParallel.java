/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.mojo.mojo.diff;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;

/**
 * 
 * @goal change-report-parallel
 * 
 * @phase generate-resources
 * 
 * @requiresDependencyResolution compile
 */
public class ChangeReportParallel extends ChangeReportBase {

    @Override
    protected void processConcepts() throws Exception {
        Ts.get().iterateConceptDataInSequence(new Processor());
    }

    private class Processor implements ProcessUnfetchedConceptDataBI {

        AtomicInteger i = new AtomicInteger();
        NidBitSetBI allConcepts;
        long beg;

        public Processor() throws IOException {
            allConcepts = Ts.get().getAllConceptNids();
            beg = System.currentTimeMillis();
        }

        @Override
        public void processUnfetchedConceptData(int cNid,
                ConceptFetcherBI fetcher) throws Exception {
            I_GetConceptData c = (I_GetConceptData) fetcher.fetch();
            processConcept(c, i.incrementAndGet(), beg);
        }

        @Override
        public NidBitSetBI getNidSet() throws IOException {
            return allConcepts;
        }

        @Override
        public boolean continueWork() {
            return true;
        }
    }
}
