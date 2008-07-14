package org.dwfa.mojo.refset.writers;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class StringRefsetHandler extends MemberRefsetHandler {
	@Override
	public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple tuple, boolean sctid) throws TerminologyException, IOException {
		I_ThinExtByRefPartString stringPart = (I_ThinExtByRefPartString) tuple.getPart();
		
		return super.formatRefsetLine(tf, tuple, sctid) + "\t"
					+ stringPart.getStringValue();
	}

	@Override
	public String getHeaderLine() {
		return super.getHeaderLine() + "\t" + "STRING_VALUE";
	}
	
	@Override
	protected I_ThinExtByRefPart processLine(String line) {
		I_ThinExtByRefPartString part;
		try {
			
			I_ThinExtByRefVersioned versioned = getExtensionVersioned(line, RefsetAuxiliary.Concept.STRING_EXTENSION);
			
			part = getTermFactory().newStringExtensionPart();
			setGenericExtensionPartFields(part);

			part.setStringValue(getNextCurrentRowToken());
			
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
