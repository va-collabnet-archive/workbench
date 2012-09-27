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



package org.ihtsdo.concurrency;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.locks.ReentrantReadWriteLock;

// TODO: Auto-generated Javadoc
/**
 * The Class ConcurrentReentrantReadWriteLocks.
 *
 * @author kec
 */
public class ConcurrentReentrantReadWriteLocks extends ConcurrencyLocks {
   
   /** The locks. */
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
    * Instantiates a new concurrent reentrant read write locks.
    *
    * @param concurrencyLevel the concurrency level
    */
   public ConcurrentReentrantReadWriteLocks(int concurrencyLevel) {
      super(concurrencyLevel);
      setupLocks();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Read lock.
    *
    * @param dbKey the db key
    */
   public void readLock(int dbKey) {
      int word = (dbKey >>> segmentShift) & segmentMask;

      locks[word].readLock().lock();
   }

   /**
    * Setup locks.
    */
   private void setupLocks() {
      locks = new ReentrantReadWriteLock[getConcurrencyLevel()];

      for (int i = 0; i < getConcurrencyLevel(); i++) {
         locks[i] = new ReentrantReadWriteLock();
      }
   }

   /**
    * Unlock read.
    *
    * @param dbKey the db key
    */
   public void unlockRead(int dbKey) {
      int word = (dbKey >>> segmentShift) & segmentMask;

      locks[word].readLock().unlock();
   }

   /**
    * Unlock write.
    *
    * @param dbKey the db key
    */
   public void unlockWrite(int dbKey) {
      int word = (dbKey >>> segmentShift) & segmentMask;

      locks[word].writeLock().unlock();
   }

   /**
    * Unlock write all.
    */
   public void unlockWriteAll() {
      for (int i = 0; i < locks.length; i++) {
         locks[i].writeLock().unlock();
      }
   }

   /**
    * Write lock.
    *
    * @param dbKey the db key
    */
   public void writeLock(int dbKey) {
      int word = (dbKey >>> segmentShift) & segmentMask;

      locks[word].writeLock().lock();
   }

   /**
    * Write lock all.
    */
   public void writeLockAll() {
      for (int i = 0; i < locks.length; i++) {
         locks[i].writeLock().lock();
      }
   }
}
