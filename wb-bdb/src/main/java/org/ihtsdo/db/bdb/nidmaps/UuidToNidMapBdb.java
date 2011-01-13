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
package org.ihtsdo.db.bdb.nidmaps;

import com.sleepycat.bind.tuple.ShortBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.PrimordialId;
import org.ihtsdo.concurrency.ConcurrencyLocks;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbMemoryMonitor.LowMemoryListener;
import org.ihtsdo.db.bdb.ComponentBdb;

/**
 *
 * @author kec
 */
public class UuidToNidMapBdb extends ComponentBdb {

    private ConcurrencyLocks locks = new ConcurrencyLocks();
    private int maxGenOneSize = 10000;
    private int maxGenTwoSize = maxGenOneSize * 10;
    private ConcurrentSkipListSet<Short> loadedDbKeys = 
            new ConcurrentSkipListSet<Short>();

    private int generate(UUID key) {
        // if can't find, then generate new...
        int nid = idSequence.getAndIncrement();
        addToDb(key, nid);
        return nid;
    }

    private void addToDb(UUID key, int nid) {
        unwrittenCount.incrementAndGet();
        UuidIntRecord rec = new UuidIntRecord(key, nid);
        Set<UuidIntRecord> uuidIntRecSet =
                unwrittenUuidIntRecordMap.get(rec.getShortUuidHash());
        if (uuidIntRecSet == null) {
            unwrittenUuidIntRecordMap.putIfAbsent(
                    rec.getShortUuidHash(),
                    getMutableKeySet(rec.getShortUuidHash()));
            uuidIntRecSet =
                    unwrittenUuidIntRecordMap.get(rec.getShortUuidHash());
        }
        uuidIntRecSet.add(rec);
        handleGenOnePut(key, nid);
    }

    private void handleGenOnePut(UUID key, int nid) {
        newGen1Puts.incrementAndGet();
        if (newGen1Puts.get() > maxGenOneSize) {
            int genOneSize = gen1UuidIntMap.size();
            if (genOneSize > maxGenOneSize) {
                gen2UuidIntMap = gen1UuidIntMap;
                loadedDbKeys.clear();
                gen1UuidIntMap = new ConcurrentHashMap<UUID, Integer>();
                newGen1Puts.set(0);
            } else {
                newGen1Puts.set(genOneSize);
            }
        }
        gen1UuidIntMap.put(key, nid);
    }

    private void handleGenTwoPuts() {
        newGen2Puts.incrementAndGet();
        if (newGen2Puts.get() > maxGenTwoSize) {
            int genTwoSize = gen2UuidIntMap.size();
            if (genTwoSize > maxGenTwoSize) {
                gen2UuidIntMap = new ConcurrentHashMap<UUID, Integer>();
                newGen2Puts.set(0);
                loadedDbKeys.clear();
            } else {
                newGen2Puts.set(genTwoSize);
            }
        }
    }

    private class IdSequence {

        private AtomicInteger sequence = new AtomicInteger(Integer.MIN_VALUE);

        public IdSequence() throws IOException {
            super();
            String nextIdStr = Bdb.getProperty(ID_NEXT);
            if (nextIdStr == null) {
                int max = Integer.MIN_VALUE;
                for (PrimordialId primoridal : PrimordialId.values()) {
                    primoridal.getNativeId();
                    max = Math.max(max, primoridal.getNativeId());
                    for (UUID uuid : primoridal.getUids()) {
                        UuidIntRecord rec = new UuidIntRecord(uuid,
                                primoridal.getNativeId());
                        gen1UuidIntMap.put(uuid, rec.getNid());
                        short shortHash = rec.getShortUuidHash();
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

        public final int getAndIncrement() {
            int next = sequence.getAndIncrement();
            return next;
        }
    }
    private static final String ID_NEXT = "org.ihtsdo.ID_NEXT";
    private static UuidNidBinder binder = new UuidNidBinder();
    private ConcurrentHashMap<UUID, Integer> gen1UuidIntMap =
            new ConcurrentHashMap<UUID, Integer>();
    private ConcurrentHashMap<UUID, Integer> gen2UuidIntMap =
            new ConcurrentHashMap<UUID, Integer>();
    private ConcurrentHashMap<Short, Set<UuidIntRecord>> unwrittenUuidIntRecordMap =
            new ConcurrentHashMap<Short, Set<UuidIntRecord>>();
    private AtomicInteger unwrittenCount = new AtomicInteger(0);
    private AtomicInteger newGen1Puts = new AtomicInteger(0);
    private AtomicInteger newGen2Puts = new AtomicInteger(0);
    private IdSequence idSequence;

    public UuidToNidMapBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
        super(readOnlyBdbEnv, mutableBdbEnv);
        idSequence = new IdSequence();
        Bdb.addMemoryMonitorListener(new LowMemoryListener() {

            @Override
            public void memoryUsageLow(long usedMemory, long maxMemory) {
                double percentageUsed = ((double) usedMemory) / maxMemory;
                AceLog.getAppLog().
                        warning("Memory low. Percent used: " + percentageUsed
                        + " UuidToNidMapBdb trying to recover memory. ");
                loadedDbKeys.clear();
                gen2UuidIntMap.clear();
                gen1UuidIntMap.clear();
                loadedDbKeys.clear();
                Set<Short> setsToWrite = unwrittenUuidIntRecordMap.keySet();
                for (Short set : setsToWrite) {
                    writeMutableKeySet(set);
                }
                System.gc();
                usedMemory = maxMemory - Runtime.getRuntime().freeMemory();
                percentageUsed = ((double) usedMemory) / maxMemory;
                AceLog.getAppLog().
                        info("UuidToNidMapBdb finished recover memory. "
                        + "Percent used: " + percentageUsed);
            }
        });
    }

    private int getNoGen(UUID key) {
        Integer nid = gen1UuidIntMap.get(key);
        if (nid != null) {
            return nid;
        }
        nid = gen2UuidIntMap.get(key);
        if (nid != null) {
            handleGenOnePut(key, nid);
            return nid;
        }
        Set<UuidIntRecord> unwritten = unwrittenUuidIntRecordMap.get(
                UuidIntRecord.getShortUuidHash(key));
        if (unwritten != null) {
            for (UuidIntRecord rec : unwritten) {
                if (rec.getMsb() == key.getMostSignificantBits()
                        && rec.getLsb() == key.getLeastSignificantBits()) {
                    nid = rec.getNid();
                    handleGenOnePut(key, nid);
                    return nid;
                }
            }
        }
        // get from database;
        nid = getFromDb(key);
        if (nid != null) {
            handleGenOnePut(key, nid);
            return nid;
        }
        return Integer.MIN_VALUE;
    }

    public int get(UUID key) {
        int nid = getNoGen(key);
        if (nid != Integer.MIN_VALUE) {
            return nid;
        }
        return generate(key);
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

    private Set<UuidIntRecord> getMutableKeySet(short dbKey) {
        locks.readLock(dbKey);
        try {
            DatabaseEntry theKey = new DatabaseEntry();
            ShortBinding.shortToEntry(dbKey, theKey);
            DatabaseEntry theData = new DatabaseEntry();
            if (mutable.get(null, theKey, theData,
                    LockMode.READ_UNCOMMITTED)
                    == OperationStatus.SUCCESS) {
                return binder.entryToObject(theData);
            }
            return new ConcurrentSkipListSet<UuidIntRecord>();
        } finally {
            locks.unlockRead(dbKey);
        }
    }

    private Set<UuidIntRecord> getReadOnlyKeySet(short dbKey) {
        DatabaseEntry theKey = new DatabaseEntry();
        ShortBinding.shortToEntry(dbKey, theKey);
        DatabaseEntry theData = new DatabaseEntry();
        if (readOnly.get(null, theKey, theData,
                LockMode.READ_UNCOMMITTED)
                == OperationStatus.SUCCESS) {
            return binder.entryToObject(theData);
        }
        return new ConcurrentSkipListSet<UuidIntRecord>();
    }

    private Integer getFromDb(UUID key) {
        Integer theNid = null;
        short dbKey = UuidIntRecord.getShortUuidHash(key);
        if (loadedDbKeys.contains((Short) dbKey)) {
            // Already loaded all from db...
            return theNid;
        }
        for (UuidIntRecord entry : getReadOnlyKeySet(dbKey)) {
            UUID theUuid = entry.getUuid();
            if (theNid == null && key.equals(entry.getUuid())) {
                theNid = entry.getNid();
            }
            newGen2Puts.incrementAndGet();
            gen2UuidIntMap.put(theUuid, entry.getNid());
        }
        for (UuidIntRecord entry : getMutableKeySet(dbKey)) {
            UUID theUuid = entry.getUuid();
            if (theNid == null && key.equals(entry.getUuid())) {
                theNid = entry.getNid();
            }
            newGen2Puts.incrementAndGet();
            gen2UuidIntMap.put(theUuid, entry.getNid());
        }
        loadedDbKeys.add((Short) dbKey);
        handleGenTwoPuts();
        return theNid;
    }

    @Override
    protected void init() throws IOException {
        // nothing to preload;
    }

    @Override
    protected String getDbName() {
        return "Uuid2NidBdb";
    }

    @Override
    public void sync() throws IOException {
        Bdb.setProperty(ID_NEXT, Integer.toString(idSequence.sequence.get()));
        Set<Short> setsToWrite = unwrittenUuidIntRecordMap.keySet();
        for (Short set : setsToWrite) {
            writeMutableKeySet(set);
        }
        super.sync();
    }

    public List<UUID> getUuidsForNid(int nid) throws IOException {
        return Bdb.getComponent(nid).getUUIDs();
    }

    public int uuidsToNid(UUID[] uuids) {
        return uuidsToNid(Arrays.asList(uuids));
    }

    public int uuidToNid(UUID uuid) {
        return get(uuid);
    }

    public boolean hasUuid(UUID uuid) {
        return getNoGen(uuid) != Integer.MIN_VALUE;
    }

    public void put(UUID uuid, int nid) {
        addToDb(uuid, nid);
    }

    public int uuidsToNid(Collection<UUID> uuids) {
        for (UUID uuid : uuids) {
            int nid = getNoGen(uuid);
            if (nid != Integer.MIN_VALUE) {
                return nid;
            }
        }
        int nid = Integer.MIN_VALUE;
        for (UUID uuid : uuids) {
            if (nid == Integer.MIN_VALUE) {
                nid = generate(uuid);
            } else {
                addToDb(uuid, nid);
            }
        }
        return nid;
    }

    public int getCurrentMaxNid() {
        return idSequence.sequence.get() - 1;
    }
}
