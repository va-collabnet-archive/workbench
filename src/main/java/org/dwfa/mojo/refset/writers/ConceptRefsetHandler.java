package org.dwfa.mojo.refset.writers;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class ConceptRefsetHandler extends MemberRefsetHandler {

	@Override
	public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple part, boolean sctid) throws TerminologyException, IOException {
		return formatRefsetLine(tf, part, part.getMemberId(), part.getRefsetId(), part.getComponentId(), sctid);
	}
	
	@Override
	public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefPart part, Integer memberId, int refsetId, int componentId, boolean sctId) throws TerminologyException, IOException {
		I_ThinExtByRefPartConcept conceptPart = (I_ThinExtByRefPartConcept) part;
		
		return super.formatRefsetLine(tf, part, memberId, refsetId, componentId, sctId) + MemberRefsetHandler.FILE_DELIMITER
					+ toId(tf, conceptPart.getConceptId(), sctId);
	}

	@Override
	public String getHeaderLine() {
		return super.getHeaderLine() + MemberRefsetHandler.FILE_DELIMITER + "CONCEPT_VALUE_ID";
	}

	@Override
	protected I_ThinExtByRefPart processLine(String line) {
		I_ThinExtByRefPartConcept part;
		try {
			
			I_ThinExtByRefVersioned versioned = getExtensionVersioned(line, RefsetAuxiliary.Concept.CONCEPT_EXTENSION);
			
			part = getTermFactory().newConceptExtensionPart();
			setGenericExtensionPartFields(part);

			String conceptValue = getNextCurrentRowToken();
			part.setConceptId(getNid(UUID.fromString(conceptValue)));
			
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
