package org.ihtsdo.project.refset;

import java.io.IOException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.api.PathBI;

public class AttributeValueRefset extends Refset {

	public AttributeValueRefset(I_GetConceptData refsetConcept) throws Exception {
		super();
		this.refsetConcept = refsetConcept;
		this.refsetName = refsetConcept.toString();
		this.refsetId = refsetConcept.getConceptNid();
		termFactory = Terms.get();
	}

	public I_GetConceptData getValue(int componentId, int key) throws IOException, TerminologyException {
		//TODO: move config to parameter
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		for (I_ExtendByRef attributeValueMember : termFactory.getAllExtensionsForComponent(componentId, true)) {
			if (attributeValueMember.getRefsetId() == this.refsetId) {
				long lastVersion = Long.MIN_VALUE;
				I_ExtendByRefPartCidCid attributeValueExtensionPart = null;
				for (I_ExtendByRefVersion loopTuple : attributeValueMember.getTuples(
						config.getConflictResolutionStrategy())) {
					if (loopTuple.getTime() >= lastVersion) {
						lastVersion = loopTuple.getTime();
						attributeValueExtensionPart = (I_ExtendByRefPartCidCid) loopTuple.getMutablePart();
					}
				}
				if (attributeValueExtensionPart.getC1id() ==  key) {
					I_GetConceptData value = termFactory.getConcept(attributeValueExtensionPart.getC2id());
					return value;
				}
			}
		}
		return null;
	}

	public void putValue(int componentId, int key, int value) throws Exception {
		//TODO: move config to parameter
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		boolean keyAlreadyPresent = false;
		for (I_ExtendByRef attributeValueMember : termFactory.getAllExtensionsForComponent(componentId)) {
			if (attributeValueMember.getRefsetId() == this.refsetId) {
				keyAlreadyPresent = true;
				long lastVersion = Long.MIN_VALUE;
				I_ExtendByRefPartCidCid attributeValueExtensionPart = null;
				for (I_ExtendByRefVersion loopTuple : attributeValueMember.getTuples(
						config.getConflictResolutionStrategy())) {
					if (loopTuple.getTime() >= lastVersion) {
						lastVersion = loopTuple.getTime();
						attributeValueExtensionPart = (I_ExtendByRefPartCidCid) loopTuple.getMutablePart();
					}
				}
				if (attributeValueExtensionPart.getC1id() ==  key) {
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartCidCid newAttributeValuePart =(I_ExtendByRefPartCidCid) 
						attributeValueExtensionPart.makeAnalog(
								ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(), 
								editPath.getConceptNid(), 
								Long.MAX_VALUE);
						newAttributeValuePart.setC2id(value);
						attributeValueMember.addVersion(newAttributeValuePart);
					}
					termFactory.addUncommittedNoChecks(attributeValueMember);
					return;
				}
			}
		}

		if (!keyAlreadyPresent) {
			I_HelpRefsets refsetHelper = termFactory.getRefsetHelper(config);
			refsetHelper.newRefsetExtension(this.refsetId, 
					componentId, EConcept.REFSET_TYPES.CID_CID, 
					new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, key).with(
							RefsetPropertyMap.REFSET_PROPERTY.CID_TWO, value), config);

			//termFactory.commit();
		}

		return;
	}
}
