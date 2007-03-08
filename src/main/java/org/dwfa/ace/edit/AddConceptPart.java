package org.dwfa.ace.edit;

import java.awt.event.ActionEvent;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_ContainTermComponent;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.ThinConPart;

public class AddConceptPart extends AddComponent {

	public AddConceptPart(I_ContainTermComponent termContainer, AceFrameConfig config) {
		super(termContainer, config);
	}

	@Override
	protected void doEdit(I_ContainTermComponent termContainer, ActionEvent e, AceFrameConfig config)
			throws Exception {
		ConceptBean cb = (ConceptBean) termContainer.getTermComponent();
		for (Path p: termContainer.getConfig().getEditingPathSet()) {
			ThinConPart part = new ThinConPart();
			part.setVersion(Integer.MAX_VALUE);
			part.setPathId(p.getConceptId());
			part.setDefined(false);
			part.setConceptStatus(AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
			cb.getConcept().getVersions().add(part);
		}
		ACE.addUncommitted(cb);
		termContainer.setTermComponent(cb);
	}
}
