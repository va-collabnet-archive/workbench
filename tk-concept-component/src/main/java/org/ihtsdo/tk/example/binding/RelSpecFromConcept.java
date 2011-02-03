package org.ihtsdo.tk.example.binding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.RelSpec;

public class RelSpecFromConcept {
	
	ConceptVersionBI concept;
	RelSpec relSpec;
	ArrayList<RelSpec> indvRelSpecs = new ArrayList<RelSpec>();
	ArrayList<RelSpec> allRelSpecs = new ArrayList<RelSpec>();
	
	public RelSpecFromConcept (ConceptVersionBI arenaConcept){
		this.concept = arenaConcept;
		makeRelSpec();
	}
	
	private void makeRelSpec (){
		try {
			Collection<? extends ConceptVersionBI> rels;
			rels = concept.getRelsOutgoingDestinationsIsa();
			String description = null;
			
			//get rels for concept
			for (ConceptVersionBI rel: rels){
				
				//get Descriptions for each rel
				Collection<? extends DescriptionVersionBI> dvs = rel.getDescsActive();
				for(DescriptionVersionBI dv : dvs){
					description = dv.getText();
				}
				
				//get uuids for each rel and add to individual rel specs
				List<UUID> uuids = rel.getUUIDs();
				
				//create RelSpec and add to small collection
				for(UUID uuid : uuids){
					String uuidStr = uuid.toString();
					ConceptSpec destination = new ConceptSpec (description, UUID.fromString(uuidStr));
					
					indvRelSpecs.add(relSpec = new RelSpec(Taxonomies.SNOMED, HistoricalRelType.MAY_BE_A, destination));
					indvRelSpecs.add(relSpec = new RelSpec(Taxonomies.SNOMED, HistoricalRelType.MOVED_TO, destination));
					indvRelSpecs.add(relSpec = new RelSpec(Taxonomies.SNOMED, HistoricalRelType.REPLACED_BY, destination));
					indvRelSpecs.add(relSpec = new RelSpec(Taxonomies.SNOMED, HistoricalRelType.SAME_AS, destination));
					indvRelSpecs.add(relSpec = new RelSpec(Taxonomies.SNOMED, HistoricalRelType.WAS_A, destination));
					}
				//add indv relSpecs to all Rel specs
				allRelSpecs.addAll(indvRelSpecs);
				}
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (ContraditionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<RelSpec> getRelSpecs(){
		return allRelSpecs;
	}	
}
