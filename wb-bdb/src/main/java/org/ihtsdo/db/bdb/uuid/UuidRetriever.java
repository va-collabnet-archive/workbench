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
package org.ihtsdo.db.bdb.uuid;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessComponentChronicleBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;

/**
 *
 * @author kec
 */
public class UuidRetriever implements ProcessUnfetchedConceptDataBI, ProcessComponentChronicleBI {
    
    AtomicInteger components = new AtomicInteger();
    AtomicInteger uuids = new AtomicInteger();

    public int getComponentCount() {
        return components.get();
    }
    public int getUuidCount() {
        return uuids.get();
    }

    public NidBitSetBI getConceptNids() {
        return conceptNids;
    }

    NidBitSetBI conceptNids;

    public UuidRetriever(NidBitSetBI concepts) {
        this.conceptNids = concepts;
    }
    
    @Override
    public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
                    ((Concept)conceptFetcher.fetch()).processComponentChronicles(this);
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return conceptNids;
    }

    @Override
    public boolean continueWork() {
        return true;
    }

    @Override
    public void process(ComponentChronicleBI componentChronicle) throws Exception {
        components.incrementAndGet();
        List<UUID> results = componentChronicle.getUUIDs();
        uuids.addAndGet(results.size());
    }

    @Override
    public String toString() {
        return "UuidRetriever{" + "components=" + components + ", uuids=" + uuids + '}';
    }
    
    
}
