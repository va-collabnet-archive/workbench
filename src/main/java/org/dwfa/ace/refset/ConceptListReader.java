package org.dwfa.ace.refset;

import org.dwfa.ace.api.ConceptDescriptor;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.file.IterableFileReader;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.TerminologyRuntimeException;

/**
 * Implements IterableFileReader to convert each line of the file to a concept.<br>
 * Expected columns in the tab delimited file are: 
 * <ul>
 * 	<li>1. Identifier (String) - may be any type of identifier, SCTID, UUID, etc.
 * 	<li>2. Description (String) - any valid description for the concept. Used to validate the ID is correct.
 * </ul>
 */
public class ConceptListReader extends IterableFileReader<I_GetConceptData> {

	protected I_TermFactory termFactory = LocalVersionedTerminology.get();

	/**
	 * @throws TerminologyRuntimeException if processing fails during iteration
	 */
	@Override
	protected I_GetConceptData processLine(String line) {

		try {
			String[] columns = line.split( "\t" );
			String conceptId = columns[0];
			String description = columns[1];
			
			if (conceptId.length() == 0 || description.length() == 0) {
				throw new TerminologyException("Invalid file format");
			}
			
			for (I_GetConceptData concept : termFactory.getConcept(conceptId)) {
				// validate against the description to ensure the id matches
				if (ConceptDescriptor.verify(concept, description)) {
					return concept;
				}
			}
			
			throw new TerminologyException(
					"Cannot find a concept with ID " + conceptId + " and the description '" + description + "'");

			
		} catch (IndexOutOfBoundsException ex) {
			throw new TerminologyRuntimeException("Invalid file format");
		} catch (Exception ex) {
			throw new TerminologyRuntimeException(ex);
		}
	}
	

}
