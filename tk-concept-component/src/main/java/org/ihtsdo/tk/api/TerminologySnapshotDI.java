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
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 * The Interface TerminologySnapshotDI provides methods for interacting with a
 * particular "snapshot" of terminology. This snapshot is created using a
 * specific
 * <code>ViewCoordinate</code> and methods on this interface all return versions
 * of a component rather than a chronicle. This removes the time of then finding
 * the correct version after a chronicle has been returned, or need to pass in a
 * <code>ViewCoordinate</code>.
 * 
 * @see ComponentChronicleBI
 * @see ComponentVersionBI
 * @see ViewCoordinate
 */
public interface TerminologySnapshotDI extends TerminologyDI {

    /**
     * Creates a new position given the path and time.
     *
     * @param path the path
     * @param time the time
     * @return the position
     * @throws IOException signals that an I/O exception has occurred
     */
    PositionBI newPosition(PathBI path, long time) throws IOException;


    /**
     * Gets the terminology builder with the editing metadata given
     * by the <code>EditCoordinate</code>. The builder is 
     * used to construct blueprints.
     *
     * @param editCoordinate the edit coordinate
     * @return the builder
     */
    TerminologyBuilderBI getBuilder(EditCoordinate editCoordinate);

    /**
     * Gets the component version associated with the given uuids.
     *
     * @param uuids the uuids
     * @return the component version
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    ComponentVersionBI getComponentVersion(Collection<UUID> uuids) throws IOException, ContradictionException;

    /**
     * Gets the component version given the component container.
     *
     * @param componentContainer the component container
     * @return the component version
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    ComponentVersionBI getComponentVersion(ComponentContainerBI componentContainer) throws IOException, ContradictionException;

    /**
     * Gets the component version associated with the given nid.
     *
     * @param nid the nid
     * @return the component version
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    ComponentVersionBI getComponentVersion(int nid) throws IOException, ContradictionException;

    /**
     * Gets the component version for the given uuid(s).
     *
     * @param uuids the uuids
     * @return the component version
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    ComponentVersionBI getComponentVersion(UUID... uuids) throws IOException, ContradictionException;

    /**
     * Gets the concept for nid. The nid can be any on the concept, such as a component, and the enclosing concept version will be returned.
     *
     * @param nid the nid
     * @return the concept for nid
     * @throws IOException signals that an I/O exception has occurred
     */
    ConceptVersionBI getConceptForNid(int nid) throws IOException;

    /**
     * Gets the concept version for the given uuids.
     *
     * @param uuids the uuids
     * @return the concept version
     * @throws IOException signals that an I/O exception has occurred
     */
    ConceptVersionBI getConceptVersion(Collection<UUID> uuids) throws IOException;

    /**
     * Gets the concept version for the given concept container.
     *
     * @param conceptContainer the concept container
     * @return the concept version
     * @throws IOException signals that an I/O exception has occurred
     */
    ConceptVersionBI getConceptVersion(ConceptContainerBI conceptContainer) throws IOException;

    /**
     * Gets the concept version for the given concept nid. This must be the nid of the actual concept, not a component within the concept.
     *
     * @param conceptNid the concept nid
     * @return the concept version
     * @throws IOException signals that an I/O exception has occurred
     */
    ConceptVersionBI getConceptVersion(int conceptNid) throws IOException;

    /**
     * Gets the concept version for the given uuid(s).
     *
     * @param uuids the uuids
     * @return the concept version
     * @throws IOException signals that an I/O exception has occurred
     */
    ConceptVersionBI getConceptVersion(UUID... uuids) throws IOException;

    /**
     * Gets the concept versions for a given set of concept nids.
     *
     * @param conceptNids the concept nids
     * @return the concept versions
     * @throws IOException signals that an I/O exception has occurred
     */
    Map<Integer, ConceptVersionBI> getConceptVersions(NidBitSetBI conceptNids) throws IOException;

    /**
     * Gets the possible children of a concept specified by the concept nid.
     *
     * @param conceptNid the concept nid
     * @return the possible children
     * @throws IOException signals that an I/O exception has occurred
     */
    int[] getPossibleChildren(int conceptNid) throws IOException;

    /**
     * Gets the view coordinate used to create the snapshot.
     *
     * @return the view coordinate
     */
    ViewCoordinate getViewCoordinate();

    /**
     * Gets the nid associated with the concept for any nid within that concept.
     *
     * @param nid the nid
     * @return the concept nid
     * @throws IOException signals that an I/O exception has occurred
     */
    int getConceptNidForNid(Integer nid) throws IOException;

    /**
     * Checks concept specified by the child nid is a child of the concept specified by the parent nid.
     *
     * @param childNid the child nid
     * @param parentNid the parent nid
     * @return <code>true</code>, if is kind of
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    boolean isKindOf(int childNid, int parentNid) throws IOException, ContradictionException;
    
    /**
     * Used to delete an uncommitted change to a relationship specified by the
     * given
     * <code>relationshipVersion</code>.
     *
     * @param relationshipVersion the uncommitted relationship version to forget
     * @throws IOException signals that an I/O exception has occurred
     * 
     */
    void forget(RelationshipVersionBI relationshipVersion) throws IOException;

    /**
     * Used to delete an uncommitted change to a description specified by the
     * given
     * <code>descriptionVersion</code>.
     *
     * @param descriptionVersion the uncommitted description version to forget
     * @throws IOException signals that an I/O exception has occurred
     * 
     */
    void forget(DescriptionVersionBI descriptionVersion) throws IOException;

    /**
     * Used to delete an uncommitted change to a refex specified by the given
     * <code>refexChronicle</code>.
     *
     * @param refexChronicle the refex which has the uncommitted changes to
     * forget
     * @throws IOException signals that an I/O exception has occurred
     * 
     */
    void forget(RefexChronicleBI refexChronicle) throws IOException;

    /**
     * Used to delete an uncommitted change to the concept attributes specified
     * by the given
     * <code>conceptAttributeVersion</code>.
     *
     * @param conceptAttributeVersion the uncommitted concept attribute version
     * to forget
     * @return <code>true</code>, if successful
     * @throws IOException signals that an I/O exception has occurred
     * 
     */
    boolean forget(ConceptAttributeVersionBI conceptAttributeVersion) throws IOException;

    /**
     * Used to delete any uncommitted changes to the concept specified by the
     * given
     * <code>conceptChronicle</code>.
     *
     * @param conceptChronicle the concept which has the uncommitted changes to
     * forget
     * @throws IOException signals that an I/O exception has occurred
     * 
     */
    void forget(ConceptChronicleBI conceptChronicle) throws IOException;

}
