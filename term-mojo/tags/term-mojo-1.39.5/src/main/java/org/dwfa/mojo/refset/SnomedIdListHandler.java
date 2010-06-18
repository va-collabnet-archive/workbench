/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.file.FileHandler;

/**
 * Processes a tab delimited file containing a snomed concept id in the first column.
 * For each concept listed an inclusion specification extension will be produced. 
 */
public class SnomedIdListHandler extends FileHandler<I_ThinExtByRefVersioned> {
	
	protected I_TermFactory termFactory = LocalVersionedTerminology.get();
	
	/**
	 * @parameter 
	 * @required
	 */
	protected ConceptDescriptor refsetType;
	
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
			
            int specTypeId = getRefsetType().getVerifiedConcept().getId().getNativeId();
            
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
				if (snomedId.equals(version.getSourceId().toString())) {
					return concept;
				}
			}
		}
		
		throw new MojoExecutionException("Unable to locate a matching concept");
	}

	public ConceptDescriptor getRefsetType() {
		assert (refsetType != null) : "A refset type has not been specified";
		return refsetType;
	}

	public void setRefsetType(ConceptDescriptor refsetType) {
		this.refsetType = refsetType;
	}

}
