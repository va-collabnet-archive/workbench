package org.ihtsdo.tk.api.relationship;

import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.TypedComponentVersionBI;

public interface RelationshipVersionBI 
	extends TypedComponentVersionBI, 
			RelationshipChronicleBI, 
			AnalogGeneratorBI<RelationshipAnalogBI> {

	public int getOriginNid();
	public int getDestinationNid();
	public int getRefinabilityNid();
	public int getCharacteristicNid();
	public int getGroup();


}
