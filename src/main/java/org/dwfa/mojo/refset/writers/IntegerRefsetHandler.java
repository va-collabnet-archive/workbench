package org.dwfa.mojo.refset.writers;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class IntegerRefsetHandler extends MemberRefsetHandler {
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
	
	@Override
	protected I_ThinExtByRefPart processLine(String line) {
		I_ThinExtByRefPartInteger part;
		try {
			
			I_ThinExtByRefVersioned versioned = getExtensionVersioned(line, RefsetAuxiliary.Concept.INT_EXTENSION);
			
			part = getTermFactory().newIntegerExtensionPart();
			setGenericExtensionPartFields(part);

			part.setValue(Integer.parseInt(getNextCurrentRowToken()));
			
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
