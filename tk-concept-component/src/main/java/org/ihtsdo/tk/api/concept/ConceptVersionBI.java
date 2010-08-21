package org.ihtsdo.tk.api.concept;

import java.io.IOException;
import java.util.Collection;

import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.constraint.ConstraintBI;
import org.ihtsdo.tk.api.constraint.ConstraintCheckType;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;

public interface ConceptVersionBI extends ComponentBI, ConceptChronicleBI {
	
	public Coordinate getCoordinate();
	
	public ConAttrVersionBI getConAttrsActive() throws IOException, ContraditionException;
	
	public Collection<? extends DescriptionVersionBI> getDescsActive() throws IOException, ContraditionException;
	public Collection<? extends DescriptionVersionBI> getDescsActive(int typeNid) throws IOException, ContraditionException;
	public Collection<? extends DescriptionVersionBI> getDescsActive(NidSetBI typeNids) throws IOException, ContraditionException;

	public Collection<? extends RelationshipVersionBI> getRelsOutgoingActive() throws IOException, ContraditionException;
	public Collection<? extends RelationshipVersionBI> getRelsIncomingActive() throws IOException, ContraditionException;
	public Collection<? extends MediaVersionBI> getMediaActive() throws IOException, ContraditionException;

	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations() throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations(int typeNid) throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations(NidSetBI typeNids) throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsIsa() throws IOException;
	
	public Collection<? extends ConceptVersionBI> getRelsIncomingOrigins() throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOrigins(int typeNid) throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOrigins(NidSetBI typeNids) throws IOException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsIsa() throws IOException;

	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive() throws IOException, ContraditionException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive(int typeNid) throws IOException, ContraditionException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive(NidSetBI typeNids) throws IOException, ContraditionException;
	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActiveIsa() throws IOException, ContraditionException;
	
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive() throws IOException, ContraditionException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(int typeNid) throws IOException, ContraditionException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(NidSetBI typeNids) throws IOException, ContraditionException;
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActiveIsa() throws IOException, ContraditionException;
	

	public Collection<? extends RelGroupVersionBI> getRelGroups() throws IOException, ContraditionException;

	public boolean isKindOf(ConceptVersionBI parentKind) throws IOException;
	
	public boolean satisfies(ConstraintBI constraint, 
			ConstraintCheckType subjectCheck,
			ConstraintCheckType propertyCheck,
			ConstraintCheckType valueCheck) throws IOException, ContraditionException;

}
