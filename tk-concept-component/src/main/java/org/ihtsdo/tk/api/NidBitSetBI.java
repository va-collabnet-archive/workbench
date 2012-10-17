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

import java.util.BitSet;

/**
 * The Interface NidBitSetBI represents a bit set optimized for use with nids. It should be used for sets with large numbers of nids.
 * For example, the lists of all concept identifiers when iterating the
 * database.
 * 
 *
 * @see BitSet
 */
public interface NidBitSetBI {

    /**
     * Add the members of the
     * <code>other</code> set to this set.
     *
     * @param other the other nid bit set
     */
    public void and(NidBitSetBI other);

    /**
     * Retains everything in this set that is not in the
     * <code>other</code>.
     *
     * @param other the other nid bit set
     */
    void andNot(NidBitSetBI other);

    /**
     * Gets the cardinality of the set.
     *
     * @return number of set bits.
     */
    public int cardinality();

    /**
     * Clear the set.
     */
    public void clear();

    /**
     * Sets the all.
     */
    public void setAll();

    /**
     * Gets the iterator for this set.
     *
     * @return the nid bit set iterator
     */
    public NidBitSetItrBI iterator();

    /**
     * Performs a logical OR of this set and the
     * <code>other</code>.
     *
     * @param other the other nid bit set
     */
    public void or(NidBitSetBI other);

    /**
     * Total bits.
     *
     * @return the int
     */
    public int totalBits();

    /**
     * Performs a logical UNION of this set and the
     * <code>other</code>.
     *
     * @param other the other nid bit set
     */
    void union(NidBitSetBI other);

    /**
     * Performs a logical XOR of this set and the
     * <code>other</code>.
     *
     * @param other the other nid bit set
     */
    void xor(NidBitSetBI other);

    //~--- get methods ---------------------------------------------------------
    /**
     * Checks if the
     * <code>nid</code> is member of this set.
     *
     * @param nid the nid to test
     * @return <code>true</code>, if the nid is member
     */
    public boolean isMember(int nid);

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the
     * <code>nid</code> as a member of this set.
     *
     * @param nid the nid to set as a member
     */
    public void setMember(int nid);

    /**
     * Sets the
     * <code>nid</code> as not a member of this set.
     *
     * @param nid the nid to set as not a member
     */
    public void setNotMember(int nid);
}
