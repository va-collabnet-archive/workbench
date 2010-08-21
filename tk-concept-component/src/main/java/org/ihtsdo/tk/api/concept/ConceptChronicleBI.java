package org.ihtsdo.tk.api.concept;

import java.io.IOException;
import java.util.Collection;

import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.conattr.ConAttrChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;

public interface ConceptChronicleBI {

	public ConAttrChronicleBI getConAttrs() throws IOException;

	public Collection<? extends DescriptionChronicleBI>  getDescs() throws IOException;
	public Collection<? extends RelationshipChronicleBI> getRelsOutgoing() throws IOException;
	public Collection<? extends RelationshipChronicleBI> getRelsIncoming() throws IOException;
	public Collection<? extends MediaChronicleBI>        getMedia() throws IOException;

	public Collection<? extends RelGroupChronicleBI>     getRelGroups() throws IOException, ContraditionException;

}
