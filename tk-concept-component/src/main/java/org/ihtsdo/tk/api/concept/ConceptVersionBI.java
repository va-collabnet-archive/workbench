package org.ihtsdo.tk.api.concept;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
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

public interface ConceptVersionBI extends ComponentVersionBI, ConceptChronicleBI {
   boolean satisfies(ConstraintBI constraint, ConstraintCheckType subjectCheck,
                     ConstraintCheckType propertyCheck, ConstraintCheckType valueCheck)
           throws IOException, ContraditionException;

   //~--- get methods ---------------------------------------------------------

   @Override
   ConceptChronicleBI getChronicle();

   ConAttrVersionBI getConAttrsActive() throws IOException, ContraditionException;

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
           throws IOException, ContraditionException;

   Collection<? extends DescriptionVersionBI> getDescsActive() throws IOException, ContraditionException;

   Collection<? extends DescriptionVersionBI> getDescsActive(int typeNid)
           throws IOException, ContraditionException;

   Collection<? extends DescriptionVersionBI> getDescsActive(NidSetBI typeNids)
           throws IOException, ContraditionException;

   Collection<? extends DescriptionVersionBI> getFsnDescsActive() throws IOException;

   DescriptionVersionBI getFullySpecifiedDescription() throws IOException, ContraditionException;

   Collection<? extends MediaVersionBI> getMediaActive() throws IOException, ContraditionException;

   Collection<List<Integer>> getNidPathsToRoot() throws IOException;

   Collection<? extends DescriptionVersionBI> getPrefDescsActive() throws IOException;

   DescriptionVersionBI getPreferredDescription() throws IOException, ContraditionException;

   Collection<? extends RefexVersionBI<?>> getRefsetMembersActive() throws IOException, ContraditionException;

   Collection<? extends RelGroupVersionBI> getRelGroups() throws IOException, ContraditionException;

   Collection<? extends RelationshipVersionBI> getRelsIncomingActive()
           throws IOException, ContraditionException;

   Collection<? extends RelationshipVersionBI> getRelsIncomingActiveIsa()
           throws IOException, ContraditionException;

   Collection<? extends ConceptVersionBI> getRelsIncomingOrigins() throws IOException;

   Collection<? extends ConceptVersionBI> getRelsIncomingOrigins(int typeNid) throws IOException;

   Collection<? extends ConceptVersionBI> getRelsIncomingOrigins(NidSetBI typeNids) throws IOException;

   Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive()
           throws IOException, ContraditionException;

   Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(int typeNid)
           throws IOException, ContraditionException;

   Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(NidSetBI typeNids)
           throws IOException, ContraditionException;

   Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActiveIsa()
           throws IOException, ContraditionException;

   Collection<? extends ConceptVersionBI> getRelsIncomingOriginsIsa() throws IOException;

   Collection<? extends RelationshipVersionBI> getRelsOutgoingActive()
           throws IOException, ContraditionException;

   Collection<? extends RelationshipVersionBI> getRelsOutgoingActiveIsa()
           throws IOException, ContraditionException;

   Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations() throws IOException;

   Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations(int typeNid) throws IOException;

   Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations(NidSetBI typeNids) throws IOException;

   Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive()
           throws IOException, ContraditionException;

   Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive(int typeNid)
           throws IOException, ContraditionException;

   Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive(NidSetBI typeNids)
           throws IOException, ContraditionException;

   Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActiveIsa()
           throws IOException, ContraditionException;

   Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsIsa() throws IOException;

   int[] getRelsOutgoingDestinationsNidsActiveIsa() throws IOException;

   Collection<? extends DescriptionVersionBI> getSynonyms() throws IOException;

   ViewCoordinate getViewCoordinate();

   boolean hasAnnotationMemberActive(int refsetNid) throws IOException;

   boolean hasChildren() throws IOException, ContraditionException;

   boolean hasHistoricalRels() throws IOException, ContraditionException;

   boolean hasRefexMemberActive(int refsetNid) throws IOException;

   boolean hasRefsetMemberForComponentActive(int componentNid) throws IOException;

   boolean isActive() throws IOException;
   
   boolean isChildOf(ConceptVersionBI child) throws IOException;

   boolean isKindOf(ConceptVersionBI parentKind) throws IOException;

   boolean isLeaf() throws IOException;

   boolean isMember(int evalRefsetNid) throws IOException;
}
