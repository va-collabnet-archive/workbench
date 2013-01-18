/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */



package org.ihtsdo.db.bdb.nidmaps;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.ShortBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.PrimordialId;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbMemoryMonitor.LowMemoryListener;
import org.ihtsdo.db.bdb.ComponentBdb;
import org.ihtsdo.tk.api.ComponentBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.concurrency.ConcurrentReentrantReadWriteLocks;

/**
 *
 * @author kec
 */
public class UuidToNidMapBdb extends ComponentBdb {
   private static final String  ID_NEXT = "org.ihtsdo.ID_NEXT";
   private static UuidNidBinder binder  = new UuidNidBinder();

   //~--- fields --------------------------------------------------------------

   private ConcurrentReentrantReadWriteLocks            locks                     = new ConcurrentReentrantReadWriteLocks();
   private int                                          maxGenOneSize             = 10000;
   private int                                          maxGenTwoSize             = maxGenOneSize * 10;
   private ConcurrentSkipListSet<Short>                 loadedDbKeys              =
      new ConcurrentSkipListSet<>();
   private ConcurrentHashMap<UUID, Integer>             gen2UuidIntMap            =
      new ConcurrentHashMap<>();
   private ConcurrentHashMap<UUID, Integer>             gen1UuidIntMap            =
      new ConcurrentHashMap<>();
   private ConcurrentSkipListSet<UUID>                  backingSet                =
      new ConcurrentSkipListSet<>();
   private ConcurrentHashMap<Short, Set<UuidIntRecord>> unwrittenUuidIntRecordMap =
      new ConcurrentHashMap<>();
   private AtomicInteger unwrittenCount = new AtomicInteger(0);
   private AtomicInteger newGen2Puts    = new AtomicInteger(0);
   private AtomicInteger newGen1Puts    = new AtomicInteger(0);
   private boolean       useBackingSet  = false;
   private IdSequence    idSequence;

   //~--- constructors --------------------------------------------------------

   public UuidToNidMapBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
      super(readOnlyBdbEnv, mutableBdbEnv);
      idSequence = new IdSequence();
      Bdb.addMemoryMonitorListener(new LowMemoryListener() {
         @Override
         public void memoryUsageLow(long usedMemory, long maxMemory) {
            double percentageUsed = ((double) usedMemory) / maxMemory;

            AceLog.getAppLog().warning("Memory low. Percent used: " + percentageUsed
                                       + " UuidToNidMapBdb trying to recover memory. ");
            locks.writeLockAll();

            try {
               clearLoadedDbKeys();
               gen2UuidIntMap.clear();
               clearLoadedDbKeys();
            } finally {
               locks.unlockWriteAll();
            }

            Set<Short> setsToWrite = unwrittenUuidIntRecordMap.keySet();

            for (Short set : setsToWrite) {
               writeMutableKeySet(set);
            }

            System.gc();
            usedMemory     = maxMemory - Runtime.getRuntime().freeMemory();
            percentageUsed = ((double) usedMemory) / maxMemory;
            AceLog.getAppLog().info("UuidToNidMapBdb finished recover memory. " + "Percent used: "
                                    + percentageUsed);
         }
      });
   }

   //~--- methods -------------------------------------------------------------

   private void addToDb(UUID key, int nid) {
      unwrittenCount.incrementAndGet();

      UuidIntRecord      rec           = new UuidIntRecord(key, nid);
      Set<UuidIntRecord> uuidIntRecSet = unwrittenUuidIntRecordMap.get(rec.getShortUuidHash());

      if (uuidIntRecSet == null) {
         unwrittenUuidIntRecordMap.putIfAbsent(rec.getShortUuidHash(),
                 getMutableKeySet(rec.getShortUuidHash()));
         uuidIntRecSet = unwrittenUuidIntRecordMap.get(rec.getShortUuidHash());
      }

      uuidIntRecSet.add(rec);
      handleGenOnePut(key, nid);
   }

   private void clearLoadedDbKeys() {
      for (Short key : loadedDbKeys) {
         locks.writeLock(key);

         try {
            loadedDbKeys.remove(key);
         } finally {
            locks.unlockWrite(key);
         }
      }
   }

   private int generate(UUID key) {
      locks.writeLock(UuidIntRecord.getShortUuidHash(key));

      try {
         int nid = getNoGen(key, false);

         // if can't find, then generate new...
         if (nid == Integer.MIN_VALUE) {
            nid = idSequence.getAndIncrement();

            if (nid == 0) {
               nid = idSequence.getAndIncrement();
            }

            addToDb(key, nid);
         }

         return nid;
      } finally {
         locks.unlockWrite(UuidIntRecord.getShortUuidHash(key));
      }
   }

   private void handleGenOnePut(UUID key, int nid) {
      newGen1Puts.incrementAndGet();

      if (newGen1Puts.get() > maxGenOneSize) {
         int genOneSize = gen1UuidIntMap.size();

         if (genOneSize > maxGenOneSize) {
            locks.writeLockAll();

            try {
               gen2UuidIntMap = gen1UuidIntMap;
               clearLoadedDbKeys();
               gen1UuidIntMap = new ConcurrentHashMap<>();
               newGen1Puts.set(0);
            } finally {
               locks.unlockWriteAll();
            }
         } else {
            newGen1Puts.set(genOneSize);
         }
      }

      gen1UuidIntMap.put(key, nid);
   }

   private void handleGenTwoPuts() {
      newGen2Puts.incrementAndGet();

      if (newGen2Puts.get() > maxGenTwoSize) {
         locks.writeLockAll();

         try {
            int genTwoSize = gen2UuidIntMap.size();

            if (genTwoSize > maxGenTwoSize) {
               gen2UuidIntMap = new ConcurrentHashMap<>();
               newGen2Puts.set(0);
               clearLoadedDbKeys();
            } else {
               newGen2Puts.set(genTwoSize);
            }
         } finally {
            locks.unlockWriteAll();
         }
      }
   }

   @Override
   protected void init() throws IOException {

      // nothing to preload;
   }

   public void put(UUID uuid, int nid) {
      addToDb(uuid, nid);
   }

   @Override
   public void sync() throws IOException {
      Bdb.setProperty(ID_NEXT, Integer.toString(idSequence.sequence.get()));

      Set<Short> setsToWrite = unwrittenUuidIntRecordMap.keySet();

      clearLoadedDbKeys();

      for (Short set : setsToWrite) {
         writeMutableKeySet(set);
      }

      super.sync();
   }

   public int uuidToNid(UUID uuid) {
      return get(uuid);
   }

   public int uuidsToNid(Collection<UUID> uuids) {
      Collection<UUID> uuidsToAdd = new ArrayList<>(uuids.size());
      int nid = Integer.MIN_VALUE;
      for (UUID uuid : uuids) {
         int tempNid = getNoGen(uuid, false);

         if (tempNid == Integer.MIN_VALUE) {
            uuidsToAdd.add(uuid);
         } else {
            nid = tempNid;
         }
      }

      for (UUID uuid : uuidsToAdd) {
         if (nid == Integer.MIN_VALUE) {
            nid = generate(uuid);
         } else {
            addToDb(uuid, nid);
         }
      }

      return nid;
   }

   public int uuidsToNid(UUID[] uuids) {
      return uuidsToNid(Arrays.asList(uuids));
   }

   private void writeMutableKeySet(Short dbKey) {
      locks.writeLock(dbKey);

      try {
         Set<UuidIntRecord> toWrite = unwrittenUuidIntRecordMap.remove(dbKey);

         if (toWrite != null) {
            DatabaseEntry theKey = new DatabaseEntry();

            ShortBinding.shortToEntry(dbKey, theKey);

            DatabaseEntry theData = new DatabaseEntry();

            binder.objectToEntry(toWrite, theData);
            mutable.put(null, theKey, theData);
         }
      } finally {
         locks.unlockWrite(dbKey);
      }
   }

   //~--- get methods ---------------------------------------------------------

   public int get(UUID key) {
      int nid = getNoGen(key, false);

      if (nid != Integer.MIN_VALUE) {
         return nid;
      }

      locks.writeLock(UuidIntRecord.getShortUuidHash(key));

      try {
         nid = getNoGen(key, false);

         if (nid != Integer.MIN_VALUE) {
            return nid;
         }

         if (useBackingSet && backingSet.contains(key)) {
            nid = getNoGen(key, true);

            if (nid != Integer.MIN_VALUE) {
               System.out.println("Lost then found.  ");

               return nid;
            }

            System.out.println("Lost NOT found.  ");
         }
      } finally {
         locks.unlockWrite(UuidIntRecord.getShortUuidHash(key));
      }

      nid = generate(key);

      if (useBackingSet) {
         backingSet.add(key);
      }

      return nid;
   }

   public int getCurrentMaxNid() {
      return idSequence.sequence.get() - 1;
   }

   @Override
   protected String getDbName() {
      return "Uuid2NidBdb";
   }

   private List<UUID> getFromDb(int nid) {
      List<UUID> uuids = new ArrayList<>(2);

      for (Map.Entry<UUID, Integer> entry : gen1UuidIntMap.entrySet()) {
         if (entry.getValue() == nid) {
            uuids.add(entry.getKey());
         }
      }

      for (Map.Entry<UUID, Integer> entry : gen2UuidIntMap.entrySet()) {
         if (entry.getValue() == nid) {
            uuids.add(entry.getKey());
         }
      }

      if (uuids.size() > 0) {
         return uuids;
      }

      for (short i = 0; i < locks.getConcurrencyLevel(); i++) {
         locks.readLock(i);

         try {
            for (UuidIntRecord entry : getReadOnlyKeySet(i)) {
               if (entry.getNid() == nid) {
                  uuids.add(entry.getUuid());
               }
            }

            for (UuidIntRecord entry : getMutableKeySet(i)) {
               if (entry.getNid() == nid) {
                  uuids.add(entry.getUuid());
               }
            }
         } finally {
            locks.unlockRead(i);
         }
      }

      return uuids;
   }

   private Integer getFromDb(UUID key, boolean force) {
      Integer theNid = null;
      short   dbKey  = UuidIntRecord.getShortUuidHash(key);

      if (!force) {
         if (loadedDbKeys.contains((Short) dbKey)) {

            // Already loaded all from db...
            return theNid;
         }
      }

      locks.readLock(UuidIntRecord.getShortUuidHash(key));

      try {
         for (UuidIntRecord entry : getReadOnlyKeySet(dbKey)) {
            UUID theUuid = entry.getUuid();

            if ((theNid == null) && key.equals(entry.getUuid())) {
               theNid = entry.getNid();
            }

            newGen2Puts.incrementAndGet();
            gen2UuidIntMap.put(theUuid, entry.getNid());
         }

         for (UuidIntRecord entry : getMutableKeySet(dbKey)) {
            UUID theUuid = entry.getUuid();

            if ((theNid == null) && key.equals(entry.getUuid())) {
               theNid = entry.getNid();
            }

            newGen2Puts.incrementAndGet();
            gen2UuidIntMap.put(theUuid, entry.getNid());
         }

         loadedDbKeys.add((Short) dbKey);
      } finally {
         locks.unlockRead(UuidIntRecord.getShortUuidHash(key));
      }

      handleGenTwoPuts();

      return theNid;
   }

   private Set<UuidIntRecord> getMutableKeySet(short dbKey) {
      locks.readLock(dbKey);

      try {
         DatabaseEntry theKey = new DatabaseEntry();

         ShortBinding.shortToEntry(dbKey, theKey);

         DatabaseEntry theData = new DatabaseEntry();

         if (mutable.get(null, theKey, theData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
            return binder.entryToObject(theData);
         }

         return new ConcurrentSkipListSet<>();
      } finally {
         locks.unlockRead(dbKey);
      }
   }

   private int getNoGen(UUID key, boolean force) {
      Integer nid = gen1UuidIntMap.get(key);

      if (nid != null) {
         return nid;
      }

      nid = gen2UuidIntMap.get(key);

      if (nid != null) {
         handleGenOnePut(key, nid);

         return nid;
      }

      Set<UuidIntRecord> unwritten = unwrittenUuidIntRecordMap.get(UuidIntRecord.getShortUuidHash(key));

      if (unwritten != null) {
         for (UuidIntRecord rec : unwritten) {
            if ((rec.getMsb() == key.getMostSignificantBits())
                    && (rec.getLsb() == key.getLeastSignificantBits())) {
               nid = rec.getNid();
               handleGenOnePut(key, nid);

               return nid;
            }
         }
      }

      // get from database;
      nid = getFromDb(key, force);

      if (nid != null) {
         handleGenOnePut(key, nid);

         return nid;
      }

      return Integer.MIN_VALUE;
   }

   private Set<UuidIntRecord> getReadOnlyKeySet(short dbKey) {
      DatabaseEntry theKey = new DatabaseEntry();

      ShortBinding.shortToEntry(dbKey, theKey);

      DatabaseEntry theData = new DatabaseEntry();

      if (readOnly.get(null, theKey, theData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
         return binder.entryToObject(theData);
      }

      return new ConcurrentSkipListSet<>();
   }

   public List<UUID> getUuidsForNid(int nid) throws IOException {
      ComponentBI component = Bdb.getComponent(nid);

      if (component != null) {
         return Bdb.getComponent(nid).getUUIDs();
      }

      return getFromDb(nid);
   }

   public boolean hasUuid(UUID uuid) {
      return getNoGen(uuid, false) != Integer.MIN_VALUE;
   }

   //~--- inner classes -------------------------------------------------------

   private class IdSequence {
      private AtomicInteger sequence = new AtomicInteger(Integer.MIN_VALUE);

      //~--- constructors -----------------------------------------------------

      public IdSequence() throws IOException {
         super();

         String nextIdStr = Bdb.getProperty(ID_NEXT);

         if (nextIdStr == null) {
            int max = Integer.MIN_VALUE;

            for (PrimordialId primoridal : PrimordialId.values()) {
               max = Math.max(max, primoridal.getNativeId());

               for (UUID uuid : primoridal.getUids()) {
                  UuidIntRecord rec = new UuidIntRecord(uuid, primoridal.getNativeId());

                  gen1UuidIntMap.put(uuid, rec.getNid());

                  short              shortHash     = rec.getShortUuidHash();
                  Set<UuidIntRecord> uuidIntRecSet = unwrittenUuidIntRecordMap.get(shortHash);

                  if (uuidIntRecSet == null) {
                     unwrittenUuidIntRecordMap.putIfAbsent(shortHash,
                             new ConcurrentSkipListSet<UuidIntRecord>());
                     uuidIntRecSet = unwrittenUuidIntRecordMap.get(shortHash);
                  }

                  uuidIntRecSet.add(rec);
                  unwrittenCount.getAndIncrement();
               }

               sequence.set(max + 1);
            }

            Bdb.setProperty(ID_NEXT, Integer.toString(sequence.get()));
         } else {
            sequence = new AtomicInteger(Integer.decode(nextIdStr));
         }
      }

      public IdSequence(int nextId) throws IOException {
         super();
         sequence = new AtomicInteger(nextId);
         Bdb.setProperty(ID_NEXT, Integer.toString(sequence.get()));
      }

      //~--- get methods ------------------------------------------------------

      public final int getAndIncrement() {
         int next = sequence.getAndIncrement();

         return next;
      }
   }
}
