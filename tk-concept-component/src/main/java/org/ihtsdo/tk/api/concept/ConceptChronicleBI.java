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
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;
import org.ihtsdo.tk.contradiction.FoundContradictionVersions;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.Set;


/**
 * The Interface ConceptChronicleBI provides methods for interacting with a
 * concept such as getting the concepts components or identifiers. A concept can
 * represent a refset, and the concept or its components can be a referenced
 * component in a refex (the generic term for a refset, annotation, or index
 * annotation). A complete description of these terms and usage can be found in
 * <code>RefexChronicleBI</code>.
 *
 * @see RefexChronicleBI
 * @see ComponentChronicleBI
 */
public interface ConceptChronicleBI extends ComponentChronicleBI<ConceptVersionBI> {

    /**
     * Cancels any uncommitted changes on this concept.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
    void cancel() throws IOException;

    /**
     * Commits any uncommitted changes on this concept.
     *
     * @param changeSetGenerationPolicy the change set generation policy to use
     * when writing changesets for this commit
     * @param changeSetGenerationThreadingPolicy the change set generation
     * threading policy
     * @return <code>true</code>, if the commit was successful
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean commit(ChangeSetGenerationPolicy changeSetGenerationPolicy,
            ChangeSetGenerationThreadingPolicy changeSetGenerationThreadingPolicy)
            throws IOException;

    /**
     * Commits any uncommitted changes on this concept. Use this method if
     * needing to specify if the commit is in the adjudication window or not.
     *
     * @param changeSetGenerationPolicy the change set generation policy
     * @param changeSetGenerationThreadingPolicy the change set generation
     * threading policy
     * @param writeAdjudication set to <code>true</code> if adjudication records
     * should be written, committing in the adjudication window
     * @return <code>true</code>, if the commit was successful
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean commit(ChangeSetGenerationPolicy changeSetGenerationPolicy,
            ChangeSetGenerationThreadingPolicy changeSetGenerationThreadingPolicy,
            boolean writeAdjudication)
            throws IOException;

    /**
     * Returns a longer - more complete - string representation of the
     * chronicle. Useful for diagnostic purposes, not suitable for display.
     *
     * @return the string representation of the chronicle, including ids and
     * versions
     */
    String toLongString();

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the concept attributes of this concept.
     *
     * @return the concept attributes of this concept
     * @throws IOException signals that an I/O exception has occurred
     */
    ConceptAttributeChronicleBI getConceptAttributes() throws IOException;

    /**
     * Gets the active refset member, based on the
     * given<code>viewCoordinate</code>, for which the component, specified by
     * the
     * <code>componentNid</code> is a referenced component.
     *
     *
     * @param viewCoordinate the view coordinate specifying which refset members
     * are active or inactive
     * @param componentNid the nid associated with the component in question
     * @return the active refset member which has the specified component as a
     * referenced component, <code>null</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    RefexVersionBI<?> getRefsetMemberActiveForComponent(ViewCoordinate viewCoordinate, int componentNid)
            throws IOException;

    /**
     * Gets any active refset members, based on the given
     * <code>viewCoordinate</code>, which have this concept or its components as
     * a referenced component.
     *
     * @param viewCoordinate the view coordinate specifying which refset members
     * are active or inactive
     * @return the active refset members which have this concept, or concept's
     * components, as a referenced component, an empty <code>Collection</code>
     * if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends RefexVersionBI<?>> getRefsetMembersActive(ViewCoordinate viewCoordinate) throws IOException;

    /**
     * Gets any active refset members, based on the given
     * <code>viewCoordinate</code>, which have this concept or its components as
     * a referenced component. Only the versions which have a time before the
     * specified
     * <code>cutoffTime</code> will be returned. Use for reporting purposes.
     *
     * @param viewCoordinate the view coordinate specifying which refset members
     * are active or inactive
     * @param cutoffTime the latest time which a returned version can have
     * @return the active refset members that have this concept, or its
     * components, as referenced component and that have a time earlier than the
     * cutoff time, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    public Collection<? extends RefexVersionBI<?>> getRefsetMembersActive(ViewCoordinate viewCoordinate, Long cutoffTime)
            throws IOException;

    /**
     * Gets all of the descriptions on this concept regardless of status.
     *
     * @return all descriptions on this concept
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends DescriptionChronicleBI> getDescriptions() throws IOException;

    /**
     * Gets the last modification sequence. This sequence is incremented any
     * time the chronicle was changed, for example this sequence can be used to
     * determine if the concept has changed since the last layout (if the layout
     * caches sequence numbers).
     *
     * @return the last modification sequence
     */
    long getLastModificationSequence();

    /**
     * Gets any media associated with this concept regardless of status.
     *
     * @return the media associated with this concept, an      * empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends MediaChronicleBI> getMedia() throws IOException;

    /**
     * Gets the refset member, regardless of status, for which the component,
     * specified by the
     * <code>componentNid</code> is a referenced component.
     *
     * @param componentNid the nid associated with the component in question
     * @return the refset member that has the specified component as a
     * referenced component, <code>null</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    RefexChronicleBI<?> getRefsetMemberForComponent(int componentNid) throws IOException;

    /**
     * Returns refset members associated with the refset identified by this
     * concept. Use this method when then this concept is a refset and you wish
     * to find all of the members associated with that refset.
     *
     * @return the refset members associated with this concept, an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends RefexChronicleBI<?>> getRefsetMembers() throws IOException;

    /**
     * Gets the groups of relationships, based on the specified
     * <code>viewCoordinate</codde>, that are associated with this concept.
     * 
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @param viewCoordinate the view coordinate specifying which version of the relationship group to return
     * @return any specified relationship groups associated with this concept,
     * an empty <code>Collection</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    Collection<? extends RelationshipGroupVersionBI> getRelationshipOutgoingGroups(ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException;

    /**
     * Gets all target relationships regardless of status.
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return the relationships target
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends RelationshipChronicleBI> getRelationshipsIncoming() throws IOException;

    /**
     * Gets all source relationships regardless of status.
     *
     * <p>In the relationship A is-a B, the concept A is a source and the concept B
     * is a target. The relationship A-B is an outgoing relationship on concept
     * A and an incoming relationship on concept B.
     *
     * @return the relationships source
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends RelationshipChronicleBI> getRelationshipsOutgoing() throws IOException;

    /**
     * Gets any versions, based on the given
     * <code>viewCoordinate</code>, of this concept that are in contradiction.
     *
     * @param viewCoordinate the view coordinate specifying which versions of
     * the concept
     * @return any spcified versions that are in contradiction in the form of *
     * a <code>FoundContradictionsVersion</code>, which represents the version
     * in contradiction and the reason for the contradiction
     */
    FoundContradictionVersions getVersionsInContradiction(ViewCoordinate viewCoordinate);

    /**
     * Gets all component nids on this concept that are associated with the
     * given
     * <code>stampNids</code>.
     *
     * @param stampNids the stamp nids associated with the desired component
     * nids
     * @return any component nids on this concept associated with the given
     * stamp nids, an empty <code>Set</code> if none are found
     * @throws IOException signals that an I/O exception has occurred
     */
    Set<Integer> getAllNidsForStamps(Set<Integer> stampNids) throws IOException;

    /**
     * Checks for any active refset members, based on the given
     * <code>viewCoordinate</code>, which have the component, specified by the
     * given
     * <code>componentNid</code>, as a referenced component.
     *
     * @param viewCoordinate the view coordinate specifying which refset members
     * are active or inactive
     * @param componentNid the nid of the component in question
     * @return <code>true</code>, if any active refset members are found which
     * have the component as a referenced component
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean hasRefsetMemberActiveForComponent(ViewCoordinate viewCoordinate, int componentNid) throws IOException;

    /**
     * Checks if this concept is an annotation style refex.
     *
     * @return <code>true</code>, if the concept is annotation
     * @throws IOException signals that an I/O exception has occurred
     * @see RefexChronicleBI
     */
    boolean isAnnotationStyleRefex() throws IOException;

    /**
     * Checks if this concept is an indexed annotation style refex..
     *
     * @return <code>true</code>, if concept is an indexed annotation
     * @throws IOException signals that an I/O exception has occurred
     * @see RefexChronicleBI
     */
    boolean isAnnotationIndex() throws IOException;

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the concept as an annotation style refex.
     *
     * @param annotationSyleRefex set to <code>true</code> to mark the concept
     * as an annotation style refex
     * @see RefexChronicleBI
     */
    void setAnnotationStyleRefex(boolean annotationSyleRefex);

    /**
     * Sets the concept as an indexed annotation style refex.
     *
     * @param annotationIndex set to <code>true</code> to mark the concept as an
     * indexed annotation style refex
     * @throws IOException signals that an I/O exception has occurred
     * @see RefexChronicleBI
     */
    void setAnnotationIndex(boolean annotationIndex) throws IOException;

    /**
     * Gets all the nids associated with this concept or its components.
     *
     * @return all the nids found on this concept
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<Integer> getAllNids() throws IOException;
}
