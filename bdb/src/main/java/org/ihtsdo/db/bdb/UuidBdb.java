package org.ihtsdo.db.bdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.db.uuidmap.UuidArrayList;
import org.ihtsdo.db.uuidmap.UuidUtil;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class UuidBdb extends ComponentBdb {
	
	private static final int UUID_MAP_SIZE = 100000;
	private List<UuidArrayList> uuidMaps;
	private int readOnlyRecords;
	
	
	public UuidBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
		super(readOnlyBdbEnv, mutableBdbEnv);
	}

	@Override
	protected void init() throws IOException {
		readOnlyRecords = (int) readOnly.count();
		int mutableRecords = (int) mutable.count();
        int mapCount = Math.max(1, readOnlyRecords + mutableRecords);
        uuidMaps = new ArrayList<UuidArrayList>(mapCount);
        for (int index = 0; index < mapCount; index++) {
        	uuidMaps.add(new UuidArrayList(UUID_MAP_SIZE));
        }
        
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry valueEntry = new DatabaseEntry();
		
		for (int i = 0; i < readOnlyRecords; i++) {
			//AceLog.getAppLog().info(" reading read-only list: " + i + " as globalList: " + i);
			IntegerBinding.intToEntry(i, keyEntry);
			UuidArrayList uuidList = uuidMaps.get(i);
			OperationStatus status = readOnly.get(null, keyEntry, valueEntry, LockMode.READ_UNCOMMITTED);
			long[] uuidArray = new long[2];
			if (status == OperationStatus.SUCCESS) {
				TupleInput ti = new TupleInput(valueEntry.getData());
				int j = 0;
				while (ti.available() > 0) {
					uuidArray[0] = ti.readLong();
					uuidArray[1] = ti.readLong();
					if (j < uuidList.size()) {
						uuidList.set(j, uuidArray);
					} else {
						uuidList.add(uuidArray);
					}
					j++;
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
			if (index >= 0) {
				UuidArrayList uuidList = uuidMaps.get(index);
				AceLog.getAppLog().info(" reading uuid bdb mutable list: " + 
						i + " as globalList: " + index);
				OperationStatus status = mutable.get(null, keyEntry, valueEntry, 
						LockMode.READ_UNCOMMITTED);
				if (status == OperationStatus.SUCCESS) {
					TupleInput ti = new TupleInput(valueEntry.getData());
					long[] uuidArray = new long[2];
					int j = 0;
					while (ti.available() > 0) {
						uuidArray[0] = ti.readLong();
						uuidArray[1] = ti.readLong();
						if (j < uuidList.size()) {
							assert uuidList.get(j)[0] == uuidArray[0] && 
								uuidList.get(j)[1] == uuidArray[1];
						} else {
							uuidList.add(uuidArray);
						}
						j++;
					}
				} else {
					throw new IOException("Unsuccessful operation: " + status);
				}
			}
		}
		
	}

	public void ensureCapacity(int nextId) throws IOException {
        int mapCount = (nextId / UUID_MAP_SIZE) + 1;
        if (mapCount > uuidMaps.size()) {
        	// Write the last map to the database
    		writeLastList();
    		uuidMaps.add(new UuidArrayList(UUID_MAP_SIZE));
		}
	}

	@Override
	public void sync() throws IOException {
		writeLastList();
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

	private void writeLastList() throws IOException {
		DatabaseEntry keyEntry = new DatabaseEntry();
		
		int key = uuidMaps.size() - 1;
		if (readOnlyRecords > 0) {
			// account for last list to be present in 
			// both the readOnly and mutable database
			key = key - (readOnlyRecords - 1);
		}
		
		UuidArrayList listToWrite = uuidMaps.get(uuidMaps.size() - 1);
		
		IntegerBinding.intToEntry(key, keyEntry);
		AceLog.getAppLog().info("Writing uuid list mutable index " + key + " globalIndex: " + 
				(uuidMaps.size() - 1) + " size: " + ((uuidMaps.size() - 2) * UUID_MAP_SIZE + 
				uuidMaps.get(uuidMaps.size() - 1).size()));
		TupleOutput output = new TupleOutput(new byte[(UUID_MAP_SIZE + 1) * 16]);
		long[] uuidArray;
		for (int i = 0; i < UUID_MAP_SIZE; i++) {
			if (i < listToWrite.size()) {
				uuidArray = uuidMaps.get(uuidMaps.size() - 1).get(i);
				output.writeLong(uuidArray[0]);
				output.writeLong(uuidArray[1]);
			} else {
				break;
			}
		}
		DatabaseEntry valueEntry = new DatabaseEntry(output.toByteArray());
		OperationStatus status = mutable.put(null, keyEntry, valueEntry);
		if (status != OperationStatus.SUCCESS) {
			throw new IOException("Unsuccessful operation: " + status);
		}
	}
	
	public synchronized int addUuid(UUID uuid) throws IOException{
		boolean details = false;
		int mapIndex = uuidMaps.size() - 1;
		int uNid = UUID_MAP_SIZE * (mapIndex) + (uuidMaps.get(mapIndex).size());
		if (details) {
			System.out.println(" uNid: " + uNid + " for: " + uuid);
		}
		assert uuid.equals(getUuid(uNid -1)) == false: " mapIndex: " + mapIndex
					+ " uNid: " + uNid;
		ensureCapacity(uNid);
		mapIndex = uuidMaps.size() - 1;
		uuidMaps.get(mapIndex).add(uuid);
		return uNid;
	}

	public long[] getUuidAsLongArray(int nid) {
		int mapIndex = nid / UUID_MAP_SIZE;
		int indexInMap = nid % UUID_MAP_SIZE;
		return uuidMaps.get(mapIndex).get(indexInMap);
	}

	public UUID getUuid(int nid) {
		if (nid < 0) {
			return new UUID(0,0);
		}
		int mapIndex = nid / UUID_MAP_SIZE;
		int indexInMap = nid % UUID_MAP_SIZE;
		return UuidUtil.convert(uuidMaps.get(mapIndex).get(indexInMap));
	}

	@Override
	protected String getDbName() {
		return "UuidDb";
	}

	public boolean searchForUuid(UUID next) {
		long[] data = new long[2];
		data[0] = next.getMostSignificantBits();
		data[1] = next.getLeastSignificantBits();
		for (UuidArrayList list: uuidMaps) {
			if (list.contains(data)) {
				return true;
			}
		}
		
		return false;
	}
}
