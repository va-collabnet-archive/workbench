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

/**
 * The Interface NidSetBI represents a serializable set of nids. All
 * implementations must serialize the set using uuids.
 */
public interface NidSetBI {

    /**
     * Checks if this nid set contains the specified
     * <code>nid</code>.
     *
     * @param nid the nid in question
     * @return <code>true</code>, if this nid set contains the nid
     */
    boolean contains(int nid);

    /**
     * Gets an array containing the nids in this nid set.
     *
     * @return an array containing the nids in this nid set
     */
    int[] getSetValues();

    /**
     * Adds the given
     * <code>nid</code> to this nid set.
     *
     * @param nid the nid to add
     */
    void add(int nid);

    /**
     * Removes the given
     * <code>nid</code> from this nid set.
     *
     * @param nid the nid to remove
     */
    void remove(int nid);

    /**
     * Adds all the nids in the given array to this nid set.
     *
     * @param nids the nids to add
     * @return this nid set with the additional nids
     */
    NidSetBI addAll(int[] nids);

    /**
     * Removes all the nids in the given array from this nid set.
     *
     * @param nids the nids to remove
     */
    void removeAll(int[] nids);

    /**
     * Removes the nids in this nid set.
     */
    void clear();

    /**
     * Get the number of nids in this nid set.
     *
     * @return an int representing the number of nids in this nid set
     */
    int size();

    /**
     * Gets the max value of a nid in this nid set.
     *
     * @return the max value of a nid in this nid set
     */
    int getMax();

    /**
     * Gets the min value of a nid in this nid set.
     *
     * @return the min value of a nid in this nid set
     */
    int getMin();

    /**
     * Checks if the nids in this set are contiguous
     *
     * @return <code>true</code>, if the nids in this set are contiguous
     */
    boolean contiguous();
}
