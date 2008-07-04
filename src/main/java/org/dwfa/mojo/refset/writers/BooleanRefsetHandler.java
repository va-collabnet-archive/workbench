package org.dwfa.mojo.refset.writers;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class BooleanRefsetHandler extends MemberRefsetHandler {
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

	@Override
	protected I_ThinExtByRefPart processLine(String line) {
		I_ThinExtByRefPartBoolean part;
		try {
			
			I_ThinExtByRefVersioned versioned = getExtensionVersioned(line, RefsetAuxiliary.Concept.BOOLEAN_EXTENSION);
			
			part = getTermFactory().newBooleanExtensionPart();
			setGenericExtensionPartFields(part);

			String value = getNextCurrentRowToken();
			assert value.equals("0") || value.equals("1");
			
			part.setValue(value.equals("1") ? true : false);
			
			versioned.addVersion(part);
			
			if (isTransactional()) {
				getTermFactory().addUncommitted(versioned);
			} else {
				getTermFactory().getDirectInterface().writeExt(versioned);
			}
		
		} catch (Exception e) {
			throw new RuntimeException("Error occred processing file " + sourceFile, e);
		}
		
		return part;
	}


}
