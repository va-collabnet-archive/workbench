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

// TODO: Auto-generated Javadoc
/**
 * The Interface ComponentBI for the
 * {@link org.ihtsdo.concept.component.ConceptComponent} Class. A component is
 * the generic term for: a concept attribute, description, relationship, refex,
 * media, or concept.
 */
public interface ComponentBI {

    /**
     * Adds the annotation.
     *
     * @param annotation the annotation
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException;

    /**
     * Returns a string representing a description on the component. The type of
     * description returned is based on the description preferences of the user.
     *
     * @return the components description
     */
    String toUserString();

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the additional ids.
     *
     * @return the additional ids
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends IdBI> getAdditionalIds() throws IOException;

    /**
     * Gets the all ids.
     *
     * @return the all ids
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends IdBI> getAllIds() throws IOException;

    /**
     * Gets the annotations.
     *
     * @return the annotations
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends RefexChronicleBI<?>> getAnnotations() throws IOException;

    /**
     * Gets the concept nid.
     *
     * @return the concept nid
     */
    int getConceptNid();

    /**
     * Returns the annotations on the component.
     *
     * @param viewCoordinate the view coordinate
     * @return the annotations active
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate viewCoordinate) throws IOException;

    /**
     * Gets the annotation members active.
     *
     * @param viewCoordinate the view coordinate
     * @param refexNid the refex nid
     * @return the annotation members active
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends RefexVersionBI<?>> getAnnotationMembersActive(ViewCoordinate viewCoordinate, int refexNid)
            throws IOException;

    /**
     * Gets the active annotations.
     *
     * @param viewCoordinate the view coordinate
     * @return the active annotations
     * @throws IOException Signals that an I/O exception has occurred.
     * @deprecated use getAnnotationsActive
     */
    @Deprecated
    Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate viewCoordinate) throws IOException;

    /**
     * Gets the active annotations.
     *
     * @param viewCoordinate the view coordinate
     * @param refexNid the refex nid
     * @return the active annotations
     * @throws IOException Signals that an I/O exception has occurred.
     * @deprecated use getAnnotationsActive
     */
    @Deprecated
    Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate viewCoordinate, int refexNid)
            throws IOException;

    /**
     * Returns any annotations on the component, or any members that are a
     * "referenced component". Refsets can only be on a concept not on a
     * component.
     *
     * @param viewCoordinate the view coordinate
     * @param refexNid the refex nid
     * @return the refex members active
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate viewCoordinate, int refexNid)
            throws IOException;

    /**
     * Gets the refexes active.
     *
     * @param viewCoordinate the view coordinate
     * @return the refexes active
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends RefexVersionBI<?>> getRefexesActive(ViewCoordinate viewCoordinate) throws IOException;

    /**
     * Gets the active refexes.
     *
     * @param viewCoordinate the view coordinate
     * @param refexNid the refex nid
     * @return the active refexes
     * @throws IOException Signals that an I/O exception has occurred.
     * @deprecated use getRefexMembersActive
     */
    @Deprecated
    Collection<? extends RefexVersionBI<?>> getActiveRefexes(ViewCoordinate viewCoordinate, int refexNid)
            throws IOException;

    /**
     * Gets the refexes inactive.
     *
     * @param viewCoordinate the view coordinate
     * @return the refexes inactive
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends RefexVersionBI<?>> getRefexesInactive(ViewCoordinate viewCoordinate) throws IOException;

    /**
     * Gets the native id.
     *
     * @return the native id
     */
    int getNid();

    /**
     * Gets the prim uuid.
     *
     * @return the primordial if known. The IUnknown UUID
     * (00000000-0000-0000-C000-000000000046) if not known.
     */
    UUID getPrimUuid();

    /**
     * Gets the refex members.
     *
     * @param refexNid the refex nid
     * @return the refex members
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refexNid) throws IOException;

    /**
     * Gets the refexes.
     *
     * @return the refexes
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException;

    /**
     * Gets the refexes.
     *
     * @param refexNid the refex nid
     * @return the refexes
     * @throws IOException Signals that an I/O exception has occurred.
     * @deprecated use getRefexMembers
     */
    @Deprecated
    Collection<? extends RefexChronicleBI<?>> getRefexes(int refexNid) throws IOException;

    /**
     * Gets the uUI ds.
     *
     * @return the uUI ds
     */
    List<UUID> getUUIDs();

    /**
     * Checks for annotation member active.
     *
     * @param viewCoordinate the view coordinate
     * @param refexNid the refex nid
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean hasAnnotationMemberActive(ViewCoordinate viewCoordinate, int refexNid) throws IOException;

    /**
     * Checks for refex member active.
     *
     * @param viewCoordinate the view coordinate
     * @param refexNid the refex nid
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean hasRefexMemberActive(ViewCoordinate viewCoordinate, int refexNid) throws IOException;
}
