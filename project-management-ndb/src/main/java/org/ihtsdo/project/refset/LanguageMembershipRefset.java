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
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;

public class LanguageMembershipRefset extends WorkflowRefset {
	private String langCode;
	public LanguageMembershipRefset(I_GetConceptData languageMembershipRefset,I_ConfigAceFrame config) throws Exception {
		super();
		validateRefsetAsMembership(languageMembershipRefset.getConceptNid(), config);
		this.refsetConcept = languageMembershipRefset;
		this.refsetName = languageMembershipRefset.toString();
		this.refsetId = languageMembershipRefset.getConceptNid();
		termFactory = Terms.get();
	}

	public static LanguageMembershipRefset createNewLanguageMembershipRefset(String name, 
			int parentId, String langCode,
			I_ConfigAceFrame config) throws Exception {
		LanguageMembershipRefset newLanguageMembershipRerset = null;
		I_GetConceptData newMembershipConcept = null;
		I_TermFactory tf = Terms.get();
		
		try {
			I_GetConceptData parentConcept = tf.getConcept(parentId);
			I_GetConceptData enumConcept=tf.getConcept(ArchitectonicAuxiliary.getLanguageConcept(langCode).getUids());
			I_GetConceptData isAConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
			I_GetConceptData commentsRelConcept = tf.getConcept(RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
			I_GetConceptData promotionRelConcept = tf.getConcept(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
			I_GetConceptData purposeRelConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
			I_GetConceptData typeRelConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids());
			I_GetConceptData langEnumRelConcept = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_ORIGIN_REL.getUids());
			I_GetConceptData memberTypeConcept = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_EXTENSION.getUids());
			I_GetConceptData purposeConcept = tf.getConcept(RefsetAuxiliary.Concept.DIALECT.getUids());
			I_GetConceptData defining = tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
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
					tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid())
					, config);
			tf.newDescription(UUID.randomUUID(), newCommentsConcept, "en",
					name + " - comments refset", 
					tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid())
					, config);
			tf.newRelationship(UUID.randomUUID(), newCommentsConcept, isAConcept, parentConcept, defining, refinability, 
					current, 0, config);
			tf.newRelationship(UUID.randomUUID(), newMembershipConcept, commentsRelConcept, newCommentsConcept, defining, refinability, 
					current, 0, config);
			
			I_GetConceptData newPromotionConcept = tf.newConcept(UUID.randomUUID(), false, config);
			tf.newDescription(UUID.randomUUID(), newPromotionConcept, "en",
					name + " - promotion refset", 
					tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid())
					, config);
			tf.newDescription(UUID.randomUUID(), newPromotionConcept, "en",
					name + " - promotion refset", 
					tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid())
					, config);
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
			
			tf.commit();
			
			newLanguageMembershipRerset = new LanguageMembershipRefset(newMembershipConcept, config);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return newLanguageMembershipRerset; 
	}
	public static LanguageMembershipRefset createLanguageMembershipRefsetFromConcept(I_GetConceptData concept, 
			String langCode,
			I_ConfigAceFrame config) throws Exception {
		LanguageMembershipRefset newLanguageMembershipRerset = null;
		I_GetConceptData newMembershipConcept = null;
		I_TermFactory tf = Terms.get();
		
		try {
			I_GetConceptData enumConcept=tf.getConcept(ArchitectonicAuxiliary.getLanguageConcept(langCode).getUids());
			I_GetConceptData isAConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
			I_GetConceptData commentsRelConcept = tf.getConcept(RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
			I_GetConceptData promotionRelConcept = tf.getConcept(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
			I_GetConceptData purposeRelConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
			I_GetConceptData typeRelConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids());
			I_GetConceptData langEnumRelConcept = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_ORIGIN_REL.getUids());
			I_GetConceptData memberTypeConcept = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_EXTENSION.getUids());
			I_GetConceptData purposeConcept = tf.getConcept(RefsetAuxiliary.Concept.DIALECT.getUids());
			I_GetConceptData defining = tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
			I_GetConceptData refinability = tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
			I_GetConceptData current = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

			newMembershipConcept = concept;
			String name = concept.toString(); 
			
			I_GetConceptData newCommentsConcept = tf.newConcept(UUID.randomUUID(), false, config);
			tf.newDescription(UUID.randomUUID(), newCommentsConcept, "en",
					name + " - comments refset", 
					tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid())
					, config);
			tf.newDescription(UUID.randomUUID(), newCommentsConcept, "en",
					name + " - comments refset", 
					tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid())
					, config);
			tf.newRelationship(UUID.randomUUID(), newCommentsConcept, isAConcept, concept, defining, refinability, 
					current, 0, config);
			I_RelVersioned r1 = tf.newRelationship(UUID.randomUUID(), newMembershipConcept, commentsRelConcept, newCommentsConcept, defining, refinability, 
					current, 0, config);
			
			I_GetConceptData newPromotionConcept = tf.newConcept(UUID.randomUUID(), false, config);
			tf.newDescription(UUID.randomUUID(), newPromotionConcept, "en",
					name + " - promotion refset", 
					tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid())
					, config);
			tf.newDescription(UUID.randomUUID(), newPromotionConcept, "en",
					name + " - promotion refset", 
					tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid())
					, config);
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
			tf.commit();
			
			tf.addUncommittedNoChecks(newMembershipConcept);
			tf.addUncommittedNoChecks(newCommentsConcept);
			tf.addUncommittedNoChecks(newPromotionConcept);
			
			tf.commit();
			
			newLanguageMembershipRerset = new LanguageMembershipRefset(newMembershipConcept, config);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return newLanguageMembershipRerset; 
	}

	public I_GetConceptData getLanguageSpecRefsetConcept(I_ConfigAceFrame config) {
		try {
			I_GetConceptData specifiesRefsetRel =
				termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
			return getLatestDestinationRelationshipSource(getRefsetConcept(), specifiesRefsetRel, config);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void validateRefsetAsMembership(int languageRefsetId, I_ConfigAceFrame config) throws Exception {
		boolean isValid = validateAsLanguageRefset(languageRefsetId, config);
		if (!isValid) throw new Exception("Refset type must be a language refset");
		return;
	}
	
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

	public String getLangCode(I_ConfigAceFrame config) {
		if (langCode== null){

			Set<? extends I_GetConceptData> refsetLangs=null;
			try {
				refsetLangs = getSourceRelTarget(this.refsetConcept, config, 
						RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_ORIGIN_REL.localize().getNid());
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (TerminologyException e1) {
				e1.printStackTrace();
			}
			if (refsetLangs!=null){
				for (I_GetConceptData refsetLang : refsetLangs) {
					try {
						langCode=ArchitectonicAuxiliary.getLanguageCode(refsetLang.getUids());
						break;
					} catch (NoSuchElementException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}


}
