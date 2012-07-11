package org.ihtsdo.tk.api.relationship.group;

import java.util.Collection;

import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

public interface RelationshipGroupChronicleBI extends ComponentChronicleBI<RelationshipGroupVersionBI> {
	
	public Collection<? extends RelationshipChronicleBI> getRelationships();
	
	public int getRelationshipGroupNumber();
}
