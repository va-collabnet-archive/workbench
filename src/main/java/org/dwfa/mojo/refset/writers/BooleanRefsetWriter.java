package org.dwfa.mojo.refset.writers;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.tapi.TerminologyException;

public class BooleanRefsetWriter extends MemberRefsetWriter {
	@Override
	public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple tuple) throws TerminologyException, IOException {
		I_ThinExtByRefPartBoolean booleanPart = (I_ThinExtByRefPartBoolean) tuple.getPart();
		
		return super.formatRefsetLine(tf, tuple) + "\t"
					+ (booleanPart.getValue() ? 1 : 0); // 1 for true, 0 for false
	}

	@Override
	public String getHeaderLine() {
		return super.getHeaderLine() + "\t" + "BOOLEAN_VALUE";
	}
}
