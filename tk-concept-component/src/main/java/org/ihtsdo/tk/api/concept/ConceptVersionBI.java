package org.ihtsdo.tk.api.concept;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.constraint.ConstraintBI;
import org.ihtsdo.tk.api.constraint.ConstraintCheckType;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.List;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;

public interface ConceptVersionBI extends ComponentVersionBI, ConceptChronicleBI {

    boolean satisfies(ConstraintBI constraint, ConstraintCheckType subjectCheck,
            ConstraintCheckType propertyCheck, ConstraintCheckType valueCheck)
            throws IOException, ContradictionException;

    //~--- get methods ---------------------------------------------------------
    @Override
    ConceptChronicleBI getChronicle();

    ConAttrVersionBI getConAttrsActive() throws IOException, ContradictionException;

    Collection<? extends RefexVersionBI<?>> getCurrentRefexMembers(int refsetNid) throws IOException;

    RefexChronicleBI<?> getCurrentRefsetMemberForComponent(int componentNid) throws IOException;

    /**
     *
     * @return
     * @throws IOException
     * @deprecated use getRefsetMembersActive
     */
    @Deprecated
    Collection<? extends RefexVersionBI<?>> getCurrentRefsetMembers()
            throws IOException, ContradictionException;

    Collection<? extends DescriptionVersionBI> getDescsActive() throws IOException, ContradictionException;

    Collection<? extends DescriptionVersionBI> getDescsActive(int typeNid)
            throws IOException, ContradictionException;

    Collection<? extends DescriptionVersionBI> getDescsActive(NidSetBI typeNids)
            throws IOException, ContradictionException;

    Collection<? extends DescriptionVersionBI> getFsnDescsActive() throws IOException;

    DescriptionVersionBI getFullySpecifiedDescription() throws IOException, ContradictionException;

    Collection<? extends MediaVersionBI> getMediaActive() throws IOException, ContradictionException;

    Collection<List<Integer>> getNidPathsToRoot() throws IOException;

    Collection<? extends DescriptionVersionBI> getPrefDescsActive() throws IOException;

    DescriptionVersionBI getPreferredDescription() throws IOException, ContradictionException;

    Collection<? extends RefexVersionBI<?>> getRefsetMembersActive() throws IOException, ContradictionException;

    Collection<? extends RelGroupVersionBI> getRelGroups() throws IOException, ContradictionException;

    Collection<? extends RelationshipVersionBI> getRelsIncomingActive()
            throws IOException, ContradictionException;

    Collection<? extends RelationshipVersionBI> getRelsIncomingActiveIsa()
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOrigins() throws IOException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOrigins(int typeNid) throws IOException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOrigins(NidSetBI typeNids) throws IOException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive()
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(int typeNid)
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(NidSetBI typeNids)
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActiveIsa()
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelsIncomingOriginsIsa() throws IOException;

    Collection<? extends RelationshipVersionBI> getRelsOutgoingActive()
            throws IOException, ContradictionException;

    Collection<? extends RelationshipVersionBI> getRelsOutgoingActiveIsa()
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations() throws IOException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations(int typeNid) throws IOException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations(NidSetBI typeNids) throws IOException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive()
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive(int typeNid)
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive(NidSetBI typeNids)
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActiveIsa()
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsIsa() throws IOException;

    int[] getRelsOutgoingDestinationsNidsActiveIsa() throws IOException;

    Collection<? extends DescriptionVersionBI> getSynonyms() throws IOException;

    ViewCoordinate getViewCoordinate();

    boolean hasAnnotationMemberActive(int refsetNid) throws IOException;

    boolean hasChildren() throws IOException, ContradictionException;

    boolean hasHistoricalRels() throws IOException, ContradictionException;

    boolean hasRefexMemberActive(int refsetNid) throws IOException;

    boolean hasRefsetMemberForComponentActive(int componentNid) throws IOException;

    boolean isActive() throws IOException;

    boolean isChildOf(ConceptVersionBI child) throws IOException;

    boolean isKindOf(ConceptVersionBI parentKind) throws IOException;

    boolean isLeaf() throws IOException;

    boolean isMember(int evalRefsetNid) throws IOException;

    ConceptCB makeBlueprint() throws IOException, ContradictionException, InvalidCAB;
    
    @Override
    ConceptCB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB;
}
