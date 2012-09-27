/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

// TODO: Auto-generated Javadoc
/**
 * The Interface ConceptVersionBI.
 */
public interface ConceptVersionBI extends ComponentVersionBI, ConceptChronicleBI {

    /**
     * Satisfies.
     *
     * @param constraint the constraint
     * @param subjectCheck the subject check
     * @param propertyCheck the property check
     * @param valueCheck the value check
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    boolean satisfies(ConstraintBI constraint, ConstraintCheckType subjectCheck,
            ConstraintCheckType propertyCheck, ConstraintCheckType valueCheck)
            throws IOException, ContradictionException;

    //~--- get methods ---------------------------------------------------------
    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ComponentVersionBI#getChronicle()
     */
    @Override
    ConceptChronicleBI getChronicle();

    /**
     * Gets the concept attributes active.
     *
     * @return the concept attributes active
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    ConceptAttributeVersionBI getConceptAttributesActive() throws IOException, ContradictionException;

    /**
     * Gets the refex members active.
     *
     * @param refsetNid the refset nid
     * @return the refex members active
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends RefexVersionBI<?>> getRefexMembersActive(int refsetNid) throws IOException;

    /**
     * Gets the refex member for component active.
     *
     * @param componentNid the component nid
     * @return the refex member for component active
     * @throws IOException Signals that an I/O exception has occurred.
     */
    RefexChronicleBI<?> getRefexMemberForComponentActive(int componentNid) throws IOException;

    /**
     * Gets the active refset members.
     *
     * @return the active refset members
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     * @deprecated use getRefsetMembersActive
     */
    @Deprecated
    Collection<? extends RefexVersionBI<?>> getActiveRefsetMembers()
            throws IOException, ContradictionException;

    /**
     * Gets the descriptions active.
     *
     * @return the descriptions active
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends DescriptionVersionBI> getDescriptionsActive() throws IOException, ContradictionException;

    /**
     * Gets the descriptions active.
     *
     * @param typeNid the type nid
     * @return the descriptions active
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends DescriptionVersionBI> getDescriptionsActive(int typeNid)
            throws IOException, ContradictionException;

    /**
     * Gets the descriptions active.
     *
     * @param typeNids the type nids
     * @return the descriptions active
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends DescriptionVersionBI> getDescriptionsActive(NidSetBI typeNids)
            throws IOException, ContradictionException;

    /**
     * Gets the descriptions fully specified active.
     *
     * @return the descriptions fully specified active
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends DescriptionVersionBI> getDescriptionsFullySpecifiedActive() throws IOException;

    /**
     * Gets the description fully specified.
     *
     * @return the description fully specified
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    DescriptionVersionBI getDescriptionFullySpecified() throws IOException, ContradictionException;

    /**
     * Gets the media active.
     *
     * @return the media active
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends MediaVersionBI> getMediaActive() throws IOException, ContradictionException;

    /**
     * Gets the native id paths to root.
     *
     * @return the native id paths to root
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<List<Integer>> getNidPathsToRoot() throws IOException;

    /**
     * Gets the descriptions preferred active.
     *
     * @return the descriptions preferred active
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends DescriptionVersionBI> getDescriptionsPreferredActive() throws IOException;

    /**
     * Gets the description preferred.
     *
     * @return the description preferred
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    DescriptionVersionBI getDescriptionPreferred() throws IOException, ContradictionException;

    /**
     * Gets the refset members active.
     *
     * @return the refset members active
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends RefexVersionBI<?>> getRefsetMembersActive() throws IOException, ContradictionException;

    /**
     * Gets the relationship groups.
     *
     * @return the relationship groups
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends RelationshipGroupVersionBI> getRelationshipGroups() throws IOException, ContradictionException;

    /**
     * Gets the relationships target active.
     *
     * @return the relationships target active
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends RelationshipVersionBI> getRelationshipsTargetActive()
            throws IOException, ContradictionException;

    /**
     * Gets the relationships target active isa.
     *
     * @return the relationships target active isa
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends RelationshipVersionBI> getRelationshipsTargetActiveIsa()
            throws IOException, ContradictionException;

    /**
     * Returns the concepts representing the sources of the target relationships.
     *
     * @return the relationships target source concepts
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConcepts() throws IOException;

    /**
     * Gets the relationships target source concepts.
     *
     * @param typeNid the type nid
     * @return the relationships target source concepts
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConcepts(int typeNid) throws IOException;

    /**
     * Gets the relationships target source concepts.
     *
     * @param typeNids the type nids
     * @return the relationships target source concepts
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConcepts(NidSetBI typeNids) throws IOException;

    /**
     * Gets the relationships target source concepts active.
     *
     * @return the relationships target source concepts active
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConceptsActive()
            throws IOException, ContradictionException;

    /**
     * Gets the relationships target source concepts active.
     *
     * @param typeNid the type nid
     * @return the relationships target source concepts active
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConceptsActive(int typeNid)
            throws IOException, ContradictionException;

    /**
     * Gets the relationships target source concepts active.
     *
     * @param typeNids the type nids
     * @return the relationships target source concepts active
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConceptsActive(NidSetBI typeNids)
            throws IOException, ContradictionException;

    /**
     * Gets the relationships target source concepts active isa.
     *
     * @return the relationships target source concepts active isa
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConceptsActiveIsa()
            throws IOException, ContradictionException;

    /**
     * Gets the relationships target source concepts isa.
     *
     * @return the relationships target source concepts isa
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends ConceptVersionBI> getRelationshipsTargetSourceConceptsIsa() throws IOException;

    /**
     * Gets the relationships source active.
     *
     * @return the relationships source active
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends RelationshipVersionBI> getRelationshipsSourceActive()
            throws IOException, ContradictionException;

    /**
     * Gets the relationships source active isa.
     *
     * @return the relationships source active isa
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends RelationshipVersionBI> getRelationshipsSourceActiveIsa()
            throws IOException, ContradictionException;
    
    /**
     * Returns the concepts representing the target concept of the source relationships.
     *
     * @return the relationships source target concepts
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConcepts() throws IOException;

    /**
     * Gets the relationships source target concepts.
     *
     * @param typeNid the type nid
     * @return the relationships source target concepts
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConcepts(int typeNid) throws IOException;

    /**
     * Gets the relationships source target concepts.
     *
     * @param typeNids the type nids
     * @return the relationships source target concepts
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConcepts(NidSetBI typeNids) throws IOException;

    /**
     * Gets the relationships source target concepts active.
     *
     * @return the relationships source target concepts active
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConceptsActive()
            throws IOException, ContradictionException;

    /**
     * Gets the relationships source target concepts active.
     *
     * @param typeNid the type nid
     * @return the relationships source target concepts active
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConceptsActive(int typeNid)
            throws IOException, ContradictionException;

    /**
     * Gets the relationships source target concepts active.
     *
     * @param typeNids the type nids
     * @return the relationships source target concepts active
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConceptsActive(NidSetBI typeNids)
            throws IOException, ContradictionException;

    /**
     * Gets the relationships source target concepts active isa.
     *
     * @return the relationships source target concepts active isa
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConceptsActiveIsa()
            throws IOException, ContradictionException;

    /**
     * Gets the relationships source target concepts isa.
     *
     * @return the relationships source target concepts isa
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends ConceptVersionBI> getRelationshipsSourceTargetConceptsIsa() throws IOException;

    /**
     * Gets the relationships source target nids active isa.
     *
     * @return the relationships source target nids active isa
     * @throws IOException Signals that an I/O exception has occurred.
     */
    int[] getRelationshipsSourceTargetNidsActiveIsa() throws IOException;

    /**
     * Gets the synonyms.
     *
     * @return the synonyms
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends DescriptionVersionBI> getSynonyms() throws IOException;

    /**
     * Gets the view coordinate.
     *
     * @return the view coordinate
     */
    ViewCoordinate getViewCoordinate();

    /**
     * Checks for annotation member active.
     *
     * @param refexNid the refex nid
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean hasAnnotationMemberActive(int refexNid) throws IOException;

    /**
     * Checks for children.
     *
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    boolean hasChildren() throws IOException, ContradictionException;

    /**
     * Checks for historical relationships.
     *
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    boolean hasHistoricalRelationships() throws IOException, ContradictionException;

    /**
     * Checks for refex member active.
     *
     * @param refexNid the refex nid
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean hasRefexMemberActive(int refexNid) throws IOException;

    /**
     * Checks for refset member for component active.
     *
     * @param componentNid the component nid
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean hasRefsetMemberForComponentActive(int componentNid) throws IOException;

    /**
     * Checks if is active.
     *
     * @return true, if is active
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean isActive() throws IOException;

    /**
     * Checks if is child of.
     *
     * @param childConceptVersion the child concept version
     * @return true, if is child of
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean isChildOf(ConceptVersionBI childConceptVersion) throws IOException;

    /**
     * Checks if is kind of.
     *
     * @param parentConceptVersion the parent concept version
     * @return true, if is kind of
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    boolean isKindOf(ConceptVersionBI parentConceptVersion) throws IOException, ContradictionException;

    /**
     * Checks if is leaf.
     *
     * @return true, if is leaf
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean isLeaf() throws IOException;

    /**
     * Checks if is member.
     *
     * @param refexCollectionNid the refex collection nid
     * @return true, if is member
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean isMember(int refexCollectionNid) throws IOException;

    /**
     * Make blueprint.
     *
     * @return the concept cb
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     * @throws InvalidCAB the invalid cab
     */
    ConceptCB makeBlueprint() throws IOException, ContradictionException, InvalidCAB;
    
    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ComponentVersionBI#makeBlueprint(org.ihtsdo.tk.api.coordinate.ViewCoordinate)
     */
    @Override
    ConceptCB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, ContradictionException, InvalidCAB;
}
