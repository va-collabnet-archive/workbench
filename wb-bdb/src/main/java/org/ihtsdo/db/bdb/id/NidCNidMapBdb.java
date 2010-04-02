package org.ihtsdo.db.bdb.id;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;

import cern.colt.map.OpenIntIntHashMap;

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
	private ReentrantLock writeLock = new ReentrantLock();

	
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
        nidCNidMaps = new AtomicReference<int[][]>(new int[nidCidMapCount][]) ;
        mapChanged = new boolean[nidCidMapCount];
        Arrays.fill(mapChanged, false);
        for (int index = 0; index < nidCidMapCount; index++) {
        	nidCNidMaps.get()[index] = new int[NID_CNID_MAP_SIZE];
        	Arrays.fill(nidCNidMaps.get()[index], Integer.MAX_VALUE);
        }
        maxId = (nidCNidMaps.get().length *  NID_CNID_MAP_SIZE) - Integer.MIN_VALUE; 
		
		readMaps(readOnly);
		readMaps(mutable);
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			printKeys("Read only keys: ", readOnly);
			printKeys("Mutable keys: " , mutable);
		}
	}

	private void readMaps(Database db) {
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
					if (nidCNidMaps.get()[index][j-1] == Integer.MAX_VALUE) {
						maxValueEntries.add("[" + index + "][" + (j-1) + "]");
					}
				}
				if (maxValueEntries.size() > 0 && index < nidCNidMaps.get().length - 1) {
					System.out.println("max value entries: " + maxValueEntries);
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
		writeLock.lock();
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
							maxValueEntries.add("[" + key + "][" + i + "]");
						}
					}
					if (maxValueEntries.size() > 0 && key < nidCNidMaps.get().length - 1) {
						System.out.println("writing max value entries: " + maxValueEntries);
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
			writeLock.unlock();
		}
	}

	public int getCNid(int nid) {
		int mapIndex = (nid  - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
		int indexInMap = (nid  - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;
		assert mapIndex >= 0 && indexInMap >= 0: "mapIndex: " + mapIndex + " indexInMap: " + 
				indexInMap + " nid: " + nid;
		if (mapIndex > nidCNidMaps.get().length) {
		    return Integer.MAX_VALUE;
		}
		return nidCNidMaps.get()[mapIndex][indexInMap];
	}
	
	public void setCidForNid(int cNid, int nid) throws IOException {
        assert cNid != Integer.MAX_VALUE;
        int mapIndex = (nid  - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        assert mapIndex >= 0: "cNid: " + cNid + " nid: " + nid + " mapIndex: " + mapIndex;
        int indexInMap = (nid  - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;
        assert indexInMap < NID_CNID_MAP_SIZE: "cNid: " + cNid + " nid: " + nid + " mapIndex: " + mapIndex
            + " indexInMap: " + indexInMap;
        
		ensureCapacity(nid);
		assert nidCNidMaps.get()[mapIndex][indexInMap] == Integer.MAX_VALUE ||
			nidCNidMaps.get()[mapIndex][indexInMap] == cNid: "processing cNid: " + cNid + 
					" nid: " + nid + " found: " + nidCNidMaps.get()[mapIndex][indexInMap];
		if (nidCNidMaps.get() != null && nidCNidMaps.get()[mapIndex] != null) {
	        if (nidCNidMaps.get()[mapIndex][indexInMap] != cNid) {
	            nidCNidMaps.get()[mapIndex][indexInMap] = cNid;
	            mapChanged[mapIndex] = true;
	        }
		} else {
		    if (nidCNidMaps.get() == null) {
	            throw new IOException("Null nidCidMap: ");
		    }
            throw new IOException("nidCidMap[" + mapIndex + "] " + 
                "is null. cNid: " + cNid + " nid: " + nid);
		}
	}

	   
    private void ensureCapacity(int nextId) throws IOException {
        int nidCidMapCount = ((nextId - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE) + 1;
        if (nidCidMapCount > nidCNidMaps.get().length) {
            writeLock.lock();
            if (nidCidMapCount > nidCNidMaps.get().length) {
                try {
                    expandCapacity(nidCidMapCount);
                } finally {
                    writeLock.unlock();
                }
            } else {
                writeLock.unlock();
            }
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
	    assert cNid > Integer.MIN_VALUE: "Invalid cNid: " + cNid;
	    assert cNid <= Bdb.getUuidsToNidMap().getCurrentMaxNid(): "Invalid cNid: " + cNid + " currentMax: " + Bdb.getUuidsToNidMap().getCurrentMaxNid();
		int mapIndex = (cNid  - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
		int indexInMap = (cNid  - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;
        assert mapIndex >= 0 && mapIndex < nidCNidMaps.get().length 
            && indexInMap >= 0 && indexInMap < NID_CNID_MAP_SIZE: "mapIndex: " + mapIndex + " indexInMap: " + 
                indexInMap + " nid: " + cNid + " number of maps: " + nidCNidMaps.get().length + " mapSize: " + NID_CNID_MAP_SIZE;
		if (nidCNidMaps.get()[mapIndex][indexInMap] == cNid) {
			return true;
		}
		return false;
	}

    public boolean hasMap(int nid) {
        int mapIndex = (nid  - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
        int indexInMap = (nid  - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;
        if (indexInMap < nidCNidMaps.get().length) {
            if (nidCNidMaps.get()[mapIndex][indexInMap] < Integer.MAX_VALUE) {
                return true;
            }
        }
        return false;
    }



}
