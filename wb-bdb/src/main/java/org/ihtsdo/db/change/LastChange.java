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

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.TermChangeListener;

//~--- JDK imports ------------------------------------------------------------

import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author kec
 */
public class LastChange {
   private static final int                                       MAP_SIZE         = 50000;
   private static int                                             concurrencyLevel = 128;
   private static int                                             sshift           = 0;
   private static int                                             ssize            = 1;
   private static int                                             segmentShift     = 32 - sshift;
   private static int                                             segmentMask      = ssize - 1;
   private static ReentrantLock[]                                 locks            =
      new ReentrantLock[concurrencyLevel];
   private static Timer                                           timer            = new Timer("LastChange",
                                                                                        true);
   private static ReentrantReadWriteLock                          rwl              =
      new ReentrantReadWriteLock();
   private static AtomicReference<int[][]>                        lastChangeMap    =
      new AtomicReference<int[][]>(new int[0][]);
   private static AtomicReference<ConcurrentSkipListSet<Integer>> changedXrefs     =
      new AtomicReference<ConcurrentSkipListSet<Integer>>(new ConcurrentSkipListSet<Integer>());
   private static AtomicReference<ConcurrentSkipListSet<Integer>> changedComponents =
      new AtomicReference<ConcurrentSkipListSet<Integer>>(new ConcurrentSkipListSet<Integer>());
   private static ConcurrentSkipListSet<WeakReference<TermChangeListener>> changeListenerRefs =
      new ConcurrentSkipListSet<WeakReference<TermChangeListener>>();

   //~--- static initializers -------------------------------------------------

   static {
      while (ssize < concurrencyLevel) {
         ++sshift;
         ssize <<= 1;
      }

      for (int i = 0; i < concurrencyLevel; i++) {
         locks[i] = new ReentrantLock();
      }

      timer.schedule(new Notifier(), 5000, 2000);
   }

   //~--- constant enums ------------------------------------------------------

   public enum Change { COMPONENT, XREF }

   //~--- methods -------------------------------------------------------------

   public static void addTermChangeListener(TermChangeListener cl) {
      changeListenerRefs.add(new ComparableWeakRef(cl));
   }

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

   public static void removeTermChangeListener(TermChangeListener cl) {
      changeListenerRefs.remove(new ComparableWeakRef(cl));
   }

   public static void touch(Concept c) {
      for (int nid : c.getUncommittedNids().getListArray()) {
         touch(nid, Change.COMPONENT);
      }

      touch(c.getNid(), Change.COMPONENT);
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
            changedComponents.get().add(nid);

            break;

         case XREF :
            short componentSequence = (short) lastChangeMap.get()[mapIndex][indexInMap];

            lastChangeMap.get()[mapIndex][indexInMap] = asInt(componentSequence,
                    BdbCommitSequence.getCommitSequence());
            changedXrefs.get().add(nid);

            break;
         }
      } finally {
         locks[word].unlock();
      }

      if (changeType == Change.XREF) {
         int cNid = Bdb.getConceptNid(nid);

         if ((cNid != nid) && (cNid != Integer.MAX_VALUE)) {
            LastChange.touch(cNid, changeType);
         }
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

   //~--- inner classes -------------------------------------------------------

   public static class ComparableWeakRef extends WeakReference<TermChangeListener>
           implements Comparable<ComparableWeakRef> {
      int id;

      //~--- constructors -----------------------------------------------------

      public ComparableWeakRef(TermChangeListener cl) {
         super(cl);
         this.id = cl.getListenerId();
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(ComparableWeakRef o) {
         return this.id - o.id;
      }
   }


   private static class Notifier extends TimerTask {
      @Override
      public void run() {
         ConcurrentSkipListSet<Integer> changedXrefs =
            LastChange.changedXrefs.getAndSet(new ConcurrentSkipListSet<Integer>());
         ConcurrentSkipListSet<Integer> changedComponents =
            LastChange.changedComponents.getAndSet(new ConcurrentSkipListSet<Integer>());
         long sequence = BdbCommitSequence.nextSequence();

         if (!changedXrefs.isEmpty() ||!changedComponents.isEmpty()) {
            List<WeakReference<TermChangeListener>> toRemove =
               new ArrayList<WeakReference<TermChangeListener>>();

            for (WeakReference<TermChangeListener> clr : changeListenerRefs) {
               TermChangeListener cl = clr.get();

               if (cl == null) {
                  toRemove.add(clr);
               } else {
                  cl.changeNotify(sequence, changedXrefs, changedComponents);
               }
            }

            changeListenerRefs.removeAll(toRemove);
         }
      }
   }
}
