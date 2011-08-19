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

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.cern.colt.map.OpenIntIntHashMap;
import org.ihtsdo.db.bdb.Bdb;

/**
 *
 * @author kec
 */
public class LastChange {
    private static final int MAP_SIZE = 50000;
    private AtomicReference<int[][]> nidCNidMaps;
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    private void printKeys(String prefix, Database db) {
        int size = (int) db.count();
        OpenIntIntHashMap nidMap = new OpenIntIntHashMap(size + 2);
        CursorConfig cursorConfig = new CursorConfig();
        cursorConfig.setReadUncommitted(true);
        Cursor cursor = db.openCursor(null, cursorConfig);
        try {
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            foundData.setPartial(true);
            foundData.setPartial(0, 0, true);
            int max = Integer.MIN_VALUE;
            while (cursor.getNext(foundKey, foundData,
                    LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
                int cNid = IntegerBinding.entryToInt(foundKey);
                nidMap.put(cNid, cNid);
                max = Math.max(max, cNid);
            }
            cursor.close();
            AceLog.getAppLog().fine(prefix + nidMap.keys().toList().toString());
        } finally {
            cursor.close();
        }

    }

    
    public int getCNid(int nid) {
        assert nid != Integer.MAX_VALUE;
        int mapIndex = (nid - Integer.MIN_VALUE) / MAP_SIZE;
        int indexInMap = (nid - Integer.MIN_VALUE) % MAP_SIZE;
        assert mapIndex >= 0 && indexInMap >= 0 : "mapIndex: " + mapIndex + " indexInMap: "
                + indexInMap + " nid: " + nid;
        if (mapIndex >= nidCNidMaps.get().length) {
            return Integer.MAX_VALUE;
        }
        return nidCNidMaps.get()[mapIndex][indexInMap];
    }

    public void setCNidForNid(int cNid, int nid) throws IOException {
        assert cNid != Integer.MAX_VALUE;
        int mapIndex = (nid - Integer.MIN_VALUE) / MAP_SIZE;
        assert mapIndex >= 0 : "cNid: " + cNid + " nid: " + nid + " mapIndex: " + mapIndex;
        int indexInMap = (nid - Integer.MIN_VALUE) % MAP_SIZE;
        assert indexInMap < MAP_SIZE : "cNid: " + cNid + " nid: " + nid + " mapIndex: " + mapIndex
                + " indexInMap: " + indexInMap;
        assert cNid == nid || hasConcept(cNid): cNid + " is not a concept nid. nid: " + nid;

        ensureCapacity(nid);
        assert nidCNidMaps.get()[mapIndex][indexInMap] == Integer.MAX_VALUE
                || nidCNidMaps.get()[mapIndex][indexInMap] == cNid : "processing cNid: " + cNid
                + " " + Bdb.getUuidsToNidMap().getUuidsForNid(cNid)
                + " nid: " + nid + " found existing cNid: " + nidCNidMaps.get()[mapIndex][indexInMap]
                + " " + Bdb.getUuidsToNidMap().getUuidsForNid(nidCNidMaps.get()[mapIndex][indexInMap])
                + "\n    " + cNid + " maps to: " + getCNid(cNid)
                + "\n    " + nidCNidMaps.get()[mapIndex][indexInMap]
                + " maps to: " + getCNid(nidCNidMaps.get()[mapIndex][indexInMap]);
        if (nidCNidMaps.get() != null && nidCNidMaps.get()[mapIndex] != null) {
            if (nidCNidMaps.get()[mapIndex][indexInMap] != cNid) {
                nidCNidMaps.get()[mapIndex][indexInMap] = cNid;
            }
        } else {
            if (nidCNidMaps.get() == null) {
                throw new IOException("Null nidCidMap: ");
            }
            throw new IOException("nidCidMap[" + mapIndex + "] "
                    + "is null. cNid: " + cNid + " nid: " + nid);
        }
    }

    private void ensureCapacity(int nextId) throws IOException {
        int nidCidMapCount = ((nextId - Integer.MIN_VALUE) / MAP_SIZE) + 1;
        rwl.readLock().lock();
        try {
            if (nidCidMapCount > nidCNidMaps.get().length) {
                rwl.readLock().unlock();
                rwl.writeLock().lock();
                if (nidCidMapCount > nidCNidMaps.get().length) {
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

    private void expandCapacity(int nidCidMapCount) throws IOException {
        int oldCount = nidCNidMaps.get().length;
        int[][] newNidCidMaps = new int[nidCidMapCount][];
        boolean[] newMapChanged = new boolean[nidCidMapCount];
        for (int i = 0; i < oldCount; i++) {
            newNidCidMaps[i] = nidCNidMaps.get()[i];
        }
        for (int i = oldCount; i < nidCidMapCount; i++) {
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine("Expanding NidCidMaps to: " + (i + 1));
            }
            newNidCidMaps[i] = new int[MAP_SIZE];
            newMapChanged[i] = true;
            Arrays.fill(newNidCidMaps[i], Integer.MAX_VALUE);
        }
        nidCNidMaps.set(newNidCidMaps);
    }

    public boolean hasConcept(int cNid) {
        assert cNid > Integer.MIN_VALUE : "Invalid cNid == Integer.MIN_VALUE: " + cNid;
        assert cNid <= Bdb.getUuidsToNidMap().getCurrentMaxNid() : "Invalid cNid: " + cNid + " currentMax: " + Bdb.getUuidsToNidMap().getCurrentMaxNid();
        int mapIndex = (cNid - Integer.MIN_VALUE) / MAP_SIZE;
        int indexInMap = (cNid - Integer.MIN_VALUE) % MAP_SIZE;
        
        if (mapIndex < 0 || mapIndex >= nidCNidMaps.get().length) {
            return false;
        }
        if (indexInMap < 0 || indexInMap >= MAP_SIZE) {
            return false;
        }
        if (nidCNidMaps.get()[mapIndex][indexInMap] == cNid) {
            return true;
        }
        return false;
    }

    
}
