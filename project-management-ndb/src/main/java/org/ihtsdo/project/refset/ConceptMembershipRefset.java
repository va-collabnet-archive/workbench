package org.ihtsdo.project.refset;

import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;

public class ConceptMembershipRefset extends ConceptRefset {
	public ConceptMembershipRefset(I_GetConceptData conceptMembershipRefset) throws Exception {
		super();
		validateRefsetAsMembership(conceptMembershipRefset.getConceptNid());
		this.refsetConcept = conceptMembershipRefset;
		this.refsetName = conceptMembershipRefset.toString();
		this.refsetId = conceptMembershipRefset.getConceptNid();
		termFactory = Terms.get();
	}

	public static ConceptMembershipRefset createNewConceptMembershipRefset(String name, int parentId) throws Exception {
		ConceptMembershipRefset newConceptMembershipRefset = null;
		I_GetConceptData newMembershipConcept = null;
		I_TermFactory tf = Terms.get();
		try {
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			I_GetConceptData parentConcept = tf.getConcept(parentId);
			I_GetConceptData isAConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
			I_GetConceptData purposeRelConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
			I_GetConceptData typeRelConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids());
			I_GetConceptData memberTypeConcept = tf.getConcept(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids());
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
			
			tf.newRelationship(UUID.randomUUID(), newMembershipConcept, purposeRelConcept, purposeConcept, defining, refinability, 
					current, 0, config);
			tf.newRelationship(UUID.randomUUID(), newMembershipConcept, typeRelConcept, memberTypeConcept, defining, refinability, 
					current, 0, config);
			
			tf.addUncommittedNoChecks(newMembershipConcept);
			
			tf.commit();
			
			newConceptMembershipRefset = new ConceptMembershipRefset(newMembershipConcept);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return newConceptMembershipRefset; 
	}

	public I_GetConceptData getConceptSpecRefsetConcept() {
		try {
			I_GetConceptData specifiesRefsetRel =
				termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
			return getLatestDestinationRelationshipSource(getRefsetConcept(), specifiesRefsetRel);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void validateRefsetAsMembership(int conceptRefsetId) throws Exception {
		I_TermFactory tf = Terms.get();
		I_GetConceptData conceptRefsetConcept = tf.getConcept(conceptRefsetId);
		I_GetConceptData refsetTypeConcept = tf.getConcept(
				RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids());
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		Set<? extends I_GetConceptData> refsetTypes = getSourceRelTarget(conceptRefsetConcept, config, 
				RefsetAuxiliary.Concept.REFSET_TYPE_REL.localize().getNid());
		boolean isValid = false;
		for (I_GetConceptData refsetType : refsetTypes) {
			if (refsetType.getConceptNid() == refsetTypeConcept.getConceptNid()) {
				isValid = true;
			}
		}
		if (!isValid) throw new Exception("Refset type must be a concept membership refset");
		return;
	}


}
