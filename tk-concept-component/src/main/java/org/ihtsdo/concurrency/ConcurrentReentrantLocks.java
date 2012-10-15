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
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Class ConcurrentReentrantLocks represents a number of
 * <code>ReentrantLocks</code> and contains methods for interacting with the
 * locks individually or as a whole.
 *
 * @see ReentrantLock
 */
public class ConcurrentReentrantLocks extends ConcurrencyLocks {

    /**
     * An array of
     * <code>ReentrantLock</code> available for this concurrent reentrant
     * locks.
     */
    public ReentrantLock[] locks;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new concurrent reentrant locks.
     */
    public ConcurrentReentrantLocks() {
        super();
        setupLocks();
    }

    /**
     * Instantiates a new concurrent reentrant locks using the given
     * <code>concurrencyLevel</code>.
     *
     * @param concurrencyLevel an <code>int</code> indicating how 
     * many <code>ReentrantLocks</code> are available for this concurrent
     * reentrant lock
     */
    public ConcurrentReentrantLocks(int concurrencyLevel) {
        super(concurrencyLevel);
        setupLocks();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Locks the reentrant lock based on the given
     * <code>dbKey</code>.
     *
     * @param dbKey an <code>int</code> indicating which reentrant locks to lock
     */
    public void lock(int dbKey) {
        int word = (dbKey >>> segmentShift) & segmentMask;

        locks[word].lock();
    }

    /**
     * Locks all reentrant locks.
     */
    public void lockAll() {
        for (int i = 0; i < locks.length; i++) {
            locks[i].lock();
        }
    }

    /**
     * Sets up the locks by creating a number of
     * <code>ReentrantLocks</code> based on the number indicated by the
     * <code>concurrencyLevel</code>.
     *
     * @see ReentrantLock
     */
    private void setupLocks() {
        locks = new ReentrantLock[concurrencyLevel];

        for (int i = 0; i < concurrencyLevel; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    /**
     * Unlocks the reentrant lock based on the given
     * <code>dbKey</code>.
     *
     * @param dbKey an <code>int</code> indicating which reentrant locks to
     * unlock
     */
    public void unlock(int dbKey) {
        int word = (dbKey >>> segmentShift) & segmentMask;

        locks[word].unlock();
    }

    /**
     * Unlocks all the locks.
     */
    public void unlockAll() {
        for (int i = 0; i < locks.length; i++) {
            locks[i].unlock();
        }
    }
}
