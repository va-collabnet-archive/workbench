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

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Set;
import org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;

// TODO: Auto-generated Javadoc
/**
 * The Interface ComponentVersionBI for the
 * {@link org.ihtsdo.concept.component.ConceptComponent} Class. A component can
 * have many versions which are based on a particular
 * <code>ViewCoordinate</code>. The first version of a component is the
 * primordial version. Each subsequent version represents the most recent
 * version of the component. The component with all of its versions is
 * represented by the
 * <code>ComponentChronicleBI</code>.
 *
 * @see org.ihtsdo.tk.api.ComponentChronicleBI
 * @see org.ihtsdo.tk.api.ComponentBI
 * @see org.ihtsdo.tk.api.coordinate.ViewCoordinate
 *
 */
public interface ComponentVersionBI extends ComponentBI, VersionPointBI {

    /**
     * Checks to see if the stamp nid for the component version is within the
     * given range.
     *
     * @param minStampNid the min stamp nid
     * @param maxStampNid the max stamp nid
     * @return true, if the stamp nid is within the given range
     * @see org.ihtsdo.tk.api.StampBI
     */
    boolean stampIsInRange(int minStampNid, int maxStampNid);

    /**
     * Creates a shortened description of the component as it is seen from
     * within the given terminology snapshot.
     *
     * @param terminologySnapshot the terminology snapshot
     * @return the <code>String</code> representing a shortened description of
     * the component.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    String toUserString(TerminologySnapshotDI terminologySnapshot) throws IOException, ContradictionException;

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the all nids for version. TODO-javadoc: what kind of nids?
     *
     * @return the all nids for version
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Set<Integer> getAllNidsForVersion() throws IOException;

    /**
     * Gets the author nid.
     *
     * @return the author nid
     */
    int getAuthorNid();

    /**
     * Gets the module nid.
     *
     * @return the module nid
     */
    int getModuleNid();

    /**
     * Gets the chronicle for the component.
     *
     * @return the chronicle for the component
     */
    ComponentChronicleBI getChronicle();

    /**
     * Gets the position.
     *
     * @return the position
     * @throws IOException Signals that an I/O exception has occurred.
     */
    PositionBI getPosition() throws IOException;

    /**
     * Gets the stamp nid.
     *
     * @return the stamp nid
     * @see org.ihtsdo.tk.api.StampBI
     */
    int getStampNid();

    /**
     * Gets the status nid.
     *
     * @return the status nid
     */
    int getStatusNid();

    /**
     * Checks if the version is active based on a set of allowed status nids.
     *
     * @param allowedStatusNids the allowed status nids
     * @return true, if is active
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean isActive(NidSetBI allowedStatusNids) throws IOException;

    /**
     * Checks if the version is active based on a
     * <code>ViewCoordinate</code>.
     *
     * @param viewCoordinate the view coordinate
     * @return true, if is active
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean isActive(ViewCoordinate viewCoordinate) throws IOException;

    /**
     * Checks if the version is uncommitted.
     *
     * @return true, if is uncommitted
     */
    public boolean isUncommitted();

    /**
     * Checks if the version is from the baseline generation of data.
     *
     * @return  <code>true</code> if this version is stored in the read-only
     * database, rather than in the mutable database. <code>false</code>
     * otherwise.
     */
    boolean isBaselineGeneration();

    /**
     * Checks if two versions are equal based on the given
     * <code>ViewCoordinates</code>.
     *
     * @param viewCoordinate1 ViewCoordinate of the first version
     * @param viewCoordinate2 ViewCoordinate of the second version
     * @param compareAuthoring Set to <code>true</code> to compare the author
     * and path of the versions. Otherwise <code>false</code> to disregard
     * author and path.
     * @return <code>true</code> if the versions are equal. <code>false</code>
     * otherwise.
     */
    boolean versionsEqual(ViewCoordinate viewCoordinate1, ViewCoordinate viewCoordinate2, Boolean compareAuthoring);

    /**
     * Makes blueprint of the component. The blueprint is a clone of the
     * component and can be used to updated the component or create a new
     * component.
     *
     * @param viewCoordinate the view coordinate
     * @return the creates the or amend blueprint
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     * @throws InvalidCAB the invalid cab
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint
     */
    CreateOrAmendBlueprint makeBlueprint(ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException, InvalidCAB;
}
