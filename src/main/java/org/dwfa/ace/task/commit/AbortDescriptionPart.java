package org.dwfa.ace.task.commit;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;

public class AbortDescriptionPart implements I_Fixup {
	I_GetConceptData concept;
	I_DescriptionVersioned desc;
	I_DescriptionPart part;
	
	public AbortDescriptionPart(I_GetConceptData concept,
			I_DescriptionVersioned desc, I_DescriptionPart part) {
		super();
		this.concept = concept;
		this.desc = desc;
		this.part = part;
	}

	public void fix() throws Exception {
        I_TermFactory tf = LocalVersionedTerminology.get();
		desc.getVersions().remove(part);
		tf.addUncommitted(concept);
		AceLog.getAppLog().info("Aborted add desc part: " + part);
	}
	
	public String toString() {
		return "remove " + part.getText();
	}

}
