/**
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
package org.ihtsdo.rules.testmodel;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.testmodel.Concept;
import org.ihtsdo.testmodel.Description;
import org.ihtsdo.testmodel.Identifier;
import org.ihtsdo.testmodel.Relationship;
import org.ihtsdo.testmodel.TerminologyComponent;

/**
 * The Class TestModelUtil.
 */
public class TestModelUtil {

	public static List<TerminologyComponent> convertUncommittedToTestModel(I_GetConceptData concept, boolean convertConcept,
			boolean convertDescriptions, boolean convertRelationships, boolean convertIds) {
		List<TerminologyComponent> convertedComponents = new ArrayList<TerminologyComponent>();
		I_TermFactory tf = Terms.get();
		try {
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			I_ConceptAttributeVersioned attributes = concept.getConceptAttributes();
			if (convertConcept && attributes != null) {
				for (I_ConceptAttributePart part : attributes.getMutableParts()) {
					if (part.getVersion() == Integer.MAX_VALUE) {
						I_GetConceptData partStatusConcept = tf.getConcept(part.getStatusId());
						I_GetConceptData activeStatusConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
						boolean isActive = activeStatusConcept.isParentOfOrEqualTo(partStatusConcept);
						UUID moduleId = tf.getConcept(part.getPathId()).getUids().iterator().next();

						Concept convertedConcept = new Concept(concept.getUids().iterator().next(),
								getDateTime(), isActive, moduleId, part.isDefined());
						convertedComponents.add(convertedConcept);
					}
				}
			}

			Collection<? extends I_DescriptionVersioned> descriptions = concept.getDescriptions();
			if (convertDescriptions && !descriptions.isEmpty()) {
				for (I_DescriptionVersioned description : descriptions) {
					for (I_DescriptionPart descriptionPart : description.getMutableParts()) {
						if (descriptionPart.getVersion() == Integer.MAX_VALUE) {
							UUID typeId = tf.getConcept(descriptionPart.getTypeId()).getUids().iterator().next();
							UUID moduleId = tf.getConcept(descriptionPart.getPathId()).getUids().iterator().next();
							I_GetConceptData partStatusConcept = tf.getConcept(descriptionPart.getStatusId());
							I_GetConceptData activeStatusConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
							boolean isActive = activeStatusConcept.isParentOfOrEqualTo(partStatusConcept);

							Description convertedDescription = new Description(
									tf.getId(description.getDescId()).getUUIDs().iterator().next(),
									getDateTime(), isActive, moduleId, concept.getUids().iterator().next(),
									descriptionPart.getLang().trim(), typeId, descriptionPart.getText().trim(),
									descriptionPart.isInitialCaseSignificant());
							convertedComponents.add(convertedDescription);
						}
					}
				}
			}

			Collection<? extends I_RelVersioned> relationships = concept.getSourceRels();
			if (convertRelationships && !relationships.isEmpty()) {
				for (I_RelVersioned relationship : relationships) {
					for (I_RelPart relPart : relationship.getMutableParts()) {
						if (relPart.getVersion() == Integer.MAX_VALUE) {
							UUID typeId = tf.getConcept(relPart.getTypeId()).getUids().iterator().next();
							UUID sourceId = tf.getConcept(relationship.getC1Id()).getUids().iterator().next();
							UUID destinationId = tf.getConcept(relationship.getC2Id()).getUids().iterator().next();
							UUID charTypeId = tf.getConcept(relPart.getCharacteristicId()).getUids().iterator().next();
							UUID modifierId = null; //TODO: define where to get this from (modifier = SOME | ALL)
							UUID moduleId = tf.getConcept(relPart.getPathId()).getUids().iterator().next();
							I_GetConceptData partStatusConcept = tf.getConcept(relPart.getStatusId());
							I_GetConceptData activeStatusConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
							boolean isActive = activeStatusConcept.isParentOfOrEqualTo(partStatusConcept);

							Relationship convertedRel = new Relationship(
									tf.getId(relationship.getRelId()).getUUIDs().iterator().next(),
									getDateTime(), isActive, moduleId,
									sourceId, typeId, destinationId, relPart.getGroup(),
									charTypeId, modifierId);
							convertedComponents.add(convertedRel);
						}
					}
				}
			}

			List<I_Identify> uncommittedIds = concept.getUncommittedIdVersioned();
			if (convertIds && !uncommittedIds.isEmpty()) {
				HashMap<UUID, Integer> sourceVersionMap = new HashMap<UUID, Integer>();
				HashMap<UUID, Object> sourceIdMap = new HashMap<UUID, Object>();
				HashMap<UUID, Boolean> sourceStatusMap = new HashMap<UUID, Boolean>();
				HashMap<UUID, UUID> sourceModuleMap = new HashMap<UUID, UUID>();

				for (I_Identify uncommittedId : uncommittedIds) {

					List<? extends I_IdPart> idParts = uncommittedId.getMutableIdParts();

					for (I_IdPart idPart : idParts) {
						if (idPart.getVersion() == Integer.MAX_VALUE) {
							UUID authorityId = tf.getConcept(idPart.getAuthorityNid()).getUids().iterator().next();
							if (sourceVersionMap.get(authorityId) == null || 
									sourceVersionMap.get(authorityId) >= idPart.getVersion()) {
								sourceVersionMap.put(authorityId, idPart.getVersion());
								sourceIdMap.put(authorityId, idPart.getDenotation());
								I_GetConceptData partStatusConcept = tf.getConcept(idPart.getStatusId());
								I_GetConceptData activeStatusConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
								boolean isActive = activeStatusConcept.isParentOfOrEqualTo(partStatusConcept);
								sourceStatusMap.put(authorityId, isActive);
								UUID moduleId = tf.getConcept(idPart.getPathId()).getUids().iterator().next();
								sourceModuleMap.put(authorityId, moduleId);
							}
						}
					}

					for (UUID authorityId : sourceIdMap.keySet()) {
						UUID iterativeIdId = UUID.randomUUID(); //TODO: is there a unique UUID for each authorityId?
						Identifier convertedIdentifier = new Identifier(
								iterativeIdId,
								getDateTime(), sourceStatusMap.get(authorityId), sourceModuleMap.get(authorityId),
								authorityId, sourceIdMap.get(authorityId).toString(), concept.getUids().iterator().next());
						convertedComponents.add(convertedIdentifier);
					}
				}

			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return convertedComponents;

	}

	/**
	 * Convert to test model.
	 * 
	 * @param concept the concept
	 * @param convertConcept the convert concept
	 * @param convertDescriptions the convert descriptions
	 * @param convertRelationships the convert relationships
	 * @param convertIds the convert ids
	 * 
	 * @return the list< terminology component>
	 */
	public static List<TerminologyComponent> convertToTestModel(I_GetConceptData concept, boolean convertConcept,
			boolean convertDescriptions, boolean convertRelationships, boolean convertIds) {
		List<TerminologyComponent> convertedComponents = new ArrayList<TerminologyComponent>();
		I_TermFactory tf = Terms.get();
		try {
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			if (convertConcept) {
				List<? extends I_ConceptAttributeTuple> conceptAttributeTuples = 
					concept.getConceptAttributeTuples(config.getPrecedence(),
							config.getConflictResolutionStrategy());
				I_ConceptAttributeTuple lastAttributeTuple = null;
				Integer lastVersion = Integer.MIN_VALUE;
				for (I_ConceptAttributeTuple attributeTuple : conceptAttributeTuples) {
					if (attributeTuple.getVersion() >= lastVersion) {
						lastVersion = attributeTuple.getVersion();
						lastAttributeTuple = attributeTuple;
					}
				}
				I_GetConceptData tupleStatusConcept = tf.getConcept(lastAttributeTuple.getStatusId());
				I_GetConceptData activeStatusConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
				boolean isActive = activeStatusConcept.isParentOfOrEqualTo(tupleStatusConcept);
				UUID moduleId = tf.getConcept(lastAttributeTuple.getPathId()).getUids().iterator().next();

				Concept convertedConcept = new Concept(concept.getUids().iterator().next(),
						getDateTime(), isActive, moduleId, lastAttributeTuple.isDefined());
				convertedComponents.add(convertedConcept);
			}

			if (convertDescriptions) {
				for (I_DescriptionTuple descriptionTuple : concept.getDescriptionTuples(config.getAllowedStatus(),
						null, config.getViewPositionSetReadOnly(), config.getPrecedence(), 
						config.getConflictResolutionStrategy())) {
					UUID typeId = tf.getConcept(descriptionTuple.getTypeId()).getUids().iterator().next();
					UUID moduleId = tf.getConcept(descriptionTuple.getPathId()).getUids().iterator().next();
					I_GetConceptData tupleStatusConcept = tf.getConcept(descriptionTuple.getStatusId());
					I_GetConceptData activeStatusConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
					boolean isActive = activeStatusConcept.isParentOfOrEqualTo(tupleStatusConcept);

					Description convertedDescription = new Description(
							tf.getId(descriptionTuple.getDescId()).getUUIDs().iterator().next(),
							getDateTime(), isActive, moduleId, concept.getUids().iterator().next(),
							descriptionTuple.getLang().trim(), typeId, descriptionTuple.getText().trim(),
							descriptionTuple.isInitialCaseSignificant());
					convertedComponents.add(convertedDescription);
				}
			}

			if (convertRelationships) {
				for (I_RelTuple relTuple : concept.getSourceRelTuples(null, null, config.getViewPositionSetReadOnly(),
						config.getPrecedence(), config.getConflictResolutionStrategy())) {
					UUID typeId = tf.getConcept(relTuple.getTypeId()).getUids().iterator().next();
					UUID sourceId = tf.getConcept(relTuple.getC1Id()).getUids().iterator().next();
					UUID destinationId = tf.getConcept(relTuple.getC2Id()).getUids().iterator().next();
					UUID charTypeId = tf.getConcept(relTuple.getCharacteristicId()).getUids().iterator().next();
					UUID modifierId = null; //TODO: define where to get this from (modifier = SOME | ALL)
					UUID moduleId = tf.getConcept(relTuple.getPathId()).getUids().iterator().next();
					I_GetConceptData tupleStatusConcept = tf.getConcept(relTuple.getStatusId());
					I_GetConceptData activeStatusConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
					boolean isActive = activeStatusConcept.isParentOfOrEqualTo(tupleStatusConcept);

					Relationship convertedRel = new Relationship(
							tf.getId(relTuple.getRelId()).getUUIDs().iterator().next(),
							getDateTime(), isActive, moduleId,
							sourceId, typeId, destinationId, relTuple.getGroup(),
							charTypeId, modifierId);
					convertedComponents.add(convertedRel);
				}
			}

			if (convertIds) {
				HashMap<UUID, Integer> sourceVersionMap = new HashMap<UUID, Integer>();
				HashMap<UUID, Object> sourceIdMap = new HashMap<UUID, Object>();
				HashMap<UUID, Boolean> sourceStatusMap = new HashMap<UUID, Boolean>();
				HashMap<UUID, UUID> sourceModuleMap = new HashMap<UUID, UUID>();

				List<? extends I_IdPart> idParts = tf.getId(concept.getNid()).getMutableIdParts();

				for (I_IdPart idPart : idParts) {
					UUID authorityId = tf.getConcept(idPart.getAuthorityNid()).getUids().iterator().next();
					if (sourceVersionMap.get(authorityId) == null || 
							sourceVersionMap.get(authorityId) >= idPart.getVersion()) {
						sourceVersionMap.put(authorityId, idPart.getVersion());
						sourceIdMap.put(authorityId, idPart.getDenotation());
						I_GetConceptData tupleStatusConcept = tf.getConcept(idPart.getStatusId());
						I_GetConceptData activeStatusConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
						boolean isActive = activeStatusConcept.isParentOfOrEqualTo(tupleStatusConcept);
						sourceStatusMap.put(authorityId, isActive);
						UUID moduleId = tf.getConcept(idPart.getPathId()).getUids().iterator().next();
						sourceModuleMap.put(authorityId, moduleId);
					}
				}

				for (UUID authorityId : sourceIdMap.keySet()) {
					UUID iterativeIdId = UUID.randomUUID(); //TODO: is there a unique UUID for each authorityId?
					Identifier convertedIdentifier = new Identifier(
							iterativeIdId,
							getDateTime(), sourceStatusMap.get(authorityId), sourceModuleMap.get(authorityId),
							authorityId, sourceIdMap.get(authorityId).toString(), concept.getUids().iterator().next());
					convertedComponents.add(convertedIdentifier);
				}

			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return convertedComponents;
	}

	/**
	 * Gets the workbench concept.
	 * 
	 * @param concept the concept
	 * 
	 * @return the workbench concept
	 * 
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static I_GetConceptData getWorkbenchConcept(Concept concept) throws TerminologyException, IOException {
		return Terms.get().getConcept(new UUID[] {concept.getId()});
	}


	/**
	 * Gets the date time.
	 * 
	 * @return the date time
	 */
	private static String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		return dateFormat.format(date);
	}

}
