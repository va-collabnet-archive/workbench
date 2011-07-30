package org.ihtsdo.rules.testmodel;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.qa.inheritance.InheritedRelationships;
import org.ihtsdo.qa.inheritance.RelationshipsDAO;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.RulesLibrary.INFERRED_VIEW_ORIGIN;
import org.ihtsdo.testmodel.DrConcept;
import org.ihtsdo.testmodel.DrDefiningRolesSet;
import org.ihtsdo.testmodel.DrDescription;
import org.ihtsdo.testmodel.DrLanguageDesignationSet;
import org.ihtsdo.testmodel.DrRelationship;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

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

			ConAttrVersionBI attributeTuple = conceptBi.getConAttrsActive();
			if (attributeTuple != null) {
				concept.setDefined(attributeTuple.isDefined());
				concept.setPathUuid(tf.nidToUuid(attributeTuple.getPathNid()).toString());
				concept.setPrimordialUuid(attributeTuple.getPrimUuid().toString());
				concept.setStatusUuid(tf.nidToUuid(attributeTuple.getStatusNid()).toString());
				concept.setTime(attributeTuple.getTime());
				concept.setFactContextName(factContextName);
			}

			Collection<? extends DescriptionVersionBI> descsActive = conceptBi.getDescsActive();

			HashMap<Integer,DrLanguageDesignationSet> languageDesignationSetsMap = new HashMap<Integer,DrLanguageDesignationSet>();
			
			for (DescriptionVersionBI descriptionVersion : descsActive) {
				Collection<? extends RefexVersionBI<?>> currentAnnotations = descriptionVersion.getChronicle().getCurrentAnnotations(config.getViewCoordinate());
				for (RefexVersionBI<?> annotation : currentAnnotations) {
					RefexCnidVersionBI annotationCnid = (RefexCnidVersionBI) annotation;
					int languageNid = annotationCnid.getCollectionNid();
					if (!languageDesignationSetsMap.containsKey(languageNid)) {
						DrLanguageDesignationSet langDefSet = new DrLanguageDesignationSet();
						langDefSet.setLanguageRefsetUuid(tf.nidToUuid(annotationCnid.getCollectionNid()).toString());
						languageDesignationSetsMap.put(languageNid, langDefSet);
					}
				}
			}

			for (DescriptionVersionBI descriptionVersion : descsActive) {
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
				loopDescription.setFactContextName(factContextName);
				concept.getDescriptions().add(loopDescription);

				Collection<? extends RefexVersionBI<?>> currentAnnotations = descriptionVersion.getChronicle().getCurrentAnnotations(config.getViewCoordinate());
				for (RefexVersionBI<?> annotation : currentAnnotations) {
					try {
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
						RefexCnidVersionBI annotationCnid = (RefexCnidVersionBI) annotation;
						int languageNid = annotationCnid.getCollectionNid();
						DrLanguageDesignationSet langDefSet = languageDesignationSetsMap.get(languageNid);
						langDescription.setAcceptabilityUuid(tf.nidToUuid(annotationCnid.getCnid1()).toString());
						langDescription.setLanguageRefsetUuid(tf.nidToUuid(annotationCnid.getCollectionNid()).toString());
						langDefSet.getDescriptions().add(langDescription);
					} catch (Exception e) {
						// not cnid annotation, ignore
					}
				}

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
				for (RelationshipVersionBI<?> relTuple : oldStyleConcept.getSourceRelTuples(config.getAllowedStatus(), 
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
						concept.getOutgoingRelationships().add(loopRel);
					}
				}
			} else if (inferredOrigin == INFERRED_VIEW_ORIGIN.INFERRED) {
				for (RelationshipVersionBI<?> relTuple : oldStyleConcept.getSourceRelTuples(config.getAllowedStatus(), 
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
						concept.getOutgoingRelationships().add(loopRel);
					}
				}
			}else if (inferredOrigin == INFERRED_VIEW_ORIGIN.CONSTRAINT_NORMAL_FORM) {
				// TODO implement CONSTRAINT_NORMAL_FORM calculation
				RelationshipsDAO rDao=new RelationshipsDAO();
				InheritedRelationships inhRel = rDao.getInheritedRelationships(oldStyleConcept);
				//Inherited single roles
				for (I_RelTuple relTuple:inhRel.getSingleRoles()){
					DrRelationship loopRel = new DrRelationship();
					loopRel.setModifierUuid("someUuid");
					loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
					loopRel.setSourceUuid(tf.nidToUuid(relTuple.getOriginNid()).toString());
					loopRel.setTargetUuid(tf.nidToUuid(relTuple.getDestinationNid()).toString());
					loopRel.setCharacteristicUuid(tf.nidToUuid(relTuple.getCharacteristicNid()).toString());
					loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
					loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
					loopRel.setRelGroup(0);
					loopRel.setStatusUuid(tf.nidToUuid(relTuple.getStatusNid()).toString());
					loopRel.setTime(relTuple.getTime());
					loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
					loopRel.setFactContextName(factContextName);
					concept.getOutgoingRelationships().add(loopRel);
				}
				//Inherited grouped roles
				int groupNr=0;
				for (I_RelTuple[] relTuples:inhRel.getRoleGroups()){
					groupNr++;
					for (I_RelTuple relTuple:relTuples){
						DrRelationship loopRel = new DrRelationship();
						loopRel.setModifierUuid("someUuid");
						loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
						loopRel.setSourceUuid(tf.nidToUuid(relTuple.getOriginNid()).toString());
						loopRel.setTargetUuid(tf.nidToUuid(relTuple.getDestinationNid()).toString());
						loopRel.setCharacteristicUuid(tf.nidToUuid(relTuple.getCharacteristicNid()).toString());
						loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
						loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
						loopRel.setRelGroup(groupNr);
						loopRel.setStatusUuid(tf.nidToUuid(relTuple.getStatusNid()).toString());
						loopRel.setTime(relTuple.getTime());
						loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
						loopRel.setFactContextName(factContextName);
						concept.getOutgoingRelationships().add(loopRel);
					}
				}
				//Is A's Stated
				List<I_RelTuple> relTuples=(List<I_RelTuple>) rDao.getStatedIsARels(oldStyleConcept);

				for (I_RelTuple relTuple:relTuples){
					DrRelationship loopRel = new DrRelationship();
					loopRel.setModifierUuid("someUuid");
					loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
					loopRel.setSourceUuid(tf.nidToUuid(relTuple.getOriginNid()).toString());
					loopRel.setTargetUuid(tf.nidToUuid(relTuple.getDestinationNid()).toString());
					loopRel.setCharacteristicUuid(tf.nidToUuid(relTuple.getCharacteristicNid()).toString());
					loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
					loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
					loopRel.setRelGroup(0);
					loopRel.setStatusUuid(tf.nidToUuid(relTuple.getStatusNid()).toString());
					loopRel.setTime(relTuple.getTime());
					loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
					loopRel.setFactContextName(factContextName);
					concept.getOutgoingRelationships().add(loopRel);
				}
				//Not defining rels
				if (CptModelRels==null){
					CptModelRels=RulesLibrary.getConceptModelRels();
				}
				for (RelationshipVersionBI relTuple : oldStyleConcept.getSourceRelTuples(config.getAllowedStatus(), 
						CptModelRels, 
						config.getViewPositionSetReadOnly(), config.getPrecedence(), 
						config.getConflictResolutionStrategy())) {
					if (!rDao.isDefiningChar(relTuple.getCharacteristicNid())){
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
						concept.getOutgoingRelationships().add(loopRel);
					}
				}
			}

			DrDefiningRolesSet statedRolesSet = new DrDefiningRolesSet();
			statedRolesSet.setRolesSetType("Stated");

			DrDefiningRolesSet inferredRolesSet = new DrDefiningRolesSet();
			inferredRolesSet.setRolesSetType("Inferred");
			
			DrDefiningRolesSet modelersRolesSet = new DrDefiningRolesSet();
			modelersRolesSet.setRolesSetType("Modelers");

			for (RelationshipVersionBI relTuple : oldStyleConcept.getSourceRelTuples(config.getAllowedStatus(), 
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

				if (relTuple.getCharacteristicNid() == historical) {
					concept.getOutgoingRelationships().add(loopRel);
				}

				if (relTuple.getCharacteristicNid() == stated) {
					statedRolesSet.getRelationships().add(loopRel);
				}
				
				if (relTuple.getCharacteristicNid() != inferred) {
					modelersRolesSet.getRelationships().add(loopRel);
				}
			}
			concept.getDefiningRoleSets().add(statedRolesSet);
			concept.getDefiningRoleSets().add(modelersRolesSet);

			//TODO: incoming rels is heavy on performance, only inserting incoming historical rels
			for (RelationshipVersionBI relTuple :  oldStyleConcept.getDestRelTuples(config.getAllowedStatus(), 
					null, 
					config.getViewPositionSetReadOnly(), config.getPrecedence(), 
					config.getConflictResolutionStrategy())) {
				if (relTuple.getCharacteristicNid() == historical) {
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
					concept.getIncomingRelationships().add(loopRel);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ContraditionException e) {
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
}
