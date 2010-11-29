package org.ihtsdo.tk.api.relationship.group;

import java.util.Collection;

import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

public interface RelGroupChronicleBI extends ComponentChroncileBI<RelGroupVersionBI> {
	
	public Collection<? extends RelationshipChronicleBI> getRels();
	
	public int getRelGroup();
}
