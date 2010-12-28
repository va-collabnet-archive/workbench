package org.ihtsdo.rules.testmodel;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
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
				allRels = tf.newIntSet();
				allRels.addAll(config.getDestRelTypes().getSetValues());
				Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
				descendants = getDescendants(descendants, tf.getConcept(UUID.fromString("f323b5dd-1f97-3873-bcbc-3563663dda14")));
				descendants = getDescendants(descendants, tf.getConcept(UUID.fromString("6155818b-09ed-388e-82ce-caa143423e99")));
				for (I_GetConceptData loopConcept : descendants) {
					allRels.add(loopConcept.getNid());
				}
			}

			if (inferredOrigin == INFERRED_VIEW_ORIGIN.CLASSIFIER) {
				for (RelationshipVersionBI relTuple : oldStyleConcept.getSourceRelTuples(config.getAllowedStatus(), 
						allRels, 
						config.getViewPositionSetReadOnly(), config.getPrecedence(), 
						config.getConflictResolutionStrategy(), config.getClassifierConcept().getNid(), 
						RelAssertionType.INFERRED)) {
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
			} else if (inferredOrigin == INFERRED_VIEW_ORIGIN.CONSTRAINTED_NORMAL_FORM) {
				// TODO implement CONSTRAINTED_NORMAL_FORM calculation
				// Temporary implementation using classifier inferred view from database
				for (RelationshipVersionBI relTuple : oldStyleConcept.getSourceRelTuples(config.getAllowedStatus(), 
						allRels, 
						config.getViewPositionSetReadOnly(), config.getPrecedence(), 
						config.getConflictResolutionStrategy(), config.getClassifierConcept().getNid(), 
						RelAssertionType.INFERRED)) {
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

			for (RelationshipVersionBI relTuple : oldStyleConcept.getSourceRelTuples(config.getAllowedStatus(), 
					allRels, 
					config.getViewPositionSetReadOnly(), config.getPrecedence(), 
					config.getConflictResolutionStrategy(), config.getClassifierConcept().getNid(), 
					RelAssertionType.STATED)) {
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
				statedRolesSet.getRelationships().add(loopRel);
			}

			concept.getDefiningRoleSets().add(statedRolesSet);
			concept.getDefiningRoleSets().add(inferredRolesSet);

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
			I_TermFactory termFactory = LocalVersionedTerminology.get();
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
