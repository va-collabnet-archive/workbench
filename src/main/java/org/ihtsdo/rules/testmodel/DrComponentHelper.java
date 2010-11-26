package org.ihtsdo.rules.testmodel;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.testmodel.DrConcept;
import org.ihtsdo.testmodel.DrDefiningRolesSet;
import org.ihtsdo.testmodel.DrDescription;
import org.ihtsdo.testmodel.DrRelationship;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class DrComponentHelper {

	public static DrConcept getDrConcept(ConceptVersionBI conceptBi, String factContextName) {
		I_TermFactory tf = Terms.get();
		DrConcept concept = new DrConcept();

		try {
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

			for (RelationshipVersionBI relTuple : conceptBi.getRelsOutgoingActive()) {
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
				// TODO: discriminate between inferred and stated
				statedRolesSet.getRelationships().add(loopRel);
				inferredRolesSet.getRelationships().add(loopRel);
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
		}

		return concept;

	}

}
