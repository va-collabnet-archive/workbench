package org.dwfa.ace.task.commit;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

public class AbortExtension implements I_Fixup {
	I_ThinExtByRefVersioned extension;
	private String textForOption;

	public AbortExtension(I_ThinExtByRefVersioned extension, String textForOption) {
		super();
		this.extension = extension;
		this.textForOption = textForOption;
	}

	public void fix() throws Exception {
        I_TermFactory tf = LocalVersionedTerminology.get();
    	((I_Transact) tf.getExtensionWrapper(extension.getMemberId())).abort();
	}
	
	public String toString() {
		return textForOption;
	}

}
