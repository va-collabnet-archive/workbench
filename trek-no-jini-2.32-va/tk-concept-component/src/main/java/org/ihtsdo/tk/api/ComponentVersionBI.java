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

/**
 * The Interface ComponentVersionBI provides terminology generic methods for
 * editing and interacting with a particular version of a component. A component
 * can have many versions which are based on a particular
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
     * Checks to see if the stamp nid for this component version is within the
     * given range.
     *
     * @param minStampNid the min stamp nid
     * @param maxStampNid the max stamp nid
     * @return <code>true</code>, if the stamp nid is within the given range
     * @see org.ihtsdo.tk.api.StampBI
     */
    boolean stampIsInRange(int minStampNid, int maxStampNid);

    /**
     * Creates a shortened description of a component based on the given
     * <code>terminologySnapshot</code>.
     *
     * @param terminologySnapshot the terminology snapshot which specifies which
     * version the component should be used
     * @return the <code>String</code> representing a shortened, user friendly,
     * description of the component.
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of the component
     * is found
     */
    String toUserString(TerminologySnapshotDI terminologySnapshot) throws IOException, ContradictionException;

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets all the nids for a concept version. This includes all the nids of
     * the components within the concept.
     *
     * @return all the nids of a concept version
     * @throws IOException signals that an I/O exception has occurred
     */
    Set<Integer> getAllNidsForVersion() throws IOException;

    /**
     * Gets the author nid associated with this component version.
     *
     * @return the author nid for this version
     */
    int getAuthorNid();

    /**
     * Gets the module nid associated with this component version.
     *
     * @return the module nid for this version.
     */
    int getModuleNid();

    /**
     * Gets the chronicle for this component version.
     *
     * @return the chronicle for this version
     */
    ComponentChronicleBI getChronicle();

    /**
     * Gets the position associated with this component version.
     *
     * @return the position for this version
     * @throws IOException signals that an I/O exception has occurred
     */
    PositionBI getPosition() throws IOException;

    /**
     * Gets the stamp nid associated with this component version.
     *
     * @return the stamp nid for this version
     * @see org.ihtsdo.tk.api.StampBI
     */
    int getStampNid();

    /**
     * Gets the status nid associated with this component version.
     *
     * @return the status nid for this version
     */
    int getStatusNid();

    /**
     * Checks if a version is active based on the version status is one of the <code>allowedStatusNids</code>.
     *
     * @param allowedStatusNids the nids representing the allowed statuses
     * @return <code>true</code>, if the version is active
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean isActive(NidSetBI allowedStatusNids) throws IOException;

    /**
     * Checks if a version is active based on the given
     * <code>viewCoordinate</code>.
     *
     * @param viewCoordinate the view coordinate specifying which versions are active and inactive
     * @return <code>true</code>, if the version is active
     * @throws IOException signals that an I/O exception has occurred
     */
    boolean isActive(ViewCoordinate viewCoordinate) throws IOException;

    /**
     * Checks if a version is uncommitted.
     *
     * @return <code>true</code>, if the version is uncommitted
     */
    public boolean isUncommitted();

    /**
     * Checks if a version is from the baseline generation of data.
     *
     * @return <code>true</code> if this version is stored in the read-only
     * database, rather than in the mutable database
     */
    boolean isBaselineGeneration();

    /**
     * Checks if two versions are equal based on the given
     * <code>viewCoordinates</code>.
     *
     * @param viewCoordinate1 the viewCoordinate of the first version
     * @param viewCoordinate2 the viewCoordinate of the second version
     * @param compareAuthoring Set to <code>true</code> to compare the author
     * and path of the versions. Otherwise <code>false</code> to disregard
     * author and path.
     * @return <code>true</code> if the versions are equal
     */
    boolean versionsEqual(ViewCoordinate viewCoordinate1, ViewCoordinate viewCoordinate2, Boolean compareAuthoring);

    /**
     * Makes blueprint of a version of a component as specified by the given
     * <code>viewCoordinate</code>. The blueprint is a clone of the component
     * and is the preferred method for editing or creating a new version of a
     * component.
     *
     * @param viewCoordinate the view coordinate specifying which version is active or inactive
     * @return the blueprint of the component
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint
     */
    CreateOrAmendBlueprint makeBlueprint(ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException, InvalidCAB;
}
