package org.ihtsdo.tk.api.concept;

import java.io.IOException;
import java.util.Collection;

import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public interface ConceptVersionBI extends ComponentBI {

	public Coordinate getCoordinate();
	
	public ConAttrVersionBI getConAttrs() throws IOException;
	public Collection<? extends DescriptionVersionBI> getDescs() throws IOException;
	public Collection<? extends RelationshipVersionBI> getRelsOutgoing() throws IOException;
	public Collection<? extends RelationshipVersionBI> getRelsIncoming() throws IOException;
	public Collection<? extends MediaVersionBI> getMedia() throws IOException;

	public ConAttrVersionBI getConAttrsActive() throws IOException;
	public Collection<? extends DescriptionVersionBI> getDescsActive() throws IOException;
	public Collection<? extends RelationshipVersionBI> getRelsOutgoingActive() throws IOException;
	public Collection<? extends RelationshipVersionBI> getRelsIncomingActive() throws IOException;
	public Collection<? extends MediaVersionBI> getMediaActive() throws IOException;

	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargetsActive() throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargetsActive(NidSetBI typeNids) throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargetsActive(Collection<ConceptVersionBI> types) throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargetsActiveIsa() throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive() throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(NidSetBI typeNids) throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(Collection<ConceptVersionBI> types) throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActiveIsa() throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargets() throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargets(NidSetBI typeNids) throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargets(Collection<ConceptVersionBI> types) throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsIsa() throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsIsa(NidSetBI typeNids) throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsIsa(Collection<ConceptVersionBI> types) throws IOException;

}
