package org.ihtsdo.db.bdb;

import java.io.IOException;
import java.util.Arrays;

import org.dwfa.ace.log.AceLog;

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
	private int maxId;
	private int readOnlyRecords;
	
	
	public NidCNidMapBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
		super(readOnlyBdbEnv, mutableBdbEnv);
	}

	@Override
	protected void init() throws IOException {
		maxId = Bdb.getUuidsToNidMap().getCurrentMaxNid();
		readOnlyRecords = (int) readOnly.count();
		int mutableRecords = (int) mutable.count();
        int nidCidMapCount = ((maxId - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE) + 1;
        nidCNidMaps = new int[nidCidMapCount][];
        for (int index = 0; index < nidCidMapCount; index++) {
        	nidCNidMaps[index] = new int[NID_CNID_MAP_SIZE];
        	Arrays.fill(nidCNidMaps[index], Integer.MAX_VALUE);
        }
        maxId = (nidCNidMaps.length *  NID_CNID_MAP_SIZE) - Integer.MIN_VALUE; 
        
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry valueEntry = new DatabaseEntry();
		
		for (int i = 0; i < readOnlyRecords; i++) {
			IntegerBinding.intToEntry(i, keyEntry);
			OperationStatus status = readOnly.get(null, keyEntry, valueEntry, LockMode.READ_UNCOMMITTED);
			if (status == OperationStatus.SUCCESS) {
				TupleInput ti = new TupleInput(valueEntry.getData());
				int j = 0;
				while (ti.available() > 0) {
					nidCNidMaps[i][j++] = ti.readInt();
				}
				
			} else {
				throw new IOException("Unsuccessful operation: " + status);
			}
		}
		
		for (int i = 0; i < mutableRecords; i++) {
			IntegerBinding.intToEntry(i, keyEntry);
			OperationStatus status = mutable.get(null, keyEntry, valueEntry, LockMode.READ_UNCOMMITTED);
			if (status == OperationStatus.SUCCESS) {
				TupleInput ti = new TupleInput(valueEntry.getData());
				int j = 0;
				while (ti.available() > 0) {
					nidCNidMaps[i + readOnlyRecords][j++] = ti.readInt();
				}
			} else {
				throw new IOException("Unsuccessful operation: " + status);
			}
		}
		
	}

	public void ensureCapacity(int nextId) throws IOException {
        int nidCidMapCount = ((nextId - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE) + 1;
        if (nidCidMapCount > nidCNidMaps.length) {
        	// Write the last map to the database
    		writeLastMap();
            int[][] newNidCidMaps = new int[nidCidMapCount][];
        	for (int i = 0; i < nidCNidMaps.length; i++) {
        		newNidCidMaps[i] = nidCNidMaps[i];
        	}
        	newNidCidMaps[nidCNidMaps.length] = new int[NID_CNID_MAP_SIZE];
        	Arrays.fill(newNidCidMaps[nidCNidMaps.length], Integer.MAX_VALUE);
        	nidCNidMaps = newNidCidMaps;
		}

        maxId = (nidCNidMaps.length *  NID_CNID_MAP_SIZE) - Integer.MIN_VALUE; 
	}

	@Override
	public void sync() throws IOException {
		writeLastMap();
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

	private void writeLastMap() throws IOException {
		DatabaseEntry keyEntry = new DatabaseEntry();

		IntegerBinding.intToEntry((nidCNidMaps.length - 1) - readOnlyRecords, keyEntry);
		TupleOutput output = new TupleOutput(new byte[NID_CNID_MAP_SIZE * 4]);
		for (int i = 0; i < NID_CNID_MAP_SIZE; i++) {
			output.writeInt(nidCNidMaps[nidCNidMaps.length - 1][i]);
		}
		DatabaseEntry valueEntry = new DatabaseEntry(output.toByteArray());
		OperationStatus status = mutable.put(null, keyEntry, valueEntry);
		if (status != OperationStatus.SUCCESS) {
			throw new IOException("Unsuccessful operation: " + status);
		}
	}

	public int getCNid(int nid) {
		if (maxId < nid) {
			throw new ArrayIndexOutOfBoundsException(" maxId: " + maxId + " nid: " + nid);
		}
		int mapIndex = (nid  - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
		int indexInMap = (nid  - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;
		return nidCNidMaps[mapIndex][indexInMap];
	}
	
	public void setCidForNid(int cNid, int nid) {
		if (maxId < cNid || maxId < nid) {
			throw new ArrayIndexOutOfBoundsException(" maxId: " + maxId + " cNid: " + cNid + " nid: " + nid);
		}
		int mapIndex = (nid  - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
		int indexInMap = (nid  - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;
		if (mapIndex < 0 || indexInMap < 0) {
			throw new ArrayIndexOutOfBoundsException(" maxId: " + maxId + " cNid: " + cNid + " nid: " + nid  + 
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
