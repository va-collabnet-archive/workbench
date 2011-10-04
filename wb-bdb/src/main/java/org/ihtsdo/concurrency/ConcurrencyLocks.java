/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ihtsdo.concurrency;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author kec
 */
public class ConcurrencyLocks {

    private int concurrencyLevel = 128;
    private int sshift = 0;
    private int ssize = 1;
    private ReentrantReadWriteLock[] locks = new ReentrantReadWriteLock[concurrencyLevel];
    private int segmentShift;
    private int segmentMask;
    private void setup() {
        while (ssize < concurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }
        segmentShift = 32 - sshift;
        segmentMask = ssize - 1;
        for (int i = 0; i < concurrencyLevel; i++) {
            locks[i] = new ReentrantReadWriteLock();
        }
    }

    public ConcurrencyLocks() {
        setup();
    }

    public void writeLock(int dbKey) {
       int word = (dbKey >>> segmentShift) & segmentMask;
       locks[word].writeLock().lock();
    }

    public void readLock(int dbKey) {
       int word = (dbKey >>> segmentShift) & segmentMask;
       locks[word].readLock().lock();
    }

    public void unlockWrite(int dbKey) {
       int word = (dbKey >>> segmentShift) & segmentMask;
       locks[word].writeLock().unlock();
    }

    public void unlockRead(int dbKey) {
       int word = (dbKey >>> segmentShift) & segmentMask;
       locks[word].readLock().unlock();
    }

}
