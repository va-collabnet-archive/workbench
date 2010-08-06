package org.ihtsdo.tk.api;

import java.io.IOException;
import java.util.Collection;

public interface ConceptChronicleBI {

	public ConAttrChronicleBI getConAttrs() throws IOException;

	public Collection<? extends DescriptionChronicleBI>  getDescs() throws IOException;
	public Collection<? extends RelationshipChronicleBI> getRelsOutgoing() throws IOException;
	public Collection<? extends RelationshipChronicleBI> getRelsIncoming() throws IOException;
	public Collection<? extends MediaChronicleBI>        getMedia() throws IOException;

}
