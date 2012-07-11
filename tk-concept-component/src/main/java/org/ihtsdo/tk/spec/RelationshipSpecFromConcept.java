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

public class RelationshipSpecFromConcept {
	
	ConceptVersionBI concept;
	RelationshipSpec relSpec;
	ArrayList<RelationshipSpec> indvRelSpecs = new ArrayList<RelationshipSpec>();
	ArrayList<RelationshipSpec> allRelSpecs = new ArrayList<RelationshipSpec>();
	
	public RelationshipSpecFromConcept (ConceptVersionBI conceptVersion){
		this.concept = conceptVersion;
		makeRelationshipSpec();
	}
	
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
	
	public ArrayList<RelationshipSpec> getRelationshipSpecs(){
		return allRelSpecs;
	}	
}
