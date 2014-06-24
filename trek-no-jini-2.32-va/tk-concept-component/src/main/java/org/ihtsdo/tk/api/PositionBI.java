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

import java.util.Collection;

/**
 * The Interface PositionBI provides methods for interacting with a position. A
 * position is defined as a specific point in time on a path.
 */
public interface PositionBI {

    /**
     * Gets the path used to create this position.
     *
     * @return the path associated with this position.
     */
    public PathBI getPath();

    /**
     * Represents the "time" for the position, compressed to 32 bits.
     *
     * @return the version
     * @deprecated use getTime()
     */
    public int getVersion();

    /**
     * Gets the time used to created this position.
     *
     * @return the time associated with this position
     */
    public long getTime();

    /**
     * Checks if this version and path are subsequent or equal to another <code>version</code>
     * and <code>pathNid</code>.
     *
     * @param version the version to compare
     * @param pathNid the nid of the path to compare
     * @return <code>true</code>, if is subsequent or equal to the other
     * @deprecated use isSubsequentOrEqualTo(long time, int pathNid)
     */
    public boolean isSubsequentOrEqualTo(int version, int pathNid);

    /**
     * Checks if this version and path are antecedent or equal to another <code>version</code>
     * and <code>pathNid</code>.
     *
     * @param version the version to compare
     * @param pathNid the nid of the path to compare
     * @return <code>true</code>, if is antecedent or equal to the other
     * @deprecated
     */
    public boolean isAntecedentOrEqualTo(int version, int pathNid);

    /**
     * Checks if this time and path are subsequent or equal to another <code>time</code> and
     * <code>pathNid</code>.
     *
     * @param time the time to compare
     * @param pathNid the nid of the path to compare
     * @return <code>true</code>, if is subsequent or equal to the other
     */
    public boolean isSubsequentOrEqualTo(long time, int pathNid);

    /**
     * Checks if this time and path are antecedent or equal to another <code>time</code> and
     * <code>pathNid</code>.
     *
     * @param time the time to compare
     * @param pathNid the nid of the path to compare
     * @return <code>true</code>, if is antecedent or equal to the other
     */
    public boolean isAntecedentOrEqualTo(long time, int pathNid);

    /**
     * Checks if this position is antecedent or equal to <code>another</code> position.
     *
     * @param another the other position
     * @return <code>true</code>, if is antecedent or equal to the other
     */
    public boolean isAntecedentOrEqualTo(PositionBI another);

    /**
     * Checks if the path associated with this position is antecedent or equal to another path's
     * <code>originPositions</code>.
     *
     * @param originPositions the origin positions
     * @return <code>true</code>, if is antecedent or equal to the other
     */
    public boolean checkAntecedentOrEqualToOrigins(Collection<? extends PositionBI> originPositions);

    /**
     * Checks if the position is subsequent or equal to <code>another</code> position.
     *
     * @param another the other position
     * @return <code>true</code>, if is subsequent or equal to the other
     */
    public boolean isSubsequentOrEqualTo(PositionBI another);

    /**
     * Checks if the version and path are equal to another <code>version</code> and <code>pathNid</code>.
     *
     * @param version the version
     * @param pathNid the path nid
     * @return <code>true</code>, if equal
     * @deprecated use equals(long time, int pathNid)
     */
    public boolean equals(int version, int pathNid);

    /**
     * Checks if the time and path are equal to another time and path.
     *
     * @param time the time
     * @param pathNid the path nid
     * @return <code>true</code>, if equal
     */
    public boolean equals(long time, int pathNid);

    /**
     * Gets the all origin positions of this path.
     *
     * @return the origin positions
     */
    public Collection<? extends PositionBI> getAllOrigins();
}
