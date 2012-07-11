package org.ihtsdo.tk.api.relationship.group;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

public interface RelationshipGroupVersionBI extends RelationshipGroupChronicleBI, ComponentVersionBI {
   Collection<? extends RelationshipVersionBI> getRelationshipsActiveAllVersions();

   Collection<? extends RelationshipVersionBI> getRelationshipsAll() throws ContradictionException;

   Collection<? extends RelationshipVersionBI> getRelationshipsActive() throws ContradictionException;
}
