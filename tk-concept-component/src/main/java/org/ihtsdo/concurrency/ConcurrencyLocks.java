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
package org.ihtsdo.concurrency;

//~--- JDK imports ------------------------------------------------------------
/**
 * The Class ConcurrencyLocks provides methods to generic to concurrency locks.
 *
 */
public abstract class ConcurrencyLocks {

    protected final int concurrencyLevel;
    private int sshift = 0;
    private int ssize = 1;
    protected int segmentMask;
    protected int segmentShift;
    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new concurrency locks. Uses a concurrently level of 128.
     */
    public ConcurrencyLocks() {
        concurrencyLevel = 128;
        setup();
    }

    /**
     * Instantiates a new concurrency locks using the given
     * <code>concurrencyLevel</code>.
     *
     * @param concurrencyLevel an <code>int</code> indicating how many locks are
     * available for this concurrency lock
     */
    public ConcurrencyLocks(int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
        setup();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Sets up the concurrency locks.
     */
    private void setup() {
        while (ssize < concurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }

        segmentShift = 32 - sshift;
        segmentMask = ssize - 1;

    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the concurrency level of this concurrency lock.
     *
     * @return the concurrency level
     */
    public int getConcurrencyLevel() {
        return concurrencyLevel;
    }
}
