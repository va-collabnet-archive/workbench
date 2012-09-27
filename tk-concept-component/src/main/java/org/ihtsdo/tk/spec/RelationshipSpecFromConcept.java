/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.spec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.binding.snomed.HistoricalRelType;
import org.ihtsdo.tk.binding.snomed.Taxonomies;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.RelationshipSpec;

// TODO: Auto-generated Javadoc
/**
 * The Class RelationshipSpecFromConcept.
 */
public class RelationshipSpecFromConcept {
	
	/** The concept. */
	ConceptVersionBI concept;
	
	/** The rel spec. */
	RelationshipSpec relSpec;
	
	/** The indv rel specs. */
	ArrayList<RelationshipSpec> indvRelSpecs = new ArrayList<RelationshipSpec>();
	
	/** The all rel specs. */
	ArrayList<RelationshipSpec> allRelSpecs = new ArrayList<RelationshipSpec>();
	
	/**
	 * Instantiates a new relationship spec from concept.
	 *
	 * @param conceptVersion the concept version
	 */
	public RelationshipSpecFromConcept (ConceptVersionBI conceptVersion){
		this.concept = conceptVersion;
		makeRelationshipSpec();
	}
	
	/**
	 * Make relationship spec.
	 */
	private void makeRelationshipSpec (){
		try {
			Collection<? extends ConceptVersionBI> rels;
			rels = concept.getRelationshipsSourceTargetConceptsIsa();
			String description = null;
			
			//get rels for concept
			for (ConceptVersionBI rel: rels){
				
				//get Descriptions for each rel
				Collection<? extends DescriptionVersionBI> dvs = rel.getDescriptionsActive();
				for(DescriptionVersionBI dv : dvs){
					description = dv.getText();
				}
				
				//get uuids for each rel and add to individual rel specs
				List<UUID> uuids = rel.getUUIDs();
				
				//create RelationshipSpec and add to small collection
				for(UUID uuid : uuids){
					String uuidStr = uuid.toString();
					ConceptSpec destination = new ConceptSpec (description, UUID.fromString(uuidStr));
					
					indvRelSpecs.add(relSpec = new RelationshipSpec(Taxonomies.SNOMED, HistoricalRelType.MAY_BE_A, destination));
					indvRelSpecs.add(relSpec = new RelationshipSpec(Taxonomies.SNOMED, HistoricalRelType.REPLACED_BY, destination));
					indvRelSpecs.add(relSpec = new RelationshipSpec(Taxonomies.SNOMED, HistoricalRelType.SAME_AS, destination));
					indvRelSpecs.add(relSpec = new RelationshipSpec(Taxonomies.SNOMED, HistoricalRelType.WAS_A, destination));
					}
				//add indv relSpecs to all Rel specs
				allRelSpecs.addAll(indvRelSpecs);
				}
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (ContradictionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the relationship specs.
	 *
	 * @return the relationship specs
	 */
	public ArrayList<RelationshipSpec> getRelationshipSpecs(){
		return allRelSpecs;
	}	
}
