package org.dwfa.mojo.refset;

import java.util.UUID;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
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
	
	protected I_TermFactory termFactory = LocalVersionedTerminology.get();
	
	@Override
	protected I_ThinExtByRefVersioned processLine(String line) {

		String[] tokens = line.split( "\t" );
		
		try {
			I_GetConceptData concept = findConcept(tokens[0]);

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

	private I_GetConceptData findConcept(String snomedId) throws Exception {
		
		Hits hits = AceConfig.getVodb().doLuceneSearch(snomedId);

		if (hits == null || hits.length() == 0) {
			throw new MojoExecutionException("Search produced no results");
		}
		
		// Find the hit that actually has our snomed id in it
		
		for (int i = 0; i < hits.length(); i++) {
			Document doc = hits.doc(i);
			int cnid = Integer.parseInt(doc.get("cnid"));
			I_GetConceptData concept = termFactory.getConcept(cnid);
			for (I_IdPart version : concept.getId().getVersions()) {
				if (version.getSourceId().equals(snomedId)) {
					return concept;
				}
			}
		}
		
		throw new MojoExecutionException("Unable to locate a matching concept");
	}
	
}
