/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.ihtsdo.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public class BatchActionProcessor implements ProcessUnfetchedConceptDataBI {

    // WORKBENCH INTERNALS
    EditCoordinate ec;
    ViewCoordinate vc;
    private boolean continueWorkB;

    public void setContinueWorkB(boolean continueWorkB) {
        this.continueWorkB = continueWorkB;
    }

    // ACTION and CONCEPTS
    List<BatchActionTask> batchActionList;
    NidBitSetBI conceptSet;

    /**
     * EditCoordinate and ViewCoordinate context is setup when this processor is instantiated.
     * 
     * @param concepts
     * @param actions
     * @param ec
     * @param vc
     * @throws IOException
     * @throws Exception
     */
    public BatchActionProcessor(Collection<ConceptChronicleBI> concepts, List<BatchActionTask> actions, EditCoordinate ec, ViewCoordinate vc)
            throws IOException, Exception {
        // SETUP WORKBENCH CONTEXT
        this.ec = ec;
        this.vc = vc;
        this.continueWorkB = true;
 
        // SETUP CONCEPTS SET FOR PROCESSING
        this.conceptSet = Ts.get().getEmptyNidSet();
        for (ConceptChronicleBI ccbi : concepts) {
            this.conceptSet.setMember(ccbi.getNid());
        }

        // SETUP LIST OF CONCEPTS TO BE APPLIED TO EACH CONCEPT
        this.batchActionList = new ArrayList<>();
        for (BatchActionTask bat : actions) {
            this.batchActionList.add(bat);
        }
    }

    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher)
            throws Exception {
        if (conceptSet.isMember(cNid)) {
            ConceptVersionBI c = fetcher.fetch(vc);

            boolean changed = false;
            for (BatchActionTask bat : batchActionList) {
                if (bat.execute(c, ec, vc)) {
                    changed = true;
                }
            }
            if (changed) {
                Ts.get().addUncommitted(c);
            }
        }
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return conceptSet;
    }

    @Override
    public boolean continueWork() {
        return this.continueWorkB;
    }
}
