package org.dwfa.mojo.refset.writers;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.tapi.TerminologyException;

public class StringRefsetWriter extends MemberRefsetWriter {
	@Override
	public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple tuple) throws TerminologyException, IOException {
		I_ThinExtByRefPartString stringPart = (I_ThinExtByRefPartString) tuple.getPart();
		
		return super.formatRefsetLine(tf, tuple) + "\t"
					+ stringPart.getStringValue();
	}

	@Override
	public String getHeaderLine() {
		return super.getHeaderLine() + "\t" + "STRING_VALUE";
	}
}
