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

// TODO: Auto-generated Javadoc
/**
 * The Class ActiveOnlyExport.
 *
 * @author kec
 */
public class ActiveOnlyExport implements ProcessUnfetchedConceptDataBI {

    /** The conversion map. */
    Map<UUID, UUID> conversionMap;
    
    /** The exclusion set. */
    NidBitSetBI exclusionSet;
    
    /** The nid set. */
    NidBitSetBI nidSet;
    
    /** The out. */
    DataOutputStream out;
    
    /** The concept vc. */
    ViewCoordinate conceptVc;
    
    /** The desc vc. */
    ViewCoordinate descVc;
    
    /** The rel vc. */
    ViewCoordinate relVc;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new active only export.
     *
     * @param conceptVc the concept vc
     * @param descVc the desc vc
     * @param relVc the rel vc
     * @param exclusionSet the exclusion set
     * @param out the out
     * @param conversionMap the conversion map
     * @throws IOException signals that an I/O exception has occurred.
     */
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
    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ContinuationTrackerBI#continueWork()
     */
    @Override
    public boolean continueWork() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#processUnfetchedConceptData(int, org.ihtsdo.tk.api.ConceptFetcherBI)
     */
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
    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#getNidSet()
     */
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return nidSet;
    }
}
