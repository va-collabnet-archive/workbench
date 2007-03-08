package org.dwfa.ace.edit;

import java.awt.event.ActionEvent;
import java.util.UUID;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_ContainTermComponent;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.ThinDescPart;
import org.dwfa.vodb.types.ThinDescVersioned;

public class AddDescription extends AddComponent {

	public AddDescription(I_ContainTermComponent termContainer, AceFrameConfig config) {
		super(termContainer, config);
	}

	@Override
	protected void doEdit(I_ContainTermComponent termContainer, ActionEvent e, AceFrameConfig config)
			throws Exception {
		ConceptBean cb = (ConceptBean) termContainer.getTermComponent();
		UUID newDescUid = UUID.randomUUID();
    	int idSource = AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());
		int descId = AceConfig.vodb.uuidToNativeWithGeneration(newDescUid, idSource,
				config.getEditingPathSet(), Integer.MAX_VALUE);
		ThinDescVersioned desc = new ThinDescVersioned(descId, cb.getConceptId(), 1);
		ThinDescPart descPart = new ThinDescPart();
		desc.addVersion(descPart);
		boolean capStatus = false;
		String lang = "en";
		int status = AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
		int typeId = AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
		String text = "New Description";
		for (Path p: termContainer.getConfig().getEditingPathSet()) {
			descPart.setVersion(Integer.MAX_VALUE);
			descPart.setPathId(p.getConceptId());
			descPart.setInitialCaseSignificant(capStatus);
			descPart.setLang(lang);
			descPart.setStatusId(status);
			descPart.setText(text);
			descPart.setTypeId(typeId);
		}
		cb.getUncommittedDescriptions().add(desc);
		cb.getUncommittedIds().add(descId);
		ACE.addUncommitted(cb);
		termContainer.setTermComponent(cb);
	}

}
