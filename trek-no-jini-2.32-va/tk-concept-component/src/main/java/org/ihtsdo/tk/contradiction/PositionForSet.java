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
package org.ihtsdo.tk.contradiction;

import org.ihtsdo.tk.hash.Hashcode;

/**
 * The Class PositionForSet represents a position based on a time-path set. Used
 * in finding contradictions.
 */
public class PositionForSet implements Comparable<PositionForSet> {

    /**
     * The time for the position.
     */
    long time;
    /**
     * The associated with the path for the position.
     */
    int pathNid;

    /**
     * Instantiates a new position based on the given
     * <code>time</code> and
     * <code>pathNid</code>.
     *
     * @param time the time of the position
     * @param pathNid the nid associated with the path the position is on
     */
    public PositionForSet(long time, int pathNid) {
        super();
        this.time = time;
        this.pathNid = pathNid;
    }

    /**
     * Checks if the time and path of this position are equal to the time and path of another position.
     *
     * @return <code>true</code>, if the positions are equal
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (PositionForSet.class.isAssignableFrom(obj.getClass())) {
            PositionForSet another = (PositionForSet) obj;
            return another.time == time && another.pathNid == pathNid;
        }

        return false;
    }

    /** 
     * Get a hash code based on the time and path for this position.
     * @return a hash code of the time and path
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Hashcode.compute(new int[]{(int) time + pathNid});
    }

    /**
     * Gets the time of the position.
     *
     * @return the time of the position
     */
    public long getTime() {
        return time;
    }

    /**
     * Gets the nid associated with the path of the position.
     *
     * @return the nid associated with the path of the position
     */
    public int getPathNid() {
        return pathNid;
    }

    /**
     * Compares the time and path of this position to the time and path of
     * <code>another</code>. Compares time first and then path.
     *
     * @return 0 if the time and path are both equal. -1 if this pathNid is less
     * than the other pathNid, 1 if the this pathNid is greater. -1 this time is
     * less than the other time, 1 if this time is greater
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(PositionForSet another) {
        if (this.time == another.time && this.pathNid == another.pathNid) {
            return 0;
        } else if (this.time == another.time) {
            // If time same, use path Id
            if (this.pathNid < another.pathNid) {
                return -1;
            } else {
                return 1;
            }
        } else {
            // If pathNid is same or different, use the time comparison for the method's result
            if (this.time < another.time) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
