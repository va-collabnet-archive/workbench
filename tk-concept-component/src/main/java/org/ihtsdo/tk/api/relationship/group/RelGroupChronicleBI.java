package org.ihtsdo.tk.api.relationship.group;

import java.util.Collection;

import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

public interface RelGroupChronicleBI extends ComponentBI {
	
	public Collection<? extends RelationshipChronicleBI> getRels() throws ContraditionException;
	
	public int getRelGroup();
}
