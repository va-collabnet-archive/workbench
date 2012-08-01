package org.ihtsdo.tk.api.concept;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.constraint.ConstraintBI;
import org.ihtsdo.tk.api.constraint.ConstraintCheckType;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;

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

    ConceptAttributeVersionBI getConceptAttributesActive() throws IOException, ContradictionException;

    Collection<? extends RefexVersionBI<?>> getRefexMembersActive(int refsetNid) throws IOException;

    RefexChronicleBI<?> getRefexMemberForComponentActive(int componentNid) throws IOException;

    /**
     *
     * @return
     * @throws IOException
     * @deprecated use getRefsetMembersActive
     */
    @Deprecated
    Collection<? extends RefexVersionBI<?>> getActiveRefsetMembers()
            throws IOException, ContradictionException;

    Collection<? extends DescriptionVersionBI> getDescriptionsActive() throws IOException, ContradictionException;

    Collection<? extends DescriptionVersionBI> getDescriptionsActive(int typeNid)
            throws IOException, ContradictionException;

    Collection<? extends DescriptionVersionBI> getDescriptionsActive(NidSetBI typeNids)
            throws IOException, ContradictionException;

    Collection<? extends DescriptionVersionBI> getDescriptionsFullySpecifiedActive() throws IOException;

    DescriptionVersionBI getDescriptionFullySpecified() throws IOException, ContradictionException;

    Collection<? extends MediaVersionBI> getMediaActive() throws IOException, ContradictionException;

    Collection<List<Integer>> getNidPathsToRoot() throws IOException;

    Collection<? extends DescriptionVersionBI> getDescriptionsPreferredActive() throws IOException;

    DescriptionVersionBI getDescriptionPreferred() throws IOException, ContradictionException;

    Collection<? extends RefexVersionBI<?>> getRefsetMembersActive() throws IOException, ContradictionException;

    Collection<? extends RelationshipGroupVersionBI> getRelationshipGroups() throws IOException, ContradictionException;

    Collection<? extends RelationshipVersionBI> getRelationshipsTargetActive()
            throws IOException, ContradictionException;

    Collection<? extends RelationshipVersionBI> getRelationshipsTargetActiveIsa()
            throws IOException, ContradictionException;

    /**
     * Returns the concepts representing the sources of the target relationships.
     * @return
     * @throws IOException 
     */
    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConcepts() throws IOException;

    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConcepts(int typeNid) throws IOException;

    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConcepts(NidSetBI typeNids) throws IOException;

    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConceptsActive()
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConceptsActive(int typeNid)
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConceptsActive(NidSetBI typeNids)
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConceptsActiveIsa()
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConceptsIsa() throws IOException;

    Collection<? extends RelationshipVersionBI> getRelationshipsSourceActive()
            throws IOException, ContradictionException;

    Collection<? extends RelationshipVersionBI> getRelationshipsSourceActiveIsa()
            throws IOException, ContradictionException;
    /**
     * Returns the concepts representing the target concept of the source relationships.
     * @return
     * @throws IOException 
     */
    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConcepts() throws IOException;

    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConcepts(int typeNid) throws IOException;

    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConcepts(NidSetBI typeNids) throws IOException;

    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConceptsActive()
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConceptsActive(int typeNid)
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConceptsActive(NidSetBI typeNids)
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConceptsActiveIsa()
            throws IOException, ContradictionException;

    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConceptsIsa() throws IOException;

    int[] getRelationshipsSourceTargetNidsActiveIsa() throws IOException;

    Collection<? extends DescriptionVersionBI> getSynonyms() throws IOException;

    ViewCoordinate getViewCoordinate();

    boolean hasAnnotationMemberActive(int refexNid) throws IOException;

    boolean hasChildren() throws IOException, ContradictionException;

    boolean hasHistoricalRelationships() throws IOException, ContradictionException;

    boolean hasRefexMemberActive(int refexNid) throws IOException;

    boolean hasRefsetMemberForComponentActive(int componentNid) throws IOException;

    boolean isActive() throws IOException;

    boolean isChildOf(ConceptVersionBI childConceptVersion) throws IOException;

    boolean isKindOf(ConceptVersionBI parentConceptVersion) throws IOException, ContradictionException;

    boolean isLeaf() throws IOException;

    boolean isMember(int refexCollectionNid) throws IOException;

    ConceptCB makeBlueprint() throws IOException, ContradictionException, InvalidCAB;
    
    @Override
    ConceptCB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, ContradictionException, InvalidCAB;
}
