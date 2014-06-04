/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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

/**
 * The Interface ConceptVersionBI provides methods for interacting with a
 * particular version of a concept and for creating new, editable versions.
 *
 * <p>In the relationship A is-a B, the concept A is a source and the concept B is
 * a target. The relationship A-B is an outgoing relationship on concept A and
 * an incoming relationship on concept B.
 *
 * @see ComponentVersionBI
 */
public interface ConceptVersionBI extends ComponentVersionBI, ConceptChronicleBI {

    /**
     * Checks if this concept satisfies the given
     * <code>constraint</code>. The constraint check type can be: ignore,
     * equals, kind of, or regex.
     *
     * @param constraint the constraint to check
     * @param subjectCheck the subject check type
     * @param propertyCheck the property check type
     * @param valueCheck the value check type
     * @return <code>true</code>, if the concept meets the constraint
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException indicates more than one version was found
     * for a given position
     */
    boolean satisfies(ConstraintBI constraint, ConstraintCheckType subjectCheck,
            ConstraintCheckType propertyCheck, ConstraintCheckType valueCheck)
            throws IOException, ContradictionException;

    //~--- get methods ---------------------------------------------------------
    /**
     *
     * @return the concept chronicle that contains this version
     */
    @Override
    ConceptChronicleBI getChronicle();

    /**
     * Gets active concept attributes for this version.
     *
     * @return active concept attributes for this version, <code>null</code> if
     * none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    ConceptAttributeVersionBI getConceptAttributesActive() throws IOException, ContradictionException;

    /**
     * Gets the active refex members, belonging to the refset specified by the
     * <code>refsetNid</code>, for which the concept is a referenced component
     * or that is a member of this concept, if the concept is a refset.
     *
     * @param refsetNid the nid of the refset in question
     * @return the specified active refex members, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends RefexVersionBI<?>> getRefexMembersActive(int refsetNid) throws IOException;

    /**
     * Gets any active refex members, for which the component, specified by the
     * <code>componentNid</code>, is a referenced component.
     *
     * @param componentNid the nid of the component in question
     * @return the active refex members for which the specified component is a
     * referenced component, <code>null</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    RefexChronicleBI<?> getRefexMemberActiveForComponent(int componentNid) throws IOException;

    /**
     * Gets any active refset members, for which the concept is a referenced
     * component or that is a member of this concept, if the concept is a
     * refset.
     *
     * @return any active refset members for this version, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if there are differing versions of the
     * active refset members returned for this version
     * @deprecated use getRefsetMembersActive()
     */
    @Deprecated
    Collection<? extends RefexVersionBI<?>> getActiveRefsetMembers()
            throws IOException, ContradictionException;

    /**
     * Gets the active descriptions for this version.
     *
     * @return the active descriptions, an empty <code>Collection</code> if none
     * are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if differing active descriptions are found
     * for this version
     */
    Collection<? extends DescriptionVersionBI> getDescriptionsActive() throws IOException, ContradictionException;

    /**
     * Gets active descriptions of the type specified by the
     * <code>typeNid</code>.
     *
     * @param typeNid the nid associated with the desired type of description
     * @return the specified active descriptions, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if differing active descriptions are found
     * for this version
     */
    Collection<? extends DescriptionVersionBI> getDescriptionsActive(int typeNid)
            throws IOException, ContradictionException;

    /**
     * Gets active descriptions of the types specified by the
     * <code>typeNids</code>.
     *
     * @param typeNids the nids associated with the desired types of
     * descriptions
     * @return the specified active descriptions, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if differing active descriptions are found
     * for this version
     */
    Collection<? extends DescriptionVersionBI> getDescriptionsActive(NidSetBI typeNids)
            throws IOException, ContradictionException;

    /**
     * Gets the active fully specified descriptions for this version.
     *
     * @return the active fully specified descriptions, more than one can be
     * returned if there were simultaneous edits of the fully specified name, an
     * empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends DescriptionVersionBI> getDescriptionsFullySpecifiedActive() throws IOException;

    /**
     * Gets the fully specified description for this version.
     *
     * @return the fully specified description for this version
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one fully specified
     * description is found for this version
     */
    DescriptionVersionBI getDescriptionFullySpecified() throws IOException, ContradictionException;

    /**
     * Gets active media associated with this version.
     *
     * @return the active media for this version, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if differing media is found for this
     * version
     */
    Collection<? extends MediaVersionBI> getMediaActive() throws IOException, ContradictionException;

    /**
     * Gets a list of concept nids which represent the sequence of concepts to
     * navigate from this concept version to the root. Each list in the
     * collection represents an independent path.
     *
     * @return a collection of lists representing the paths from this concept to
     * the root
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<List<Integer>> getNidPathsToRoot() throws IOException;

    /**
     * Gets the active preferred descriptions for this version.
     *
     * @return the active preferred descriptions, more than one can be returned
     * if there were simultaneous edits of the preferred term, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends DescriptionVersionBI> getDescriptionsPreferredActive() throws IOException;

    /**
     * Gets the preferred description for this version.
     *
     * @return the preferred description for this version, <code>null</code> if
     * none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one preferred description was
     * found for this version
     */
    DescriptionVersionBI getDescriptionPreferred() throws IOException, ContradictionException;

    /**
     * Gets any active refset members, for which the concept is a referenced
     * component or that is a member of this concept, if the concept is a
     * refset.
     *
     * @return active refset members for this version, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if differing refset members are found for
     * this version
     */
    Collection<? extends RefexVersionBI<?>> getRefsetMembersActive() throws IOException, ContradictionException;
    
    /**
     * Gets any nids of any active refset members which have this concept or its components as
     * a referenced component.
     *
     * @return the nids of any active refset members which have this concept, or concept's
     * components, as a referenced component, an empty <code>Collection</code>
     * if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<Integer> getRefsetMemberNidsActive() throws IOException, ContradictionException;

    /**
     * Gets all the relationship groups for this version regardless of status.
     *
     * @return all the relationship groups for this version, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if differing relationship groups are found
     * for this version
     */
    Collection<? extends RelationshipGroupVersionBI> getRelationshipGroups() throws IOException, ContradictionException;

    /**
     * Gets active incoming relationships for this version.
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return the active target relationships, an empty <code>Collection</code>
     * if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if different target relationships are
     * found for this version
     */
    Collection<? extends RelationshipVersionBI> getRelationshipsIncomingActive()
            throws IOException, ContradictionException;

    /**
     * Gets the active incoming "is a" relationships for this version.
     *
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return the active target "is a" relationships, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if differing "is a" relationships are
     * found for this version
     */
    Collection<? extends RelationshipVersionBI> getRelationshipsIncomingActiveIsa()
            throws IOException, ContradictionException;

    /**
     * Returns the concepts representing the sources of the incoming
     * relationships for this version. All status values will be returned.
     *
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return the concepts representing the destinations of the target
     * relationships for this version, an empty <code>Collection</code> if none
     * are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConcepts() throws IOException;

    /**
     * Returns the concepts representing the sources of the incoming
     * relationships of the type specified by the
     * <code>typeNid</code> for this version. All status values will be
     * returned.
     *
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @param typeNid the nid representing the type of the relationship in
     * question
     * @return the concepts representing the destinations of the target
     * relationships for this version, an empty <code>Collection</code> if none
     * are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConcepts(int typeNid) throws IOException;

    /**
     * Returns the concepts representing the sources of the incoming
     * relationships of the type specified by the
     * <code>typeNids</code> for this version. All status values will be
     * returned.
     *
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @param typeNids the nids representing the desired types of relationships
     * @return the concepts representing the destinations of the target
     * relationships for this version, an empty <code>Collection</code> if none
     * are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConcepts(NidSetBI typeNids) throws IOException;

    /**
     * Returns the concepts representing the sources of the incoming target
     * relationships for this version.
     *
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return the concepts representing the destinations of the active target
     * relationships for this version, an empty <code>Collection</code> if none
     * are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if different destination concepts are
     * returned for this version
     */
    Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConceptsActive()
            throws IOException, ContradictionException;

    /**
     * Returns the concepts representing the sources of the active incoming
     * relationships of the type specified by the
     * <code>typeNid</code> for this version.
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @param typeNid the nid representing the desired type of relationship
     * @return the concepts representing the destination of the specified active
     * target relationships for this version, an empty <code>Collection</code>
     * if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if different destination concepts are
     * returned for this version
     */
    Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConceptsActive(int typeNid)
            throws IOException, ContradictionException;

    /**
     * Returns the concepts representing the sources of the active incoming
     * relationships of the type specified by the
     * <code>typeNids</code> for this version.
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @param typeNids the nids representing the desired types of relationships
     * @return the concepts representing the destination of the specified active
     * target relationships for this version, an empty <code>Collection</code>
     * if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if different destination concepts are
     * returned for this version
     */
    Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConceptsActive(NidSetBI typeNids)
            throws IOException, ContradictionException;

    /**
     * Returns the concepts representing the sources of the active "is a"
     * incoming relationships for this version.
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return the concepts representing the destination of the active "is a"
     * target relationships for this version, an empty <code>Collection</code>
     * if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if different destination concepts are
     * returned for this version
     */
    Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConceptsActiveIsa()
            throws IOException, ContradictionException;

    /**
     * Returns the concepts representing the sources of the "is a" incoming
     * relationships for this version regardless of status.
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return the concepts representing the destination of the target
     * relationships for this version, an empty <code>Collection</code> if none
     * are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConceptsIsa() throws IOException;

    /**
     * Gets active outgoing relationships for this version. 
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return the active source relationships, an empty <code>Collection</code>
     * if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if different source relationships are
     * found for this version
     */
    Collection<? extends RelationshipVersionBI> getRelationshipsOutgoingActive()
            throws IOException, ContradictionException;

    /**
     * Gets the active outgoing "is a" relationships for this version.
     *
     *
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return the active source "is a" relationships, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if differing "is a" relationships are
     * found for this version
     */
    Collection<? extends RelationshipVersionBI> getRelationshipsOutgoingActiveIsa()
            throws IOException, ContradictionException;

    /**
     * Returns the concepts representing the targets of the outgoing relationships
     * for this version. All status values will be returned.
     *
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return the concepts representing the origins of the source relationships
     * for this version, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConcepts() throws IOException;

    /**
     * Returns the concepts representing the targets of the outgoing relationships
     * of the type specified by the
     * <code>typeNid</code> for this version. All status values will be
     * returned.
     *
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @param typeNid the nid representing the type of the relationship in
     * question
     * @return the concepts representing the origins of the source relationships
     * for this version, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConcepts(int typeNid) throws IOException;

    /**
     * Returns the concepts representing the targets of the outgoing relationships
     * of the type specified by the
     * <code>typeNids</code> for this version. All status values will be
     * returned.
     *
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @param typeNids the nids representing the desired types of relationships
     * @return the concepts representing the origins of the source relationships
     * for this version, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConcepts(NidSetBI typeNids) throws IOException;

    /**
     * Returns the concepts representing the targets of the active outgoing
     * relationships for this version.
     *
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return the concepts representing the origins of the active source
     * relationships for this version, an empty <code>Collection</code> if none
     * are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if different destination concepts are
     * returned for this version
     */
    Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConceptsActive()
            throws IOException, ContradictionException;

    /**
     * Returns the concepts representing the targets of the active outgoing
     * relationships of the type specified by the
     * <code>typeNid</code> for this version.
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @param typeNid the nid representing the desired type of relationship
     * @return the concepts representing the origins of the specified active
     * sources relationships for this version, an empty <code>Collection</code>
     * if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if different destination concepts are
     * returned for this version
     */
    Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConceptsActive(int typeNid)
            throws IOException, ContradictionException;

    /**
     * Returns the concepts representing the targets of the active outgoing
     * relationships of the type specified by the
     * <code>typeNids</code> for this version.
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @param typeNids the nids representing the desired types of relationships
     * @return the concepts representing the origins of the specified active
     * source relationships for this version, an empty <code>Collection</code>
     * if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if different destination concepts are
     * returned for this version
     */
    Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConceptsActive(NidSetBI typeNids)
            throws IOException, ContradictionException;

    /**
     * Returns the concepts representing the targets of the active "is a" outgoing
     * relationships for this version.
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return the concepts representing the origin of the active "is a" source
     * relationships for this version, an empty <code>Collection</code> if none
     * are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if different destination concepts are
     * returned for this version
     */
    Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConceptsActiveIsa()
            throws IOException, ContradictionException;

    /**
     * Returns the concepts representing the target of the "is a" outgoing
     * relationships for this version regardless of status.
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return the concepts representing the origins of the source relationships
     * for this version, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConceptsIsa() throws IOException;

    /**
     * Returns the concepts representing the target of the "is a" outgoing
     * relationships for this version regardless of status.
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return an array of nids associated with the origins of the source
     * relationships for this version, an empty <code>Collection</code> if none
     * are found
     * @throws IOException signals that an I/O exception has occurred
     */
    int[] getRelationshipsOutgoingTargetNidsActiveIsa() throws IOException;

    /**
     * Gets the synonyms or this concept version.
     *
     * @return the synonyms associated with this concept version
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends DescriptionVersionBI> getSynonyms() throws IOException;

    /**
     * Gets the view coordinate that this version is based on.
     *
     * @return the view coordinate used for this version
     */
    ViewCoordinate getViewCoordinate();

    /**
     * Checks for active annotations members in the refex specified by the given
     * <code>refexNid</code> on this version of the concept.
     *
     * @param refexNid the refex nid
     * @return <code>true</code>, if successful
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean hasAnnotationMemberActive(int refexNid) throws IOException;

    /**
     * Checks if this version of the concept has child concepts.
     *
     * @return <code>true</code>, if the concept has children
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if differing children are found for this
     * version
     */
    boolean hasChildren() throws IOException, ContradictionException;

    /**
     * Checks if this version of the concept has historical relationships, such
     * as "was a", "maybe a."
     *
     * @return <code>true</code>, if the concept has historical relationships
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if differing historical relationships are
     * found for this version
     */
    boolean hasHistoricalRelationships() throws IOException, ContradictionException;

    /**
     * Checks for active refex members on this version of the concept.
     *
     * @param refexNid the nid associated with the refex in quesion
     * @return <code>true</code>, if successful
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean hasRefexMemberActive(int refexNid) throws IOException;

    /**
     * Checks if this version of the concept has active refset members with a
     * referenced component matching the component specified by the given
     * <code>componentNid</code>.
     *
     * @param componentNid the nid of the referenced component in question
     * @return <code>true</code>, if this version has active refset members for
     * the specified referenced component
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean hasRefsetMemberActiveForComponent(int componentNid) throws IOException;

    /**
     * Checks if this version of the concept is active.
     *
     * @return <code>true</code>, if the status of this version of the concept
     * is active
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean isActive() throws IOException;

    /**
     * Checks if this version the concept is a direct child of the specified
     * <code>parentConceptVersion</code>.
     *
     * @param parentConceptVersion the version of the parent concept to test
     * @return <code>true</code>, if this version is a child of the specified
     * parent version
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean isChildOf(ConceptVersionBI parentConceptVersion) throws IOException;

    /**
     * Checks if this version the concept is a kind of the specified
     * <code>parentConceptVersion</code>. Considers all possible children of the
     * parent.
     *
     * @param parentConceptVersion the version of the parent concept to test
     * @return <code>true</code>, if this version is a kind of the specified
     * parent version
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    boolean isKindOf(ConceptVersionBI parentConceptVersion) throws IOException, ContradictionException;

    /**
     * Checks this version of the concept is a leaf in the taxonomy. Used in
     * creating the taxonomy, to determine which concepts are leaves or not.
     *
     * @return <code>true</code>, if the concept is a leaf
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean isLeaf() throws IOException;

    /**
     * Checks if this version of the concept is member of the refex specified by
     * the
     * <code>refexCollectionNid</code>.
     *
     * @param refexCollectionNid the nid of the refex collection in question
     * @return <code>true</code>, if this version is a member
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean isMember(int refexCollectionNid) throws IOException;

    ConceptCB makeBlueprint() throws IOException, ContradictionException, InvalidCAB;

    /**
     * @param viewCoordinate the view coordinate specifying which version of the
     * concept to make a blueprint of
     * @return the concept blueprint, which can be constructed to create a <code>ConceptChronicleBI</code>
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of the
     * description was returned for the specified view coordinate
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint
     */
    @Override
    ConceptCB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, ContradictionException, InvalidCAB;
}
