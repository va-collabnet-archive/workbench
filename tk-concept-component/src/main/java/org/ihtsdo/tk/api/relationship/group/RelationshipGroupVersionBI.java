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
package org.ihtsdo.tk.api.relationship.group;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

// TODO: Auto-generated Javadoc
/**
 * The Interface RelationshipGroupVersionBI provides methods for interacting
 * with a version of a relationship group.
 * 
 * TODO-javadoc: all these methods seem to return what is on the chronicle rather than on the version,
 * why are they on the version? or why aren't they returning relationship from the version?
 */
public interface RelationshipGroupVersionBI extends RelationshipGroupChronicleBI, ComponentVersionBI {

    /**
     * Gets all the active relationships found in this group. If the
     * relationship group version contains a
     * <code>viewCoordinate</code> all active relationships found using that
     * <code>viewCoordinate</code> will be returned, regardless of which
     * <code>RelationshipGroupVersion</code> the are found in. If relationship
     * group version does not contain a
     * <code>viewCoordinate</code>, all active relationships found in any
     * version will be returned.
     *
     * @return active relationships found in the relationship group chronicle
     * associated that contains this relationship group version
     */
    Collection<? extends RelationshipVersionBI> getRelationshipsActiveAllVersions();

    /**
     * Gets all the relationships found in this relationship group version regardless of status.
     *
     * @return all the relationships in this version of the relationship group
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends RelationshipVersionBI> getRelationshipsAll() throws ContradictionException;

    /**
     * Gets the relationships active.
     *
     * @return the relationships active
     * @throws ContradictionException the contradiction exception
     */
    Collection<? extends RelationshipVersionBI> getRelationshipsActive() throws ContradictionException;
}
