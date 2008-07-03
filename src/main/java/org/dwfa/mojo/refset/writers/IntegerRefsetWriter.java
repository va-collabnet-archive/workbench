package org.dwfa.mojo.refset.writers;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.tapi.TerminologyException;

public class IntegerRefsetWriter extends MemberRefsetWriter {
	@Override
	public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple tuple) throws TerminologyException, IOException {
		I_ThinExtByRefPartInteger integerPart = (I_ThinExtByRefPartInteger) tuple.getPart();
		
		return super.formatRefsetLine(tf, tuple) + "\t"
					+ integerPart.getValue();
	}

	@Override
	public String getHeaderLine() {
		return super.getHeaderLine() + "\t" + "INTEGER_VALUE";
	}
}
