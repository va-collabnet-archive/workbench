package org.ihtsdo.tk.api.relationship.group;

import java.util.Collection;

import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public interface RelGroupVersionBI extends RelGroupChronicleBI {

	public Collection<? extends RelationshipVersionBI> getRels() throws ContraditionException;

}
