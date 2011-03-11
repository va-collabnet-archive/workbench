package org.ihtsdo.tk.api.concept;

import java.io.IOException;
import java.util.Collection;

import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.constraint.ConstraintBI;
import org.ihtsdo.tk.api.constraint.ConstraintCheckType;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;

public interface ConceptVersionBI extends ComponentVersionBI, ConceptChronicleBI {

    ViewCoordinate getViewCoordinate();

    ConceptChronicleBI getChronicle();

    ConAttrVersionBI getConAttrsActive() throws IOException, ContraditionException;

    DescriptionVersionBI getPreferredDescription() throws IOException, ContraditionException;

    DescriptionVersionBI getFullySpecifiedDescription() throws IOException, ContraditionException;

    Collection<? extends DescriptionVersionBI> getSynonyms() throws IOException;

    Collection<? extends DescriptionVersionBI> getDescsActive() throws IOException, ContraditionException;

    Collection<? extends DescriptionVersionBI> getDescsActive(int typeNid) throws IOException, ContraditionException;

    Collection<? extends DescriptionVersionBI> getDescsActive(NidSetBI typeNids) throws IOException, ContraditionException;

    Collection<? extends RelationshipVersionBI> getRelsOutgoingActive() throws IOException, ContraditionException;

    Collection<? extends RelationshipVersionBI> getRelsIncomingActive() throws IOException, ContraditionException;

    Collection<? extends MediaVersionBI> getMediaActive() throws IOException, ContraditionException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations() throws IOException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations(int typeNid) throws IOException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations(NidSetBI typeNids) throws IOException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsIsa() throws IOException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOrigins() throws IOException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOrigins(int typeNid) throws IOException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOrigins(NidSetBI typeNids) throws IOException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOriginsIsa() throws IOException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive() throws IOException, ContraditionException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive(int typeNid) throws IOException, ContraditionException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive(NidSetBI typeNids) throws IOException, ContraditionException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActiveIsa() throws IOException, ContraditionException;

    int[] getRelsOutgoingDestinationsNidsActiveIsa() throws IOException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive() throws IOException, ContraditionException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(int typeNid) throws IOException, ContraditionException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(NidSetBI typeNids) throws IOException, ContraditionException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActiveIsa() throws IOException, ContraditionException;

    @Override
    Collection<? extends RelGroupVersionBI> getRelGroups() throws IOException, ContraditionException;

    Collection<? extends RefexVersionBI<?>> getCurrentRefsetMembers()
            throws IOException;

    boolean isKindOf(ConceptVersionBI parentKind) throws IOException;

    boolean satisfies(ConstraintBI constraint,
            ConstraintCheckType subjectCheck,
            ConstraintCheckType propertyCheck,
            ConstraintCheckType valueCheck) throws IOException, ContraditionException;
    //TODO

    boolean isMember(int evalRefsetNid) throws IOException;

    //TODO to here
    boolean hasHistoricalRels() throws IOException, ContraditionException;

    boolean hasChildren() throws IOException, ContraditionException;
}
