package org.dwfa.mojo.refset.writers;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.tapi.TerminologyException;

public class ConceptRefsetWriter extends MemberRefsetWriter {

	@Override
	public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple part) throws TerminologyException, IOException {
		I_ThinExtByRefPartConcept conceptPart = (I_ThinExtByRefPartConcept) part.getPart();
		
		return super.formatRefsetLine(tf, part) + "\t"
					+ toId(tf, conceptPart.getConceptId());
	}

	@Override
	public String getHeaderLine() {
		return super.getHeaderLine() + "\t" + "CONCEPT_VALUE_ID";
	}

}
