package org.ihtsdo.tk.api.concept;

import java.io.IOException;
import java.util.Collection;

import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.conattr.ConAttrChronicleBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public interface ConceptVersionBI extends ComponentBI {
	
	public ConceptChronicleBI getConceptChronicle();

	public Coordinate getCoordinate();
	
	public ConAttrChronicleBI getConAttrs() throws IOException;
	public Collection<? extends DescriptionChronicleBI> getDescs() throws IOException;
	public Collection<? extends RelationshipChronicleBI> getRelsOutgoing() throws IOException;
	public Collection<? extends RelationshipChronicleBI> getRelsIncoming() throws IOException;
	public Collection<? extends MediaChronicleBI> getMedia() throws IOException;

	public ConAttrVersionBI getConAttrsActive() throws IOException, ContraditionException;
	public Collection<? extends DescriptionVersionBI> getDescsActive() throws IOException, ContraditionException;
	public Collection<? extends RelationshipVersionBI> getRelsOutgoingActive() throws IOException, ContraditionException;
	public Collection<? extends RelationshipVersionBI> getRelsIncomingActive() throws IOException, ContraditionException;
	public Collection<? extends MediaVersionBI> getMediaActive() throws IOException, ContraditionException;

	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargets() throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargets(NidSetBI typeNids) throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargetsIsa() throws IOException;
	
	public Collection<? extends ConceptVersionBI> getRelsIncomingOrigins() throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOrigins(NidSetBI typeNids) throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsIsa() throws IOException;

	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargetsActive() throws IOException, ContraditionException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargetsActive(NidSetBI typeNids) throws IOException, ContraditionException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargetsActiveIsa() throws IOException, ContraditionException;
	
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive() throws IOException, ContraditionException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(NidSetBI typeNids) throws IOException, ContraditionException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActiveIsa() throws IOException, ContraditionException;
	
	public boolean isKindOf(ConceptVersionBI parentKind) throws IOException;

}
