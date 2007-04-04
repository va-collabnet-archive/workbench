package org.dwfa.ace.edit;

import java.awt.event.ActionEvent;
import java.util.UUID;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelVersioned;

public class AddRelationship extends AddComponent {

	public AddRelationship(I_ContainTermComponent termContainer, I_ConfigAceFrame config) {
		super(termContainer, config);
	}
	
	@Override
	protected void doEdit(I_ContainTermComponent termContainer, ActionEvent e,
			I_ConfigAceFrame config) throws Exception {
		ConceptBean cb = (ConceptBean) termContainer.getTermComponent();
		UUID newRelUid = UUID.randomUUID();
    	int idSource = AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());
		int relId = AceConfig.vodb.uuidToNativeWithGeneration(newRelUid, idSource,
				config.getEditingPathSet(), Integer.MAX_VALUE);
		ThinRelVersioned rel = new ThinRelVersioned(relId, cb.getConceptId(), config.getHierarchySelection().getConceptId(),
				1);
		ThinRelPart relPart = new ThinRelPart();
		rel.addVersion(relPart);
		int status = AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
		for (I_Path p: termContainer.getConfig().getEditingPathSet()) {
			relPart.setVersion(Integer.MAX_VALUE);
			relPart.setPathId(p.getConceptId());
			relPart.setStatusId(status);
			relPart.setRelTypeId(config.getDefaultRelationshipType().getConceptId());
			relPart.setCharacteristicId(config.getDefaultRelationshipCharacteristic().getConceptId());
			relPart.setRefinabilityId(config.getDefaultRelationshipRefinability().getConceptId());
			relPart.setGroup(0);
		}
		cb.getUncommittedSourceRels().add(rel);
		cb.getUncommittedIds().add(relId);
		ACE.addUncommitted(cb);
		termContainer.setTermComponent(cb);
	}

}
