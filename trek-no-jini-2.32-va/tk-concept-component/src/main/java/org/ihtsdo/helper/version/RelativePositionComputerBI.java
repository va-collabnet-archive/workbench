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
package org.ihtsdo.helper.version;

import java.io.IOException;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.VersionPointBI;

/**
 * The Interface RelativePositionComputerBI contains methods for finding the
 * relative position of
 * <code>VersionPointBI</code>.
 *
 */
public interface RelativePositionComputerBI {

    /**
     * Possible results when comparing two positions with respect to a
     * destination position.
     *
     */
    public enum RelativePosition {

        /**
         * The relative position is before.
         */
        BEFORE,
        /**
         * The relative position is equal.
         */
        EQUAL,
        /**
         * The relative position is after.
         */
        AFTER,
        /**
         * The relative position are in contradiction.
         */
        CONTRADICTION,
        /**
         * The relative position of one is unreachable from the other.
         */
        UNREACHABLE
    };

    /**
     * Gets the
     * <code>RelativePosition</code> of
     * <code>v1</code> compared to
     * <code>v2</code>. Bypasses the onRoute test of
     * <code>relativePosition</code>.
     *
     * @param v1 the first part of the comparison.
     * @param v2 the second part of the comparison.
     * @param precedencePolicy the precedence policy
     * @return the <code>RelativePosition</code> of part1 compared to part2 with
     * respect to the destination position of the class's instance.
     */
    RelativePosition fastRelativePosition(VersionPointBI v1, VersionPointBI v2, Precedence precedencePolicy);

    /**
     * Gets the destination of the
     * class's instance.
     *
     * @return the position representing the destination
     */
    PositionBI getDestination();

    /**
     * Tests if the
     * <code>version</code>'s position is on the route to the destination of the
     * class's instance.
     *
     * @param version the part to be tested to determine if it is on route to
     * the destination.
     * @return <code>true</code> if the part's position is on the route to the
     * destination of the class's instance.
     */
    boolean onRoute(VersionPointBI version);

    /**
     * Gets the
     * <code>RelativePosition</code> of
     * <code>v1</code> compared to
     * <code>v2</code> with respect to the destination position of the class's
     * instance.
     *
     * @param v1 the first part of the comparison.
     * @param v2 the second part of the comparison.
     * @return the <code>RelativePosition</code> of v1 compared to v2 with
     * respect to the destination position of the class's instance.
     * @throws IOException signals that an I/O exception has occurred
     */
    RelativePosition relativePosition(VersionPointBI v1, VersionPointBI v2) throws IOException;
}
