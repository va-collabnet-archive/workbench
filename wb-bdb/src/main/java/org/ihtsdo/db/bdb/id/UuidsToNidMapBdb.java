package org.ihtsdo.db.bdb.id;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.PrimordialId;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;
import org.ihtsdo.db.uuidmap.IntUuidProxyIntProcedure;
import org.ihtsdo.db.uuidmap.IntUuidProxyToNidMap;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class UuidsToNidMapBdb extends ComponentBdb {
	private class IdSequence {
		
		private AtomicInteger sequence;
		public IdSequence(int max) {
			super();
			sequence = new AtomicInteger(max + 1);
		}

		public final int getAndIncrement() {
			int next = sequence.getAndIncrement();
			return next;
		}

		public int get() {
			return sequence.get();
		}

		public void set(int next) {
			sequence.set(next);
		}
	}
	
	private static final String UUIDS_TO_NID_MAP_SIZE = "UuidsToNidMap.size";
	private static final int INCREMENT_SIZE = 100000;

	private IntUuidProxyToNidMap readOnlyUuidsToNidMap;
	private IntUuidProxyToNidMap mutableUuidsToNidMap;
	private IdSequence sequence;

	
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();


	public UuidsToNidMapBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
		super(readOnlyBdbEnv, mutableBdbEnv);
	}


	@Override
	protected void init() throws IOException {
		String mapSizeString = Bdb.getProperty(UUIDS_TO_NID_MAP_SIZE);
		int mapSize = 20;
		if (mapSizeString != null) {
			mapSize = Integer.parseInt(mapSizeString);
		}
		readOnlyUuidsToNidMap = new IntUuidProxyToNidMap(mapSize + INCREMENT_SIZE, .2, .7);
		mutableUuidsToNidMap = new IntUuidProxyToNidMap(INCREMENT_SIZE, .2, .7);
		if (sequence == null) {
			int max = Integer.MIN_VALUE;
	        sequence = new IdSequence(max);
		}
		putDataInMap(readOnly, false);
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("Read-only IdSequence value: " + sequence.get() + " (" + (sequence.get() - Integer.MAX_VALUE) + ")");
			AceLog.getAppLog().fine("Read-only Ids in map: " + readOnlyUuidsToNidMap.size());
		}
		putDataInMap(mutable, true);
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("All IdSequence value: " + sequence.get() + " (" + (sequence.get() - Integer.MAX_VALUE) + ")");
			AceLog.getAppLog().fine("All Ids in map: " + readOnlyUuidsToNidMap.size());
		}
	}


	/**
	 * This method keeps all the "full" database entries 
	 * in the read only hash map, and
	 * puts only the last entry in the mutable database into the 
	 * mutable hash map. This strategy minimizes the memory that 
	 * would otherwise be required to keep track of what entries are
	 * new and need to be written to the database. 
	 * @param db
	 * @param mutable
	 * @throws IOException
	 */
	private void putDataInMap(Database db, boolean mutable)
			throws IOException {
		IntUuidProxyToNidMap mapToAddTo = readOnlyUuidsToNidMap;
		int records = (int) db.count();
		if (records == 0) {
			if (mutable) {
				mapToAddTo = mutableUuidsToNidMap;
			}
	        for (PrimordialId pid : PrimordialId.values()) {
	            for (UUID uid : pid.getUids()) {
	            	if (!mapToAddTo.containsKey(uid)) {
	            		if (readOnlyUuidsToNidMap.containsKey(uid)) {
	            			int uNid = readOnlyUuidsToNidMap.getUNid(uid);
	            			mapToAddTo.put(uNid, 
	    	            			pid.getNativeId(Integer.MIN_VALUE));
	            		} else {
	    	            	mapToAddTo.put(uid, pid.getNativeId(Integer.MIN_VALUE));
	            		}
	            	}
	             	int max = Math.max(sequence.get(), pid.getNativeId(Integer.MIN_VALUE) + 1);
	             	if (max > sequence.get()) {
	             		sequence.set(max);
	             	}
	            }
	        }
		}
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		int max = Integer.MIN_VALUE;
		Cursor c = db.openCursor(null, null);
		try {
		while (c.getNext(key, value, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
			TupleInput ti = new TupleInput(value.getData());
			while (ti.available() > 0) {
				int uNid = ti.readInt();
				int nid = ti.readInt();
				max = Math.max(nid, max);
				readOnlyUuidsToNidMap.put(uNid, nid);
			}
		}
		} finally {
			c.close();
		}
		if (sequence.get() <= max) {
			sequence.set(max + 1);
		}
	}
	
	private class WriteToBdb implements IntUuidProxyIntProcedure {

		private int count = 0;
		TupleOutput tos;
		
		public WriteToBdb() {
			super();
			tos = new TupleOutput();
		}

		@Override
		public boolean apply(int uNid, int value) {
			tos.writeInt(uNid);
			tos.writeInt(value);
			count++;
			return true;
		}
		
		public void close() throws IOException {
			if (count != 0) {
				int key = (int) mutable.count();
				DatabaseEntry keyEntry = new DatabaseEntry();
				IntegerBinding.intToEntry(key, keyEntry);
				DatabaseEntry valueEntry = new DatabaseEntry();
				valueEntry.setData(tos.toByteArray());
				mutable.put(null, keyEntry, valueEntry);
				int hashMapSize = readOnlyUuidsToNidMap.size();
				Bdb.setProperty(UUIDS_TO_NID_MAP_SIZE, Integer.toString(hashMapSize));
			}
		}		
	}

	private class AddToReadOnlyMap implements IntUuidProxyIntProcedure {
		WriteToBdb writer = new WriteToBdb();
		
		@Override
		public boolean apply(int uNid, int value) {
			readOnlyUuidsToNidMap.put(uNid, value);
			writer.apply(uNid, value);
			return true;
		}

		@Override
		public void close() throws IOException {
			writer.close();
			mutableUuidsToNidMap.clear();
		}
		
	}

	@Override
	public void sync() throws IOException {
		w.lock();
		AddToReadOnlyMap writer = new AddToReadOnlyMap();
		mutableUuidsToNidMap.forEachPair(writer);
		writer.close();
		w.unlock();
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

	public boolean hasUuid(UUID uuid) {
        r.lock();
        if (readOnlyUuidsToNidMap.containsKey(uuid)) {
            r.unlock();
            return true;
        }
        if (mutableUuidsToNidMap.containsKey(uuid)) {
            r.unlock();
            return true;
        }
        r.unlock();
	    return false;
	}
	public int uuidToNid(UUID uuid)  {
		r.lock();
		if (readOnlyUuidsToNidMap.containsKey(uuid)) {
			int nid = readOnlyUuidsToNidMap.get(uuid);
			r.unlock();
			return nid;
		}
		if (mutableUuidsToNidMap.containsKey(uuid)) {
			int nid = mutableUuidsToNidMap.get(uuid);
			r.unlock();
			return nid;
		}
		r.unlock();
		// get lock here...
		w.lock();
		if (readOnlyUuidsToNidMap.containsKey(uuid)) {
			int nid = readOnlyUuidsToNidMap.get(uuid);
			w.unlock();
			return nid;
		}
		if (mutableUuidsToNidMap.containsKey(uuid)) {
			int nid = mutableUuidsToNidMap.get(uuid);
			w.unlock();
			return nid;
		}
		int newNid = sequence.getAndIncrement();
		mutableUuidsToNidMap.put(uuid, newNid);
		w.unlock();
		return newNid;
	}

	public int uuidsToNid(UUID[] uuids) {
		return uuidsToNid(Arrays.asList(uuids));
	}

	public int uuidsToNid(Collection<UUID> uuids) {
		for (UUID uuid : uuids) {
			if (readOnlyUuidsToNidMap.containsKey(uuid)) {
				return readOnlyUuidsToNidMap.get(uuid);
			}
		}
		for (UUID uuid : uuids) {
			if (mutableUuidsToNidMap.containsKey(uuid)) {
				int nid = mutableUuidsToNidMap.get(uuid);
				return nid;
			}
		}
		w.lock();
		for (UUID uuid : uuids) {
			if (mutableUuidsToNidMap.containsKey(uuid)) {
				int nid = mutableUuidsToNidMap.get(uuid);
				w.unlock();
				return nid;
			}
		}
		/*
		assert Bdb.getUuidDb().searchForUuid(uuids.iterator().next()) 
			== false: " Attempt to add duplicate uuid: " + uuids;
			*/
		int newNid = sequence.getAndIncrement();
		if (newNid > -2144319243) {
		    System.out.println("newNid: " + newNid);
		}
		for (UUID uuid : uuids) {
			mutableUuidsToNidMap.put(uuid, newNid);
		}
		w.unlock();
		return newNid;
	}

	@Override
	protected String getDbName() {
		return "UuidsToNidMap";
	}


	public int getCurrentMaxNid() {
		return sequence.get() - 1;
	}

	public int getUNid(UUID primordialComponentUuid) {
		if (readOnlyUuidsToNidMap.containsKey(primordialComponentUuid)) {
			return readOnlyUuidsToNidMap.getUNid(primordialComponentUuid);
		}
		mutableUuidsToNidMap.get(primordialComponentUuid);
		return mutableUuidsToNidMap.getUNid(primordialComponentUuid);
	}


	public void put(UUID denotation, int nid) {
		w.lock();
		mutableUuidsToNidMap.put(denotation, nid); 
		w.unlock();
	}


	public List<UUID> getUuidsForNid(int nid) {
		List<UUID> list = readOnlyUuidsToNidMap.keysOf(nid);
		list.addAll(mutableUuidsToNidMap.keysOf(nid));
		return list;
	}
}
