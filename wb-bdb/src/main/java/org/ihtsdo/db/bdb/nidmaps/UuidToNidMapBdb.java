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

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import org.dwfa.cement.PrimordialId;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.db.uuidmap.UuidIntProcedure;
import org.ihtsdo.db.uuidmap.UuidToIntHashMap;

/**
 *
 * @author kec
 */
public class UuidToNidMapBdb extends ComponentBdb {
   private static final String  ID_NEXT = "org.ihtsdo.ID_NEXT";

   //~--- fields --------------------------------------------------------------

   private IdSequence    idSequence;
   private ReentrantLock generateLock = new ReentrantLock();
   private int largestNid = Integer.MIN_VALUE;
   
    UuidToIntHashMap readOnlyMap;
    ConcurrentHashMap<UUID,Integer> mutableMap;
    UuidIntConcurrentHashMapBinder mutableMapBinder;

    public void setMap(UuidToIntHashMap readOnlyMap, ConcurrentHashMap mutableMap) {
        this.readOnlyMap = readOnlyMap;
        this.mutableMap = mutableMap;
   }

    //~--- constructors --------------------------------------------------------

   public UuidToNidMapBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
      super(readOnlyBdbEnv, mutableBdbEnv);
   }

   //~--- methods -------------------------------------------------------------

   private void addToDb(UUID key, int nid) {
       if (mutableMap != null) {
           mutableMap.put(key, nid);
       }

   }


   private int generate(UUID key) {
        generateLock.lock();
        try {
         int nid = getNoGen(key);

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
            generateLock.unlock();
        }
   }

   @Override
   protected void init() throws IOException {
       idSequence = new IdSequence();
       mutableMapBinder = new UuidIntConcurrentHashMapBinder();
       
       DatabaseEntry theKey = new DatabaseEntry();
       IntegerBinding.intToEntry(0, theKey);

       DatabaseEntry theData = new DatabaseEntry();
       UuidToIntHashMap.UuidToIntHashMapBinder uuidIntMapBinder = UuidToIntHashMap.getUuidIntMapBinder();
       if (readOnly.get(null, theKey, theData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
           readOnlyMap = uuidIntMapBinder.entryToObject(theData);
       } else {
           readOnlyMap = new UuidToIntHashMap();
       }
       
       if(uuidIntMapBinder.needToWriteBack == true){
           DatabaseEntry valueEntry = new DatabaseEntry();
           uuidIntMapBinder.objectToEntry(readOnlyMap, valueEntry);
           theKey = new DatabaseEntry();

           IntegerBinding.intToEntry(0, theKey);
           readOnly.put(null, theKey, valueEntry);
       }

       if (mutable.get(null, theKey, theData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
           mutableMap = mutableMapBinder.entryToObject(theData);
       } else {
           mutableMap = new ConcurrentHashMap<>();
       }
       largestNid = Integer.MIN_VALUE;
       readOnlyMap.forEachPair(new UuidIntProcedure() {

           @Override
           public boolean apply(long[] uuid, int nid) {
               largestNid = Math.max(largestNid, nid);
               return true;
           }

           @Override
           public void close() {
               //nothing to do
           }
       });
       
       Iterator<Map.Entry<UUID, Integer>> mutableMapItr = mutableMap.entrySet().iterator();
       
       while(mutableMapItr.hasNext()){
           int nid = mutableMapItr.next().getValue();
           largestNid = Math.max(largestNid, nid);
       }
       
       //add + 1 get what the next nid (sequence) should be
       largestNid = largestNid + 1;
       
       //write to log if the two disagree (sequence and found value);
       if(largestNid != idSequence.sequence.get()){
           Logger.getLogger(UuidToNidMapBdb.class.getName()).log(Level.SEVERE, "Nid sequence and nids do not match. Sequence is: " + idSequence.sequence.get() + " Should be: " + largestNid);
           new IdSequence(largestNid);
       }
   }

   public void put(UUID uuid, int nid) {
      addToDb(uuid, nid);
   }

   @Override
   public void sync() throws IOException {
           Bdb.setProperty(ID_NEXT, Integer.toString(idSequence.sequence.get()));

           DatabaseEntry valueEntry = new DatabaseEntry();
           mutableMapBinder.objectToEntry(mutableMap, valueEntry);
           DatabaseEntry theKey = new DatabaseEntry();

           IntegerBinding.intToEntry(0, theKey);
           mutable.put(null, theKey, valueEntry);

           super.sync();
   }

   public int uuidToNid(UUID uuid) {
      return get(uuid);
   }

   public int uuidsToNid(Collection<UUID> uuids) {
      Collection<UUID> uuidsToAdd = new ArrayList<>(uuids.size());
      int nid = Integer.MIN_VALUE;
      for (UUID uuid : uuids) {
         int tempNid = getNoGen(uuid);

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

   //~--- get methods ---------------------------------------------------------

   public int get(UUID key) {
      int nid = getNoGen(key);

      if (nid != Integer.MIN_VALUE) {
         return nid;
      }

      nid = generate(key);

      return nid;
   }

   public int getCurrentMaxNid() {
      return idSequence.sequence.get() - 1;
   }

   @Override
   protected String getDbName() {
      return "Uuid2NidBdb";
   }

   private int getNoGen(UUID key) {
       if (readOnlyMap != null) {
           int nid = readOnlyMap.get(key);
           if (nid != Integer.MAX_VALUE) {
               return nid;
           }
       }
       if (mutableMap != null && mutableMap.containsKey(key)) {
           int nid = mutableMap.get(key);
           if (nid != Integer.MAX_VALUE) {
                return nid;
           }
       }
      return Integer.MIN_VALUE;
   }

   public List<UUID> getUuidsForNid(int nid) throws IOException {
      List<UUID> uuids = new ArrayList<>();
       if (readOnlyMap != null) {
           uuids = readOnlyMap.keysOf(nid);
       }
       if (mutableMap != null && mutableMap.containsValue(nid)) {
           for(Map.Entry<UUID, Integer> entry : mutableMap.entrySet()){
               if(entry.getValue().intValue() == nid){
                   uuids.add(entry.getKey());
               }
           }
       }
       return uuids;
   }

   public boolean hasUuid(UUID uuid) {
      return getNoGen(uuid) != Integer.MIN_VALUE;
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
