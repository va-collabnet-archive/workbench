package org.ihtsdo.rules.testmodel;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
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
import org.ihtsdo.testmodel.DrRelationship;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class DrComponentHelper {

	public static I_IntSet allRels;
	public static I_IntSet histRels;
	public static I_IntSet CptModelRels;

	@SuppressWarnings("unchecked")
	public static DrConcept getDrConcept(ConceptVersionBI conceptBi, String factContextName, 
			INFERRED_VIEW_ORIGIN inferredOrigin) {
		I_TermFactory tf = Terms.get();
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

			DrDefiningRolesSet statedRolesSet = new DrDefiningRolesSet();
			statedRolesSet.setRolesSetType("Stated");

			DrDefiningRolesSet inferredRolesSet = new DrDefiningRolesSet();
			inferredRolesSet.setRolesSetType("Inferred");

			//TODO int identifiers = Ts.get().get

			for (DescriptionVersionBI descriptionVersion : conceptBi.getDescsActive()) {
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
			}

			if (allRels == null) {
				allRels =RulesLibrary.getAllRels();
			}

			int defining = tf.uuidToNative(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
			int stated = tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
			int statedAndInferred = tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_INFERRED_RELATIONSHIP.getUids());

			if (inferredOrigin == INFERRED_VIEW_ORIGIN.CLASSIFIER) {
				for (RelationshipVersionBI<?> relTuple : oldStyleConcept.getSourceRelTuples(config.getAllowedStatus(), 
						null, 
						config.getViewPositionSetReadOnly(), config.getPrecedence(), 
						config.getConflictResolutionStrategy(), config.getClassifierConcept().getNid(), 
						RelAssertionType.INFERRED)) {
					if (relTuple.getCharacteristicNid() == defining ||
							relTuple.getCharacteristicNid() == stated ||
							relTuple.getCharacteristicNid() == statedAndInferred) {
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
						inferredRolesSet.getRelationships().add(loopRel);
					}
				}
			} else if (inferredOrigin == INFERRED_VIEW_ORIGIN.FULL) {
				for (RelationshipVersionBI<?> relTuple : oldStyleConcept.getSourceRelTuples(config.getAllowedStatus(), 
						null, 
						config.getViewPositionSetReadOnly(), config.getPrecedence(), 
						config.getConflictResolutionStrategy())) {
					if (relTuple.getCharacteristicNid() == defining ||
							relTuple.getCharacteristicNid() == stated ||
							relTuple.getCharacteristicNid() == statedAndInferred) {
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
						inferredRolesSet.getRelationships().add(loopRel);
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
					inferredRolesSet.getRelationships().add(loopRel);
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
						inferredRolesSet.getRelationships().add(loopRel);
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
					inferredRolesSet.getRelationships().add(loopRel);
				}
				//historical rels
				if (histRels==null){
					histRels=RulesLibrary.getHistoricalRels();
				}
				for (RelationshipVersionBI relTuple : oldStyleConcept.getSourceRelTuples(config.getAllowedStatus(), 
						histRels, 
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
					concept.getOutgoingRelationships().add(loopRel);
					inferredRolesSet.getRelationships().add(loopRel);
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
						inferredRolesSet.getRelationships().add(loopRel);
					}
				}
			}

//			for (RelationshipVersionBI relTuple : oldStyleConcept.getSourceRelTuples(config.getAllowedStatus(), 
//					allRels, 
//					config.getViewPositionSetReadOnly(), config.getPrecedence(), 
//					config.getConflictResolutionStrategy(), config.getClassifierConcept().getNid(), 
//					RelAssertionType.STATED)) {
//				DrRelationship loopRel = new DrRelationship();
//				loopRel.setModifierUuid("someUuid");
//				loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
//				loopRel.setSourceUuid(tf.nidToUuid(relTuple.getOriginNid()).toString());
//				loopRel.setTargetUuid(tf.nidToUuid(relTuple.getDestinationNid()).toString());
//				loopRel.setCharacteristicUuid(tf.nidToUuid(relTuple.getCharacteristicNid()).toString());
//				loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
//				loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
//				loopRel.setRelGroup(relTuple.getGroup());
//				loopRel.setStatusUuid(tf.nidToUuid(relTuple.getStatusNid()).toString());
//				loopRel.setTime(relTuple.getTime());
//				loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
//				loopRel.setFactContextName(factContextName);
//				concept.getOutgoingRelationships().add(loopRel);
//				statedRolesSet.getRelationships().add(loopRel);
//			}
//
//			concept.getDefiningRoleSets().add(statedRolesSet);
//			concept.getDefiningRoleSets().add(inferredRolesSet);

			// TODO: incoming rels is heavy on performance, evaluate requirements
			//			for (RelationshipVersionBI relTuple : conceptBi.getRelsIncomingActive()) {
			//				DrRelationship loopRel = new DrRelationship();
			//				loopRel.setModifierUuid("someUuid");
			//				loopRel.setAuthorUuid(tf.nidToUuid(relTuple.getAuthorNid()).toString());
			//				loopRel.setSourceUuid(tf.nidToUuid(relTuple.getOriginNid()).toString());
			//				loopRel.setTargetUuid(tf.nidToUuid(relTuple.getDestinationNid()).toString());
			//				loopRel.setCharacteristicUuid(tf.nidToUuid(relTuple.getCharacteristicNid()).toString());
			//				loopRel.setPathUuid(tf.nidToUuid(relTuple.getPathNid()).toString());
			//				loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
			//				loopRel.setRelGroup(relTuple.getGroup());
			//				loopRel.setStatusUuid(tf.nidToUuid(relTuple.getStatusNid()).toString());
			//				loopRel.setTime(relTuple.getTime());
			//				loopRel.setTypeUuid(tf.nidToUuid(relTuple.getTypeNid()).toString());
			//				loopRel.setFactContextName(factContextName);
			//				concept.getIncomingRelationships().add(loopRel);
			//			}

			//TODO: implement extensions filler

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ContraditionException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return concept;

	}

	public static Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			I_TermFactory termFactory = Terms.get();
			//TODO: get config as parameter
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return descendants;
	}
}
