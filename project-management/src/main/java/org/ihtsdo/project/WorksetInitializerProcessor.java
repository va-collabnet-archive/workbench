/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.refset.PromotionRefset;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * The Class WorksetInitializerProcessor.
 */
public class WorksetInitializerProcessor implements ProcessUnfetchedConceptDataBI {

    /**
     * The members nid set.
     */
    NidBitSetBI membersNidSet;
    /**
     * The vc.
     */
    ViewCoordinate vc;
    /**
     * The prom ref.
     */
    PromotionRefset promRef;
    /**
     * The active nid.
     */
    int activeNid;
    /**
     * The active uuid.
     */
    UUID activeUuid;
    /**
     * The inactive nid.
     */
    int inactiveNid;
    /**
     * The inactive uuid.
     */
    UUID inactiveUuid;
    /**
     * The updater.
     */
    ActivityUpdater updater;
    /**
     * The tc.
     */
    TerminologyBuilderBI tc;
    /**
     * The work set nid.
     */
    int workSetNid;
    /**
     * The work set prom nid.
     */
    int workSetPromNid;
    /**
     * The excluded concepts.
     */
    NidBitSetBI excludedConcepts;
    /**
     * The other worksets.
     */
    NidBitSetBI otherWorksetsNids;
    /**
     * The current members concepts.
     */
    NidBitSetBI currentMembersConcepts;
    /**
     * The included concepts.
     */
    NidBitSetBI includedConcepts;
    /**
     * The included counter.
     */
    int includedCounter;
    /**
     * The excluded by policy counter.
     */
    int excludedByPolicyCounter;
    /**
     * The completed workflow nids.
     */
    NidBitSetBI completedWorkflowNids;

    /**
     * Instantiates a new workset initializer processor.
     *
     * @param workSet the work set
     * @param sourceRefset the source refset
     * @param config the config
     * @param updater the updater
     */
    public WorksetInitializerProcessor(WorkSet workSet, I_GetConceptData sourceRefset, I_ConfigAceFrame config, ActivityUpdater updater) {
        super();
        try {
            includedCounter = 0;
            excludedByPolicyCounter = 0;
            vc = config.getViewCoordinate();
            tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate());
            this.updater = updater;
            this.workSetNid = workSet.getId();
            this.workSetPromNid = workSet.getPromotionRefset(config).getRefsetId();
            promRef = workSet.getPromotionRefset(config);
            TerminologyStoreDI ts = Ts.get();
            I_TermFactory termFactory = Terms.get();

            excludedConcepts = ts.getEmptyNidSet();
            otherWorksetsNids = ts.getEmptyNidSet();
            currentMembersConcepts = ts.getEmptyNidSet();
            includedConcepts = ts.getEmptyNidSet();
            membersNidSet = ts.getEmptyNidSet();

            completedWorkflowNids = ts.getEmptyNidSet();

//            completedWorkflowNids.setMember(ArchitectonicAuxiliary.Concept.COMPLETED.localize().getNid());
//            completedWorkflowNids.setMember(ArchitectonicAuxiliary.Concept.APPROVED_FOR_PUBLICATION_EB_STATUS.localize().getNid());
//            completedWorkflowNids.setMember(ArchitectonicAuxiliary.Concept.APPROVED_FOR_PUBLICATION_FAST_TRACK_STATUS.localize().getNid());
//            completedWorkflowNids.setMember(ArchitectonicAuxiliary.Concept.APPROVED_FOR_PUBLICATION_STATUS.localize().getNid());
//            completedWorkflowNids.setMember(ArchitectonicAuxiliary.Concept.APPROVED_FOR_PUBLICATION_TPO_REV_STATUS.localize().getNid());
//            completedWorkflowNids.setMember(ArchitectonicAuxiliary.Concept.APPROVED_FOR_PUBLICATION_TPO_REV_STATUS.localize().getNid());
            if (Terms.get().getActiveAceFrameConfig().isVaProject()) {
            	completedWorkflowNids.setMember(Terms.get().uuidToNative(UUID.fromString("cdd1524b-f308-53f9-8361-0c2098458eb0")));
            } else {
            	completedWorkflowNids.setMember(Terms.get().uuidToNative(UUID.fromString("b59420f6-c6a1-5bab-a379-45f0642044c4")));
            }

            updater.setTaskMessage("Processing sourceMembers");
            ConceptChronicleBI sourceRefsetChronicle = (ConceptChronicleBI) sourceRefset;
            Collection<? extends RefexVersionBI<?>> sourceRefsetMembersList = sourceRefsetChronicle.getRefsetMembersActive(vc);
            for (RefexVersionBI<?> loopMember : sourceRefsetMembersList) {
                includedConcepts.setMember(loopMember.getReferencedComponentNid());
                membersNidSet.setMember(loopMember.getReferencedComponentNid());
            }

            updater.setTaskMessage("Processing oldMembers");
            ConceptChronicleBI worksetConceptChronicle = (ConceptChronicleBI) workSet.getConcept();
            Collection<? extends RefexVersionBI<?>> worksetMembersList = worksetConceptChronicle.getRefsetMembersActive(vc);
            for (RefexVersionBI<?> loopMember : worksetMembersList) {
                currentMembersConcepts.setMember(loopMember.getReferencedComponentNid());
                membersNidSet.setMember(loopMember.getReferencedComponentNid());
            }

            List<I_GetConceptData> exclusionRefsets = workSet.getExclusionRefsets();

            updater.setTaskMessage("Processing exclusions");
            for (I_GetConceptData loopRefset : exclusionRefsets) {
                ConceptChronicleBI loopRefsetConcept = (ConceptChronicleBI) loopRefset;
                Collection<? extends RefexVersionBI<?>> exclusionMembersList = loopRefset.getRefsetMembersActive(vc);
                for (RefexVersionBI<?> loopMember : exclusionMembersList) {
                    excludedConcepts.setMember(loopMember.getReferencedComponentNid());
                    membersNidSet.setMember(loopMember.getReferencedComponentNid());
                }
            }

            updater.setTaskMessage("Processing other worksets");
            List<WorkSet> otherWorkSets = TerminologyProjectDAO.getAllWorkSetsForProject(workSet.getProject(config), config);
            for (WorkSet loopWorkSet : otherWorkSets) {
                if (loopWorkSet.getId() != workSet.getId()) {
                    otherWorksetsNids.setMember(loopWorkSet.getPromotionRefset(config).getRefsetId());
                }
            }

            activeNid = SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid();
            inactiveNid = SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getNid();
            activeUuid = SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid();
            inactiveUuid = SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid();
            updater.setTaskMessage("Initializing WorkSet");
            updater.startCount(membersNidSet.cardinality());
        } catch (ValidationException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ContinuationTrackerBI#continueWork()
     */
    @Override
    public boolean continueWork() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#getNidSet()
     */
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return membersNidSet;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#processUnfetchedConceptData(int, org.ihtsdo.tk.api.ConceptFetcherBI)
     */
    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher)
            throws Exception {
        if (membersNidSet.isMember(cNid)) {
            ConceptVersionBI c = fetcher.fetch(vc);
            if (processConcept(c)) {
                //fetcher.update(c.getChronicle());
                Terms.get().addUncommittedNoChecks((I_GetConceptData) c.getChronicle());
            }
        }
    }

    /**
     * Process concept.
     *
     * @param concept the concept
     * @return true, if successful
     */
    private boolean processConcept(ConceptVersionBI concept) {
        boolean update = false;
        updater.incrementCount();

        boolean excludedByPolicy = false;
        try {
            for (RefexVersionBI<?> loopAnnot :
                    concept.getAnnotationsActive(vc)) {
                if (loopAnnot.getRefexNid() != workSetPromNid) {
                    if (otherWorksetsNids.isMember(loopAnnot.getRefexNid())) {
                        RefexNidVersionBI loopCidAnnot = (RefexNidVersionBI) loopAnnot;
                        if (!completedWorkflowNids.isMember(loopCidAnnot.getNid1())) {
                            excludedByPolicy = true;
                        }
                    }
                }
            }
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }

        if (currentMembersConcepts.isMember(concept.getNid())
                && includedConcepts.isMember(concept.getNid())
                && !excludedConcepts.isMember(concept.getNid()) && !excludedByPolicy) {
            // already a member, do nothing
            includedCounter++;
        } else if (!currentMembersConcepts.isMember(concept.getNid())
                && includedConcepts.isMember(concept.getNid())
                && !excludedConcepts.isMember(concept.getNid()) && !excludedByPolicy) {
            // should add
            includedCounter++;
            update = true;
            try {
                RefexCAB newSpec = new RefexCAB(
                        TK_REFEX_TYPE.CID,
                        concept.getNid(),
                        workSetNid);
                newSpec.put(RefexProperty.CNID1, activeNid);
                RefexChronicleBI<?> newRefex = tc.constructIfNotCurrent(newSpec);

                RefexCAB newSpecForProm = new RefexCAB(
                        TK_REFEX_TYPE.CID,
                        concept.getNid(),
                        promRef.getRefsetId());
                newSpecForProm.put(RefexProperty.CNID1, activeNid);
                RefexChronicleBI<?> newRefexForProm = tc.constructIfNotCurrent(newSpecForProm);
                concept.addAnnotation(newRefexForProm);
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        } else if (currentMembersConcepts.isMember(concept.getNid())
                && (!includedConcepts.isMember(concept.getNid())
                || excludedConcepts.isMember(concept.getNid()) || excludedByPolicy)) {
            // should remove
            excludedByPolicyCounter++;
            update = true;
            try {
                RefexCAB newSpec = new RefexCAB(
                        TK_REFEX_TYPE.CID,
                        concept.getNid(),
                        workSetNid);
                newSpec.put(RefexProperty.CNID1, inactiveNid);
                newSpec.setStatusUuid(inactiveUuid);
                RefexChronicleBI<?> newRefex = tc.constructIfNotCurrent(newSpec);

                RefexCAB newSpecForProm = new RefexCAB(
                        TK_REFEX_TYPE.CID,
                        concept.getNid(),
                        promRef.getRefsetId());
                newSpecForProm.put(RefexProperty.CNID1, inactiveNid);
                newSpecForProm.setStatusUuid(inactiveUuid);
                RefexChronicleBI<?> newRefexForProm = tc.constructIfNotCurrent(newSpecForProm);
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        } else {
            // do nothing
        }
        return update;

    }

    /**
     * @return the includedCounter
     */
    public int getIncludedCounter() {
        return includedCounter;
    }

    /**
     * @return the excludedByPolicyCounter
     */
    public int getExcludedByPolicyCounter() {
        return excludedByPolicyCounter;
    }
}
