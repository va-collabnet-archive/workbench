package org.ihtsdo.db.bdb;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.PathManager;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.Position;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * @author kec
 *
 */
public class StatusAtPositionBdb extends ComponentBdb {
	private static PositionArrays readOnlyArray;
	private static PositionArrays readWriteArray;

	private static PositionArrayBinder positionArrayBinder = new PositionArrayBinder();
	private static LinkedList<PositionMapper> mapperCache = new LinkedList<PositionMapper>();
	
	private static final int FIRST_ID = Integer.MIN_VALUE + 1;

	
	private static HashMap<UncommittedStatusForPath, Integer> uncomittedStatusPathEntries = 
		new HashMap<UncommittedStatusForPath, Integer>();
	
	private AtomicInteger sequence;

	/**
	 * TODO future optimization is to use a map that uses an index to the <code>PositionArrays</code>
	 * rather than duplicating the key data. 
	 */
	private StatusAtPositionToIntHashMap sapToIntMap;
	private static class PositionArrayBinder extends
			TupleBinding<PositionArrays> {

		@Override
		public PositionArrays entryToObject(TupleInput input) {
			int length = input.readInt();
			PositionArrays pa = new PositionArrays(length);
			for (int i = 0; i < length; i++) {
				pa.pathNids[i] = input.readInt();
				pa.statusNids[i] = input.readInt();
				pa.commitTimes[i] = input.readLong();
			}
			return pa;
		}

		@Override
		public void objectToEntry(PositionArrays object, TupleOutput output) {
			output.writeInt(object.pathNids.length);
			for (int i = 0; i < object.pathNids.length; i++) {
				output.writeInt(object.pathNids[i]);
				output.writeInt(object.statusNids[i]);
				output.writeLong(object.commitTimes[i]);
			}
		}
	}

	private static final int MIN_ARRAY_SIZE = 500;
	private static class PositionArrays {
		int size = 0;
		int[] pathNids;
		int[] statusNids;
		long[] commitTimes;
		
		public PositionArrays() {
			pathNids = new int[MIN_ARRAY_SIZE];
			statusNids = new int[MIN_ARRAY_SIZE];
			commitTimes = new long[MIN_ARRAY_SIZE];
			this.size = 0;
		}
		
		public PositionArrays(int size) {
			pathNids = new int[size];
			statusNids = new int[size];
			commitTimes = new long[size];
			this.size = size;
		}

		private void setSize(int size) {
			this.size = size;
			ensureCapacity(size);
		}

		private int getSize() {
			return size;
		}

		private int getCapacity() {
			return pathNids.length;
		}

		private void ensureCapacity(int size) {
			if (size > getCapacity()) {
				int newCapacity = Math.max(pathNids.length * 2, MIN_ARRAY_SIZE);
				
				if (newCapacity > 100000) {
					newCapacity = pathNids.length + 1000;
				}
				
				int[] tempPathNids = new int[newCapacity];
				System.arraycopy(pathNids, 0, tempPathNids, 0, pathNids.length);
				pathNids = tempPathNids;
				
				int[] tempStatusNids = new int[newCapacity];
				System.arraycopy(statusNids, 0, tempStatusNids, 0, statusNids.length);
				statusNids = tempStatusNids;
				
				long[] tempCommitTimes = new long[newCapacity];
				System.arraycopy(commitTimes, 0, tempCommitTimes, 0, commitTimes.length);
				commitTimes = tempCommitTimes;
			}
			
		}
	}

	public StatusAtPositionBdb(Bdb readOnlyBdbEnv, Bdb readWriteBdbEnv)
			throws IOException {
		super(readOnlyBdbEnv, readWriteBdbEnv);
	}

	public int getPositionCount() {
		return readOnlyArray.getSize() + readWriteArray.getSize();
	}
	


	protected void init() throws IOException {
		sequence = new AtomicInteger(Integer.MIN_VALUE + 1);
		DatabaseEntry theKey = new DatabaseEntry();
		IntegerBinding.intToEntry(0, theKey);
		DatabaseEntry theData = new DatabaseEntry();
		try {
			if (readOnly.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				readOnlyArray = positionArrayBinder.entryToObject(theData);
			} else {
				readOnlyArray = new PositionArrays();
			}
			if (readWrite.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				readWriteArray = positionArrayBinder.entryToObject(theData);
			} else {
				readWriteArray = new PositionArrays();
			}
			int size = getPositionCount();
			int readOnlySize = readOnlyArray.getSize();
			int readWriteSize = readWriteArray.getSize();
			sequence.set(FIRST_ID + size);
			sapToIntMap = new StatusAtPositionToIntHashMap(sequence.get() + FIRST_ID);
			for (int i = 0; i < readOnlySize; i++) {
				sapToIntMap.put(readOnlyArray.commitTimes[i], 
						readOnlyArray.statusNids[i], 
						readOnlyArray.pathNids[i], i + FIRST_ID);
			}
			for (int i = 0; i < readWriteSize; i++) {
				int j = i + readOnlySize;
				sapToIntMap.put(readOnlyArray.commitTimes[getReadOnlyIndex(j)], 
						readOnlyArray.statusNids[getReadOnlyIndex(j)], 
						readOnlyArray.pathNids[getReadOnlyIndex(j)], j + FIRST_ID);
			}
		} catch (DatabaseException e) {
			throw new IOException(e);
		}
	}

	public I_Position getPosition(int index) throws IOException, PathNotExistsException, TerminologyException {
		int pathNid = -1;
		long time = -1;
		if (index < readOnlyArray.getSize()) {
			pathNid = readOnlyArray.pathNids[getReadOnlyIndex(index)];
			time = readOnlyArray.commitTimes[getReadOnlyIndex(index)];
		} else {
			pathNid = readWriteArray.pathNids[getReadWriteIndex(index)];
			time = readWriteArray.commitTimes[getReadWriteIndex(index)];
		}
		I_Path path = new PathManager().get(pathNid);
		return new Position(ThinVersionHelper.convert(time), path);
	}
	
	public int getPathId(int index) {
		if (index < readOnlyArray.getSize()) {
			return readOnlyArray.pathNids[getReadOnlyIndex(index)];
		} else {
			return readWriteArray.pathNids[getReadWriteIndex(index)];
		}
	}

	public int getStatusId(int index) {
		if (index < readOnlyArray.getSize()) {
			return readOnlyArray.statusNids[getReadOnlyIndex(index)];
		} else {
			return readWriteArray.statusNids[getReadWriteIndex(index)];
		}
	}

	public long getTime(int index) {
		if (index < readOnlyArray.getSize()) {
			return readOnlyArray.commitTimes[getReadOnlyIndex(index)];
		} else {
			return readWriteArray.commitTimes[getReadWriteIndex(index)];
		}
	}

	public int getReadOnlyMax() {
		throw new UnsupportedOperationException();
	}
	
	public int getVersion(int index) {
		if (index < readOnlyArray.getSize()) {
			return ThinVersionHelper.convert(readOnlyArray.commitTimes[getReadOnlyIndex(index)]);
		} else {
			return ThinVersionHelper.convert(readWriteArray.commitTimes[getReadWriteIndex(index)]);
		}
	}

	public PositionMapper getMapper(I_Position position) {
		for (PositionMapper pm: mapperCache) {
			if (pm.getDestination().equals(position)) {
				return pm;
			}
		}
		PositionMapper pm = new PositionMapper(position);
		mapperCache.addFirst(pm);
		while (mapperCache.size() > 2) {
			mapperCache.removeLast();
		}
		return new PositionMapper(position);
	}
	
	public void abort(int[] indexes) throws IOException {
		for (int i: indexes) {
			assert readWriteArray.commitTimes[i] == Long.MAX_VALUE;
			readWriteArray.commitTimes[i] = Long.MIN_VALUE;
		}
		DatabaseEntry theKey = new DatabaseEntry();
		IntegerBinding.intToEntry(0, theKey);
		DatabaseEntry theData = new DatabaseEntry();
		positionArrayBinder.objectToEntry(readWriteArray, theData);
		try {
			if (readWrite.put(null, theKey, theData) == OperationStatus.SUCCESS) {
				readWriteArray = positionArrayBinder.entryToObject(theData);
			}
			mapperCache.clear();
		} catch (DatabaseException e) {
			throw new IOException(e);
		}
	}
	
	
	private void commit(int[] indexes, long time) throws IOException {
		for (int i: indexes) {
			assert readWriteArray.commitTimes[i] == Long.MAX_VALUE;
			readWriteArray.commitTimes[i] = time;
		}
		DatabaseEntry theKey = new DatabaseEntry();
		IntegerBinding.intToEntry(0, theKey);
		DatabaseEntry theData = new DatabaseEntry();
		positionArrayBinder.objectToEntry(readWriteArray, theData);
		try {
			if (readWrite.put(null, theKey, theData) == OperationStatus.SUCCESS) {
				readWriteArray = positionArrayBinder.entryToObject(theData);
			}
			mapperCache.clear();
		} catch (DatabaseException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected String getDbName() {
		return "positionDb";
	}
	
	protected void commit(long time) throws IOException {
		int[] indexes = new int[uncomittedStatusPathEntries.size()];
		int i = 0;
		for (int index: uncomittedStatusPathEntries.values()) {
			indexes[i] = index;
			i++;
		}
		commit(indexes, time);
		uncomittedStatusPathEntries.clear();
	}
	
	private int getReadOnlyIndex(int index) {
		return index - FIRST_ID;
	}

	private int getReadWriteIndex(int index) {
		return index - readOnlyArray.getSize() - FIRST_ID;
	}

	public int getStatusAtPositionNid(int statusNid, int pathNid, long time) {
		if (time == Long.MAX_VALUE) {
			UncommittedStatusForPath usp = new UncommittedStatusForPath(statusNid, pathNid);
			if (uncomittedStatusPathEntries.containsKey(usp)) {
				return uncomittedStatusPathEntries.get(usp);
			} else {
				int statusAtPositionNid = sequence.getAndIncrement();
				readWriteArray.setSize(getReadWriteIndex(statusAtPositionNid) + 1);
				readWriteArray.commitTimes[getReadWriteIndex(statusAtPositionNid)] = time;
				readWriteArray.statusNids[getReadWriteIndex(statusAtPositionNid)] = statusNid;
				readWriteArray.pathNids[getReadWriteIndex(statusAtPositionNid)] = pathNid;
				uncomittedStatusPathEntries.put(usp, statusAtPositionNid);
				return statusAtPositionNid;
			}
		}
		if (sapToIntMap.containsKey(time, statusNid, pathNid)) {
			return sapToIntMap.get(time, statusNid, pathNid);
		}
		boolean immediateAcquire = expandPermit.tryAcquire();
		if (immediateAcquire == false) {
			expandPermit.acquireUninterruptibly();
			// Try one last time...
			if (sapToIntMap.containsKey(time, statusNid, pathNid)) {
				return sapToIntMap.get(time, statusNid, pathNid);
			}
		}
		int statusAtPositionNid = sequence.getAndIncrement();
		readWriteArray.setSize(getReadWriteIndex(statusAtPositionNid) + 1);
		expandPermit.release();
		readWriteArray.commitTimes[getReadWriteIndex(statusAtPositionNid)] = time;
		readWriteArray.statusNids[getReadWriteIndex(statusAtPositionNid)] = statusNid;
		readWriteArray.pathNids[getReadWriteIndex(statusAtPositionNid)] = pathNid;
		
		return statusAtPositionNid;
	}
	Semaphore expandPermit = new Semaphore(1);

	public int getStatusAtPositionNid(TimeStatusPosition tsp) {
		return getStatusAtPositionNid(tsp.getStatusNid(), tsp.getPathNid(), tsp.getTime());
	}
	
}
