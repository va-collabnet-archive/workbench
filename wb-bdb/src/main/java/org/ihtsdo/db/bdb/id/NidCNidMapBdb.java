package org.ihtsdo.db.bdb.id;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.cern.colt.map.OpenIntIntHashMap;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 *
 * @author kec
 *
 */
public class NidCNidMapBdb extends ComponentBdb {

    private static final int NID_CNID_MAP_SIZE = 50000;
    private AtomicReference<int[][]> nidCNidMaps;
    private boolean[] mapChanged;
    private int readOnlyRecords;
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    public NidCNidMapBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
        super(readOnlyBdbEnv, mutableBdbEnv);
    }

    @Override
    protected void init() throws IOException {
        int maxId = Bdb.getUuidsToNidMap().getCurrentMaxNid();
        readOnlyRecords = (int) readOnly.count();
        int mutableRecords = (int) mutable.count();
        AceLog.getAppLog().info("NidCidMap readOnlyRecords: " + readOnlyRecords);
        AceLog.getAppLog().info("NidCidMap mutableRecords: " + mutableRecords);
        int nidCidMapCount = ((maxId - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE) + 1;
        nidCNidMaps = new AtomicReference<int[][]>(new int[nidCidMapCount][]);
        mapChanged = new boolean[nidCidMapCount];
        Arrays.fill(mapChanged, false);
        for (int index = 0; index < nidCidMapCount; index++) {
            nidCNidMaps.get()[index] = new int[NID_CNID_MAP_SIZE];
            Arrays.fill(nidCNidMaps.get()[index], Integer.MAX_VALUE);
        }
        maxId = (nidCNidMaps.get().length * NID_CNID_MAP_SIZE) - Integer.MIN_VALUE;

        readMaps(readOnly, true);
        readMaps(mutable, false);
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            printKeys("Read only keys: ", readOnly);
            printKeys("Mutable keys: ", mutable);
        }
    }

    private void readMaps(Database db, boolean readOnly) {
        CursorConfig cursorConfig = new CursorConfig();
        cursorConfig.setReadUncommitted(true);
        Cursor cursor = db.openCursor(null, cursorConfig);
        try {
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            while (cursor.getNext(foundKey, foundData,
                    LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
                int index = IntegerBinding.entryToInt(foundKey);
                TupleInput ti = new TupleInput(foundData.getData());
                int j = 0;
                List<String> maxValueEntries = new ArrayList<String>();
                while (ti.available() > 0) {
                    nidCNidMaps.get()[index][j++] = ti.readInt();
                    if (nidCNidMaps.get()[index][j - 1] == Integer.MAX_VALUE) {
                        maxValueEntries.add("[" + index + "][" + (j - 1) + "]: " + ((index * NID_CNID_MAP_SIZE) + (j - 1) + Integer.MIN_VALUE));
                    }
                }
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    if (readOnly) {
                        AceLog.getAppLog().info("\n\nmax value entry count for read only index[" + index + "]: " + maxValueEntries.size());
                    } else {
                        AceLog.getAppLog().info("\n\nmax value entry count for mutable index[" + index + "]: " + maxValueEntries.size());
                        if (maxValueEntries.size() > 0 && index < (nidCNidMaps.get().length - 1)) {
                            AceLog.getAppLog().info("\n\n\nmax value entries: " + maxValueEntries);
                        }
                    }
                }
            }
        } finally {
            cursor.close();
        }
    }

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

    @Override
    public void sync() throws IOException {
        writeChangedMaps();
        super.sync();
    }

    @Override
    public void close() {
        try {
            sync();
        } catch (IOException e) {
            AceLog.getAppLog().severe(e.getLocalizedMessage(), e);
        }
        super.close();
    }

    private void writeChangedMaps() throws IOException {
        rwl.writeLock().lock();
        try {
            DatabaseEntry keyEntry = new DatabaseEntry();
            TupleOutput output = new TupleOutput(new byte[NID_CNID_MAP_SIZE * 4]);
            DatabaseEntry valueEntry = new DatabaseEntry(output.toByteArray());

            for (int key = 0; key < nidCNidMaps.get().length; key++) {
                if (mapChanged[key]) {
                    IntegerBinding.intToEntry(key, keyEntry);
                    output = new TupleOutput(new byte[NID_CNID_MAP_SIZE * 4]);
                    List<String> maxValueEntries = new ArrayList<String>();
                    for (int i = 0; i < NID_CNID_MAP_SIZE; i++) {
                        output.writeInt(nidCNidMaps.get()[key][i]);
                        if (nidCNidMaps.get()[key][i] == Integer.MAX_VALUE) {
                            if (i > 0) {
                                if (nidCNidMaps.get()[key][i - 1] != Integer.MAX_VALUE) {
                                    maxValueEntries.add("\n[" + key + "][" + (i - 1) + "]: "
                                            + nidCNidMaps.get()[key][i - 1] + "\n");
                                }
                                maxValueEntries.add("[" + key + "][" + i + "]");
                                if (i + 1 < NID_CNID_MAP_SIZE && nidCNidMaps.get()[key][i + 1] != Integer.MAX_VALUE) {
                                    maxValueEntries.add("\n[" + key + "][" + (i + 1) + "]: "
                                            + nidCNidMaps.get()[key][i + 1] + "\n");
                                }
                            } else {
                                maxValueEntries.add("[" + key + "][" + i + "]");
                            }
                        }
                    }
                    if (maxValueEntries.size() > 0 && key < nidCNidMaps.get().length - 1) {
                        System.out.println("writing max value entries: " + maxValueEntries);
                    } else {
                        //System.out.println("max value entry count in last array: " + maxValueEntries.size());
                    }
                    valueEntry = new DatabaseEntry(output.toByteArray());
                    OperationStatus status = mutable.put(null, keyEntry, valueEntry);
                    if (status != OperationStatus.SUCCESS) {
                        throw new IOException("Unsuccessful operation: " + status);
                    }
                    mapChanged[key] = false;
                }
            }
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public int getCNid(int nid) {
        assert nid != Integer.MAX_VALUE;
        int mapIndex = (nid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        int indexInMap = (nid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;
        assert mapIndex >= 0 && indexInMap >= 0 : "mapIndex: " + mapIndex + " indexInMap: "
                + indexInMap + " nid: " + nid;
        if (mapIndex >= nidCNidMaps.get().length) {
            return Integer.MAX_VALUE;
        }
        return nidCNidMaps.get()[mapIndex][indexInMap];
    }

    public void setCNidForNid(int cNid, int nid) throws IOException {
        assert cNid != Integer.MAX_VALUE;
        int mapIndex = (nid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        assert mapIndex >= 0 : "cNid: " + cNid + " nid: " + nid + " mapIndex: " + mapIndex;
        int indexInMap = (nid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;
        assert indexInMap < NID_CNID_MAP_SIZE : "cNid: " + cNid + " nid: " + nid + " mapIndex: " + mapIndex
                + " indexInMap: " + indexInMap;
        assert cNid == nid || hasConcept(cNid): cNid + " is not a concept nid. nid: " + nid;

        ensureCapacity(nid);
        assert nidCNidMaps.get()[mapIndex][indexInMap] == Integer.MAX_VALUE
                || nidCNidMaps.get()[mapIndex][indexInMap] == cNid : "processing cNid: " + cNid
                + " nid: " + nid + " found existing cNid: " + nidCNidMaps.get()[mapIndex][indexInMap]
                + "\n    " + cNid + " maps to: " + getCNid(cNid)
                + "\n    " + nidCNidMaps.get()[mapIndex][indexInMap]
                + " maps to: " + getCNid(nidCNidMaps.get()[mapIndex][indexInMap]);
        if (nidCNidMaps.get() != null && nidCNidMaps.get()[mapIndex] != null) {
            if (nidCNidMaps.get()[mapIndex][indexInMap] != cNid) {
                nidCNidMaps.get()[mapIndex][indexInMap] = cNid;
                mapChanged[mapIndex] = true;
            }
        } else {
            if (nidCNidMaps.get() == null) {
                throw new IOException("Null nidCidMap: ");
            }
            throw new IOException("nidCidMap[" + mapIndex + "] "
                    + "is null. cNid: " + cNid + " nid: " + nid);
        }
    }

    public void resetCidForNid(int cNid, int nid) throws IOException {
        assert cNid != Integer.MAX_VALUE;
        int mapIndex = (nid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        assert mapIndex >= 0 : "cNid: " + cNid + " nid: " + nid + " mapIndex: " + mapIndex;
        int indexInMap = (nid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;
        assert indexInMap < NID_CNID_MAP_SIZE : "cNid: " + cNid + " nid: " + nid + " mapIndex: " + mapIndex
                + " indexInMap: " + indexInMap;

        ensureCapacity(nid);
        if (nidCNidMaps.get() != null && nidCNidMaps.get()[mapIndex] != null) {
            if (nidCNidMaps.get()[mapIndex][indexInMap] != cNid) {
                nidCNidMaps.get()[mapIndex][indexInMap] = cNid;
                mapChanged[mapIndex] = true;
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
        int nidCidMapCount = ((nextId - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE) + 1;
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
            newMapChanged[i] = mapChanged[i];
        }
        for (int i = oldCount; i < nidCidMapCount; i++) {
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine("Expanding NidCidMaps to: " + (i + 1));
            }
            newNidCidMaps[i] = new int[NID_CNID_MAP_SIZE];
            newMapChanged[i] = true;
            Arrays.fill(newNidCidMaps[i], Integer.MAX_VALUE);
        }
        nidCNidMaps.set(newNidCidMaps);
        mapChanged = newMapChanged;
    }

    @Override
    protected String getDbName() {
        return "NidCidMap";
    }

    public boolean hasConcept(int cNid) {
        assert cNid > Integer.MIN_VALUE : "Invalid cNid == Integer.MIN_VALUE: " + cNid;
        assert cNid <= Bdb.getUuidsToNidMap().getCurrentMaxNid() : "Invalid cNid: " + cNid + " currentMax: " + Bdb.getUuidsToNidMap().getCurrentMaxNid();
        int mapIndex = (cNid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        int indexInMap = (cNid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;
        assert mapIndex >= 0 && mapIndex < nidCNidMaps.get().length
                && indexInMap >= 0 && indexInMap < NID_CNID_MAP_SIZE : "mapIndex: " + mapIndex + " indexInMap: "
                + indexInMap + " nid: " + cNid + " number of maps: " + nidCNidMaps.get().length + " mapSize: " + NID_CNID_MAP_SIZE;
        if (nidCNidMaps.get()[mapIndex][indexInMap] == cNid) {
            return true;
        }
        return false;
    }

    public boolean hasMap(int nid) {
        int mapIndex = (nid - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        int indexInMap = (nid - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;
        if (mapIndex < nidCNidMaps.get().length && indexInMap < NID_CNID_MAP_SIZE) {
            if (nidCNidMaps.get()[mapIndex][indexInMap] < Integer.MAX_VALUE) {
                return true;
            }
        }
        return false;
    }
}
