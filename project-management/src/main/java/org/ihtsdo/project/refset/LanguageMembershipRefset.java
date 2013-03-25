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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;

/**
 * The Class LanguageMembershipRefset.
 */
public class LanguageMembershipRefset extends WorkflowRefset {

    /**
     * The lang code.
     */
    private String langCode;

    /**
     * Instantiates a new language membership refset.
     *
     * @param languageMembershipRefset the language membership refset
     * @param config the config
     * @throws Exception the exception
     */
    public LanguageMembershipRefset(I_GetConceptData languageMembershipRefset, I_ConfigAceFrame config) throws Exception {
        super();
        validateRefsetAsMembership(languageMembershipRefset.getConceptNid(), config);
        this.refsetConcept = languageMembershipRefset;
        this.refsetName = languageMembershipRefset.toString();
        this.refsetId = languageMembershipRefset.getConceptNid();
        termFactory = Terms.get();
    }

    /**
     * Creates the new language membership refset.
     *
     * @param name the name
     * @param parentId the parent id
     * @param langCode the lang code
     * @param config the config
     * @return the language membership refset
     * @throws Exception the exception
     */
    public static LanguageMembershipRefset createNewLanguageMembershipRefset(String name,
            int parentId, String langCode,
            I_ConfigAceFrame config) throws Exception {
        LanguageMembershipRefset newLanguageMembershipRerset = null;
        I_GetConceptData newMembershipConcept = null;
        I_TermFactory tf = Terms.get();

        try {
            I_GetConceptData parentConcept = tf.getConcept(parentId);
            I_GetConceptData enumConcept = tf.getConcept(ArchitectonicAuxiliary.getLanguageConcept(langCode).getUids());
            I_GetConceptData isAConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
            I_GetConceptData commentsRelConcept = tf.getConcept(RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
            I_GetConceptData promotionRelConcept = tf.getConcept(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
            I_GetConceptData purposeRelConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
            I_GetConceptData typeRelConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids());
            I_GetConceptData langEnumRelConcept = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_ORIGIN_REL.getUids());
            I_GetConceptData memberTypeConcept = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_EXTENSION.getUids());
            I_GetConceptData purposeConcept = tf.getConcept(RefsetAuxiliary.Concept.DIALECT.getUids());
            I_GetConceptData defining = tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
            I_GetConceptData refinability = tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            I_GetConceptData current = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

            newMembershipConcept = tf.newConcept(UUID.randomUUID(), false, config);
            tf.newDescription(UUID.randomUUID(), newMembershipConcept, "en",
                    name,
                    tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()),
                    config);
            tf.newDescription(UUID.randomUUID(), newMembershipConcept, "en",
                    name,
                    tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()),
                    config);
            tf.newRelationship(UUID.randomUUID(), newMembershipConcept, isAConcept, parentConcept, defining, refinability,
                    current, 0, config);

            I_GetConceptData newCommentsConcept = tf.newConcept(UUID.randomUUID(), false, config);
            tf.newDescription(UUID.randomUUID(), newCommentsConcept, "en",
                    name + " - comments refset",
                    tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()), config);
            tf.newDescription(UUID.randomUUID(), newCommentsConcept, "en",
                    name + " - comments refset",
                    tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()), config);
            tf.newRelationship(UUID.randomUUID(), newCommentsConcept, isAConcept, parentConcept, defining, refinability,
                    current, 0, config);
            tf.newRelationship(UUID.randomUUID(), newMembershipConcept, commentsRelConcept, newCommentsConcept, defining, refinability,
                    current, 0, config);

            I_GetConceptData newPromotionConcept = tf.newConcept(UUID.randomUUID(), false, config);
            newPromotionConcept.setAnnotationStyleRefex(true);
            tf.newDescription(UUID.randomUUID(), newPromotionConcept, "en",
                    name + " - promotion refset",
                    tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()), config);
            tf.newDescription(UUID.randomUUID(), newPromotionConcept, "en",
                    name + " - promotion refset",
                    tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()), config);
            tf.newRelationship(UUID.randomUUID(), newPromotionConcept, isAConcept, parentConcept, defining, refinability,
                    current, 0, config);
            tf.newRelationship(UUID.randomUUID(), newMembershipConcept, promotionRelConcept, newPromotionConcept, defining, refinability,
                    current, 0, config);

            tf.newRelationship(UUID.randomUUID(), newMembershipConcept, purposeRelConcept, purposeConcept, defining, refinability,
                    current, 0, config);
            tf.newRelationship(UUID.randomUUID(), newMembershipConcept, typeRelConcept, memberTypeConcept, defining, refinability,
                    current, 0, config);
            tf.newRelationship(UUID.randomUUID(), newMembershipConcept, langEnumRelConcept, enumConcept, defining, refinability,
                    current, 0, config);

            tf.addUncommittedNoChecks(newMembershipConcept);
            tf.addUncommittedNoChecks(newCommentsConcept);
            tf.addUncommittedNoChecks(newPromotionConcept);

            newMembershipConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);

            newLanguageMembershipRerset = new LanguageMembershipRefset(newMembershipConcept, config);

        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return null;
        }
        return newLanguageMembershipRerset;
    }

    /**
     * Creates the language membership refset from concept.
     *
     * @param concept the concept
     * @param langCode the lang code
     * @param config the config
     * @return the language membership refset
     * @throws Exception the exception
     */
    public static LanguageMembershipRefset createLanguageMembershipRefsetFromConcept(I_GetConceptData concept,
            String langCode,
            I_ConfigAceFrame config) throws Exception {
        LanguageMembershipRefset newLanguageMembershipRerset = null;
        I_GetConceptData newMembershipConcept = null;
        I_TermFactory tf = Terms.get();

        try {
            I_GetConceptData enumConcept = tf.getConcept(ArchitectonicAuxiliary.getLanguageConcept(langCode).getUids());
            I_GetConceptData isAConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
            I_GetConceptData commentsRelConcept = tf.getConcept(RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
            I_GetConceptData promotionRelConcept = tf.getConcept(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
            I_GetConceptData purposeRelConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
            I_GetConceptData typeRelConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids());
            I_GetConceptData langEnumRelConcept = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_ORIGIN_REL.getUids());
            I_GetConceptData memberTypeConcept = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_EXTENSION.getUids());
            I_GetConceptData purposeConcept = tf.getConcept(RefsetAuxiliary.Concept.DIALECT.getUids());
            I_GetConceptData defining = tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
            I_GetConceptData refinability = tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            I_GetConceptData current = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

            newMembershipConcept = concept;
            String name = concept.toString();

            I_GetConceptData newCommentsConcept = tf.newConcept(UUID.randomUUID(), false, config);
            tf.newDescription(UUID.randomUUID(), newCommentsConcept, "en",
                    name + " - comments refset",
                    tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()), config);
            tf.newDescription(UUID.randomUUID(), newCommentsConcept, "en",
                    name + " - comments refset",
                    tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()), config);
            tf.newRelationship(UUID.randomUUID(), newCommentsConcept, isAConcept, concept, defining, refinability,
                    current, 0, config);
            I_RelVersioned r1 = tf.newRelationship(UUID.randomUUID(), newMembershipConcept, commentsRelConcept, newCommentsConcept, defining, refinability,
                    current, 0, config);

            I_GetConceptData newPromotionConcept = tf.newConcept(UUID.randomUUID(), false, config);
            tf.newDescription(UUID.randomUUID(), newPromotionConcept, "en",
                    name + " - promotion refset",
                    tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()), config);
            tf.newDescription(UUID.randomUUID(), newPromotionConcept, "en",
                    name + " - promotion refset",
                    tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()), config);
            tf.newRelationship(UUID.randomUUID(), newPromotionConcept, isAConcept, concept, defining, refinability,
                    current, 0, config);
            I_RelVersioned r2 = tf.newRelationship(UUID.randomUUID(), newMembershipConcept, promotionRelConcept, newPromotionConcept, defining, refinability,
                    current, 0, config);

            I_RelVersioned r3 = tf.newRelationship(UUID.randomUUID(), newMembershipConcept, purposeRelConcept, purposeConcept, defining, refinability,
                    current, 0, config);
            I_RelVersioned r4 = tf.newRelationship(UUID.randomUUID(), newMembershipConcept, typeRelConcept, memberTypeConcept, defining, refinability,
                    current, 0, config);
            I_RelVersioned r5 = tf.newRelationship(UUID.randomUUID(), newMembershipConcept, langEnumRelConcept, enumConcept, defining, refinability,
                    current, 0, config);

            tf.addUncommittedNoChecks(newMembershipConcept);
            tf.addUncommittedNoChecks(newCommentsConcept);
            tf.addUncommittedNoChecks(newPromotionConcept);
            newMembershipConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);

            newLanguageMembershipRerset = new LanguageMembershipRefset(newMembershipConcept, config);

        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return null;
        }
        return newLanguageMembershipRerset;
    }

    /**
     * Gets the language spec refset concept.
     *
     * @param config the config
     * @return the language spec refset concept
     */
    public I_GetConceptData getLanguageSpecRefsetConcept(I_ConfigAceFrame config) {
        try {
            I_GetConceptData specifiesRefsetRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
            return getLatestDestinationRelationshipSource(getRefsetConcept(), specifiesRefsetRel, config);
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            return null;
        }
    }

    /**
     * Validate refset as membership.
     *
     * @param languageRefsetId the language refset id
     * @param config the config
     * @throws Exception the exception
     */
    private static void validateRefsetAsMembership(int languageRefsetId, I_ConfigAceFrame config) throws Exception {
        boolean isValid = validateAsLanguageRefset(languageRefsetId, config);
        if (!isValid) {
            throw new Exception("Refset type must be a language refset");
        }
        return;
    }

    /**
     * Validate as language refset.
     *
     * @param languageRefsetId the language refset id
     * @param config the config
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public static boolean validateAsLanguageRefset(int languageRefsetId, I_ConfigAceFrame config) throws IOException, TerminologyException {
        I_TermFactory tf = Terms.get();
        I_GetConceptData languageRefsetConcept = tf.getConcept(languageRefsetId);
        I_GetConceptData refsetTypeConcept = tf.getConcept(
                RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_EXTENSION.getUids());
        Set<? extends I_GetConceptData> refsetTypes = getSourceRelTarget(languageRefsetConcept, config,
                RefsetAuxiliary.Concept.REFSET_TYPE_REL.localize().getNid());
        boolean isValid = false;
        for (I_GetConceptData refsetType : refsetTypes) {
            if (refsetType.getConceptNid() == refsetTypeConcept.getConceptNid()) {
                isValid = true;
            }
        }
        return isValid;
    }

    /**
     * Gets the lang code.
     *
     * @param config the config
     * @return the lang code
     */
    public String getLangCode(I_ConfigAceFrame config) {
        if (langCode == null) {

            Set<? extends I_GetConceptData> refsetLangs = null;
            try {
                refsetLangs = getSourceRelTarget(this.refsetConcept, config,
                        RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_ORIGIN_REL.localize().getNid());
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (TerminologyException e1) {
                e1.printStackTrace();
            }
            if (refsetLangs != null) {
                for (I_GetConceptData refsetLang : refsetLangs) {
                    try {
                        langCode = ArchitectonicAuxiliary.getLanguageCode(refsetLang.getUids());
                        if (langCode != null) {
                            langCode = ArchitectonicAuxiliary.LANG_CODE.valueOf(langCode).getFormatedLanguageCode();
                        }
                        break;
                    } catch (NoSuchElementException e) {
                        AceLog.getAppLog().alertAndLogException(e);
                    } catch (IOException e) {
                        AceLog.getAppLog().alertAndLogException(e);
                    }
                }
            }
        }
        return langCode;
    }

    /**
     * Sets the lang code.
     *
     * @param langCode the new lang code
     */
    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }
}
