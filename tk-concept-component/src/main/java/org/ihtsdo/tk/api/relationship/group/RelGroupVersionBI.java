package org.ihtsdo.tk.api.relationship.group;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

public interface RelGroupVersionBI extends RelGroupChronicleBI, ComponentVersionBI {
   Collection<? extends RelationshipVersionBI> getAllCurrentRelVersions();

   Collection<? extends RelationshipVersionBI> getAllRels() throws ContraditionException;

   Collection<? extends RelationshipVersionBI> getCurrentRels() throws ContraditionException;
}
