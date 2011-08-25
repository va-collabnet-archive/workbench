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



package org.ihtsdo.db.change;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author kec
 */
public class LastChange {
   private static final int MAP_SIZE = 50000;

   // Find power-of-two sizes best matching arguments
   private static int                      concurrencyLevel = 128;
   private static ReentrantReadWriteLock   rwl              = new ReentrantReadWriteLock();
   private static AtomicReference<int[][]> lastChangeMap    = new AtomicReference<int[][]>(new int[0][]);

   // TODO Consider using an implementation that uses
   // AtomicLongArray rather than simply a long[]...
   private static int             sshift       = 0;
   private static int             ssize        = 1;
   private static int             segmentShift = 32 - sshift;
   private static int             segmentMask  = ssize - 1;
   private static ReentrantLock[] locks        = new ReentrantLock[concurrencyLevel];

   //~--- static initializers -------------------------------------------------

   static {
      while (ssize < concurrencyLevel) {
         ++sshift;
         ssize <<= 1;
      }
   }

   static {
      for (int i = 0; i < concurrencyLevel; i++) {
         locks[i] = new ReentrantLock();
      }
   }

   //~--- constant enums ------------------------------------------------------

   public enum Change { COMPONENT, XREF }

   //~--- methods -------------------------------------------------------------

   private static int asInt(short componentSequence, short xrefSequence) {
      int returnValue = xrefSequence;

      returnValue = returnValue & 0x0000FFFF;

      int componentSequenceInt = componentSequence;

      componentSequenceInt = componentSequenceInt & 0x0000FFFF;
      returnValue          = returnValue << 16;
      returnValue          = returnValue | componentSequenceInt;

      return returnValue;
   }

   private static void ensureCapacity(int nextId) {
      int nidCidMapCount = ((nextId - Integer.MIN_VALUE) / MAP_SIZE) + 1;

      rwl.readLock().lock();

      try {
         if (nidCidMapCount > lastChangeMap.get().length) {
            rwl.readLock().unlock();
            rwl.writeLock().lock();

            if (nidCidMapCount > lastChangeMap.get().length) {
               try {
                  expandCapacity(nidCidMapCount);
               } finally {
                  rwl.readLock().lock();
                  rwl.writeLock().unlock();
               }
            } else {
               rwl.readLock().lock();
               rwl.writeLock().unlock();
            }
         }
      } finally {
         rwl.readLock().unlock();
      }
   }

   private static void expandCapacity(int lastChangeMapCount) {
      int       oldCount      = lastChangeMap.get().length;
      int[][]   newNidCidMaps = new int[lastChangeMapCount][];
      boolean[] newMapChanged = new boolean[lastChangeMapCount];

      System.arraycopy(lastChangeMap.get(), 0, newNidCidMaps, 0, oldCount);

      for (int i = oldCount; i < lastChangeMapCount; i++) {
         newNidCidMaps[i] = new int[MAP_SIZE];
         newMapChanged[i] = true;
         Arrays.fill(newNidCidMaps[i], Integer.MAX_VALUE);
      }

      lastChangeMap.set(newNidCidMaps);
   }

   public static void touch(int nid, Change changeType) {
      assert nid != Integer.MAX_VALUE;
      ensureCapacity(nid);

      int word = (nid >>> segmentShift) & segmentMask;

      locks[word].lock();

      try {
         int mapIndex   = (nid - Integer.MIN_VALUE) / MAP_SIZE;
         int indexInMap = (nid - Integer.MIN_VALUE) % MAP_SIZE;

         assert(mapIndex >= 0) && (indexInMap >= 0) :
               "mapIndex: " + mapIndex + " indexInMap: " + indexInMap + " nid: " + nid;

         switch (changeType) {
         case COMPONENT :
            short xrefSequence = (short) (lastChangeMap.get()[mapIndex][indexInMap] >> 16);

            lastChangeMap.get()[mapIndex][indexInMap] = asInt(BdbCommitSequence.getCommitSequence(),
                    xrefSequence);

            break;

         case XREF :
            short componentSequence = (short) lastChangeMap.get()[mapIndex][indexInMap];

            lastChangeMap.get()[mapIndex][indexInMap] = asInt(componentSequence,
                    BdbCommitSequence.getCommitSequence());

            break;
         }
      } finally {
         locks[word].unlock();
      }
   }

   public static void touchComponent(int nid) {
      touch(nid, Change.COMPONENT);
   }

   public static void touchXref(int nid) {
      touch(nid, Change.XREF);
   }

   //~--- get methods ---------------------------------------------------------

   public static short getLastTouch(int nid, Change changeType) {
      assert nid != Integer.MAX_VALUE;
      ensureCapacity(nid);

      int mapIndex   = (nid - Integer.MIN_VALUE) / MAP_SIZE;
      int indexInMap = (nid - Integer.MIN_VALUE) % MAP_SIZE;

      assert(mapIndex >= 0) && (indexInMap >= 0) :
            "mapIndex: " + mapIndex + " indexInMap: " + indexInMap + " nid: " + nid;

      switch (changeType) {
      case COMPONENT :
         return (short) lastChangeMap.get()[mapIndex][indexInMap];

      case XREF :
         return (short) (lastChangeMap.get()[mapIndex][indexInMap] >> 16);

      default :
         throw new UnsupportedOperationException("can't handle type: " + changeType);
      }
   }

   public static short getLastTouchForComponent(int nid) {
      return getLastTouch(nid, Change.COMPONENT);
   }

   public static short getLastTouchForXref(int nid) {
      return getLastTouch(nid, Change.XREF);
   }
}
