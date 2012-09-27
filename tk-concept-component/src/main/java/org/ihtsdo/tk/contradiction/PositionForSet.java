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

package org.ihtsdo.tk.contradiction;


import org.ihtsdo.tk.hash.Hashcode;

// TODO: Auto-generated Javadoc
/**
 * The Class PositionForSet.
 */
public class PositionForSet implements Comparable<PositionForSet> {
    
    /** The time. */
    long time;
    
    /** The path nid. */
    int pathNid;

    /**
     * Instantiates a new position for set.
     *
     * @param time the time
     * @param pathNid the path nid
     */
    public PositionForSet(long time, int pathNid) {
        super();
        this.time = time;
        this.pathNid = pathNid;
    }

    /* (non-Javadoc)
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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Hashcode.compute(new int[] { (int) time + pathNid } );
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * Gets the path nid.
     *
     * @return the path nid
     */
    public int getPathNid() {
        return pathNid;
    }

	/* (non-Javadoc)
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
