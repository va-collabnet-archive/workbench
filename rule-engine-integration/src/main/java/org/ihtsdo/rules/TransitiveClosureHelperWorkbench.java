package org.ihtsdo.rules;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.helper.TransitiveClosureHelperMock;

public class TransitiveClosureHelperWorkbench extends TransitiveClosureHelperMock {

	public TransitiveClosureHelperWorkbench() {
		super();
	}
	
	public boolean isParentOf(UUID parent, UUID subtype) throws TerminologyException, IOException {
		//TODO add config as parameter
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_GetConceptData wbParent = Terms.get().getConcept(parent);
		I_GetConceptData wbSubtype = Terms.get().getConcept(subtype);
		return wbParent.isParentOf(wbSubtype);
	}

	public boolean isParentOfOrEqualTo(UUID parent, UUID subtype) throws TerminologyException, IOException {
		//TODO add config as parameter
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_GetConceptData wbParent = Terms.get().getConcept(parent);
		I_GetConceptData wbSubtype = Terms.get().getConcept(subtype);
		return wbParent.isParentOfOrEqualTo(wbSubtype);
	}

	public boolean isParentOf(String parents, UUID subtype) throws Exception {
		//TODO add config as parameter
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		boolean result = false;
		I_GetConceptData wbSubtype = Terms.get().getConcept(subtype);
		String[] parentsStrings = parents.trim().split(",");
		for (String parentString : parentsStrings) {
			I_GetConceptData wbParent = Terms.get().getConcept(UUID.fromString(parentString.trim()));
			if (wbParent.isParentOf(wbSubtype)) result = true;
		}
		return result;
	}

	public boolean isParentOfOrEqualTo(String parents, UUID subtype)
			throws Exception {
		//TODO add config as parameter
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		boolean result = false;
		I_GetConceptData wbSubtype = Terms.get().getConcept(subtype);
		String[] parentsStrings = parents.trim().split(",");
		for (String parentString : parentsStrings) {
			I_GetConceptData wbParent = Terms.get().getConcept(UUID.fromString(parentString.trim()));
			if (wbParent.isParentOfOrEqualTo(wbSubtype)) result = true;
		}
		return result;
	}

}
