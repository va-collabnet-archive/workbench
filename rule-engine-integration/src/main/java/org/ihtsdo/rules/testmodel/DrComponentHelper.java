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
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.qa.inheritance.RelationshipsDAO;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.RulesLibrary.INFERRED_VIEW_ORIGIN;
import org.ihtsdo.testmodel.DrConcept;
import org.ihtsdo.testmodel.DrDefiningRolesSet;
import org.ihtsdo.testmodel.DrDescription;
import org.ihtsdo.testmodel.DrLanguageDesignationSet;
import org.ihtsdo.testmodel.DrRelationship;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ConceptSpec;

public class DrComponentHelper {

	public static I_IntSet allRels;
	public static I_IntSet histRels;
	public static I_IntSet CptModelRels;

	public static DrConcept getDrConcept(ConceptVersionBI conceptBi, String factContextName, 
			INFERRED_VIEW_ORIGIN inferredOrigin) {
		I_TermFactory tf = Terms.get();
		TerminologyStoreDI ts = Ts.get();
		DrConcept concept = new DrConcept();

		try {
			I_GetConceptData oldStyleConcept = tf.getConcept(conceptBi.getNid());
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();

			List<? extends I_ConceptAttributeTuple> attributeTuples = oldStyleConcept.getConceptAttributeTuples(null, 
					config.getViewPositionSetReadOnly(), config.getPrecedence(), 
					config.getConflictResolutionStrategy());

			if (attributeTuples != null && !attributeTuples.isEmpty()) {
				I_ConceptAttributeTuple attributeTuple = attributeTuples.iterator().next();
				concept.setDefined(attributeTuple.isDefined());
				concept.setPathUuid(tf.nidToUuid(attributeTuple.getPathNid()).toString());
				concept.setPrimordialUuid(attributeTuple.getPrimUuid().toString());
				concept.setStatusUuid(tf.nidToUuid(attributeTuple.getStatusNid()).toString());
				concept.setTime(attributeTuple.getTime());
				concept.setFactContextName(factContextName);
			}

			Collection<? extends DescriptionVersionBI> descriptionsList = oldStyleConcept.getDescriptionTuples(null, 
					null, config.getViewPositionSetReadOnly(), 
					config.getPrecedence(), config.getConflictResolutionStrategy());

			HashMap<Integer,DrLanguageDesignationSet> languageDesignationSetsMap = new HashMap<Integer,DrLanguageDesignationSet>();

			ConceptSpec referToRefset = new ConceptSpec("REFERS TO concept association reference set (foundation metadata concept)", UUID.fromString("d15fde65-ed52-3a73-926b-8981e9743ee9"));

			for (DescriptionVersionBI descriptionVersion : descriptionsList) {
				Collection<? extends RefexVersionBI<?>> currentAnnotations = descriptionVersion.getChronicle().getCurrentAnnotations(config.getViewCoordinate());
				for (RefexVersionBI<?> annotation : currentAnnotations) {
					RefexCnidVersionBI annotationCnid = (RefexCnidVersionBI) annotation;
					int languageNid = annotationCnid.getCollectionNid();
					if (!languageDesignationSetsMap.containsKey(languageNid) && annotationCnid.getCollectionNid() != referToRefset.getLenient().getNid()) {
						DrLanguageDesignationSet langDefSet = new DrLanguageDesignationSet();
						langDefSet.setLanguageRefsetUuid(tf.nidToUuid(annotationCnid.getCollectionNid()).toString());
						languageDesignationSetsMap.put(languageNid, langDefSet);
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
				loopDescription.setStatusUuid(tf.nidToUuid(descriptionVersion.getStatusNid()).toString());
				loopDescription.setPathUuid(tf.nidToUuid(descriptionVersion.getPathNid()).toString());
				loopDescription.setPrimordialUuid(descriptionVersion.getPrimUuid().toString());
				loopDescription.setTypeUuid(tf.nidToUuid(descriptionVersion.getTypeNid()).toString());
				loopDescription.setPublished(!getSnomedIntId(descriptionVersion.getNid()).equals("0"));
				loopDescription.setFactContextName(factContextName);

				Collection<? extends RefexVersionBI<?>> currentAnnotations = descriptionVersion.getChronicle().getCurrentAnnotations(config.getViewCoordinate());
				for (RefexVersionBI<?> annotation : currentAnnotations) {
					try {
						RefexCnidVersionBI annotationCnid = (RefexCnidVersionBI) annotation;
						if (annotationCnid.getCollectionNid() == referToRefset.getLenient().getNid()) {
							loopDescription.setReferToConceptUuid(tf.nativeToUuid(annotationCnid.getCnid1()).iterator().next().toString());
						} else {
							DrDescription langDescription = new DrDescription();
							langDescription.setAuthorUuid(tf.nidToUuid(descriptionVersion.getAuthorNid()).toString());
							langDescription.setConceptUuid(tf.nidToUuid(descriptionVersion.getConceptNid()).toString());
							langDescription.setInitialCaseSignificant(descriptionVersion.isInitialCaseSignificant());
							langDescription.setLang(descriptionVersion.getLang());
							langDescription.setText(descriptionVersion.getText());
							langDescription.setTime(descriptionVersion.getTime());
							langDescription.setStatusUuid(tf.nidToUuid(descriptionVersion.getStatusNid()).toString());
							langDescription.setPathUuid(tf.nidToUuid(descriptionVersion.getPathNid()).toString());
							langDescription.setPrimordialUuid(descriptionVersion.getPrimUuid().toString());
							langDescription.setTypeUuid(tf.nidToUuid(descriptionVersion.getTypeNid()).toString());
							langDescription.setFactContextName(factContextName);

							int languageNid = annotationCnid.getCollectionNid();
							DrLanguageDesignationSet langDefSet = languageDesignationSetsMap.get(languageNid);
							langDescription.setAcceptabilityUuid(tf.nidToUuid(annotationCnid.getCnid1()).toString());
							langDescription.setLanguageRefsetUuid(tf.nidToUuid(annotationCnid.getCollectionNid()).toString());
							langDefSet.getDescriptions().add(langDescription);
						}
					} catch (Exception e) {
						// not cnid annotation, ignore
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
						config.getViewPositionSetReadOnly(), config.getPrecedence(), 
						config.getConflictResolutionStrategy())) {
					if (relTuple.getCharacteristicNid() == stated) {
						DrRelationship loopRel = new DrRelationship();
						loopRel.setModifierUuid("someUuid");
						loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
						loopRel.setSourceUuid(tf.nidToUuid(relTuple.getOriginNid()).toString());
						loopRel.setTargetUuid(tf.nidToUuid(relTuple.getDestinationNid()).toString());
						loopRel.setCharacteristicUuid(tf.nidToUuid(relTuple.getCharacteristicNid()).toString());
						loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
						loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
						loopRel.setRelGroup(relTuple.getGroup());
						loopRel.setStatusUuid(tf.nidToUuid(relTuple.getStatusNid()).toString());
						loopRel.setTime(relTuple.getTime());
						loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
						loopRel.setFactContextName(factContextName);
						loopRel.setPublished(!getSnomedIntId(relTuple.getNid()).equals("0"));
						concept.getOutgoingRelationships().add(loopRel);
					}
				}
			} else if (inferredOrigin == INFERRED_VIEW_ORIGIN.INFERRED) {
				for (RelationshipVersionBI<?> relTuple : oldStyleConcept.getSourceRelTuples(null, 
						null, 
						config.getViewPositionSetReadOnly(), config.getPrecedence(), 
						config.getConflictResolutionStrategy())) {
					if (relTuple.getCharacteristicNid() == inferred) {
						DrRelationship loopRel = new DrRelationship();
						loopRel.setModifierUuid("someUuid");
						loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
						loopRel.setSourceUuid(tf.nidToUuid(relTuple.getOriginNid()).toString());
						loopRel.setTargetUuid(tf.nidToUuid(relTuple.getDestinationNid()).toString());
						loopRel.setCharacteristicUuid(tf.nidToUuid(relTuple.getCharacteristicNid()).toString());
						loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
						loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
						loopRel.setRelGroup(relTuple.getGroup());
						loopRel.setStatusUuid(tf.nidToUuid(relTuple.getStatusNid()).toString());
						loopRel.setTime(relTuple.getTime());
						loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
						loopRel.setFactContextName(factContextName);
						loopRel.setPublished(!getSnomedIntId(relTuple.getNid()).equals("0"));
						concept.getOutgoingRelationships().add(loopRel);
					}
				}
			}else if (inferredOrigin == INFERRED_VIEW_ORIGIN.CONSTRAINT_NORMAL_FORM) {
				RelationshipsDAO rDao=new RelationshipsDAO();
				concept.getOutgoingRelationships().addAll(rDao.getConstraintNormalForm(oldStyleConcept, factContextName));
				rDao=null;
				System.gc();
			}

			DrDefiningRolesSet statedRolesSet = new DrDefiningRolesSet();
			statedRolesSet.setRolesSetType("Stated");

			DrDefiningRolesSet inferredRolesSet = new DrDefiningRolesSet();
			inferredRolesSet.setRolesSetType("Inferred");

			DrDefiningRolesSet modelersRolesSet = new DrDefiningRolesSet();
			modelersRolesSet.setRolesSetType("Modelers");
			
			DrDefiningRolesSet definingFormRolesSet = new DrDefiningRolesSet();
			modelersRolesSet.setRolesSetType("Defining");

			for (RelationshipVersionBI relTuple : oldStyleConcept.getSourceRelTuples(null, 
					null, 
					config.getViewPositionSetReadOnly(), config.getPrecedence(), 
					config.getConflictResolutionStrategy())) {
				DrRelationship loopRel = new DrRelationship();
				loopRel.setModifierUuid("someUuid");
				loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
				loopRel.setSourceUuid(tf.nidToUuid(relTuple.getOriginNid()).toString());
				loopRel.setTargetUuid(tf.nidToUuid(relTuple.getDestinationNid()).toString());
				loopRel.setCharacteristicUuid(tf.nidToUuid(relTuple.getCharacteristicNid()).toString());
				loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
				loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
				loopRel.setRelGroup(relTuple.getGroup());
				loopRel.setStatusUuid(tf.nidToUuid(relTuple.getStatusNid()).toString());
				loopRel.setTime(relTuple.getTime());
				loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
				loopRel.setFactContextName(factContextName);
				loopRel.setPublished(!getSnomedIntId(relTuple.getNid()).equals("0"));

				if (relTuple.getCharacteristicNid() == historical && 
						config.getAllowedStatus().contains(relTuple.getStatusNid())) {
					concept.getOutgoingRelationships().add(loopRel);
				}

				if (relTuple.getCharacteristicNid() == stated && 
						config.getAllowedStatus().contains(relTuple.getStatusNid())) {
					statedRolesSet.getRelationships().add(loopRel);
				}

				if (relTuple.getCharacteristicNid() != inferred) {
					modelersRolesSet.getRelationships().add(loopRel);
				}

				if (relTuple.getCharacteristicNid() == inferred && 
						config.getAllowedStatus().contains(relTuple.getStatusNid())) {
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
			//					config.getViewPositionSetReadOnly(), config.getPrecedence(), 
			//					config.getConflictResolutionStrategy())) {
			//				if (relTuple.getCharacteristicNid() == historical) {
			//					DrRelationship loopRel = new DrRelationship();
			//					loopRel.setModifierUuid("someUuid");
			//					loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
			//					loopRel.setSourceUuid(tf.nidToUuid(relTuple.getOriginNid()).toString());
			//					loopRel.setTargetUuid(tf.nidToUuid(relTuple.getDestinationNid()).toString());
			//					loopRel.setCharacteristicUuid(tf.nidToUuid(relTuple.getCharacteristicNid()).toString());
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

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}

		return concept;

	}

	public static Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			childrenSet.addAll(concept.getDestRelOrigins(config.getAllowedStatus(), 
					config.getDestRelTypes(), config.getViewPositionSetReadOnly()
					, config.getPrecedence(), config.getConflictResolutionStrategy()));
			descendants.addAll(childrenSet);
			for (I_GetConceptData loopConcept : childrenSet) {
				descendants = getDescendants(descendants, loopConcept);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return descendants;
	}
	
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

}
