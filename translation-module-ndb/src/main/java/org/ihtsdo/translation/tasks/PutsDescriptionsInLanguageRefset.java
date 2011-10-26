package org.ihtsdo.translation.tasks;

import java.util.List;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.ContextualizedDescription;

public class PutsDescriptionsInLanguageRefset implements I_ProcessConcepts {
	I_GetConceptData snomedRoot;
	I_ConfigAceFrame config;
	I_IntSet isaType;
	I_GetConceptData englishLanguageRefsetConcept;
	I_GetConceptData spanishLanguageRefsetConcept;
	I_GetConceptData swedishLanguageRefsetConcept;
	I_GetConceptData preferred;
	I_GetConceptData acceptable;
	I_GetConceptData fsn;
	I_GetConceptData synonym;

	public PutsDescriptionsInLanguageRefset(I_GetConceptData snomedRoot, I_ConfigAceFrame config, 
			I_IntSet isaType, I_GetConceptData englishLanguageRefsetConcept,
			I_GetConceptData spanishLanguageRefsetConcept,I_GetConceptData swedishLanguageRefsetConcept,
			I_GetConceptData preferred, I_GetConceptData acceptable, I_GetConceptData fsn,
			I_GetConceptData synonym) {
		super();
		this.snomedRoot = snomedRoot;
		this.config = config;
		this.isaType = isaType;
		this.englishLanguageRefsetConcept = englishLanguageRefsetConcept;
		this.spanishLanguageRefsetConcept = spanishLanguageRefsetConcept;
		this.swedishLanguageRefsetConcept = swedishLanguageRefsetConcept;
		this.preferred = preferred;
		this.acceptable = acceptable;
		this.fsn = fsn;
		this.synonym = synonym;
	}

	public PutsDescriptionsInLanguageRefset(I_GetConceptData snomedRoot,
			I_ConfigAceFrame config, I_IntSet isaType,
			I_GetConceptData englishLanguageRefsetConcept,
			I_GetConceptData preferred, I_GetConceptData acceptable,
			I_GetConceptData fsn, I_GetConceptData synonym) {
		super();
		this.snomedRoot = snomedRoot;
		this.config = config;
		this.isaType = isaType;
		this.englishLanguageRefsetConcept = englishLanguageRefsetConcept;
		this.spanishLanguageRefsetConcept = null;
		this.swedishLanguageRefsetConcept = null;
		this.preferred = preferred;
		this.acceptable = acceptable;
		this.fsn = fsn;
		this.synonym = synonym;
	}

	@Override
	public void processConcept(I_GetConceptData loopConcept) throws Exception {
		//System.out.println(loopConcept.toString());
		
		List<? extends I_ConceptAttributeTuple> attributes = loopConcept.getConceptAttributeTuples(config.getPrecedence(), 
				config.getConflictResolutionStrategy());
		
		I_ConceptAttributeTuple attribute = null;
		
		if (attributes != null && !attributes.isEmpty()) {
			attribute = attributes.iterator().next();
		}
		
		//if (attribute != null && attribute.getPathId() == ArchitectonicAuxiliary.Concept.SNOMED_CORE.localize().getNid()) {
		
		if (snomedRoot.isParentOfOrEqualTo(loopConcept, config.getAllowedStatus(), config.getDestRelTypes(), 
				config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy())) {
			//System.out.println("Subtype!");
			List<? extends I_DescriptionTuple> descriptions = loopConcept.getDescriptionTuples(null, 
					config.getDescTypes(), 
					config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
			for (I_DescriptionTuple loopDescription : descriptions) {
				//System.out.println("--- desc: " + loopDescription.getText());
				if (loopDescription.getLang().toLowerCase().trim().equals("en")){
					//System.out.println("--------- was en");
					ContextualizedDescription description = new ContextualizedDescription(loopDescription.getDescId(),
							loopDescription.getConceptNid(), englishLanguageRefsetConcept.getConceptNid());
					if (loopDescription.getTypeNid() == fsn.getConceptNid()) {
						description.contextualizeThisDescription(englishLanguageRefsetConcept.getConceptNid(), 
								preferred.getConceptNid());
					} else if (loopDescription.getTypeNid() == synonym.getConceptNid()) {
						description.contextualizeThisDescription(englishLanguageRefsetConcept.getConceptNid(), 
								acceptable.getConceptNid());
					} else {
						description.contextualizeThisDescription(englishLanguageRefsetConcept.getConceptNid(), 
								preferred.getConceptNid());
					}
				} else if (spanishLanguageRefsetConcept != null && loopDescription.getLang().toLowerCase().trim().equals("es")){
					//System.out.println("--------- was es");
					ContextualizedDescription description = new ContextualizedDescription(loopDescription.getDescId(),
							loopDescription.getConceptNid(), spanishLanguageRefsetConcept.getConceptNid());
					if (loopDescription.getTypeNid() == fsn.getConceptNid()) {
						description.contextualizeThisDescription(spanishLanguageRefsetConcept.getConceptNid(), 
								preferred.getConceptNid());
					} else if (loopDescription.getTypeNid() == synonym.getConceptNid()) {
						description.contextualizeThisDescription(spanishLanguageRefsetConcept.getConceptNid(), 
								acceptable.getConceptNid());
					} else {
						description.contextualizeThisDescription(spanishLanguageRefsetConcept.getConceptNid(), 
								preferred.getConceptNid());
					}
				} else if (swedishLanguageRefsetConcept != null && loopDescription.getLang().toLowerCase().trim().equals("sv-se")){
					//System.out.println("--------- was sv-se");
					ContextualizedDescription description = new ContextualizedDescription(loopDescription.getDescId(),
							loopDescription.getConceptNid(), swedishLanguageRefsetConcept.getConceptNid());
					if (loopDescription.getTypeNid() == fsn.getConceptNid()) {
						description.contextualizeThisDescription(swedishLanguageRefsetConcept.getConceptNid(), 
								preferred.getConceptNid());
					} else if (loopDescription.getTypeNid() == synonym.getConceptNid()) {
						description.contextualizeThisDescription(swedishLanguageRefsetConcept.getConceptNid(), 
								acceptable.getConceptNid());
					} else {
						description.contextualizeThisDescription(swedishLanguageRefsetConcept.getConceptNid(), 
								preferred.getConceptNid());
					}
				}

			}
			Terms.get().addUncommittedNoChecks(loopConcept);
		} else {
			//System.out.println("Not subtype!");
		}
	}

}
