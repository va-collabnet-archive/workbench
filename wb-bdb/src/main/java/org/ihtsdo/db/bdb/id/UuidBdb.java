package org.ihtsdo.db.bdb.id;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;
import org.ihtsdo.db.uuidmap.UuidArrayList;
import org.ihtsdo.db.uuidmap.UuidUtil;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class UuidBdb extends ComponentBdb {
	
	private static final int UUID_MAP_SIZE = 100000;
	private List<UuidArrayList> uuidMaps;
	private int readOnlyRecords;
	private Lock writeLock = new ReentrantLock();
	
	
	public UuidBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
		super(readOnlyBdbEnv, mutableBdbEnv);
	}

	@Override
	protected void init() throws IOException {
		readOnlyRecords = (int) readOnly.count();
		int mutableRecords = (int) mutable.count();
        uuidMaps = new ArrayList<UuidArrayList>(Math.max(1, readOnlyRecords + mutableRecords));
        		
		readUuids(readOnly);
		readUuids(mutable);
		if (uuidMaps.size() == 0) {
        	uuidMaps.add(new UuidArrayList(UUID_MAP_SIZE));
		}
	}

	private void readUuids(Database db) {
		CursorConfig cursorConfig = new CursorConfig();
		cursorConfig.setReadUncommitted(true);
		Cursor cursor = db.openCursor(null, cursorConfig);
		try {
			DatabaseEntry foundKey = new DatabaseEntry();
			DatabaseEntry foundData = new DatabaseEntry();
			while (cursor.getNext(foundKey, foundData,
					LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				int index = IntegerBinding.entryToInt(foundKey);
				UuidArrayList newList = new UuidArrayList(UUID_MAP_SIZE);
				if (index == uuidMaps.size()) {
		        	uuidMaps.add(newList);
				} else if (index < uuidMaps.size()) {
					uuidMaps.set(index, newList);
				} else {
					throw new RuntimeException("Data out of order. Encountered index: " + 
							index + " list size: " + uuidMaps.size());
				}

				TupleInput ti = new TupleInput(foundData.getData());
				int j = 0;
				long[] uuidArray = new long[2];
				while (ti.available() > 0) {
					uuidArray[0] = ti.readLong();
					uuidArray[1] = ti.readLong();
					if (j < newList.size()) {
						assert newList.get(j)[0] == uuidArray[0] && 
						newList.get(j)[1] == uuidArray[1];
					} else {
						newList.add(uuidArray);
					}
					j++;
				}
				if (AceLog.getAppLog().isLoggable(Level.FINE)) {
					AceLog.getAppLog().fine("Populated UUID list: " + index + " size: " + newList.size() + " capacity: " +
							newList.getCapacity());
				}
			}
		} finally {
			cursor.close();
		}
	}

	public void ensureCapacity(int nextId) throws IOException {
        int mapCount = (nextId / UUID_MAP_SIZE) + 1;
        if (mapCount > uuidMaps.size()) {
        	writeLock.lock();
            if (mapCount > uuidMaps.size()) {
            	// Write the last map to the database
        		writeLastList();
        		uuidMaps.add(new UuidArrayList(UUID_MAP_SIZE));
    		}
    		writeLock.unlock();
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
		
		UuidArrayList listToWrite = uuidMaps.get(key);
		
		IntegerBinding.intToEntry(key, keyEntry);
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("Writing uuid list mutable index " + key + " globalIndex: " + 
					(uuidMaps.size() - 1) + " size: " + ((uuidMaps.size() - 2) * UUID_MAP_SIZE + 
					uuidMaps.get(uuidMaps.size() - 1).size()));
		}
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
	
	public int addUuid(UUID uuid) throws IOException{
		int mapIndex = uuidMaps.size() - 1;
		int uNid = UUID_MAP_SIZE * (mapIndex) + (uuidMaps.get(mapIndex).size());
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
		return UuidUtil.convert(uuidMaps.get(mapIndex).get(indexInMap, nid));
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
