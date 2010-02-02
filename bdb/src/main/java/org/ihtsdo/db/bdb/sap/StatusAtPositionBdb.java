package org.ihtsdo.db.bdb.sap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;
import org.ihtsdo.db.bdb.computer.version.PositionMapper;

import cern.colt.list.IntArrayList;

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
	
	private static final int FIRST_ID = 0;
	private static final int MIN_ARRAY_SIZE = 100;

	
	private static HashMap<UncommittedStatusForPath, Integer> uncomittedStatusPathEntries = 
		new HashMap<UncommittedStatusForPath, Integer>();
	
	private AtomicInteger sequence;
	
	private static AtomicInteger hits = new AtomicInteger(0);
	
	private static AtomicInteger misses = new AtomicInteger(0);

	/**
	 * TODO future optimization is to use a map that uses an index to the <code>PositionArrays</code>
	 * rather than duplicating the key data. 
	 */
	private SapToIntHashMap sapToIntMap;
	private boolean changedSinceSync = false;
	private static class PositionArrayBinder extends
			TupleBinding<PositionArrays> {

		@Override
		public PositionArrays entryToObject(TupleInput input) {
			int size = input.readInt();
			int length = input.readInt();
			PositionArrays pa = new PositionArrays(length);
			for (int i = 0; i < length; i++) {
				pa.pathNids[i] = input.readInt();
				pa.statusNids[i] = input.readInt();
				pa.commitTimes[i] = input.readLong();
			}
			pa.size = size;
			return pa;
		}

		@Override
		public void objectToEntry(PositionArrays pa, TupleOutput output) {
			output.writeInt(pa.size);
			output.writeInt(pa.pathNids.length);
			for (int i = 0; i < pa.pathNids.length; i++) {
				output.writeInt(pa.pathNids[i]);
				output.writeInt(pa.statusNids[i]);
				output.writeLong(pa.commitTimes[i]);
			}
		}
	}

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
				int newCapacity = pathNids.length + MIN_ARRAY_SIZE;
				
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

	public StatusAtPositionBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv)
			throws IOException {
		super(readOnlyBdbEnv, mutableBdbEnv);
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
			if (readOnly.get(null, theKey, theData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				readOnlyArray = positionArrayBinder.entryToObject(theData);
			} else {
				readOnlyArray = new PositionArrays();
			}
			if (mutable.get(null, theKey, theData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				readWriteArray = positionArrayBinder.entryToObject(theData);
			} else {
				readWriteArray = new PositionArrays();
			}
			int size = getPositionCount();
			int readOnlySize = readOnlyArray.getSize();
			int readWriteSize = readWriteArray.getSize();
			sequence.set(FIRST_ID + size);
			sapToIntMap = new SapToIntHashMap(sequence.get() + FIRST_ID);
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
		if (getReadOnlyIndex(index) < readOnlyArray.getSize()) {
			pathNid = readOnlyArray.pathNids[getReadOnlyIndex(index)];
			time = readOnlyArray.commitTimes[getReadOnlyIndex(index)];
		} else {
			pathNid = readWriteArray.pathNids[getReadWriteIndex(index)];
			time = readWriteArray.commitTimes[getReadWriteIndex(index)];
		}
		I_Path path = Bdb.getPathManager().get(pathNid);
		return new Position(ThinVersionHelper.convert(time), path);
	}
	
	public int getPathId(int index) {
		if (getReadOnlyIndex(index) < readOnlyArray.getSize()) {
			return readOnlyArray.pathNids[getReadOnlyIndex(index)];
		} else {
			return readWriteArray.pathNids[getReadWriteIndex(index)];
		}
	}

	public int getStatusId(int index) {
		if (getReadOnlyIndex(index) < readOnlyArray.getSize()) {
			return readOnlyArray.statusNids[getReadOnlyIndex(index)];
		} else {
			return readWriteArray.statusNids[getReadWriteIndex(index)];
		}
	}

	public long getTime(int index) {
		if (getReadOnlyIndex(index) < readOnlyArray.getSize()) {
			return readOnlyArray.commitTimes[getReadOnlyIndex(index)];
		} else {
			return readWriteArray.commitTimes[getReadWriteIndex(index)];
		}
	}

	public int getReadOnlyMax() {
		return readOnlyArray.size -1;
	}
	
	public int getVersion(int index) {
		if (getReadOnlyIndex(index) < readOnlyArray.getSize()) {
			return ThinVersionHelper.convert(readOnlyArray.commitTimes[getReadOnlyIndex(index)]);
		} else {
			return ThinVersionHelper.convert(readWriteArray.commitTimes[getReadWriteIndex(index)]);
		}
	}

	public void clearMapperCache() {
		mapperCache.clear();
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
			if (mutable.put(null, theKey, theData) == OperationStatus.SUCCESS) {
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
			if (mutable.put(null, theKey, theData) == OperationStatus.SUCCESS) {
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
	
	public void commit(long time) throws IOException {
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

	public int getSapNid(int statusNid, int pathNid, long time) {
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
				hits.incrementAndGet();
				return statusAtPositionNid;
			}
		}
		if (sapToIntMap.containsKey(time, statusNid, pathNid)) {
			hits.incrementAndGet();
			return sapToIntMap.get(time, statusNid, pathNid);
		}
		try {
			boolean immediateAcquire = expandPermit.tryAcquire();
			if (immediateAcquire == false) {
				expandPermit.acquireUninterruptibly();
				// Try one last time...
				if (sapToIntMap.containsKey(time, statusNid, pathNid)) {
					hits.incrementAndGet();
					expandPermit.release();
					return sapToIntMap.get(time, statusNid, pathNid);
				}
			}
			int statusAtPositionNid = sequence.getAndIncrement();
			changedSinceSync  = true;
			sapToIntMap.put(time, statusNid, pathNid, statusAtPositionNid);
			readWriteArray.setSize(getReadWriteIndex(statusAtPositionNid) + 1);
			expandPermit.release();
			readWriteArray.commitTimes[getReadWriteIndex(statusAtPositionNid)] = time;
			readWriteArray.statusNids[getReadWriteIndex(statusAtPositionNid)] = statusNid;
			readWriteArray.pathNids[getReadWriteIndex(statusAtPositionNid)] = pathNid;
			misses.incrementAndGet();
			return statusAtPositionNid;
		} catch (Throwable e) {
			expandPermit.release();
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	private Semaphore expandPermit = new Semaphore(1);

	public int getSapNid(TimeStatusPosition tsp) {
		return getSapNid(tsp.getStatusNid(), tsp.getPathNid(), tsp.getTime());
	}
	
	public static void reportStats() {
		float hitStat = hits.get();
		float misStat = misses.get();
		float percent = hitStat / (misStat + hitStat);
		System.out.println("hits: " + (int) hitStat + " misses: " + (int) misStat);
		System.out.println("hit %: " + percent);
	}
	
	public static void reset() {
		hits.set(0);
		misses.set(0);
	}

	@Override
	public void close() {
		try {
			this.sync();
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		super.close();
	}

	@Override
	public void sync() throws IOException {
		if (changedSinceSync) {
			DatabaseEntry valueEntry = new DatabaseEntry();
			expandPermit.acquireUninterruptibly();
			positionArrayBinder.objectToEntry(readWriteArray, valueEntry);
			DatabaseEntry theKey = new DatabaseEntry();
			IntegerBinding.intToEntry(0, theKey);
			mutable.put(null, theKey, valueEntry);
			changedSinceSync = false;
			expandPermit.release();
		}
		super.sync();
	}

	public List<TimePathId> getTimePathList() {
		HashSet<TimePathId> returnValues = new HashSet<TimePathId>();
		IntArrayList values = sapToIntMap.values();
		for (int i = 0; i < values.size(); i++) {
			int sapNid = values.getQuick(i);
			returnValues.add(new TimePathId(getVersion(sapNid), getPathId(sapNid)));
		}
		return new ArrayList<TimePathId>(returnValues);
	}

}
