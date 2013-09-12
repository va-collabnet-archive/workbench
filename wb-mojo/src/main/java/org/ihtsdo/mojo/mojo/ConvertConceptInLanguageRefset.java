/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.mojo.mojo;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * Goal which converts a refset concept in la language refste concept adding promotion refset,
 * comments refset, type and language code.
 *
 * @goal convert-in-language-refset
 *
 * @phase prepare-package
 */
public class ConvertConceptInLanguageRefset extends AbstractMojo {

	/**
	 * Language refset concept.
	 * 
	 * @parameter
	 * @required
	 */
	private ConceptDescriptor languageRefsetConcept;

	/**
	 * Language code.
	 * 
	 * @parameter
	 * @required
	 */
	private String langCode;

	I_TermFactory tf;

	@Override
	public void execute() throws MojoExecutionException {
		executeMojo();

	}

	void executeMojo() throws MojoExecutionException {

		try {
			tf = Terms.get();
			I_GetConceptData membershipConcept = languageRefsetConcept.getVerifiedConcept();

			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();

			I_GetConceptData refsetTypeConcept = tf.getConcept(
					RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_EXTENSION.getUids());
			Set<? extends I_GetConceptData> refsetTypes = getSourceRelTarget(membershipConcept,
					config, 
					RefsetAuxiliary.Concept.REFSET_TYPE_REL.localize().getNid());
			boolean isValid = false;
			for (I_GetConceptData refsetType : refsetTypes) {
				if (refsetType.getConceptNid() == refsetTypeConcept.getConceptNid()) {
					isValid = true;
				}
			}

			if (!isValid) {
				int parentId = tf.uuidToNative(RefsetAuxiliary.Concept.SUPPORTING_REFSETS.getUids());
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

				String name = membershipConcept.toString(); 

				I_GetConceptData newCommentsConcept = null;
                if(Ts.get().hasUuid(Type5UuidFactory.get(name  + " - comments refset - for UUID generation"))){
                    newCommentsConcept = (I_GetConceptData) Ts.get().getConcept(Type5UuidFactory.get(name  + " - comments refset - for UUID generation"));
                }else{
                    newCommentsConcept = tf.newConcept(
                        Type5UuidFactory.get(name  + " - comments refset - for UUID generation"), 
                        false, config);
                }
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
				I_RelVersioned r1 = tf.newRelationship(UUID.randomUUID(), membershipConcept, commentsRelConcept, newCommentsConcept, defining, refinability, 
						current, 0, config);

                I_GetConceptData newPromotionConcept = null;
                if(Ts.get().hasUuid(Type5UuidFactory.get(name  + " - promotion refset - for UUID generation"))){
                    newPromotionConcept = (I_GetConceptData) Ts.get().getConcept(Type5UuidFactory.get(name  + " - promotion refset - for UUID generation"));
                }else{
                    newPromotionConcept = tf.newConcept(
                        Type5UuidFactory.get(name  + " - promotion refset - for UUID generation")
                        , false, config);
                }
				newPromotionConcept.setAnnotationStyleRefex(true);
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
				I_RelVersioned r2 = tf.newRelationship(UUID.randomUUID(), membershipConcept, promotionRelConcept, newPromotionConcept, defining, refinability, 
						current, 0, config);

				I_RelVersioned r3 = tf.newRelationship(UUID.randomUUID(), membershipConcept, purposeRelConcept, purposeConcept, defining, refinability, 
						current, 0, config);
				I_RelVersioned r4 = tf.newRelationship(UUID.randomUUID(), membershipConcept, typeRelConcept, memberTypeConcept, defining, refinability, 
						current, 0, config);
				I_RelVersioned r5 = tf.newRelationship(UUID.randomUUID(), membershipConcept, langEnumRelConcept, enumConcept, defining, refinability, 
						current, 0, config);

				tf.addUncommittedNoChecks(membershipConcept);
				tf.addUncommittedNoChecks(newCommentsConcept);
				tf.addUncommittedNoChecks(newPromotionConcept);
				tf.commit();
			}
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		} catch (Throwable ex) {
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}

	}

	private Set<? extends I_GetConceptData> getSourceRelTarget(I_GetConceptData refsetIdentityConcept, 
			I_ConfigAceFrame config,
			int relTypeNid) throws IOException, TerminologyException {
		I_TermFactory tf = Terms.get();
		I_IntSet allowedTypes = tf.newIntSet();
		allowedTypes.add(relTypeNid);
		//			    System.out.println("************* relTypeNid: " + relTypeNid);
		//I_GetConceptData relConcept = tf.getConcept(relTypeNid);
		//			    System.out.println("************* relConcept: " + relConcept.toString());
		//			    System.out.println("************* allowedTypes: " + allowedTypes);
		Set<? extends I_GetConceptData> matchingConcepts = refsetIdentityConcept.getSourceRelTargets(config.getAllowedStatus(),
				allowedTypes, config.getViewPositionSetReadOnly(),config.getPrecedence(), config.getConflictResolutionStrategy());
		//			    System.out.println("************* matchingConcepts: " + matchingConcepts.size());
		return matchingConcepts;
	}

}
