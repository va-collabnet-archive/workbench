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
package org.ihtsdo.project.refset;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.query.helper.RefsetHelper;

/**
 * The Class LanguageSpecRefset.
 */
public class LanguageSpecRefset extends WorkflowRefset {

    /**
     * Instantiates a new language spec refset.
     *
     * @param languageSpecRefset the language spec refset
     */
    public LanguageSpecRefset(I_GetConceptData languageSpecRefset) {
        super();
        this.refsetConcept = languageSpecRefset;
        this.refsetName = languageSpecRefset.toString();
        this.refsetId = languageSpecRefset.getConceptNid();
        termFactory = Terms.get();
    }

    /**
     * Creates the new language spec refset.
     *
     * @param name the name
     * @param parentId the parent id
     * @param membershipRefsetId the membership refset id
     * @param enumeratedOriginId the enumerated origin id
     * @param config the config
     * @return the language spec refset
     * @throws Exception the exception
     */
    public static LanguageSpecRefset createNewLanguageSpecRefset(String name, int parentId, int membershipRefsetId,
            int enumeratedOriginId, I_ConfigAceFrame config) throws Exception {
        LanguageSpecRefset newLanguageSpecRerset = null;
        I_GetConceptData newSpecConcept = null;
        I_TermFactory tf = Terms.get();
        try {
            I_GetConceptData parentConcept = tf.getConcept(parentId);
            I_GetConceptData isAConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
            I_GetConceptData membershipRefsetConcept = tf.getConcept(membershipRefsetId);
            I_GetConceptData commentsRelConcept = tf.getConcept(RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
            I_GetConceptData promotionRelConcept = tf.getConcept(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
            I_GetConceptData purposeRelConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
            I_GetConceptData typeRelConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids());
            I_GetConceptData atributeValueRefsetRel = tf.getConcept(RefsetAuxiliary.Concept.ATTRIBUTE_VALUE_REFSET_REL.getUids());
            I_GetConceptData memberTypeConcept = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_EXTENSION.getUids());
            I_GetConceptData purposeConcept = tf.getConcept(RefsetAuxiliary.Concept.DIALECT.getUids());
            I_GetConceptData defining = tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
            I_GetConceptData refinability = tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            I_GetConceptData current = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
            I_GetConceptData specifies = tf.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
            I_GetConceptData enumeratedOriginConcept = tf.getConcept(enumeratedOriginId);
            I_GetConceptData enumeratedRel = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_ORIGIN_REL.getUids());

            newSpecConcept = tf.newConcept(UUID.randomUUID(), false, config);
            tf.newDescription(UUID.randomUUID(), newSpecConcept, "en",
                    name, tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);
            tf.newDescription(UUID.randomUUID(), newSpecConcept, "en",
                    name, tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);
            tf.newRelationship(UUID.randomUUID(), newSpecConcept, isAConcept, parentConcept, defining, refinability,
                    current, 0, config);

            I_GetConceptData newCommentsConcept = tf.newConcept(UUID.randomUUID(), false, config);
            tf.newDescription(UUID.randomUUID(), newCommentsConcept, "en",
                    name + " - comments refset", tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);
            tf.newDescription(UUID.randomUUID(), newCommentsConcept, "en",
                    name + " - comments refset", tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);
            tf.newRelationship(UUID.randomUUID(), newCommentsConcept, isAConcept, parentConcept, defining, refinability,
                    current, 0, config);
            tf.newRelationship(UUID.randomUUID(), newSpecConcept, commentsRelConcept, newCommentsConcept, defining, refinability,
                    current, 0, config);

            I_GetConceptData newPromotionConcept = tf.newConcept(UUID.randomUUID(), false, config);
            tf.newDescription(UUID.randomUUID(), newPromotionConcept, "en",
                    name + " - promotion refset", tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);
            tf.newDescription(UUID.randomUUID(), newPromotionConcept, "en",
                    name + " - promotion refset", tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);
            tf.newRelationship(UUID.randomUUID(), newPromotionConcept, isAConcept, parentConcept, defining, refinability,
                    current, 0, config);
            tf.newRelationship(UUID.randomUUID(), newSpecConcept, promotionRelConcept, newPromotionConcept, defining, refinability,
                    current, 0, config);

            I_GetConceptData newAttributeValueConcept = tf.newConcept(UUID.randomUUID(), false, config);
            tf.newDescription(UUID.randomUUID(), newAttributeValueConcept, "en",
                    name + " - attribute value refset", tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);
            tf.newDescription(UUID.randomUUID(), newAttributeValueConcept, "en",
                    name + " - attribute value refset", tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);
            tf.newRelationship(UUID.randomUUID(), newAttributeValueConcept, isAConcept, parentConcept, defining, refinability,
                    current, 0, config);
            tf.newRelationship(UUID.randomUUID(), newSpecConcept, atributeValueRefsetRel, newAttributeValueConcept, defining, refinability,
                    current, 0, config);

            tf.newRelationship(UUID.randomUUID(), newSpecConcept, purposeRelConcept, purposeConcept, defining, refinability,
                    current, 0, config);
            tf.newRelationship(UUID.randomUUID(), newSpecConcept, typeRelConcept, memberTypeConcept, defining, refinability,
                    current, 0, config);
            tf.newRelationship(UUID.randomUUID(), newSpecConcept, specifies, membershipRefsetConcept, defining, refinability,
                    current, 0, config);
            tf.newRelationship(UUID.randomUUID(), newSpecConcept, enumeratedRel, enumeratedOriginConcept, defining, refinability,
                    current, 0, config);

            tf.addUncommittedNoChecks(newSpecConcept);
            tf.addUncommittedNoChecks(newCommentsConcept);
            tf.addUncommittedNoChecks(newPromotionConcept);
            tf.addUncommittedNoChecks(newAttributeValueConcept);

            tf.commit();

            newLanguageSpecRerset = new LanguageSpecRefset(newSpecConcept);

        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return null;
        }
        return newLanguageSpecRerset;
    }

    /**
     * Gets the language membership refset concept.
     *
     * @param config the config
     * @return the language membership refset concept
     */
    public I_GetConceptData getLanguageMembershipRefsetConcept(I_ConfigAceFrame config) {
        try {
            I_GetConceptData specifiesRefsetRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
            return getLatestSourceRelationshipTarget(getRefsetConcept(), specifiesRefsetRel, config);
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return null;
        }
    }

    /**
     * Gets the enumerated origin refset concept.
     *
     * @param config the config
     * @return the enumerated origin refset concept
     */
    public I_GetConceptData getEnumeratedOriginRefsetConcept(I_ConfigAceFrame config) {
        try {
            I_GetConceptData languageEnumeratedOriginRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_ORIGIN_REL.getUids());
            I_GetConceptData specRefsetConcept = getRefsetConcept();
            if (specRefsetConcept == null) {
                return null;
            }

            return getLatestSourceRelationshipTarget(specRefsetConcept, languageEnumeratedOriginRel, config);
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return null;
        }
    }

    /**
     * Gets the attribute value refset.
     *
     * @param config the config
     * @return the attribute value refset
     */
    public AttributeValueRefset getAttributeValueRefset(I_ConfigAceFrame config) {
        try {
            I_GetConceptData attributeValueRel = termFactory.getConcept(
                    RefsetAuxiliary.Concept.ATTRIBUTE_VALUE_REFSET_REL.getUids());
            I_GetConceptData specRefsetConcept = getRefsetConcept();
            if (specRefsetConcept == null) {
                return null;
            }

            return new AttributeValueRefset(
                    getLatestSourceRelationshipTarget(specRefsetConcept, attributeValueRel, config));
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return null;
        }
    }

    /**
     * Compute language refset spec.
     *
     * @param config the config
     */
    public void computeLanguageRefsetSpec(I_ConfigAceFrame config) {
        I_TermFactory tf = Terms.get();
        try {
            I_GetConceptData current = tf.getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
            I_GetConceptData retired = tf.getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
            I_GetConceptData notAcceptable = tf.getConcept(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid());
            I_GetConceptData enumeratedOriginConcept = getEnumeratedOriginRefsetConcept(config);
            I_GetConceptData languageMembershipConcept = getLanguageMembershipRefsetConcept(config);
            HashMap<Integer, Integer> descIdAcceptabilityMap = new HashMap<Integer, Integer>();

            // adding enumerated members to map
            for (I_ExtendByRef enumeratedOriginMember :
                    tf.getRefsetExtensionMembers(enumeratedOriginConcept.getConceptNid())) {
                long lastVersion = Long.MIN_VALUE;
                I_ExtendByRefPartCid languageExtensionPart = null;
                for (I_ExtendByRefVersion loopTuple : enumeratedOriginMember.getTuples(
                        config.getConflictResolutionStrategy())) {
                    if (loopTuple.getTime() >= lastVersion) {
                        lastVersion = loopTuple.getTime();
                        languageExtensionPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
                    }
                }
                descIdAcceptabilityMap.put(enumeratedOriginMember.getComponentNid(),
                        languageExtensionPart.getC1id());
            }

            // adding exceptions to map
            for (I_ExtendByRef loopMember :
                    tf.getRefsetExtensionMembers(this.refsetId)) {
                long lastVersion = Long.MIN_VALUE;
                I_ExtendByRefPartCid loopSpecPart = null;
                for (I_ExtendByRefVersion loopTuple : loopMember.getTuples(
                        config.getConflictResolutionStrategy())) {
                    if (loopTuple.getTime() >= lastVersion) {
                        lastVersion = loopTuple.getTime();
                        loopSpecPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
                    }
                }
                descIdAcceptabilityMap.put(loopMember.getComponentNid(),
                        loopSpecPart.getC1id());
            }

            // retiring not acceptable and missing descriptions from previous computation
            for (I_ExtendByRef previousComputationMember : tf.getRefsetExtensionMembers(
                    languageMembershipConcept.getConceptNid())) {
                if (descIdAcceptabilityMap.get(previousComputationMember.getComponentNid())
                        == notAcceptable.getConceptNid()
                        || !descIdAcceptabilityMap.containsKey(previousComputationMember.getComponentNid())) {
                    long lastVersion = Long.MIN_VALUE;
                    I_ExtendByRefPartCid previousComputationPart = null;
                    for (I_ExtendByRefVersion loopTuple : previousComputationMember.getTuples(config.getConflictResolutionStrategy())) {
                        if (loopTuple.getTime() >= lastVersion) {
                            lastVersion = loopTuple.getTime();
                            previousComputationPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
                        }
                    }
                    for (PathBI editPath : config.getEditingPathSet()) {
                        I_ExtendByRefPartCid newExtConceptPart = (I_ExtendByRefPartCid) 
                                previousComputationPart.makeAnalog(retired.getConceptNid(),
                                Long.MAX_VALUE,
                                config.getDbConfig().getUserConcept().getNid(),
                                config.getEditCoordinate().getModuleNid(),
                                editPath.getConceptNid());
                        previousComputationMember.addVersion(newExtConceptPart);
                    }
                    tf.addUncommittedNoChecks(previousComputationMember);
                    tf.commit();
                }
            }
            //Adding or updating positive acceptance members
            for (Integer loopDescId : descIdAcceptabilityMap.keySet()) {
                if (descIdAcceptabilityMap.get(loopDescId) != notAcceptable.getConceptNid()) {
                    I_ExtendByRef currentMember = null;
                    for (I_ExtendByRef specMember : tf.getAllExtensionsForComponent(loopDescId)) {
                        if (specMember.getRefsetId() == languageMembershipConcept.getConceptNid()) {
                            currentMember = specMember;
                        }
                    }
                    if (currentMember != null) {
                        long lastVersion = Long.MIN_VALUE;
                        I_ExtendByRefPartCidInt specPart = null;
                        for (I_ExtendByRefVersion loopTuple : currentMember.getTuples(config.getConflictResolutionStrategy())) {
                            if (loopTuple.getTime() >= lastVersion) {
                                lastVersion = loopTuple.getTime();
                                specPart = (I_ExtendByRefPartCidInt) loopTuple.getMutablePart();
                            }
                        }
                        for (PathBI editPath : config.getEditingPathSet()) {
                            I_ExtendByRefPartCid newExtConceptPart = (I_ExtendByRefPartCid) 
                                    specPart.makeAnalog(current.getConceptNid(),
                                    Long.MAX_VALUE,
                                    config.getDbConfig().getUserConcept().getNid(),
                                    config.getEditCoordinate().getModuleNid(),
                                    editPath.getConceptNid());
                            newExtConceptPart.setC1id(descIdAcceptabilityMap.get(loopDescId));
                            currentMember.addVersion(newExtConceptPart);
                        }
                        tf.addUncommittedNoChecks(currentMember);
                        tf.commit();
                    } else {
                        RefsetHelper helper = new RefsetHelper(config.getViewCoordinate(), config.getEditCoordinate());
                        helper.newConceptRefsetExtension(languageMembershipConcept.getConceptNid(), loopDescId, descIdAcceptabilityMap.get(loopDescId));
                        for (I_ExtendByRef extension : tf.getAllExtensionsForComponent(loopDescId)) {
                            if (extension.getMutableParts().iterator().next().getTime() == Long.MAX_VALUE) {
                                termFactory.addUncommittedNoChecks(extension);
                                termFactory.commit();
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return;
    }
}
