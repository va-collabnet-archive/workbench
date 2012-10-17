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
package org.ihtsdo.tk.api;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


/**
 * The Interface ComponentBI provides terminology generic methods for editing or
 * creating a component. A component is the generic term for: a concept
 * attribute, description, relationship, refex, media, or concept.
 */
public interface ComponentBI {

    /**
     * Adds an annotation to a component.
     *
     * @param annotation the annotation to add
     * @return <code>true</code>, if the addition was successful
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException;

    /**
     * Returns a string representing a description on a component. The type of
     * description returned is based on the description preferences of the user,
     * set in the <code>viewCoordinate</code>
     *
     * @return the component's description
     * @see ViewCoordinate
     */
    String toUserString();

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the additional identifiers, other than the primordial UUID, of a component.
     * This method would be used to get the SCT ID of a component.
     *
     * @return the additional ids of the component
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends IdBI> getAdditionalIds() throws IOException;

    /**
     * Gets the all identifiers of a component.
     *
     * @return all the ids of the component
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends IdBI> getAllIds() throws IOException;

    /**
     * Gets the annotations on a component.
     *
     * @return the annotations of the component
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends RefexChronicleBI<?>> getAnnotations() throws IOException;

    /**
     * Gets the concept nid associated with a component. This is not the nid of the component, rather the
     * nid of the enclosing concept,the concept which this component is a part of. 
     *
     * @return the enclosing concept nid
     */
    int getConceptNid();

    /**
     * Returns the active annotations on a component based on the given
     * <code>viewCoordinate</code>.
     *
     * @param viewCoordinate the view coordinate specifying which annotations are active or inactive
     * @return the specified active annotations on the component
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate viewCoordinate) throws IOException;

    /**
     * Gets active annotations for the refex, specified by the <code>refexNid</code>, on a component based on
     * the given
     * <code>viewCoordiante</code>.
     *
     * @param viewCoordinate the view coordinate specifying which annotations are active or inactive
     * @param refexNid the nid associated with the desired refex
     * @return the specified active annotations
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends RefexVersionBI<?>> getAnnotationMembersActive(ViewCoordinate viewCoordinate, int refexNid)
            throws IOException;

    /**
     * Gets the active annotations on a component.
     *
     * @param viewCoordinate the view coordinate specifying which annotations are active or inactive
     * @return the specified active annotations
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated use getAnnotationsActive(ViewCoordinate viewCoordinate)
     */
    Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate viewCoordinate) throws IOException;

    /**
     * Gets active annotations for the a given refex on the component based on
     * the given
     * <code>ViewCoordiante</code> and the refex nid.
     *
     * @param viewCoordinate the view coordinate specifying which annotations are active or inactive
     * @param refexNid the nid associated with the desired refex
     * @return the specified active annotations
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated use getAnnotationMembersActive(ViewCoordinate viewCoordinate, int refexNid)
     */
    Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate viewCoordinate, int refexNid)
            throws IOException;

    /**
     * Returns any annotations on the component, or any members that are a
     * "referenced component" of the refset specified by the <code>refexNid</code> and based on the given
     * <code>viewCoordinate</code>. This does not return the
     * refset members as a whole, as a refset is a concept not a component.
     *
     * @param viewCoordinate the view coordinate specifying which annotations are active or inactive
     * @param refexNid the nid associated with the desired refex
     * @return the spcified active refex members
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate viewCoordinate, int refexNid)
            throws IOException;

    /**
     * Returns any active annotations on the component, or any active
     * "referenced components" of refsets based on the given
     * <code>viewCoordinate</code>. This does not return the refset members as a
     * whole, as a refset is a concept not a component.
     *
     * @param viewCoordinate the view coordinate specifying which annotations are active or inactive
     * @return the spcified active refexes
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends RefexVersionBI<?>> getRefexesActive(ViewCoordinate viewCoordinate) throws IOException;

    /**
     * Returns any active annotations on the component, or any active
     * "referenced components" of the refset spcified by the <code>refexNid</code> and based on the given
     * <code>ViewCoordinate</code> . This does not return the
     * refset members as a whole, as a refset is a concept not a component.
     *
     * @param viewCoordinate the view coordinate specifying which annotations are active or inactive
     * @param refexNid the nid associated with the desired refex
     * @return the spcified active refexes
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated use getRefexMembersActive(ViewCoordinate viewCoordinate, int refexNid)
     */
    Collection<? extends RefexVersionBI<?>> getActiveRefexes(ViewCoordinate viewCoordinate, int refexNid)
            throws IOException;

    /**
     * Returns any inactive annotations on the component, or any inactive
     * "referenced components" of refsets based on the given
     * <code>ViewCoordinate</code>. This does not return the refset members as a
     * whole, as a refset is a concept not a component.
     *
     * @param viewCoordinate the view coordinate specifying which annotations are active or inactive
     * @return the spcified inactive refexes
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends RefexVersionBI<?>> getRefexesInactive(ViewCoordinate viewCoordinate) throws IOException;

    /**
     * Gets the native identifier associated with a component.
     *
     * @return the native id of the component
     */
    int getNid();

    /**
     * Gets the primordial uuid of a component. The primordial uuid is the uuid associated with the
     * first version of a component. More than one uuid can be associated with a
     * component, but each component will have only one primordial uuid.
     *
     * @return the primordial if known. The IUnknown UUID
     * (00000000-0000-0000-C000-000000000046) if not known.
     */
    UUID getPrimUuid();

    /**
     * Returns any annotations on a component, or any "referenced components"
     * of a refset based on the given<code>refexNid</code>. This does not return the refset
     * members as a whole, as a refset is a concept not a component.
     *
     * @param refexNid the nid associated with the desired refex
     * @return the specified refex members
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refexNid) throws IOException;

    /**
     * Returns any active annotations on a component, or any active
     * "referenced components" of a refset. This does not return the refset
     * members as a whole, as a refset is a concept not a component.
     *
     * @return the refexes on the component
     * @throws IOException signals that an I/O exception has occurred
     */
    Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException;

    /**
     * Returns any annotations on the component, or any "referenced components"
     * of a refset based on the given <code>refexNid</code>. This does not return the refset
     * members as a whole, as a refset is a concept not a component.
     *
     * @param refexNid the nid of the desired refex
     * @return the specified refexes
     * @throws IOException signals that an I/O exception has occurred
     * @deprecated use getRefexMembers
     */
    @Deprecated
    Collection<? extends RefexChronicleBI<?>> getRefexes(int refexNid) throws IOException;

    /**
     * Gets the UUIDs associated with a component.
     *
     * @return the UUIDs for the component
     */
    List<UUID> getUUIDs();

    /**
     * Checks if the component has active annotations for the specified <code>refexNid</code>
     * and based on the given
     * <code>viewCoordinate</code>.
     *
     * @param viewCoordinate the view coordinate specifying which annotations are active or inactive
     * @param refexNid the nid of the desired refex
     * @return <code>true</code>, if the component has active annotations
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean hasAnnotationMemberActive(ViewCoordinate viewCoordinate, int refexNid) throws IOException;

    /**
     * Checks if the component has active annotations, or is a referenced
     * component, for the specified <code>refexNid</code> and based on the given
     * <code>viewCoordinate</code>.
     *
     * @param viewCoordinate the view coordinate specifying which annotations are active or inactive
     * @param refexNid the nid of the desired refex
     * @return <code>true</code>, if the component has active annotation or is an active referenced component
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean hasRefexMemberActive(ViewCoordinate viewCoordinate, int refexNid) throws IOException;
}
