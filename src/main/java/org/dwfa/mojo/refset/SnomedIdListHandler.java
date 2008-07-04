package org.dwfa.mojo.refset;

import java.io.IOException;
import java.util.UUID;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.mojo.file.FileHandler;

/**
 * Processes a tab delimited file containing a snomed concept id in the first column.
 * For each concept listed an inclusion specification extension will be produced. 
 */
public class SnomedIdListHandler extends FileHandler<I_ThinExtByRefVersioned> {
	
	@Override
	protected I_ThinExtByRefVersioned processLine(String line) {

		String[] tokens = line.split( "\t" );
		
		try {
			// Search for the concept by the snomed id. It should be the top scoring hit.
			
			Hits hits = AceConfig.getVodb().doLuceneSearch(tokens[0]);
			Document doc = getTopHit(hits);
			int cnid = Integer.parseInt(doc.get("cnid"));

			I_TermFactory termFactory = LocalVersionedTerminology.get();
			I_GetConceptData concept = termFactory.getConcept(cnid);
		
			// Create the new refset specification concept extension
			
			int memberId = termFactory.uuidToNativeWithGeneration( UUID.randomUUID(),
	                ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
	                termFactory.getPaths(), Integer.MAX_VALUE );
			
			int refsetTypeId = termFactory.uuidToNative( 
					RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next() );
			
            int statusId = termFactory.uuidToNative(
                    ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());
			
            int specTypeId = termFactory.uuidToNative(
            		RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids().iterator().next());
            
			I_ThinExtByRefPartConcept extPart = termFactory.newConceptExtensionPart();			
			extPart.setConceptId( specTypeId );
			extPart.setStatus( statusId );
			
			I_ThinExtByRefVersioned extension = termFactory.newExtension( 0, memberId, concept.getConceptId(), refsetTypeId );
			extension.addVersion(extPart);

			return extension;
				
		} catch (Exception e) {
			throw new RuntimeException("Unable to process extension for snomed id " + tokens[0], e);
		}
	}

	/**
	 * Return the document for the highest scoring search hit 
	 */
	private Document getTopHit(Hits hits) throws MojoExecutionException {
		int topHit = 0;
		if (hits == null || hits.length() == 0) {
			throw new MojoExecutionException("Search produced no results");
		}
		try {
			for (int i = 0; i < hits.length(); i++) {
				if (hits.score(topHit) < hits.score(i)) {
					topHit = i;
				}
			}
			return hits.doc(topHit);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
	
}
