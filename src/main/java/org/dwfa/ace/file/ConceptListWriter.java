package org.dwfa.ace.file;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * Writes out a "Concept List" which is a tab delimited list of concepts containing
 * the concepts identifier and a description. The identifier will be the concept's
 * SNOMED CT ID if it has one, or its UUID if it does not.
 * 
 * @author Dion
 *
 */
public class ConceptListWriter extends GenericFileWriter<I_GetConceptData> {

	private Integer snomedIntId;
	
	/* (non-Javadoc)
	 * @see org.dwfa.ace.file.GenericFileWriter#serialize(java.lang.Object)
	 */
	@Override
	protected String serialize(I_GetConceptData concept) throws IOException, TerminologyException {
	
		if (snomedIntId == null) {
			snomedIntId = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
		}
		
		Object conceptId = concept.getId(snomedIntId);
		
		if (conceptId == null) {
			conceptId = concept.getUids().iterator().next();
		}
		
		//spit out a SNOMED CT ID if possible, if not a UUID
		//description is arbitrary - just pick one
		return conceptId + "\t" + concept.getInitialText();
	}
}
