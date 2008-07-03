package org.dwfa.mojo.refset.writers;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.tapi.TerminologyException;

public class ConceptIntegerRefsetWriter extends MemberRefsetWriter {
	@Override
	public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple tuple) throws TerminologyException, IOException {
		I_ThinExtByRefPartConceptInt conceptIntegerPart = (I_ThinExtByRefPartConceptInt) tuple.getPart();
		
		return super.formatRefsetLine(tf, tuple) + "\t"
					+ toId(tf, conceptIntegerPart.getConceptId()) + "\t"
					+ conceptIntegerPart.getIntValue();
	}

	@Override
	public String getHeaderLine() {
		return super.getHeaderLine() + "\tCONCEPT_VALUE\tINTEGER_VALUE";
	}
}
