/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.api;

import java.util.Collection;

// TODO: Auto-generated Javadoc
/**
 * The Interface PositionBI.
 */
public interface PositionBI {

    /**
     * Gets the path.
     *
     * @return the path
     */
    public PathBI getPath();

    /**
     * Gets the version.
     *
     * @return the version
     */
    public int getVersion();

    /**
     * Gets the time.
     *
     * @return the time
     */
    public long getTime();

    /**
     * Checks if is subsequent or equal to.
     *
     * @param version the version
     * @param pathNid the path nid
     * @return true, if is subsequent or equal to
     */
    public boolean isSubsequentOrEqualTo(int version, int pathNid);

    /**
     * Checks if is antecedent or equal to.
     *
     * @param version the version
     * @param pathNid the path nid
     * @return true, if is antecedent or equal to
     */
    public boolean isAntecedentOrEqualTo(int version, int pathNid);

    /**
     * Checks if is subsequent or equal to.
     *
     * @param time the time
     * @param pathNid the path nid
     * @return true, if is subsequent or equal to
     */
    public boolean isSubsequentOrEqualTo(long time, int pathNid);

    /**
     * Checks if is antecedent or equal to.
     *
     * @param time the time
     * @param pathNid the path nid
     * @return true, if is antecedent or equal to
     */
    public boolean isAntecedentOrEqualTo(long time, int pathNid);

    /**
     * Checks if is antecedent or equal to.
     *
     * @param another the another
     * @return true, if is antecedent or equal to
     */
    public boolean isAntecedentOrEqualTo(PositionBI another);

    /**
     * Check antecedent or equal to origins.
     *
     * @param originPositions the origin positions
     * @return true, if successful
     */
    public boolean checkAntecedentOrEqualToOrigins(Collection<? extends PositionBI> originPositions);

    /**
     * Checks if is subsequent or equal to.
     *
     * @param another the another
     * @return true, if is subsequent or equal to
     */
    public boolean isSubsequentOrEqualTo(PositionBI another);

    /**
     * Equals.
     *
     * @param version the version
     * @param pathNid the path nid
     * @return true, if successful
     */
    public boolean equals(int version, int pathNid);

    /**
     * Equals.
     *
     * @param time the time
     * @param pathNid the path nid
     * @return true, if successful
     */
    public boolean equals(long time, int pathNid);

    /**
     * Gets the all origins.
     *
     * @return the all origins
     */
    public Collection<? extends PositionBI> getAllOrigins();

}
