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
package org.ihtsdo.rules.testmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.qa.inheritance.RelationshipsDAO;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.RulesLibrary.INFERRED_VIEW_ORIGIN;
import org.ihtsdo.testmodel.DrConcept;
import org.ihtsdo.testmodel.DrDefiningRolesSet;
import org.ihtsdo.testmodel.DrDescription;
import org.ihtsdo.testmodel.DrIdentifier;
import org.ihtsdo.testmodel.DrLanguageDesignationSet;
import org.ihtsdo.testmodel.DrRefsetExtension;
import org.ihtsdo.testmodel.DrRelationship;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSet;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_float.RefexFloatVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_float.RefexNidFloatVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_int.RefexNidIntVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_long.RefexNidLongVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_string.RefexNidNidStringVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * The Class DrComponentHelper.
 */
public class DrComponentHelper {

	/** The all rels. */
	public static I_IntSet allRels;

	/** The hist rels. */
	public static I_IntSet histRels;

	/** The Cpt model rels. */
	public static I_IntSet CptModelRels;

	/**
	 * Gets the dr concept.
	 *
	 * @param conceptBi the concept bi
	 * @param factContextName the fact context name
	 * @param inferredOrigin the inferred origin
	 * @return the dr concept
	 */
	public static DrConcept getDrConcept(ConceptVersionBI conceptBi, String factContextName, 
			INFERRED_VIEW_ORIGIN inferredOrigin) throws Exception {
		I_TermFactory tf = Terms.get();
		TerminologyStoreDI ts = Ts.get();
		DrConcept concept = new DrConcept();

		try {
			I_GetConceptData oldStyleConcept = tf.getConcept(conceptBi.getNid());
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();

			Set<PositionBI> viewPositions =  new HashSet<PositionBI>();
			for (PathBI loopPath : config.getEditingPathSet()) {
				PositionBI pos = Terms.get().newPosition(loopPath, Long.MAX_VALUE);
				viewPositions.add(pos);
			}
			PositionSet mockViewSet = new PositionSet(viewPositions);
			ViewCoordinate mockVc = new ViewCoordinate(config.getViewCoordinate());
			mockVc.setPositionSet(mockViewSet);

			List<? extends I_ConceptAttributeTuple> attributeTuples = oldStyleConcept.getConceptAttributeTuples(null, 
					mockViewSet, config.getPrecedence(), 
					config.getConflictResolutionStrategy());

			if (attributeTuples != null && !attributeTuples.isEmpty()) {
				I_ConceptAttributeTuple attributeTuple = attributeTuples.iterator().next();
				concept.setDefined(attributeTuple.isDefined());
				concept.setPathUuid(tf.nidToUuid(attributeTuple.getPathNid()).toString());
				concept.setPrimordialUuid(attributeTuple.getPrimUuid().toString());
				concept.setStatusUuid(tf.nidToUuid(attributeTuple.getStatusNid()).toString());
				concept.setTime(attributeTuple.getTime());
				concept.setExtensionId(tf.nidToUuid(attributeTuple.getModuleNid()).toString());
				concept.setFactContextName(factContextName);
			}

			Collection<? extends DescriptionVersionBI> descriptionsList = oldStyleConcept.getDescriptionTuples(null, 
					null, mockViewSet, 
					config.getPrecedence(), config.getConflictResolutionStrategy());

			HashMap<Integer,DrLanguageDesignationSet> languageDesignationSetsMap = new HashMap<Integer,DrLanguageDesignationSet>();

			ConceptSpec referToRefset = new ConceptSpec("REFERS TO concept association reference set (foundation metadata concept)", UUID.fromString("d15fde65-ed52-3a73-926b-8981e9743ee9"));

			for (DescriptionVersionBI descriptionVersion : descriptionsList) {
				Collection<? extends RefexVersionBI<?>> currentAnnotations = descriptionVersion.getChronicle().getAnnotationsActive(mockVc);
				for (RefexVersionBI<?> annotation : currentAnnotations) {
					if (annotation instanceof RefexNidVersionBI) {
						RefexNidVersionBI annotationNid = (RefexNidVersionBI) annotation;
						int languageNid = annotationNid.getRefexNid();
						if (!languageDesignationSetsMap.containsKey(languageNid) && annotationNid.getRefexNid() != referToRefset.getLenient().getNid()) {
							DrLanguageDesignationSet langDefSet = new DrLanguageDesignationSet();
							langDefSet.setLanguageRefsetUuid(tf.nidToUuid(annotationNid.getRefexNid()).toString());
							languageDesignationSetsMap.put(languageNid, langDefSet);
						}
					}
				}
			}

			for (DescriptionVersionBI descriptionVersion : descriptionsList) {
				DrDescription loopDescription = new DrDescription();
				loopDescription.setAuthorUuid(tf.nidToUuid(descriptionVersion.getAuthorNid()).toString());
				loopDescription.setConceptUuid(tf.nidToUuid(descriptionVersion.getConceptNid()).toString());
				loopDescription.setInitialCaseSignificant(descriptionVersion.isInitialCaseSignificant());
				loopDescription.setLang(descriptionVersion.getLang());
				loopDescription.setText(descriptionVersion.getText());
				loopDescription.setTime(descriptionVersion.getTime());
				loopDescription.setExtensionId(tf.nidToUuid(descriptionVersion.getModuleNid()).toString());
				loopDescription.setStatusUuid(tf.nidToUuid(descriptionVersion.getStatusNid()).toString());
				loopDescription.setPathUuid(tf.nidToUuid(descriptionVersion.getPathNid()).toString());
				loopDescription.setPrimordialUuid(descriptionVersion.getPrimUuid().toString());
				loopDescription.setTypeUuid(tf.nidToUuid(descriptionVersion.getTypeNid()).toString());
				loopDescription.setPublished(!getSnomedIntId(descriptionVersion.getNid()).equals("0"));
				loopDescription.setFactContextName(factContextName);
				addAnnotationsToDescription(loopDescription, descriptionVersion, mockVc, factContextName);

				loopDescription.setIdentifiers(new ArrayList<DrIdentifier>());
				for (IdBI id : descriptionVersion.getAllIds()) {
					DrIdentifier drId = new DrIdentifier();
					drId.setAuthorityUuid(tf.nidToUuid(id.getAuthorityNid()).toString());
					drId.setDenotation(id.getDenotation().toString());
					drId.setPathUuid(tf.nidToUuid(id.getPathNid()).toString());
					drId.setStatusUuid(tf.nidToUuid(id.getStatusNid()).toString());
					drId.setTime(id.getTime());
					drId.setExtensionId(tf.nidToUuid(id.getModuleNid()).toString());
					loopDescription.getIdentifiers().add(drId);
				}

				Collection<? extends RefexVersionBI<?>> currentAnnotations = descriptionVersion.getChronicle().getAnnotationsActive(mockVc);
				for (RefexVersionBI<?> annotation : currentAnnotations) {
					try {
						if (annotation instanceof RefexNidVersionBI) {
							RefexNidVersionBI annotationNid = (RefexNidVersionBI) annotation;
							if (annotationNid.getRefexNid() == referToRefset.getLenient().getNid()) {
								loopDescription.setReferToConceptUuid(tf.nativeToUuid(annotationNid.getNid1()).iterator().next().toString());
							} else {
								DrDescription langDescription = new DrDescription();
								langDescription.setAuthorUuid(tf.nidToUuid(descriptionVersion.getAuthorNid()).toString());
								langDescription.setConceptUuid(tf.nidToUuid(descriptionVersion.getConceptNid()).toString());
								langDescription.setInitialCaseSignificant(descriptionVersion.isInitialCaseSignificant());
								langDescription.setLang(descriptionVersion.getLang());
								langDescription.setText(descriptionVersion.getText());
								langDescription.setTime(descriptionVersion.getTime());
								langDescription.setExtensionId(tf.nidToUuid(descriptionVersion.getModuleNid()).toString());
								langDescription.setStatusUuid(tf.nidToUuid(descriptionVersion.getStatusNid()).toString());
								langDescription.setPathUuid(tf.nidToUuid(descriptionVersion.getPathNid()).toString());
								langDescription.setPrimordialUuid(descriptionVersion.getPrimUuid().toString());
								langDescription.setTypeUuid(tf.nidToUuid(descriptionVersion.getTypeNid()).toString());
								langDescription.setFactContextName(factContextName);
								langDescription.setIdentifiers(new ArrayList<DrIdentifier>());
								for (IdBI id : descriptionVersion.getAllIds()) {
									DrIdentifier drId = new DrIdentifier();
									drId.setAuthorityUuid(tf.nidToUuid(id.getAuthorityNid()).toString());
									drId.setDenotation(id.getDenotation().toString());
									drId.setPathUuid(tf.nidToUuid(id.getPathNid()).toString());
									drId.setStatusUuid(tf.nidToUuid(id.getStatusNid()).toString());
									drId.setTime(id.getTime());
									drId.setExtensionId(tf.nidToUuid(id.getModuleNid()).toString());
									langDescription.getIdentifiers().add(drId);
								}

								int languageNid = annotationNid.getRefexNid();
								DrLanguageDesignationSet langDefSet = languageDesignationSetsMap.get(languageNid);
								langDescription.setAcceptabilityUuid(tf.nidToUuid(annotationNid.getNid1()).toString());
								langDescription.setLanguageRefsetUuid(tf.nidToUuid(annotationNid.getRefexNid()).toString());
								langDefSet.getDescriptions().add(langDescription);
							}
						}
					} catch (Exception e) {
						// not Nid annotation, ignore
					}
				}

				concept.getDescriptions().add(loopDescription);

			}

			for (DrLanguageDesignationSet langSet : languageDesignationSetsMap.values()) {
				concept.getLanguageDesignationSets().add(langSet);
			}

			if (allRels == null) {
				allRels =RulesLibrary.getAllRels();
			}

			int stated = SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid();
			int inferred = SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid();
			int historical = tf.uuidToNative(ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC.getUids());

			if (inferredOrigin == INFERRED_VIEW_ORIGIN.STATED) {
				for (RelationshipVersionBI<?> relTuple : oldStyleConcept.getSourceRelTuples(null, 
						null, 
						mockViewSet, config.getPrecedence(), 
						config.getConflictResolutionStrategy())) {
					if (relTuple.getCharacteristicNid() == stated) {
						DrRelationship loopRel = new DrRelationship();
						loopRel.setModifierUuid("someUuid");
						loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
						loopRel.setSourceUuid(tf.nidToUuid(relTuple.getSourceNid()).toString());
						loopRel.setTargetUuid(tf.nidToUuid(relTuple.getTargetNid()).toString());
						loopRel.setCharacteristicUuid(tf.nidToUuid(relTuple.getCharacteristicNid()).toString());
						loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
						loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
						loopRel.setRelGroup(relTuple.getGroup());
						loopRel.setStatusUuid(tf.nidToUuid(relTuple.getStatusNid()).toString());
						loopRel.setTime(relTuple.getTime());
						loopRel.setExtensionId(tf.nidToUuid(relTuple.getModuleNid()).toString());
						loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
						loopRel.setFactContextName(factContextName);
						loopRel.setPublished(!getSnomedIntId(relTuple.getNid()).equals("0"));
						loopRel.setIdentifiers(new ArrayList<DrIdentifier>());
						for (IdBI id : relTuple.getAllIds()) {
							DrIdentifier drId = new DrIdentifier();
							drId.setAuthorityUuid(tf.nidToUuid(id.getAuthorityNid()).toString());
							drId.setDenotation(id.getDenotation().toString());
							drId.setPathUuid(tf.nidToUuid(id.getPathNid()).toString());
							drId.setStatusUuid(tf.nidToUuid(id.getStatusNid()).toString());
							drId.setTime(id.getTime());
							drId.setExtensionId(tf.nidToUuid(id.getModuleNid()).toString());
							loopRel.getIdentifiers().add(drId);
						}
						concept.getOutgoingRelationships().add(loopRel);
					}
				}
			} else if (inferredOrigin == INFERRED_VIEW_ORIGIN.INFERRED) {
				for (RelationshipVersionBI<?> relTuple : oldStyleConcept.getSourceRelTuples(null, 
						null, 
						mockViewSet, config.getPrecedence(), 
						config.getConflictResolutionStrategy())) {
					if (relTuple.getCharacteristicNid() == inferred) {
						DrRelationship loopRel = new DrRelationship();
						loopRel.setModifierUuid("someUuid");
						loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
						loopRel.setSourceUuid(tf.nidToUuid(relTuple.getSourceNid()).toString());
						loopRel.setTargetUuid(tf.nidToUuid(relTuple.getTargetNid()).toString());
						loopRel.setCharacteristicUuid(tf.nidToUuid(relTuple.getCharacteristicNid()).toString());
						loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
						loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
						loopRel.setRelGroup(relTuple.getGroup());
						loopRel.setStatusUuid(tf.nidToUuid(relTuple.getStatusNid()).toString());
						loopRel.setTime(relTuple.getTime());
						loopRel.setExtensionId(tf.nidToUuid(relTuple.getModuleNid()).toString());
						loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
						loopRel.setFactContextName(factContextName);
						loopRel.setPublished(!getSnomedIntId(relTuple.getNid()).equals("0"));
						loopRel.setIdentifiers(new ArrayList<DrIdentifier>());
						for (IdBI id : relTuple.getAllIds()) {
							DrIdentifier drId = new DrIdentifier();
							drId.setAuthorityUuid(tf.nidToUuid(id.getAuthorityNid()).toString());
							drId.setDenotation(id.getDenotation().toString());
							drId.setPathUuid(tf.nidToUuid(id.getPathNid()).toString());
							drId.setStatusUuid(tf.nidToUuid(id.getStatusNid()).toString());
							drId.setTime(id.getTime());
							drId.setExtensionId(tf.nidToUuid(id.getModuleNid()).toString());
							loopRel.getIdentifiers().add(drId);
						}
						concept.getOutgoingRelationships().add(loopRel);
					}
				}
			}else if (inferredOrigin == INFERRED_VIEW_ORIGIN.CONSTRAINT_NORMAL_FORM) {
				RelationshipsDAO rDao=new RelationshipsDAO();
				concept.getOutgoingRelationships().addAll(rDao.getConstraintNormalForm(oldStyleConcept, factContextName));
				rDao=null;
			}

			DrDefiningRolesSet statedRolesSet = new DrDefiningRolesSet();
			statedRolesSet.setRolesSetType("Stated");

			DrDefiningRolesSet inferredRolesSet = new DrDefiningRolesSet();
			inferredRolesSet.setRolesSetType("Inferred");

			DrDefiningRolesSet modelersRolesSet = new DrDefiningRolesSet();
			modelersRolesSet.setRolesSetType("Modelers");

			DrDefiningRolesSet definingFormRolesSet = new DrDefiningRolesSet();
			definingFormRolesSet.setRolesSetType("Defining");

			for (RelationshipVersionBI relTuple : oldStyleConcept.getSourceRelTuples(null, 
					null, 
					mockViewSet, config.getPrecedence(), 
					config.getConflictResolutionStrategy())) {
				DrRelationship loopRel = new DrRelationship();
				loopRel.setModifierUuid("someUuid");
				loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
				loopRel.setSourceUuid(tf.nidToUuid(relTuple.getSourceNid()).toString());
				loopRel.setTargetUuid(tf.nidToUuid(relTuple.getTargetNid()).toString());
				loopRel.setCharacteristicUuid(tf.nidToUuid(relTuple.getCharacteristicNid()).toString());
				loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
				loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
				loopRel.setRelGroup(relTuple.getGroup());
				loopRel.setStatusUuid(tf.nidToUuid(relTuple.getStatusNid()).toString());
				loopRel.setTime(relTuple.getTime());
				loopRel.setExtensionId(tf.nidToUuid(relTuple.getModuleNid()).toString());
				loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
				loopRel.setFactContextName(factContextName);
				loopRel.setPublished(!getSnomedIntId(relTuple.getNid()).equals("0"));
				loopRel.setIdentifiers(new ArrayList<DrIdentifier>());
				for (IdBI id : relTuple.getAllIds()) {
					DrIdentifier drId = new DrIdentifier();
					drId.setAuthorityUuid(tf.nidToUuid(id.getAuthorityNid()).toString());
					drId.setDenotation(id.getDenotation().toString());
					drId.setPathUuid(tf.nidToUuid(id.getPathNid()).toString());
					drId.setStatusUuid(tf.nidToUuid(id.getStatusNid()).toString());
					drId.setTime(id.getTime());
					drId.setExtensionId(tf.nidToUuid(id.getModuleNid()).toString());
					loopRel.getIdentifiers().add(drId);
				}

				if (relTuple.getCharacteristicNid() == historical) {
					concept.getOutgoingRelationships().add(loopRel);
				}

				if (relTuple.getCharacteristicNid() == stated) {
					statedRolesSet.getRelationships().add(loopRel);
				}

				if (relTuple.getCharacteristicNid() != inferred) {
					modelersRolesSet.getRelationships().add(loopRel);
				}

				if (relTuple.getCharacteristicNid() == inferred) {
					//System.out.println(loopRel.toString());
					inferredRolesSet.getRelationships().add(loopRel);
				}
			}
			concept.getDefiningRoleSets().add(statedRolesSet);
			concept.getDefiningRoleSets().add(modelersRolesSet);
			concept.getDefiningRoleSets().add(inferredRolesSet);

			definingFormRolesSet.setRelationships(new ArrayList<DrRelationship>());
			definingFormRolesSet.getRelationships().addAll(concept.getOutgoingRelationships());
			concept.getDefiningRoleSets().add(definingFormRolesSet);
			//TODO: incoming rels is heavy on performance moved to helper method
			//			for (RelationshipVersionBI relTuple :  oldStyleConcept.getDestRelTuples(config.getAllowedStatus(), 
			//					null, 
			//					mockViewSet, config.getPrecedence(), 
			//					config.getConflictResolutionStrategy())) {
			//				if (relTuple.getCharacteristiNid() == historical) {
			//					DrRelationship loopRel = new DrRelationship();
			//					loopRel.setModifierUuid("someUuid");
			//					loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
			//					loopRel.setSourceUuid(tf.nidToUuid(relTuple.getSourceNid()).toString());
			//					loopRel.setTargetUuid(tf.nidToUuid(relTuple.getTargetNid()).toString());
			//					loopRel.setCharacteristicUuid(tf.nidToUuid(relTuple.getCharacteristiNid()).toString());
			//					loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
			//					loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
			//					loopRel.setRelGroup(relTuple.getGroup());
			//					loopRel.setStatusUuid(tf.nidToUuid(relTuple.getStatusNid()).toString());
			//					loopRel.setTime(relTuple.getTime());
			//					loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
			//					loopRel.setFactContextName(factContextName);
			//					concept.getIncomingRelationships().add(loopRel);
			//				}
			//			}

			concept.setIdentifiers(new ArrayList<DrIdentifier>());
			for (IdBI id : conceptBi.getAllIds()) {
				DrIdentifier drId = new DrIdentifier();
				drId.setAuthorityUuid(tf.nidToUuid(id.getAuthorityNid()).toString());
				drId.setDenotation(id.getDenotation().toString());
				drId.setPathUuid(tf.nidToUuid(id.getPathNid()).toString());
				drId.setStatusUuid(tf.nidToUuid(id.getStatusNid()).toString());
				drId.setTime(id.getTime());
				drId.setExtensionId(tf.nidToUuid(id.getModuleNid()).toString());
				concept.getIdentifiers().add(drId);
			}
			addAnnotationsToConcept(concept, conceptBi, mockVc, factContextName);

		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return concept;

	}

	/**
	 * Gets the descendants.
	 *
	 * @param descendants the descendants
	 * @param concept the concept
	 * @return the descendants
	 */
	public static Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			Set<PositionBI> viewPositions =  new HashSet<PositionBI>();
			for (PathBI loopPath : config.getEditingPathSet()) {
				PositionBI pos = termFactory.newPosition(loopPath, Long.MAX_VALUE);
				viewPositions.add(pos);
			}
			PositionSet mockViewSet = new PositionSet(viewPositions);
			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			childrenSet.addAll(concept.getDestRelOrigins(config.getAllowedStatus(), 
					config.getDestRelTypes(), mockViewSet
					, config.getPrecedence(), config.getConflictResolutionStrategy()));
			descendants.addAll(childrenSet);
			for (I_GetConceptData loopConcept : childrenSet) {
				descendants = getDescendants(descendants, loopConcept);
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return descendants;
	}

	/**
	 * Gets the snomed int id.
	 *
	 * @param nid the nid
	 * @return the snomed int id
	 * @throws IOException signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public static String getSnomedIntId(int nid) throws IOException, TerminologyException {
		Long descriptionId = 0L; //If description is new then descriptionid doesn't exist in workbench so use dummy value.
		I_Identify desc_Identify = Terms.get().getId(nid);
		List<? extends I_IdVersion> i_IdentifyList = desc_Identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotation = (Object) i_IdVersion.getDenotation();
				int authorityNid = i_IdVersion.getAuthorityNid();
				int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
				if (authorityNid == arcAuxSnomedIntegerNid) {
					descriptionId = (Long) denotation;
				}
			}
		}
		return descriptionId.toString();
	}

	private static void addAnnotationsToConcept(DrConcept concept, ConceptVersionBI componentBi, 
			ViewCoordinate mockVc, String factContextName) throws Exception {
		I_TermFactory tf = Terms.get();
		if (componentBi != null && componentBi.getConceptAttributesActive() != null) {
			Collection<? extends RefexVersionBI<?>> annotations = componentBi.getConceptAttributesActive().getAnnotationsActive(mockVc);

			for (RefexVersionBI annotation : annotations) {
				DrRefsetExtension extension = new DrRefsetExtension();
				extension.setActive(true);
				extension.setComponentUuid(tf.nidToUuid(annotation.getReferencedComponentNid()).toString());
				extension.setRefsetUuid(tf.nidToUuid(annotation.getRefexNid()).toString());
				extension.setPrimordialUuid(annotation.getPrimUuid().toString());
				extension.setFactContextName(factContextName); 
				if (annotation instanceof RefexNidVersionBI) {
					RefexNidVersionBI annotationTyped = (RefexNidVersionBI) annotation;
					extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
					extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
					extension.setTime(annotationTyped.getTime());
					extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
				} else if (annotation instanceof RefexNidNidNidVersionBI) {
					RefexNidNidNidVersionBI annotationTyped = (RefexNidNidNidVersionBI) annotation;
					extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
					extension.setC2Uuid(tf.nidToUuid(annotationTyped.getNid2()).toString());
					extension.setC3Uuid(tf.nidToUuid(annotationTyped.getNid3()).toString());
					extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
					extension.setTime(annotationTyped.getTime());
					extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
				} else if (annotation instanceof RefexNidNidStringVersionBI) {
					RefexNidNidStringVersionBI annotationTyped = (RefexNidNidStringVersionBI) annotation;
					extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
					extension.setC2Uuid(tf.nidToUuid(annotationTyped.getNid2()).toString());
					extension.setStrValue(annotationTyped.getString1());
					extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
					extension.setTime(annotationTyped.getTime());
					extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
				} else if (annotation instanceof RefexNidNidVersionBI) {
					RefexNidNidVersionBI annotationTyped = (RefexNidNidVersionBI) annotation;
					extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
					extension.setC2Uuid(tf.nidToUuid(annotationTyped.getNid2()).toString());
					extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
					extension.setTime(annotationTyped.getTime());
					extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
				} else if (annotation instanceof RefexNidFloatVersionBI) {
					RefexNidFloatVersionBI annotationTyped = (RefexNidFloatVersionBI) annotation;
					extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
					extension.setFloatValue(annotationTyped.getFloat1());
					extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
					extension.setTime(annotationTyped.getTime());
					extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
				} else if (annotation instanceof RefexNidIntVersionBI) {
					RefexNidIntVersionBI annotationTyped = (RefexNidIntVersionBI) annotation;
					extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
					extension.setIntValue(annotationTyped.getInt1());
					extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
					extension.setTime(annotationTyped.getTime());
					extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
				} else if (annotation instanceof RefexNidLongVersionBI) {
					RefexNidLongVersionBI annotationTyped = (RefexNidLongVersionBI) annotation;
					extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
					extension.setLongValue(annotationTyped.getLong1());
					extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
					extension.setTime(annotationTyped.getTime());
					extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
				} else if (annotation instanceof RefexNidStringVersionBI) {
					RefexNidStringVersionBI annotationTyped = (RefexNidStringVersionBI) annotation;
					extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
					extension.setStrValue(annotationTyped.getString1());
					extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
					extension.setTime(annotationTyped.getTime());
					extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
				} else if (annotation instanceof RefexFloatVersionBI) {
					RefexFloatVersionBI annotationTyped = (RefexFloatVersionBI) annotation;
					extension.setFloatValue(annotationTyped.getFloat1());
					extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
					extension.setTime(annotationTyped.getTime());
					extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
				} else if (annotation instanceof RefexIntVersionBI) {
					RefexIntVersionBI annotationTyped = (RefexIntVersionBI) annotation;
					extension.setIntValue(annotationTyped.getInt1());
					extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
					extension.setTime(annotationTyped.getTime());
					extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
				} else if (annotation instanceof RefexLongVersionBI) {
					RefexLongVersionBI annotationTyped = (RefexLongVersionBI) annotation;
					extension.setLongValue(annotationTyped.getLong1());
					extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
					extension.setTime(annotationTyped.getTime());
					extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
				} else if (annotation instanceof RefexStringVersionBI) {
					RefexStringVersionBI annotationTyped = (RefexStringVersionBI) annotation;
					extension.setStrValue(annotationTyped.getString1());
					extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
					extension.setTime(annotationTyped.getTime());
					extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
				} else {
					// unknown refset
				}
				if (concept.getExtensions() == null) {
					concept.setExtensions(new ArrayList<DrRefsetExtension>());
				}
				concept.getExtensions().add(extension);
			}
		}
	}

	private static void addAnnotationsToDescription(DrDescription description, DescriptionVersionBI componentBi, 
			ViewCoordinate mockVc, String factContextName) throws IOException {
		I_TermFactory tf = Terms.get();
		Collection<? extends RefexVersionBI<?>> annotations = componentBi.getAnnotationsActive(mockVc);

		for (RefexVersionBI annotation : annotations) {
			DrRefsetExtension extension = new DrRefsetExtension();
			extension.setActive(true);
			extension.setComponentUuid(tf.nidToUuid(annotation.getReferencedComponentNid()).toString());
			extension.setRefsetUuid(tf.nidToUuid(annotation.getRefexNid()).toString());
			extension.setPrimordialUuid(annotation.getPrimUuid().toString());
			extension.setFactContextName(factContextName);                
			if (annotation instanceof RefexNidVersionBI) {
				RefexNidVersionBI annotationTyped = (RefexNidVersionBI) annotation;
				extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
				extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
				extension.setTime(annotationTyped.getTime());
				extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
			} else if (annotation instanceof RefexNidNidNidVersionBI) {
				RefexNidNidNidVersionBI annotationTyped = (RefexNidNidNidVersionBI) annotation;
				extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
				extension.setC2Uuid(tf.nidToUuid(annotationTyped.getNid2()).toString());
				extension.setC3Uuid(tf.nidToUuid(annotationTyped.getNid3()).toString());
				extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
				extension.setTime(annotationTyped.getTime());
				extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
			} else if (annotation instanceof RefexNidNidStringVersionBI) {
				RefexNidNidStringVersionBI annotationTyped = (RefexNidNidStringVersionBI) annotation;
				extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
				extension.setC2Uuid(tf.nidToUuid(annotationTyped.getNid2()).toString());
				extension.setStrValue(annotationTyped.getString1());
				extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
				extension.setTime(annotationTyped.getTime());
				extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
			} else if (annotation instanceof RefexNidNidVersionBI) {
				RefexNidNidVersionBI annotationTyped = (RefexNidNidVersionBI) annotation;
				extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
				extension.setC2Uuid(tf.nidToUuid(annotationTyped.getNid2()).toString());
				extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
				extension.setTime(annotationTyped.getTime());
				extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
			} else if (annotation instanceof RefexNidFloatVersionBI) {
				RefexNidFloatVersionBI annotationTyped = (RefexNidFloatVersionBI) annotation;
				extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
				extension.setFloatValue(annotationTyped.getFloat1());
				extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
				extension.setTime(annotationTyped.getTime());
				extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
			} else if (annotation instanceof RefexNidIntVersionBI) {
				RefexNidIntVersionBI annotationTyped = (RefexNidIntVersionBI) annotation;
				extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
				extension.setIntValue(annotationTyped.getInt1());
				extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
				extension.setTime(annotationTyped.getTime());
				extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
			} else if (annotation instanceof RefexNidLongVersionBI) {
				RefexNidLongVersionBI annotationTyped = (RefexNidLongVersionBI) annotation;
				extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
				extension.setLongValue(annotationTyped.getLong1());
				extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
				extension.setTime(annotationTyped.getTime());
				extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
			} else if (annotation instanceof RefexNidStringVersionBI) {
				RefexNidStringVersionBI annotationTyped = (RefexNidStringVersionBI) annotation;
				extension.setC1Uuid(tf.nidToUuid(annotationTyped.getNid1()).toString());
				extension.setStrValue(annotationTyped.getString1());
				extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
				extension.setTime(annotationTyped.getTime());
				extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
			} else if (annotation instanceof RefexFloatVersionBI) {
				RefexFloatVersionBI annotationTyped = (RefexFloatVersionBI) annotation;
				extension.setFloatValue(annotationTyped.getFloat1());
				extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
				extension.setTime(annotationTyped.getTime());
				extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
			} else if (annotation instanceof RefexIntVersionBI) {
				RefexIntVersionBI annotationTyped = (RefexIntVersionBI) annotation;
				extension.setIntValue(annotationTyped.getInt1());
				extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
				extension.setTime(annotationTyped.getTime());
				extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
			} else if (annotation instanceof RefexLongVersionBI) {
				RefexLongVersionBI annotationTyped = (RefexLongVersionBI) annotation;
				extension.setLongValue(annotationTyped.getLong1());
				extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
				extension.setTime(annotationTyped.getTime());
				extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
			} else if (annotation instanceof RefexStringVersionBI) {
				RefexStringVersionBI annotationTyped = (RefexStringVersionBI) annotation;
				extension.setStrValue(annotationTyped.getString1());
				extension.setStatusUuid(tf.nidToUuid(annotationTyped.getStatusNid()).toString());
				extension.setTime(annotationTyped.getTime());
				extension.setAuthorUuid(tf.nidToUuid(annotationTyped.getAuthorNid()).toString());
			} else {
				// unknown refset
			}
			if (description.getExtensions() == null) {
				description.setExtensions(new ArrayList<DrRefsetExtension>());
			}
			description.getExtensions().add(extension);
		}
	}

}
