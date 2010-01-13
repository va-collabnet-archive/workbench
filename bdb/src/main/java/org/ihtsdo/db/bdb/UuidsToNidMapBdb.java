package org.ihtsdo.db.bdb;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.PrimordialId;
import org.ihtsdo.db.uuidmap.UuidIntProcedure;
import org.ihtsdo.db.uuidmap.UuidToIntHashMap;
import org.ihtsdo.db.uuidmap.UuidUtil;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
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
			try {
				Bdb.getNidCNidMap().ensureCapacity(next);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return next;
		}
	}

	private static final int RECORD_SIZE = 10000;
	private static final int UUID_INT_BYTES = 20;
	
	private UuidToIntHashMap readOnlyUuidsToNidMap = new UuidToIntHashMap(RECORD_SIZE);
	private UuidToIntHashMap mutableUuidsToNidMap = new UuidToIntHashMap(RECORD_SIZE);
	private IdSequence sequence;

	
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();


	protected UuidsToNidMapBdb(Bdb readOnlyBdbEnv, Bdb readWriteBdbEnv) throws IOException {
		super(readOnlyBdbEnv, readWriteBdbEnv);
		int max = Integer.MIN_VALUE;
        for (PrimordialId pid : PrimordialId.values()) {
            for (UUID uid : pid.getUids()) {
            	readOnlyUuidsToNidMap.put(UuidUtil.convert(uid), 
            			pid.getNativeId(Integer.MIN_VALUE));
            	mutableUuidsToNidMap.put(UuidUtil.convert(uid), 
            			pid.getNativeId(Integer.MIN_VALUE));
             	max = Math.max(max, pid.getNativeId(Integer.MIN_VALUE));
            }
        }
        sequence = new IdSequence(max);
	}


	@Override
	protected void init() throws IOException {
		int readOnlyRecords = (int) readOnly.count();
		int mutableRecords = (int) mutable.count();
		readOnlyUuidsToNidMap = new UuidToIntHashMap(readOnlyRecords + mutableRecords);
		mutableUuidsToNidMap = new UuidToIntHashMap(RECORD_SIZE);
		putDataInMap(readOnly, false);
		putDataInMap(mutable, true);
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
		int records = (int) db.count();
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		UuidToIntHashMap mapToAddTo = readOnlyUuidsToNidMap;
		for (int i = 0; i < records; i++) {
			if (mutable && (i == (records - 1))) {
				mapToAddTo = mutableUuidsToNidMap;
			}
			IntegerBinding.intToEntry(i, key);
			OperationStatus status = db.get(null, key, value, LockMode.READ_UNCOMMITTED);
			if (status != OperationStatus.SUCCESS) {
				throw new IOException("Operation failed with status: " + status);
			}
			TupleInput ti = new TupleInput(value.getData());
			long[] uuidData = new long[2];
			while (ti.available() > 0) {
				uuidData[0] = ti.readLong();
				uuidData[1] = ti.readLong();
				int nid = ti.readInt();
				mapToAddTo.put(uuidData, nid);
			}
		}
	}
	
	private class WriteToBdb implements UuidIntProcedure {

		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry valueEntry = new DatabaseEntry();
		private int keyIndex = 0;
		private int count = 0;
		private TupleOutput data;
		
		public WriteToBdb() {
			super();
			byte[] outputBytes = new byte[UUID_INT_BYTES * RECORD_SIZE + UUID_INT_BYTES];
			data = new TupleOutput(outputBytes);
			keyIndex = (int) mutable.count() - 1;
		}

		@Override
		public boolean apply(long[] uuid, int value) {
			data.writeLong(uuid[0]);
			data.writeLong(uuid[1]);
			data.writeInt(value);
			count++;
			if (count == RECORD_SIZE) {
				IntegerBinding.intToEntry(keyIndex, keyEntry);
				valueEntry.setData(data.toByteArray());
				mutable.put(null, keyEntry, valueEntry);
				count = 0;
				data.reset();
			} 
			return true;
		}
		
		public void close() {
			if (count != 0) {
				IntegerBinding.intToEntry(keyIndex, keyEntry);
				valueEntry.setData(data.toByteArray());
				mutable.put(null, keyEntry, valueEntry);				
			}
		}
		
	}

	private class AddToReadOnlyMap implements UuidIntProcedure {
		WriteToBdb writer = new WriteToBdb();
		
		@Override
		public boolean apply(long[] key, int value) {
			readOnlyUuidsToNidMap.put(key, value);
			writer.apply(key, value);
			return true;
		}

		@Override
		public void close() {
			writer.close();
		}
		
	}

	@Override
	public void sync() throws IOException {
		WriteToBdb writer = new WriteToBdb();
		mutableUuidsToNidMap.forEachPair(writer);
		writer.close();
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
		if (mutableUuidsToNidMap.size() == RECORD_SIZE) {
			readOnlyUuidsToNidMap.ensureCapacity(mutableUuidsToNidMap.size() + readOnlyUuidsToNidMap.size() + 1);
			mutableUuidsToNidMap.forEachPair(new AddToReadOnlyMap());
			mutableUuidsToNidMap.clear();
			mutableUuidsToNidMap.ensureCapacity(RECORD_SIZE + 1);
		}
		int newNid = sequence.getAndIncrement();
		readOnlyUuidsToNidMap.put(UuidUtil.convert(uuid), newNid);
		w.unlock();
		return newNid;
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
		int newNid = sequence.getAndIncrement();
		for (UUID uuid : uuids) {
			readOnlyUuidsToNidMap.put(UuidUtil.convert(uuid), newNid);
		}
		w.unlock();
		return newNid;
	}

	@Override
	protected String getDbName() {
		return "UuidsToNidMap";
	}

}
