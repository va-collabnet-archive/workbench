/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.helper.refex;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.ConceptInactivationType;
import org.ihtsdo.tk.binding.snomed.HistoricalRelType;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * Maintains refexs needed for RF2 release. This should only run on the delta files.
 *
 */
public class Rf2RefexComputer implements ProcessUnfetchedConceptDataBI {

    private EditCoordinate editCoordinate;
    private ViewCoordinate viewCoordinate;
    private ViewCoordinate vcAllStatus;
    String tempKey;
    private File output;
    private Set<Integer> stampNids;
    private HashSet<Integer> historicalRelTypes;
    private int descInactiveRefexNid;
    private int conceptInactiveRefexNid;
    private ConcurrentSkipListSet<Integer> newStamps = new ConcurrentSkipListSet<>();
    private String sourceTime;
    private String targetTime;

    public Rf2RefexComputer(ViewCoordinate viewCoordinate, EditCoordinate editCoordinate,
            File output, Set<Integer> stampNids, String sourceTime, String targetTime) {
        this.viewCoordinate = viewCoordinate;
        this.editCoordinate = editCoordinate;
        this.output = output;
        this.stampNids = stampNids;
        this.sourceTime = sourceTime;
        this.targetTime = targetTime;
    }

    public void setup() throws ValidationException, IOException {
        vcAllStatus = viewCoordinate.getViewCoordinateWithAllStatusValues();
        historicalRelTypes = new HashSet<Integer>();
        historicalRelTypes.add(HistoricalRelType.MAY_BE_A.getLenient().getConceptNid());
        historicalRelTypes.add(HistoricalRelType.MOVED_TO.getLenient().getConceptNid());
        historicalRelTypes.add(HistoricalRelType.REPLACED_BY.getLenient().getConceptNid());
        historicalRelTypes.add(HistoricalRelType.SAME_AS.getLenient().getConceptNid());
        historicalRelTypes.add(HistoricalRelType.WAS_A.getLenient().getConceptNid());
        descInactiveRefexNid = SnomedMetadataRf2.DESC_INACTIVE_REFSET.getLenient().getConceptNid();
        conceptInactiveRefexNid = SnomedMetadataRf2.CONCEPT_INACTIVE_REFSET.getLenient().getConceptNid();
        tempKey = UUID.randomUUID().toString();
        String changsetFileName = Ts.get().getConceptForNid(editCoordinate.getAuthorNid()).toUserString() + "#rf2Refset#"
                + UUID.randomUUID().toString() + ".eccs";
        ChangeSetGeneratorBI generator
                = Ts.get().createDtoChangeSetGenerator(new File(output, changsetFileName), new File(output,
                                "#0#" + changsetFileName), ChangeSetGenerationPolicy.MUTABLE_ONLY);
        Ts.get().addChangeSetGenerator(tempKey, generator);

    }

    public void addModuleDependencyMember() throws IOException, InvalidCAB, ContradictionException, ParseException {
        Date sourceDate = new Date(TimeHelper.getTimeFromString(sourceTime,
                TimeHelper.getAltFileDateFormat()));
        Date targetDate = new Date(TimeHelper.getTimeFromString(targetTime,
                TimeHelper.getAltFileDateFormat()));
        sourceTime = TimeHelper.getShortFileDateFormat().format(sourceDate);
        targetTime = TimeHelper.getShortFileDateFormat().format(targetDate);
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.STR_STR,
                Snomed.CORE_MODULE.getLenient().getConceptNid(),
                Snomed.MODULE_DEPENDENCY.getLenient().getConceptNid());
        memberBp.put(RefexCAB.RefexProperty.STRING1, sourceTime);
        memberBp.put(RefexCAB.RefexProperty.STRING2, targetTime);
        memberBp.setMemberContentUuid();
        TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(editCoordinate, viewCoordinate);
        RefexChronicleBI<?> newMember = builder.constructIfNotCurrent(memberBp);
        Ts.get().addUncommitted(Ts.get().getConcept(Snomed.MODULE_DEPENDENCY.getLenient().getConceptNid()));
        Ts.get().commit();
        for (int stampNid : newMember.getAllStampNids()) {
            newStamps.add(stampNid);
        }
    }

    public void cleanup() throws IOException {
        Ts.get().removeChangeSetGenerator(tempKey);
    }

    @Override
    public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
        process(conceptFetcher.fetch());
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        //TODO may want to filter
        return Ts.get().getAllConceptNids();
    }

    @Override
    public boolean continueWork() {
        return true;
    }

    /*
     * 
     * Association: if concept has historical relationship, description retired as inappropriate. If reactivated need to inactivate membership.
     * "same as" refset
     *"was a" refset
     *"refers to" refset
     *"replaced by" refset
     * 
     *AttributeValue: previously released retried description or concept. If reactivated need to inactivate membership.
     *description inactivation refset
     *concept inactivation refset
     */
    private void process(ConceptChronicleBI conceptChronicle) throws ContradictionException, IOException, InvalidCAB, ParseException {
        TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(editCoordinate, viewCoordinate);
        ConceptVersionBI cvAllStatus = conceptChronicle.getVersion(vcAllStatus);
        Collection<? extends RelationshipVersionBI> latestRels = cvAllStatus.getRelationshipsOutgoingActive();
        Collection<? extends DescriptionVersionBI> latestDescs = cvAllStatus.getDescriptionsActive();
        boolean changed = false;
        if (conceptChronicle.getPrimUuid().equals(UUID.fromString("9465bd0c-bca9-360b-a36b-0cf5a236c14c"))) {
            System.out.println("DEBUG -- Inactive concept FLPR");
        }
        for (RelationshipVersionBI historicalRel : latestRels) {
                if (stampNids.contains(historicalRel.getStampNid())) {
                    if (historicalRelTypes.contains(historicalRel.getTypeNid())) {
                        int refexNid = getAssociationRefexForTypeNid(historicalRel.getTypeNid());
                        if (historicalRel.isActive(viewCoordinate)) {
                            RefexCAB refexBp = new RefexCAB(TK_REFEX_TYPE.CID,
                                    historicalRel.getSourceNid(),
                                    refexNid);
                            refexBp.put(RefexCAB.RefexProperty.CNID1, historicalRel.getTargetNid());
                            RefexChronicleBI<?> member = builder.constructIfNotCurrent(refexBp);
                            conceptChronicle.addAnnotation(member);
                            for (int stampNid : member.getAllStampNids()) {
                                newStamps.add(stampNid);
                            }
                            //retire any active relationships
                            Collection<? extends RelationshipVersionBI> activeRels = conceptChronicle.getVersion(viewCoordinate).getRelationshipsOutgoingActive();
                            for (RelationshipVersionBI rv : activeRels) {
                                if (rv.isStated()) {
                                    RelationshipCAB relBp = rv.makeBlueprint(viewCoordinate);
                                    relBp.setRetired();
                                    relBp.setComponentUuidNoRecompute(rv.getPrimUuid());
                                    RelationshipChronicleBI updatedRel = builder.construct(relBp);
                                    newStamps.addAll(updatedRel.getAllStampNids());
                                }
                                if(rv.isInferred()){ //classifier runs before this, so need to retire inferred rel also
                                    AnalogBI updatedRel = rv.makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getConceptNid(),
                                            Long.MAX_VALUE,
                                            editCoordinate.getAuthorNid(),
                                            editCoordinate.getModuleNid(),
                                            editCoordinate.getEditPaths()[0]);
                                    newStamps.addAll(rv.getAllStampNids());
                                }
                            }
                            //fix relationships for RF2, historical Is-a should be inactive
                            RelationshipCAB relBp = historicalRel.makeBlueprint(viewCoordinate);
                            relBp.setRetired();
                            relBp.setComponentUuidNoRecompute(historicalRel.getPrimUuid());
                        } else {
                            Collection<? extends RefexVersionBI<?>> members = cvAllStatus.getAnnotationMembersActive(viewCoordinate, refexNid);
                            if (members.isEmpty()) { //historical rel was added in international edition, but retired on extension
                                RefexCAB refexBp = new RefexCAB(TK_REFEX_TYPE.CID,
                                        historicalRel.getSourceNid(),
                                        refexNid);
                                refexBp.put(RefexCAB.RefexProperty.CNID1, historicalRel.getTargetNid());
                                refexBp.setRetired();
                                RefexChronicleBI<?> member = builder.constructIfNotCurrent(refexBp);
                                conceptChronicle.addAnnotation(member);
                                for (int stampNid : member.getAllStampNids()) {
                                    newStamps.add(stampNid);
                                }
                            } else {
                                for (RefexVersionBI member : members) {
                                    RefexCAB refexBp = member.makeBlueprint(viewCoordinate);
                                    refexBp.setRetired();
                                    refexBp.setMemberUuid(member.getPrimUuid());
                                    builder = Ts.get().getTerminologyBuilder(editCoordinate, viewCoordinate);
                                    RefexChronicleBI<?> retiredMember = builder.constructIfNotCurrent(refexBp);
                                    conceptChronicle.addAnnotation(retiredMember);
                                    for (int stampNid : retiredMember.getAllStampNids()) {
                                        newStamps.add(stampNid);
                                    }
                                }
                            }
                        }
                        //need to add the descriptions to the Description inactivation indicator 
                        //reference set with a value of Concept non-current (TIG 5.5.3.1)
                        for (DescriptionVersionBI desc : conceptChronicle.getVersion(viewCoordinate).getDescriptionsActive()) {
                            boolean hasSctId = false;
                            if (desc.getAdditionalIds() != null) {
                                for (IdBI id : desc.getAdditionalIds()) {
                                    if (id.getAuthorityNid() == TermAux.SCT_ID_AUTHORITY.getLenient().getConceptNid()) {
                                        hasSctId = true;
                                    }
                                }
                            }

                            if (hasSctId) {
                                RefexCAB refexBp = new RefexCAB(TK_REFEX_TYPE.CID,
                                        desc.getNid(),
                                        descInactiveRefexNid);
                                refexBp.put(RefexCAB.RefexProperty.CNID1,
                                        SnomedMetadataRf2.CONCEPT_NON_CURRENT_RF2.getLenient().getNid());
                                RefexChronicleBI<?> newMember = builder.construct(refexBp);
                                for (int stampNid : newMember.getAllStampNids()) {
                                    newStamps.add(stampNid);
                                }
                            }
                        }

                        Ts.get().addUncommitted(Ts.get().getConcept(descInactiveRefexNid));
                        Ts.get().commit(Ts.get().getConcept(descInactiveRefexNid));

                        Ts.get().addUncommitted(Ts.get().getConcept(refexNid));
                        Ts.get().commit(Ts.get().getConcept(refexNid));
                        changed = true;
                    } else if (historicalRel.getTargetNid() == ConceptInactivationType.REASON_NOT_STATED_CONCEPT.getLenient().getConceptNid()) {
                        //need to add to description inactivation refset? what about historical association refset?
                        //only need to retire any active relationships
                        Collection<? extends RelationshipVersionBI> activeRels = conceptChronicle.getVersion(viewCoordinate).getRelationshipsOutgoingActive();
                        for (RelationshipVersionBI rv : activeRels) {
                            if (rv.isStated()) {
                                RelationshipCAB relBp = rv.makeBlueprint(viewCoordinate);
                                relBp.setRetired();
                                relBp.setComponentUuidNoRecompute(rv.getPrimUuid());
                                RelationshipChronicleBI updatedRel = builder.construct(relBp);
                                newStamps.addAll(updatedRel.getAllStampNids());
                            }
                            if (rv.isInferred()) { //classifier runs before this, so need to retire inferred rel also
                                AnalogBI updatedRel = rv.makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getConceptNid(),
                                        Long.MAX_VALUE,
                                        editCoordinate.getAuthorNid(),
                                        editCoordinate.getModuleNid(),
                                        editCoordinate.getEditPaths()[0]);
                                newStamps.addAll(rv.getAllStampNids());
                            }
                        }
                        //need to add the descriptions to the Description inactivation indicator 
                        //reference set with a value of Concept non-current (TIG 5.5.3.1)
                        for (DescriptionVersionBI desc : conceptChronicle.getVersion(viewCoordinate).getDescriptionsActive()) {
                            boolean hasSctId = false;
                            if (desc.getAdditionalIds() != null) {
                                for (IdBI id : desc.getAdditionalIds()) {
                                    if (id.getAuthorityNid() == TermAux.SCT_ID_AUTHORITY.getLenient().getConceptNid()) {
                                        hasSctId = true;
                                    }
                                }
                            }

                            if (hasSctId) {
                                RefexCAB refexBp = new RefexCAB(TK_REFEX_TYPE.CID,
                                        desc.getNid(),
                                        descInactiveRefexNid);
                                refexBp.put(RefexCAB.RefexProperty.CNID1,
                                        SnomedMetadataRf2.CONCEPT_NON_CURRENT_RF2.getLenient().getNid());
                                RefexChronicleBI<?> newMember = builder.construct(refexBp);
                                for (int stampNid : newMember.getAllStampNids()) {
                                    newStamps.add(stampNid);
                                }
                            }
                        }
                        changed = true;
                    }
                }
            }
            ConceptVersionBI cv = conceptChronicle.getVersion(viewCoordinate);
            if (cv.getConceptAttributes() != null) {
                ComponentVersionBI cav = cv.getConceptAttributes().getVersion(vcAllStatus);
                if (!cv.isActive() && cav != null) {
                    if (stampNids.contains(cav.getStampNid())) {
                        if (getValueForRetirement(cv.getNid()) != null) {
                            RefexCAB refexBp = new RefexCAB(TK_REFEX_TYPE.CID,
                                    cv.getNid(),
                                    conceptInactiveRefexNid);
                            refexBp.put(RefexCAB.RefexProperty.CNID1, getValueForRetirement(cv.getNid()));
                            RefexChronicleBI<?> member = builder.constructIfNotCurrent(refexBp);
                            conceptChronicle.addAnnotation(member);
                            for (int stampNid : member.getAllStampNids()) {
                                newStamps.add(stampNid);
                            }
                            Ts.get().addUncommitted(Ts.get().getConcept(conceptInactiveRefexNid));
                            Ts.get().commit(Ts.get().getConcept(conceptInactiveRefexNid));
                            changed = true;
                        }
                    }

                }
            }
            for (DescriptionVersionBI desc : latestDescs) {
                if (stampNids.contains(desc.getStampNid())) {
                    if (desc.hasAnnotationMemberActive(viewCoordinate, SnomedMetadataRf2.REFERS_TO_REFSET_RF2.getLenient().getConceptNid())) {
                        //desc is added to refex during retire automation, only need to inactivate here if description has been reactivated
                        if (desc.isActive(viewCoordinate)) {
                            Collection<? extends RefexVersionBI<?>> members = desc.getAnnotationMembersActive(
                                    viewCoordinate,
                                    SnomedMetadataRf2.REFERS_TO_REFSET_RF2.getLenient().getConceptNid());
                            for (RefexVersionBI member : members) {
                                RefexCAB refexBp = member.makeBlueprint(viewCoordinate);
                                refexBp.setRetired();
                                refexBp.setMemberUuid(member.getPrimUuid());
                                RefexChronicleBI<?> newMember = builder.construct(refexBp);
                                for (int stampNid : newMember.getAllStampNids()) {
                                    newStamps.add(stampNid);
                                }
                            }
                        } else {
                            RefexCAB refexBp = new RefexCAB(TK_REFEX_TYPE.CID,
                                    desc.getNid(),
                                    descInactiveRefexNid);
                            refexBp.put(RefexCAB.RefexProperty.CNID1,
                                    SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getLenient().getNid());
                            RefexChronicleBI<?> newMember = builder.construct(refexBp);
                            for (int stampNid : newMember.getAllStampNids()) {
                                newStamps.add(stampNid);
                            }
                        }
                        Ts.get().addUncommitted(Ts.get().getConcept(descInactiveRefexNid));
                        Ts.get().commit(Ts.get().getConcept(descInactiveRefexNid));
                        changed = true;
                    }
                }
            }
            //TODO assuming that refexes are annotations
            if (changed) {
                Ts.get().addUncommitted(conceptChronicle);
                Ts.get().commit(conceptChronicle);
            }
    }

    private Integer getValueForRetirement(int conceptNid) throws ValidationException, IOException, ContradictionException {
        if (Ts.get().isChildOf(conceptNid,
                ConceptInactivationType.AMBIGUOUS_CONCEPT.getLenient().getNid(), viewCoordinate)) {
            return SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getLenient().getNid();
        } else if (Ts.get().isChildOf(conceptNid,
                ConceptInactivationType.DUPLICATE_CONCEPT.getLenient().getNid(), viewCoordinate)) {
            return SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getLenient().getNid();
        } else if (Ts.get().isChildOf(conceptNid,
                ConceptInactivationType.ERRONEOUS_CONCEPT.getLenient().getNid(), viewCoordinate)) {
            return SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getLenient().getNid();
        } else if (Ts.get().isChildOf(conceptNid,
                ConceptInactivationType.LIMITED_STATUS_CONCEPT.getLenient().getNid(), viewCoordinate)) {
            return SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getNid();
        } else if (Ts.get().isChildOf(conceptNid,
                ConceptInactivationType.MOVED_ELSEWHERE.getLenient().getNid(), viewCoordinate)) {
            SnomedMetadataRf2.COMPONENT_MOVED_ELSEWHERE_RF2.getLenient().getPrimUuid();
        } else if (Ts.get().isChildOf(conceptNid,
                ConceptInactivationType.OUTDATED_CONCEPT.getLenient().getNid(), viewCoordinate)) {
            return SnomedMetadataRf2.OUTDATED_COMPONENT_RF2.getLenient().getNid();
        } else if (Ts.get().isChildOf(conceptNid,
                ConceptInactivationType.REASON_NOT_STATED_CONCEPT.getLenient().getNid(), viewCoordinate)) {
            return null;
        }
        return null;
    }

    private Integer getAssociationRefexForTypeNid(int typeNid) throws ValidationException, IOException {
        if (typeNid == HistoricalRelType.MAY_BE_A.getLenient().getNid()) {
            return SnomedMetadataRf2.POSSIBLY_EQUIVALENT_TO_REFSET_RF2.getLenient().getNid();
        } else if (typeNid == HistoricalRelType.SAME_AS.getLenient().getNid()) {
            return SnomedMetadataRf2.SAME_AS_REFSET_RF2.getLenient().getNid();
        } else if (typeNid == HistoricalRelType.WAS_A.getLenient().getNid()) {
            return SnomedMetadataRf2.WAS_A_REFSET_RF2.getLenient().getNid();
        } else if (typeNid == HistoricalRelType.REPLACED_BY.getLenient().getNid()) {
            return SnomedMetadataRf2.REPLACED_BY_REFSET_RF2.getLenient().getNid();
        }
        return null;
    }

    public Set<Integer> getNewStampNids() {
        return newStamps;
    }
}
