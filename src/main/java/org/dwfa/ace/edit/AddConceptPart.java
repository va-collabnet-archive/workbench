package org.dwfa.ace.edit;

import java.awt.event.ActionEvent;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinConPart;

public class AddConceptPart extends AddComponent {

	public AddConceptPart(I_ContainTermComponent termContainer, I_ConfigAceFrame config) {
		super(termContainer, config);
	}

	@Override
	protected void doEdit(I_ContainTermComponent termContainer, ActionEvent e, I_ConfigAceFrame config)
			throws Exception {
		ConceptBean cb = (ConceptBean) termContainer.getTermComponent();
		for (I_Path p: termContainer.getConfig().getEditingPathSet()) {
			ThinConPart part = new ThinConPart();
			part.setVersion(Integer.MAX_VALUE);
			part.setPathId(p.getConceptId());
			part.setDefined(false);
			part.setConceptStatus(AceConfig.getVodb().uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
			cb.getConceptAttributes().getVersions().add(part);
		}
		ACE.addUncommitted(cb);
		termContainer.setTermComponent(cb);
	}
}
