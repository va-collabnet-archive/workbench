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
package org.ihtsdo.db.bdb;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.ConceptComponent.Version;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.helper.promote.TerminologyPromoterBI;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.ProcessStampDataBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.StampBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.binding.snomed.RefsetAux;

/**
 *
 *
 * @author aimeefurber
 */
public class BdbTermPromoter implements ProcessStampDataBI, ProcessUnfetchedConceptDataBI, TerminologyPromoterBI {

    ViewCoordinate sourceViewCoordinate;
    EditCoordinate sourceEditCoordinate;
    ViewCoordinate targetViewCoordinate;
    boolean writeBack;
    ConcurrentSkipListSet<Integer> newStamps = new ConcurrentSkipListSet<>();
    int targetPathNid;
    PositionBI targetPosition;
    NidBitSetBI promotionConceptNids;
    HashSet<Integer> nidsToSkip = new HashSet<>();

    public BdbTermPromoter(ViewCoordinate sourceViewCoordinate, EditCoordinate sourceEditCoordinate,
            ViewCoordinate targetViewCoordinate) {
        this.sourceViewCoordinate = sourceViewCoordinate.getViewCoordinateWithAllStatusValues();
        this.sourceEditCoordinate = sourceEditCoordinate;
        this.targetViewCoordinate = targetViewCoordinate;

    }

    @Override
    public boolean promote(NidBitSetBI promotionNids, boolean writeBack) throws IOException, Exception {
        this.writeBack = writeBack;

        nidsToSkip.add(RefsetAux.ADJ_REFEX.getLenient().getConceptNid());
        nidsToSkip.add(RefsetAux.COMMIT_REFEX.getLenient().getConceptNid());
        nidsToSkip.add(RefsetAux.WFHX_REFEX.getLenient().getConceptNid());

        promotionConceptNids = Bdb.getConceptDb().getEmptyIdSet();
        PositionBI[] positionArray = targetViewCoordinate.getPositionSet().getPositionArray();
        //assuming only one view position
        targetPosition = positionArray[0];

        targetPathNid = targetPosition.getPath().getConceptNid();

        Bdb.getSapDb().iterateStampDataInSequence(this);

        NidBitSetItrBI iterator = promotionNids.iterator();
        while (iterator.next()) {
            int nid = iterator.nid();
            promotionConceptNids.setMember(Bdb.getConceptNid(nid));
        }
        Bdb.getConceptDb().iterateConceptDataInParallel(this);
        
        boolean commit = BdbCommitManager.commit();
        return commit;
    }

    private void processConcept(ConceptChronicleBI conceptChronicle) throws IOException, ContradictionException, Exception {
        if (conceptChronicle.getPrimUuid().equals(UUID.fromString("9c682197-2518-4794-bbe5-5ec5fa8b6b10"))) {
            System.out.println("DEBUG");
        }
        Set<Integer> nidsToPromote = conceptChronicle.getAllNidsForStamps(newStamps);

        if (!nidsToPromote.isEmpty()) {
            //promote entire concept
                for (int nid : nidsToPromote) {
                    //promote concept attribute
                    if (nid == conceptChronicle.getConceptNid()) {
                        Collection<? extends ConceptAttributeVersionBI> caVersions = conceptChronicle.getConceptAttributes().getVersions();
                        for (ConceptAttributeVersionBI ca : caVersions) {
                            if (newStamps.contains(ca.getStampNid())) {
                                promoteConceptAttribute((ConceptAttributes.Version)ca);
                            }
                        }
                    } else { //promote component
                        ComponentBI c = Bdb.getComponent(nid);
                        if (c != null) { //wfhx records returning null, don't want anyway
                            ConceptComponent component = (ConceptComponent) c;
                            promoteComponent(component);
                        }
                    }
                }
            BdbCommitManager.addUncommittedNoChecks(conceptChronicle);
        }
    }

    private void promoteComponent(ConceptComponent component) {
        boolean okay = true;
        if (RefsetMember.class.isAssignableFrom(component.getClass())) {
            RefsetMember member = (RefsetMember) component;
            if (nidsToSkip.contains(member.refsetNid)) {
                okay = false;
            }
        }
        //don't promote commit/adj records
        if (okay) {
            List<Version> versions = component.getVersions(sourceViewCoordinate);
            for (Version version : versions) {
                version.makeAnalog(version.getStatusNid(),
                        Long.MAX_VALUE,
                        sourceEditCoordinate.getAuthorNid(),
                        version.getModuleNid(),
                        targetPathNid);
                if (writeBack) {
                    version.makeAnalog(version.getStatusNid(),
                            Long.MAX_VALUE,
                            sourceEditCoordinate.getAuthorNid(),
                            version.getModuleNid(),
                            version.getPathNid());
                }
            }
        }
    }
    
    private void promoteConceptAttribute(ConceptAttributes.Version version) {
        version.makeAnalog(version.getStatusNid(),
                Long.MAX_VALUE,
                sourceEditCoordinate.getAuthorNid(),
                version.getModuleNid(),
                targetPathNid);
        if (writeBack) {
            version.makeAnalog(version.getStatusNid(),
                    Long.MAX_VALUE,
                    sourceEditCoordinate.getAuthorNid(),
                    version.getModuleNid(),
                    version.getPathNid());
        }
    }

    private void processStamp(StampBI sap) {
        int pathNid = sap.getPathNid();
        long time = sap.getTime();

        //is new stamp nid and needs to be promoted
        if (!targetPosition.isSubsequentOrEqualTo(time, pathNid)) {
            newStamps.add(sap.getStampNid());
        }

    }

    @Override
    public void processStampData(StampBI stamp) throws Exception {
        processStamp(stamp);
    }

    @Override
    public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI fetcher) throws Exception {
        processConcept(fetcher.fetch());
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return promotionConceptNids;
    }

    @Override
    public boolean continueWork() {
        return true;
    }
}
