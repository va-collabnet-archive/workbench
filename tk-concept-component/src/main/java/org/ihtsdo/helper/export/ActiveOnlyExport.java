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
 * The Class ActiveOnlyExport exports an eConcept file only containing active
 * content.
 *
 */
public class ActiveOnlyExport implements ProcessUnfetchedConceptDataBI {

    private Map<UUID, UUID> conversionMap;
    private NidBitSetBI exclusionSet;
    private NidBitSetBI nidSet;
    private DataOutputStream out;
    private ViewCoordinate conceptVc;
    private ViewCoordinate descVc;
    private ViewCoordinate relVc;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new active only export.
     *
     * @param conceptViewCoordinate the view coordinate specifying which
     * concepts are active or inactive
     * @param descriptionViewCoordinate the view coordinate specifying which
     * descriptions are active or inactive
     * @param relVc the view coordinate specifying which relationships are
     * active or inactive
     * @param exclusionSet the set of nids representing components to exclude
     * from the export
     * @param out the output stream writing the eConcept file
     * @param conversionMap the uuid-uuid map for converting uuid in the
     * exported eConcepts
     * @throws IOException signals that an I/O exception has occurred
     */
    public ActiveOnlyExport(ViewCoordinate conceptViewCoordinate, ViewCoordinate descriptionViewCoordinate, ViewCoordinate relVc,
            NidBitSetBI exclusionSet, DataOutputStream out,
            Map<UUID, UUID> conversionMap)
            throws IOException {
        this.conceptVc = conceptViewCoordinate;
        this.descVc = descriptionViewCoordinate;
        this.relVc = relVc;
        this.exclusionSet = exclusionSet;
        this.nidSet = Ts.get().getAllConceptNids();
        this.out = out;
        this.conversionMap = conversionMap;
    }

    //~--- methods -------------------------------------------------------------
    /**
     *
     * @return <code>true</code>
     */
    @Override
    public boolean continueWork() {
        return true;
    }

    /**
     * Creates a new eConcept based on the specified view coordinates, excluded
     * components, and uuids to convert. Writes the eConcept to the specified output stream
     *
     * @param cNid the nid associated with the concept to process
     * @param fetcher the fetcher which will get the specified concept from the
     * database
     * @throws Exception indicates an exception has occurred
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
    /**
     * 
     * @return the set of nids representing the concepts to process
     * @throws IOException signals that an I/O exception has occurred
     */
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return nidSet;
    }
}
