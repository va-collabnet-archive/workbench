package org.ihtsdo.db.bdb.sap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.concept.Concept;
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
	private static ConcurrentHashMap<I_Position, PositionMapper> mapperCache = new ConcurrentHashMap<I_Position, PositionMapper>();
	private static Set<Integer> currentPaths;

	private static final int MIN_ARRAY_SIZE = 100;

	private static Map<UncommittedStatusForPath, Integer> uncomittedStatusPathEntries = new ConcurrentHashMap<UncommittedStatusForPath, Integer>();

	private AtomicInteger sequence;

	private static AtomicInteger hits = new AtomicInteger(0);

	private static AtomicInteger misses = new AtomicInteger(0);

	/**
	 * TODO future optimization is to use a map that uses an index to the
	 * <code>PositionArrays</code> rather than duplicating the key data.
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
				System.arraycopy(statusNids, 0, tempStatusNids, 0,
						statusNids.length);
				statusNids = tempStatusNids;

				long[] tempCommitTimes = new long[newCapacity];
				System.arraycopy(commitTimes, 0, tempCommitTimes, 0,
						commitTimes.length);
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
			sequence.set(size);
			sapToIntMap = new SapToIntHashMap(sequence.get());
			for (int i = 0; i < readOnlySize; i++) {
				sapToIntMap.put(readOnlyArray.commitTimes[i],
						readOnlyArray.statusNids[i], readOnlyArray.pathNids[i],
						i);
			}
			for (int i = 0; i < readWriteSize; i++) {
				int readWriteIndex = i + readOnlySize;
				assert i < readWriteArray.commitTimes.length : " readWriteIndex: "
						+ readWriteIndex
						+ " commitTimes.length: "
						+ readWriteArray.commitTimes.length;

				assert i < readWriteArray.statusNids.length : " readWriteIndex: "
						+ readWriteIndex
						+ " commitTimes.length: "
						+ readWriteArray.statusNids.length;

				assert i < readOnlyArray.pathNids.length : " readWriteIndex: "
						+ readWriteIndex + " commitTimes.length: "
						+ readWriteArray.pathNids.length;

				sapToIntMap.put(readWriteArray.commitTimes[i],
						readWriteArray.statusNids[i],
						readWriteArray.pathNids[i], readWriteIndex);

			}
		} catch (DatabaseException e) {
			throw new IOException(e);
		}
	}

	public I_Position getPosition(int index) throws IOException,
			PathNotExistsException, TerminologyException {
		int pathNid = -1;
		long time = -1;
		if (index < readOnlyArray.getSize()) {
			pathNid = readOnlyArray.pathNids[index];
			time = readOnlyArray.commitTimes[index];
		} else {
			pathNid = readWriteArray.pathNids[getReadWriteIndex(index)];
			time = readWriteArray.commitTimes[getReadWriteIndex(index)];
		}
		I_Path path = Bdb.getPathManager().get(pathNid);
		return new Position(ThinVersionHelper.convert(time), path);
	}

	public int getPathId(int index) {
        if (index < 0) {
            return Integer.MIN_VALUE;
        }
		if (index < readOnlyArray.getSize()) {
			return readOnlyArray.pathNids[index];
		} else {
			return readWriteArray.pathNids[getReadWriteIndex(index)];
		}
	}

	public int getStatusId(int index) {
	    if (index < 0) {
	        return Integer.MIN_VALUE;
	    }
		if (index < readOnlyArray.getSize()) {
			return readOnlyArray.statusNids[index];
		} else {
			return readWriteArray.statusNids[getReadWriteIndex(index)];
		}
	}

	public long getTime(int index) {
	    if (index < 0) {
	        return Long.MIN_VALUE;
	    }
		if (index < readOnlyArray.getSize()) {
			return readOnlyArray.commitTimes[index];
		} else {
			return readWriteArray.commitTimes[getReadWriteIndex(index)];
		}
	}

	public int getReadOnlyMax() {
		return readOnlyArray.size - 1;
	}

	public int getVersion(int index) {
        if (index < 0) {
            return Integer.MIN_VALUE;
        }
		if (index < readOnlyArray.getSize()) {
			return ThinVersionHelper.convert(readOnlyArray.commitTimes[index]);
		} else {
			return ThinVersionHelper
					.convert(readWriteArray.commitTimes[getReadWriteIndex(index)]);
		}
	}

	public void clearMapperCache() {
		mapperCache.clear();
	}

	public PositionMapper getMapper(I_Position position) {
		PositionMapper pm = mapperCache.get(position);
		if (pm != null) {
			return pm;
		}
		pm = new PositionMapper(position);
		PositionMapper existing = mapperCache.putIfAbsent(position, pm);
		if (existing != null) {
			pm = existing;
		} else {
			pm.queueForSetup();
		}
		trimCache();
		return pm;
	}

	/**
	 * TODO make this trim algorithm more intelligent.
	 */
	private static void trimCache() {
	    boolean continueTrim = mapperCache.size() > 1;
	    long now = System.currentTimeMillis();
		while (continueTrim) {
			Entry<I_Position, PositionMapper> looser = null;
			for (Entry<I_Position, PositionMapper> entry : mapperCache
					.entrySet()) {
				if (looser == null) {
					looser = entry;
				} else {
					if (looser.getValue().getLastRequestTime() > entry
							.getValue().getLastRequestTime()) {
						looser = entry;
					} else if (looser.getValue().getLastRequestTime() == entry
							.getValue().getLastRequestTime()) {
						if (looser.getValue().getQueryCount() > entry
								.getValue().getQueryCount())
							looser = entry;
					}
				}
			}
			if (now - looser.getValue().getLastRequestTime() > 1000) {
	            mapperCache.remove(looser.getKey());
	            continueTrim = mapperCache.size() > 1;
			} else {
			    continueTrim = false;
			}
		}
	}

	@Override
	protected String getDbName() {
		return "positionDb";
	}

	public IntSet commit(long time) throws IOException {
		IntSet committedSapNids = new IntSet();
		synchronized (uncomittedStatusPathEntries) {
			for (int sapNid : uncomittedStatusPathEntries.values()) {
				changedSinceSync = true;
				readWriteArray.commitTimes[getReadWriteIndex(sapNid)] = time;
				committedSapNids.add(sapNid);
			}
			assert committedSapNids.contiguous() : " commit sapNids are not contiguous: "
				+ committedSapNids.toString();
			uncomittedStatusPathEntries.clear();
			mapperCache.clear();
		}
		sync();
		return committedSapNids;
	}

	private int getReadWriteIndex(int index) {
		return index - readOnlyArray.getSize();
	}

	public int getSapNid(int statusNid, int pathNid, long time) {
		try {
			assert getCurrentPaths().size() == 0
					|| getCurrentPaths().contains(pathNid) : "pathNid: "
					+ pathNid + " invalid. Concept: "
					+ Concept.get(pathNid).getInitialText();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (time == Long.MAX_VALUE) {
			UncommittedStatusForPath usp = new UncommittedStatusForPath(
					statusNid, pathNid);
			if (uncomittedStatusPathEntries.containsKey(usp)) {
				return uncomittedStatusPathEntries.get(usp);
			} else {
			    synchronized (usp) {
                    
                }
				int statusAtPositionNid = sequence.getAndIncrement();
				mapperCache.clear();
				readWriteArray
						.setSize(getReadWriteIndex(statusAtPositionNid) + 1);
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
			mapperCache.clear();
			changedSinceSync = true;
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
		System.out.println("hits: " + (int) hitStat + " misses: "
				+ (int) misStat);
		System.out.println("hit %: " + percent);
	}

	public static void reset() {
		hits.set(0);
		misses.set(0);
		currentPaths = null;
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
			returnValues.add(new TimePathId(getVersion(sapNid),
					getPathId(sapNid)));
		}
		return new ArrayList<TimePathId>(returnValues);
	}

	public static Set<Integer> getCurrentPaths() {
		try {
			currentPaths = Bdb.getPathManager().getPathNids();
			return currentPaths;
		} catch (TerminologyException e) {
			throw new RuntimeException(e);
		}
	}

}
