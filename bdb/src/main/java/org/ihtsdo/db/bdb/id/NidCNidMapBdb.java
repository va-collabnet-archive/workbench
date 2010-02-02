package org.ihtsdo.db.bdb.id;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
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
	private int[][] nidCNidMaps;
	private boolean[] mapChanged;
	private int readOnlyRecords;
	ReentrantLock writeLock = new ReentrantLock();

	
	public NidCNidMapBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
		super(readOnlyBdbEnv, mutableBdbEnv);
	}

	@Override
	protected void init() throws IOException {
		int maxId = Bdb.getUuidsToNidMap().getCurrentMaxNid();
		readOnlyRecords = (int) readOnly.count();
		int mutableRecords = (int) mutable.count();
        int nidCidMapCount = ((maxId - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE) + 1;
        nidCNidMaps = new int[nidCidMapCount][];
        mapChanged = new boolean[nidCidMapCount];
        Arrays.fill(mapChanged, false);
        for (int index = 0; index < nidCidMapCount; index++) {
        	nidCNidMaps[index] = new int[NID_CNID_MAP_SIZE];
        	Arrays.fill(nidCNidMaps[index], Integer.MAX_VALUE);
        }
        maxId = (nidCNidMaps.length *  NID_CNID_MAP_SIZE) - Integer.MIN_VALUE; 
        
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry valueEntry = new DatabaseEntry();
		
		for (int i = 0; i < readOnlyRecords; i++) {
			IntegerBinding.intToEntry(i, keyEntry);
			OperationStatus status = readOnly.get(null, keyEntry, 
					valueEntry, LockMode.READ_UNCOMMITTED);
			if (status == OperationStatus.SUCCESS) {
				List<String> maxValueEntries = new ArrayList<String>();
				TupleInput ti = new TupleInput(valueEntry.getData());
				int j = 0;
				while (ti.available() > 0) {
					nidCNidMaps[i][j++] = ti.readInt();
					if (nidCNidMaps[i][j-1] == Integer.MAX_VALUE) {
						maxValueEntries.add("[" + i + "][" + (j-1) + "]");
					}
				}
				if (maxValueEntries.size() > 0 && i < nidCNidMaps.length - 1) {
					System.out.println("read-only max value entries: " + maxValueEntries);
				}
			} else {
				throw new IOException("Unsuccessful operation: " + status);
			}
		}
		
		for (int i = 0; i < mutableRecords; i++) {
			IntegerBinding.intToEntry(i, keyEntry);
			int index = i;
			if (readOnlyRecords > 0) {
				index = (i + readOnlyRecords - 1);
			}
			AceLog.getAppLog().info(" reading nid cNid mutable list: " + i + 
					" as globalList: " + index);
			OperationStatus status = mutable.get(null, keyEntry, valueEntry, 
					LockMode.READ_UNCOMMITTED);
			if (status == OperationStatus.SUCCESS) {
				TupleInput ti = new TupleInput(valueEntry.getData());
				int j = 0;
				List<String> maxValueEntries = new ArrayList<String>();
				while (ti.available() > 0) {
					nidCNidMaps[index][j++] = ti.readInt();
					if (nidCNidMaps[i][j-1] == Integer.MAX_VALUE) {
						maxValueEntries.add("[" + i + "][" + (j-1) + "]");
					}
				}
				if (maxValueEntries.size() > 0 && i < nidCNidMaps.length - 1) {
					System.out.println("mutable max value entries: " + maxValueEntries);
				}
			} else {
				throw new IOException("Unsuccessful operation: " + 
						status + " index: " + i +
						" mutable records: " + mutableRecords);
			}
		}
		
	}

	private void ensureCapacity(int nextId) throws IOException {
        int nidCidMapCount = ((nextId - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE) + 1;
        if (nidCidMapCount > nidCNidMaps.length) {
        	// Write the last map to the database
        	writeLock.lock();
        	if (nidCidMapCount > nidCNidMaps.length) {
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
		int[][] newNidCidMaps = new int[nidCidMapCount][];
		boolean[] newMapChanged = new boolean[nidCidMapCount];
		for (int i = 0; i < nidCNidMaps.length; i++) {
			newNidCidMaps[i] = nidCNidMaps[i];
			newMapChanged[i] = mapChanged[i];
		}
		newNidCidMaps[nidCNidMaps.length] = new int[NID_CNID_MAP_SIZE];
		newMapChanged[nidCNidMaps.length] = true;
		Arrays.fill(newNidCidMaps[nidCNidMaps.length], Integer.MAX_VALUE);
		nidCNidMaps = newNidCidMaps;
		mapChanged = newMapChanged;
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
			int key = nidCNidMaps.length - 1;
			if (readOnlyRecords > 0) {
				// account for last list to be present in 
				// both the readOnly and mutable database
				key = key - (readOnlyRecords - 1);
			}
			IntegerBinding.intToEntry(key, keyEntry);
			TupleOutput output = new TupleOutput(new byte[NID_CNID_MAP_SIZE * 4]);
			for (int i = 0; i < NID_CNID_MAP_SIZE; i++) {
				output.writeInt(nidCNidMaps[nidCNidMaps.length - 1][i]);
			}
			DatabaseEntry valueEntry = new DatabaseEntry(output.toByteArray());
			OperationStatus status = mutable.put(null, keyEntry, valueEntry);
			if (status != OperationStatus.SUCCESS) {
				throw new IOException("Unsuccessful operation: " + status);
			}
			if (readOnlyRecords > 0) {
				// account for last list to be present in 
				// both the readOnly and mutable database
				key = (readOnlyRecords - 1);
			} else {
				key = 0;
			}
			while (key < nidCNidMaps.length) {
				if (mapChanged[key]) {
					IntegerBinding.intToEntry(key, keyEntry);
					output = new TupleOutput(new byte[NID_CNID_MAP_SIZE * 4]);
					List<String> maxValueEntries = new ArrayList<String>();
					for (int i = 0; i < NID_CNID_MAP_SIZE; i++) {
						output.writeInt(nidCNidMaps[key][i]);
						if (nidCNidMaps[key][i] == Integer.MAX_VALUE) {
							maxValueEntries.add("[" + key + "][" + i + "]");
						}
					}
					if (maxValueEntries.size() > 0 && key < nidCNidMaps.length - 1) {
						System.out.println("writing max value entries: " + maxValueEntries);
					}
					valueEntry = new DatabaseEntry(output.toByteArray());
					status = mutable.put(null, keyEntry, valueEntry);
					if (status != OperationStatus.SUCCESS) {
						throw new IOException("Unsuccessful operation: " + status);
					}
					mapChanged[key] = false;
				}
				key++;
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
		return nidCNidMaps[mapIndex][indexInMap];
	}
	
	public void setCidForNid(int cNid, int nid) throws IOException {
		ensureCapacity(nid);
		assert cNid != Integer.MAX_VALUE;
		int mapIndex = (nid  - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
		mapChanged[mapIndex] = true;
		int indexInMap = (nid  - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;
		if (mapIndex < 0 || indexInMap < 0) {
			throw new ArrayIndexOutOfBoundsException(" cNid: " + cNid + " nid: " + nid  + 
					" mapIndex: " + mapIndex + " indexInMap: " + indexInMap);
		}
		assert nidCNidMaps[mapIndex][indexInMap] == Integer.MAX_VALUE: "processing cNid: " + cNid + 
					" nid: " + nid + " found: " + nidCNidMaps[mapIndex][indexInMap];
		nidCNidMaps[mapIndex][indexInMap] = cNid;
	}

	@Override
	protected String getDbName() {
		return "NidCidMap";
	}



}
