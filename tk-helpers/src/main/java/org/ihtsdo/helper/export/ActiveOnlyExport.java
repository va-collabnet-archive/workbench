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
package org.ihtsdo.helper.export;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.dto.concept.TkConcept;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataOutputStream;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

/**
 *
 * @author kec
 */
public class ActiveOnlyExport implements ProcessUnfetchedConceptDataBI {

    Map<UUID, UUID> conversionMap;
    NidBitSetBI exclusionSet;
    NidBitSetBI nidSet;
    DataOutputStream out;
    ViewCoordinate conceptVc;
    ViewCoordinate descVc;
    ViewCoordinate relVc;

    //~--- constructors --------------------------------------------------------
    public ActiveOnlyExport(ViewCoordinate conceptVc, ViewCoordinate descVc, ViewCoordinate relVc, 
            NidBitSetBI exclusionSet, DataOutputStream out,
            Map<UUID, UUID> conversionMap)
            throws IOException {
        this.conceptVc = conceptVc;
        this.descVc = descVc;
        this.relVc = relVc;
        this.exclusionSet = exclusionSet;
        this.nidSet = Ts.get().getAllConceptNids();
         this.out = out;
        this.conversionMap = conversionMap;
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public boolean continueWork() {
        return true;
    }

    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
        if (!exclusionSet.isMember(cNid)) {
            ConceptVersionBI conceptVersion = fetcher.fetch(conceptVc);

            if ((conceptVersion.getPrimUuid() != null) && conceptVersion.isActive()) {
                TkConcept tkc = new TkConcept(conceptVersion, exclusionSet, conversionMap, 0, true, 
                        conceptVc, descVc, relVc);
                tkc.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return nidSet;
    }
}
