package org.dwfa.mojo.refset.writers;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class ConceptIntegerRefsetHandler extends MemberRefsetHandler {
	@Override
	public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple tuple) throws TerminologyException, IOException {
		I_ThinExtByRefPartConceptInt conceptIntegerPart = (I_ThinExtByRefPartConceptInt) tuple.getPart();
		
		return super.formatRefsetLine(tf, tuple) + MemberRefsetHandler.FILE_DELIMITER
					+ toId(tf, conceptIntegerPart.getConceptId()) + MemberRefsetHandler.FILE_DELIMITER
					+ conceptIntegerPart.getIntValue();
	}

	@Override
	public String getHeaderLine() {
		return super.getHeaderLine() + MemberRefsetHandler.FILE_DELIMITER + "CONCEPT_VALUE" + MemberRefsetHandler.FILE_DELIMITER + "INTEGER_VALUE";
	}
	
	@Override
	protected I_ThinExtByRefPart processLine(String line) {
		I_ThinExtByRefPartConceptInt part;
		try {
			
			I_ThinExtByRefVersioned versioned = getExtensionVersioned(line, RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION);
			
			part = getTermFactory().newConceptIntExtensionPart();
			setGenericExtensionPartFields(part);

			String conceptValue = getNextCurrentRowToken();
			part.setConceptId(getNid(UUID.fromString(conceptValue)));
			
			part.setIntValue(Integer.parseInt(getNextCurrentRowToken()));
			
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
