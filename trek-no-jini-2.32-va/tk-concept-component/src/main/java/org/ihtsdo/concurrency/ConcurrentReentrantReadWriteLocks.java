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
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The Class ConcurrentReentrantReadWriteLocks represents a number of
 * <code>ReentrantReadWriteLocks</code> and contains methods for interacting
 * with the locks individually or as a whole.
 *
 * @see ReentrantReadWriteLock
 */
public class ConcurrentReentrantReadWriteLocks extends ConcurrencyLocks {

    /**
     * An array of
     * <code>ReentrantReadWriteLocks</code> available for this concurrent
     * reentrant locks.
     */
    ReentrantReadWriteLock[] locks;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new concurrent reentrant read write locks.
     */
    public ConcurrentReentrantReadWriteLocks() {
        super();
        setupLocks();
    }

    /**
     * Instantiates a new concurrent reentrant read write locks using the given
     * <code>concurrencyLevel</code>.
     *
     * @param concurrencyLevel an <code>int</code> indicating how      * many <code>ReentrantReadWriteLocks</code> are available for this
     * concurrent reentrant lock
     */
    public ConcurrentReentrantReadWriteLocks(int concurrencyLevel) {
        super(concurrencyLevel);
        setupLocks();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Locks the reentrant read lock based on the given
     * <code>dbKey</code>.
     *
     * @param dbKey an <code>int</code> indicating which reentrant read lock to
     * lock
     */
    public void readLock(int dbKey) {
        int word = (dbKey >>> segmentShift) & segmentMask;

        locks[word].readLock().lock();
    }

    /**
     * /**
     * Sets up the locks by creating a number of
     * <code>ReentrantReadWriteLocks</code> based on the number indicated by the
     * <code>concurrencyLevel</code>.
     *
     * @see ReentrantReadWriteLock
     */
    private void setupLocks() {
        locks = new ReentrantReadWriteLock[getConcurrencyLevel()];

        for (int i = 0; i < getConcurrencyLevel(); i++) {
            locks[i] = new ReentrantReadWriteLock();
        }
    }

    /**
     * Unlocks the reentrant read lock based on the given
     * <code>dbKey</code>.
     *
     * @param dbKey an <code>int</code> indicating which reentrant read lock to
     * unlock
     */
    public void unlockRead(int dbKey) {
        int word = (dbKey >>> segmentShift) & segmentMask;

        locks[word].readLock().unlock();
    }

    /**
     * Unlocks the reentrant write lock based on the given
     * <code>dbKey</code>.
     *
     * @param dbKey an <code>int</code> indicating which reentrant write lock to
     * unlock
     */
    public void unlockWrite(int dbKey) {
        int word = (dbKey >>> segmentShift) & segmentMask;

        locks[word].writeLock().unlock();
    }

    /**
     * Unlocks all write locks.
     */
    public void unlockWriteAll() {
        for (int i = 0; i < locks.length; i++) {
            locks[i].writeLock().unlock();
        }
    }

    /**
     * Locks the reentrant write lock based on the given
     * <code>dbKey</code>.
     *
     * @param dbKey an <code>int</code> indicating which reentrant write lock to
     * lock
     */
    public void writeLock(int dbKey) {
        int word = (dbKey >>> segmentShift) & segmentMask;

        locks[word].writeLock().lock();
    }

    /**
     * Locks all write locks.
     */
    public void writeLockAll() {
        for (int i = 0; i < locks.length; i++) {
            locks[i].writeLock().lock();
        }
    }
}
