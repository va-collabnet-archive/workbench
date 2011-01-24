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
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.tk.api.PathBI;

public class LanguageSpecRefset extends WorkflowRefset {

	public LanguageSpecRefset(I_GetConceptData languageSpecRefset) {
		super();
		this.refsetConcept = languageSpecRefset;
		this.refsetName = languageSpecRefset.toString();
		this.refsetId = languageSpecRefset.getConceptNid();
		termFactory = Terms.get();
	}

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
			I_GetConceptData defining = tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
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
			e.printStackTrace();
			return null;
		}
		return newLanguageSpecRerset; 
	}

	public I_GetConceptData getLanguageMembershipRefsetConcept(I_ConfigAceFrame config) {
		try {
			I_GetConceptData specifiesRefsetRel =
				termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
			return getLatestSourceRelationshipTarget(getRefsetConcept(), specifiesRefsetRel, config);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

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
			e.printStackTrace();
			return null;
		}
	}

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
			e.printStackTrace();
			return null;
		}
	}

	public void computeLanguageRefsetSpec(I_ConfigAceFrame config) {
		I_TermFactory tf = Terms.get();
		try {
			I_GetConceptData current = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			I_GetConceptData retired = tf.getConcept(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
			I_GetConceptData notAcceptable = tf.getConcept(ArchitectonicAuxiliary.Concept.NOT_ACCEPTABLE.getUids());
			I_GetConceptData enumeratedOriginConcept = getEnumeratedOriginRefsetConcept(config);
			I_GetConceptData languageMembershipConcept = getLanguageMembershipRefsetConcept(config);
			I_HelpRefsets refsetHelper = tf.getRefsetHelper(config);
			HashMap<Integer, Integer> descIdAcceptabilityMap = new HashMap<Integer,Integer>();

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
			for (I_ExtendByRef previousComputationMember: tf.getRefsetExtensionMembers(
					languageMembershipConcept.getConceptNid())) {
				if (descIdAcceptabilityMap.get(previousComputationMember.getComponentNid()) ==
					notAcceptable.getConceptNid() || 
					!descIdAcceptabilityMap.containsKey(previousComputationMember.getComponentNid())
				) {
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
								editPath.getConceptNid(), 
								Long.MAX_VALUE);
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
									editPath.getConceptNid(), 
									Long.MAX_VALUE);
							newExtConceptPart.setC1id(descIdAcceptabilityMap.get(loopDescId));
							currentMember.addVersion(newExtConceptPart);
						}
						tf.addUncommittedNoChecks(currentMember);
						tf.commit();
					} else {
						refsetHelper.newRefsetExtension(languageMembershipConcept.getConceptNid(), 
								loopDescId, EConcept.REFSET_TYPES.CID, 
								new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, descIdAcceptabilityMap.get(loopDescId)), config);
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
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

}
