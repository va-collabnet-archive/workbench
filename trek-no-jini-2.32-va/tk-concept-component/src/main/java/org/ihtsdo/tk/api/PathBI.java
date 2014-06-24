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

//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The Interface PathBI represents a single path for editing or viewing. Paths
 * have both an origin and a parent. The parent is only used for displaying the
 * path concept in the taxonomy. The origins are the paths from which this path
 * inherits information.
 */
public interface PathBI {

    /**
     * Converts a path text description to an html string. Includes origins if
     * available.
     *
     * @return the html formatted string
     * @throws IOException signals that an I/O exception has occurred
     */
    public String toHtmlString() throws IOException;

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the concept nid for a path.
     *
     * @return the concept nid associated with a path concept
     */
    public int getConceptNid();

    /**
     * Get all origins and origin of origins, etc., for this path.
     *
     * @return a set of inherited origin positions
     */
    public Set<? extends PositionBI> getInheritedOrigins();

    /**
     * Gets the matching path for the given
     * <code>pathNid</code> from this path or its origins.
     *
     * @param pathNid the nid associated with matching path
     * @return the matching path if found, <code>null</code> otherwise
     */
    public PathBI getMatchingPath(int pathNid);

    /**
     * Similar to {@link #getInheritedOrigins()} however superseded origins
     * (where there is more than one origin for the same path but with an
     * earlier version) will be excluded.
     *
     * @return a set normalised origin positions
     */
    public Set<? extends PositionBI> getNormalisedOrigins();

    /**
     * Gets the positions of this paths origins. Does not include inherited
     * origins.
     *
     * @return a set of origin positions.
     */
    public Collection<? extends PositionBI> getOrigins();

    /**
     * Gets the UUIDs associated with this path.
     *
     * @return the UUIDs associated with this path
     */
    public List<UUID> getUUIDs();
}
