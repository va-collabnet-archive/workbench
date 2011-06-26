package org.ihtsdo.db.bdb.sap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;
import org.ihtsdo.db.bdb.computer.version.PositionMapper;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

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

    private static int initialPosition;

    public static int getInitialPosition() {
        return initialPosition;
    }
    private static PositionArrays readOnlyArray;
    private static PositionArrays mutableArray;
    private static PositionArrayBinder positionArrayBinder = new PositionArrayBinder();
    private static ConcurrentHashMap<PositionBI, PositionMapper> mapperCache = new ConcurrentHashMap<PositionBI, PositionMapper>();
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

    public void cancelAfterCommit(NidSetBI commitSapNids) throws IOException {
        synchronized (uncomittedStatusPathEntries) {
            int min = Integer.MAX_VALUE;
            expandPermit.acquireUninterruptibly();
            for (int sapNid : commitSapNids.getSetValues()) {
                min = Math.min(min, sapNid);
            }
            
            for (int sapNid : uncomittedStatusPathEntries.values()) {
                min = Math.min(min, sapNid);
            }
            
            for (int i = min; i < sequence.get(); i++) {
                changedSinceSync = true;
                mutableArray.commitTimes[getReadWriteIndex(i)] = Long.MIN_VALUE;
                sapToIntMap.put(getStatusNid(i), getAuthorNid(i), getPathNid(i), Long.MIN_VALUE, i);
            }

            uncomittedStatusPathEntries.clear();
            mapperCache.clear();
            expandPermit.release();
        }
        sync();
    }

    private static class PositionArrayBinder extends TupleBinding<PositionArrays> {

        @Override
        public PositionArrays entryToObject(TupleInput input) {
            int size = input.readInt();
            int length = input.readInt();
            PositionArrays pa = new PositionArrays(length);
            for (int i = 0; i < length; i++) {
                pa.statusNids[i] = input.readInt();
                pa.authorNids[i] = input.readInt();
                pa.pathNids[i] = input.readInt();
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
                output.writeInt(pa.statusNids[i]);
                output.writeInt(pa.authorNids[i]);
                output.writeInt(pa.pathNids[i]);
                output.writeLong(pa.commitTimes[i]);
            }
        }
    }

    private static class PositionArrays {

        int size = 0;
        int[] statusNids;
        int[] authorNids;
        int[] pathNids;
        long[] commitTimes;

        public PositionArrays() {
            statusNids = new int[MIN_ARRAY_SIZE];
            authorNids = new int[MIN_ARRAY_SIZE];
            pathNids = new int[MIN_ARRAY_SIZE];
            commitTimes = new long[MIN_ARRAY_SIZE];
            this.size = 0;
        }

        public PositionArrays(int size) {
            statusNids = new int[size];
            authorNids = new int[size];
            pathNids = new int[size];
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

                int[] tempStatusNids = new int[newCapacity];
                System.arraycopy(statusNids, 0, tempStatusNids, 0,
                        statusNids.length);
                statusNids = tempStatusNids;

                int[] tempAuthorhNids = new int[newCapacity];
                System.arraycopy(authorNids, 0, tempAuthorhNids, 0, authorNids.length);
                authorNids = tempAuthorhNids;

                int[] tempPathNids = new int[newCapacity];
                System.arraycopy(pathNids, 0, tempPathNids, 0, pathNids.length);
                pathNids = tempPathNids;

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
        return readOnlyArray.getSize() + mutableArray.getSize();
    }

    protected void init() throws IOException {
        preloadBoth();
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
                mutableArray = positionArrayBinder.entryToObject(theData);
            } else {
                mutableArray = new PositionArrays();
            }
            int size = getPositionCount();
            int readOnlySize = readOnlyArray.getSize();
            int mutableSize = mutableArray.getSize();
            sequence.set(Math.max(size, 1));
            sapToIntMap = new SapToIntHashMap(sequence.get());
            for (int i = 0; i < readOnlySize; i++) {
                if (readOnlyArray.commitTimes[i] != 0) {
                    sapToIntMap.put(
                            readOnlyArray.statusNids[i],
                            readOnlyArray.authorNids[i],
                            readOnlyArray.pathNids[i],
                            readOnlyArray.commitTimes[i],
                            i);
                }
            }
            closeReadOnly();
            for (int i = 0; i < mutableSize; i++) {
                int mutableIndex = i + readOnlySize;
                assert i < mutableArray.commitTimes.length : " mutableIndex: "
                        + mutableIndex
                        + " commitTimes.length: "
                        + mutableArray.commitTimes.length;

                assert i < mutableArray.statusNids.length : " mutableIndex: "
                        + mutableIndex
                        + " statusNids.length: "
                        + mutableArray.statusNids.length;

                assert i < mutableArray.pathNids.length : " mutableIndex: "
                        + mutableIndex + " pathNids.length: "
                        + mutableArray.pathNids.length;

                if (mutableArray.commitTimes[i] != 0) {
                    sapToIntMap.put(
                            mutableArray.statusNids[i],
                            mutableArray.authorNids[i],
                            mutableArray.pathNids[i],
                            mutableArray.commitTimes[i], mutableIndex);
                }
            }
            if (size == 0) {
                initialPosition = 1;
            } else if (readOnlyArray.size > 0 && readOnlyArray.pathNids[0] == 0) {
                initialPosition = 1;
            } else if (readOnlyArray.size == 0 && mutableArray.pathNids[0] == 0) {
                initialPosition = 1;
            } else {
                initialPosition = 0;
            }
        } catch (DatabaseException e) {
            throw new IOException(e);
        }
    }

    public PositionBI getPosition(int sapNid) throws IOException,
            PathNotExistsException, TerminologyException {
        int pathNid = -1;
        long time = -1;
        if (sapNid < readOnlyArray.getSize()) {
            pathNid = readOnlyArray.pathNids[sapNid];
            time = readOnlyArray.commitTimes[sapNid];
        } else {
            pathNid = mutableArray.pathNids[getReadWriteIndex(sapNid)];
            time = mutableArray.commitTimes[getReadWriteIndex(sapNid)];
        }
        PathBI path = Bdb.getPathManager().get(pathNid);
        return new Position(time, path);
    }

    public int getPathNid(int index) {
        if (index < 0) {
            return Integer.MIN_VALUE;
        }
        if (index < readOnlyArray.getSize()) {
            return readOnlyArray.pathNids[index];
        } else {
            return mutableArray.pathNids[getReadWriteIndex(index)];
        }
    }

    public int getStatusNid(int index) {
        if (index < 0) {
            return Integer.MIN_VALUE;
        }
        if (index < readOnlyArray.getSize()) {
            return readOnlyArray.statusNids[index];
        } else {
            return mutableArray.statusNids[getReadWriteIndex(index)];
        }
    }

    public long getTime(int index) {
        if (index == Integer.MAX_VALUE) {
            throw new RuntimeException("index == Integer.MAX_VALUE");
        }
        if (index < 0) {
            return Long.MIN_VALUE;
        }
        if (index < readOnlyArray.getSize()) {
            return readOnlyArray.commitTimes[index];
        } else {
            return mutableArray.commitTimes[getReadWriteIndex(index)];
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
            return ThinVersionHelper.convert(mutableArray.commitTimes[getReadWriteIndex(index)]);
        }
    }

    public void clearMapperCache() {
        mapperCache.clear();
    }

    public PositionMapper getMapper(PositionBI position) {
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
            Entry<PositionBI, PositionMapper> looser = null;
            for (Entry<PositionBI, PositionMapper> entry : mapperCache.entrySet()) {
                if (looser == null) {
                    looser = entry;
                } else {
                    if (looser.getValue().getLastRequestTime() > entry.getValue().getLastRequestTime()) {
                        looser = entry;
                    } else if (looser.getValue().getLastRequestTime() == entry.getValue().getLastRequestTime()) {
                        if (looser.getValue().getQueryCount() > entry.getValue().getQueryCount()) {
                            looser = entry;
                        }
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
            expandPermit.acquireUninterruptibly();
            for (int sapNid : uncomittedStatusPathEntries.values()) {
                changedSinceSync = true;
                mutableArray.commitTimes[getReadWriteIndex(sapNid)] = time;
                sapToIntMap.put(getStatusNid(sapNid), getAuthorNid(sapNid), getPathNid(sapNid), time, sapNid);

                committedSapNids.add(sapNid);
            }
            uncomittedStatusPathEntries.clear();
            mapperCache.clear();
            expandPermit.release();
        }
        sync();
        return committedSapNids;
    }

    private int getReadWriteIndex(int index) {
        return index - readOnlyArray.getSize();
    }

    public int getSapNid(int statusNid, int authorNid, int pathNid, long time) {
        if (time == Long.MAX_VALUE) {
            UncommittedStatusForPath usp = new UncommittedStatusForPath(
                    statusNid, pathNid);
            if (uncomittedStatusPathEntries.containsKey(usp)) {
                return uncomittedStatusPathEntries.get(usp);
            } else {
                int statusAtPositionNid = sequence.getAndIncrement();
                mapperCache.clear();
                mutableArray.setSize(getReadWriteIndex(statusAtPositionNid) + 1);
                mutableArray.statusNids[getReadWriteIndex(statusAtPositionNid)] = statusNid;
                mutableArray.authorNids[getReadWriteIndex(statusAtPositionNid)] = authorNid;
                mutableArray.pathNids[getReadWriteIndex(statusAtPositionNid)] = pathNid;
                mutableArray.commitTimes[getReadWriteIndex(statusAtPositionNid)] = time;
                uncomittedStatusPathEntries.put(usp, statusAtPositionNid);
                hits.incrementAndGet();
                return statusAtPositionNid;
            }
        }
        if (sapToIntMap.containsKey(statusNid, authorNid, pathNid, time)) {
            hits.incrementAndGet();
            return sapToIntMap.get(statusNid, authorNid, pathNid, time);
        }
        try {
            boolean immediateAcquire = expandPermit.tryAcquire();
            if (immediateAcquire == false) {
                expandPermit.acquireUninterruptibly();
                // Try one last time...
                if (sapToIntMap.containsKey(statusNid, authorNid, pathNid, time)) {
                    hits.incrementAndGet();
                    expandPermit.release();
                    return sapToIntMap.get(statusNid, authorNid, pathNid, time);
                }
            }
            int statusAtPositionNid = sequence.getAndIncrement();
            mapperCache.clear();
            changedSinceSync = true;
            sapToIntMap.put(statusNid, authorNid, pathNid, time, statusAtPositionNid);
            mutableArray.setSize(getReadWriteIndex(statusAtPositionNid) + 1);
            expandPermit.release();
            mutableArray.statusNids[getReadWriteIndex(statusAtPositionNid)] = statusNid;
            mutableArray.authorNids[getReadWriteIndex(statusAtPositionNid)] = authorNid;
            mutableArray.pathNids[getReadWriteIndex(statusAtPositionNid)] = pathNid;
            mutableArray.commitTimes[getReadWriteIndex(statusAtPositionNid)] = time;
            misses.incrementAndGet();
            return statusAtPositionNid;
        } catch (Throwable e) {
            expandPermit.release();
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private Semaphore expandPermit = new Semaphore(1);

    public int getSapNid(StatusAuthorPosition tsp) {
        return getSapNid(tsp.getStatusNid(), tsp.getAuthorNid(), tsp.getPathNid(), tsp.getTime());
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
            positionArrayBinder.objectToEntry(mutableArray, valueEntry);
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
        Collection<Integer> values = sapToIntMap.values();
        for (int sapNid : values) {
            returnValues.add(new TimePathId(getVersion(sapNid),
                    getPathNid(sapNid)));
        }
        return new ArrayList<TimePathId>(returnValues);
    }

    public static Set<Integer> getCurrentPaths() {
        try {
            currentPaths = Bdb.getPathManager().getPathNids();
            return currentPaths;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public IntSet getSpecifiedSapNids(IntSet pathIds, long startTime, long endTime) {
        IntSet specifiedSapNids = new IntSet();
        Collection<Integer> values = sapToIntMap.values();
        if (pathIds != null && pathIds.size() > 0) {
            for (int sapNid : values) {
                if (pathIds.contains(getPathNid(sapNid))) {
                    checkTimeAndAdd(startTime, endTime, specifiedSapNids, sapNid);
                }
            }
        } else {
            for (int sapNid : values) {
                checkTimeAndAdd(startTime, endTime, specifiedSapNids, sapNid);
            }
        }
        return specifiedSapNids;
    }

    private void checkTimeAndAdd(long startTime, long endTime, IntSet specifiedSapNids, int sapNid) {
        long time = getTime(sapNid);
        if (time >= startTime && time <= endTime) {
            specifiedSapNids.add(sapNid);
        }
    }

    public int getAuthorNid(int sapNid) {
        if (sapNid < 0) {
            return Integer.MIN_VALUE;
        }
        if (sapNid < readOnlyArray.getSize()) {
            return readOnlyArray.authorNids[sapNid];
        } else {
            return mutableArray.authorNids[getReadWriteIndex(sapNid)];
        }
    }
}
