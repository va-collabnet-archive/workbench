package org.ihtsdo.db.bdb.sap;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Position;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.tk.api.*;

/**
 * @author kec
 *
 */
public class StatusAtPositionBdb extends ComponentBdb {

    private static final int MIN_ARRAY_SIZE = 100;
    private static int initialPosition = -1;
    private static PositionArrayBinder positionArrayBinder =
            new PositionArrayBinder();
    private static final Map<UncommittedStatusForPath, Integer> uncomittedStatusPathEntries =
            new ConcurrentHashMap<>();
    private static CountDownLatch setupLatch = new CountDownLatch(1);
    private static AtomicInteger misses = new AtomicInteger(0);
    private static AtomicInteger hits = new AtomicInteger(0);
    private static Set<Integer> currentPaths;
    private static PositionArrays mutableArray;
    private static PositionArrays readOnlyArray;
    private static int readOnlyArraySize;
    //~--- fields --------------------------------------------------------------
    private Semaphore expandPermit = new Semaphore(1);
    /**
     * TODO future optimization is to use a map that uses an index to the
     * <code>PositionArrays</code> rather than duplicating the key data.
     */
    private SapToIntHashMap sapToIntMap;
    private AtomicInteger sequence;

    //~--- constructors --------------------------------------------------------
    public StatusAtPositionBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
        super(readOnlyBdbEnv, mutableBdbEnv);
    }

    //~--- methods -------------------------------------------------------------
    public void cancelAfterCommit(NidSetBI commitSapNids) throws IOException {
        synchronized (uncomittedStatusPathEntries) {
            int min = Integer.MAX_VALUE;

            expandPermit.acquireUninterruptibly();

            try {
                for (int sapNid : commitSapNids.getSetValues()) {
                    min = Math.min(min, sapNid);
                }

                for (int sapNid : uncomittedStatusPathEntries.values()) {
                    min = Math.min(min, sapNid);
                }

                for (int i = min; i < sequence.get(); i++) {
                    mutableArray.commitTimes[i - readOnlyArraySize] = Long.MIN_VALUE;
                    sapToIntMap.put(getStatusNid(i), Long.MIN_VALUE, getAuthorNid(i), getModuleNid(i), getPathNid(i), i);
                }

                uncomittedStatusPathEntries.clear();
            } finally {
                expandPermit.release();
            }
        }

        sync();
    }

    private void checkTimeAndAdd(long startTime, long endTime, IntSet specifiedSapNids, int sapNid) {
        long time = getTime(sapNid);

        if ((time > startTime) && (time <= endTime)) {
            specifiedSapNids.add(sapNid);
        }
    }


    @Override
    public void close() {
        try {
            this.sync();
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }

        super.close();
        initialPosition = -1;
        positionArrayBinder =
                new PositionArrayBinder();
        uncomittedStatusPathEntries.clear();
        setupLatch = new CountDownLatch(1);
        misses = new AtomicInteger(0);
        hits = new AtomicInteger(0);
        currentPaths = null;
        mutableArray = null;
        readOnlyArray = null;
    }

    public IntSet commit(long time) throws IOException {
        IntSet committedSapNids = new IntSet();

        synchronized (uncomittedStatusPathEntries) {
            expandPermit.acquireUninterruptibly();

            try {
                for (int sapNid : uncomittedStatusPathEntries.values()) {
                    mutableArray.commitTimes[sapNid - readOnlyArraySize] = time;
                    sapToIntMap.put(getStatusNid(sapNid), time, getAuthorNid(sapNid), getModuleNid(sapNid), getPathNid(sapNid),  sapNid);
                    committedSapNids.add(sapNid);
                }

                uncomittedStatusPathEntries.clear();
            } finally {
                expandPermit.release();
            }
        }

        sync();

        return committedSapNids;
    }

    @Override
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
            readOnlyArraySize = readOnlyArray.getSize();

            if (mutable.get(null, theKey, theData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
                mutableArray = positionArrayBinder.entryToObject(theData);
            } else {
                mutableArray = new PositionArrays();
            }

            int size = getPositionCount();
            int mutableSize = mutableArray.getSize();

            sequence.set(Math.max(size, 1));
            sapToIntMap = new SapToIntHashMap(sequence.get());

            for (int i = 0; i < readOnlyArraySize; i++) {
                if (readOnlyArray.commitTimes[i] != 0) {
                    sapToIntMap.put(readOnlyArray.statusNids[i], readOnlyArray.commitTimes[i], readOnlyArray.authorNids[i],
                            readOnlyArray.moduleNids[i], readOnlyArray.pathNids[i], i);
                }
            }

            closeReadOnly();

            for (int i = 0; i < mutableSize; i++) {
                int mutableIndex = i + readOnlyArraySize;

                assert i < mutableArray.commitTimes.length :
                        " mutableIndex: " + mutableIndex + " commitTimes.length: "
                        + mutableArray.commitTimes.length;
                assert i < mutableArray.statusNids.length :
                        " mutableIndex: " + mutableIndex + " statusNids.length: " + mutableArray.statusNids.length;
                assert i < mutableArray.pathNids.length :
                        " mutableIndex: " + mutableIndex + " pathNids.length: " + mutableArray.pathNids.length;

                if (mutableArray.commitTimes[i] != 0) {
                    sapToIntMap.put(mutableArray.statusNids[i], mutableArray.commitTimes[i], mutableArray.authorNids[i],
                            mutableArray.moduleNids[i], mutableArray.pathNids[i],  mutableIndex);
                }
            }

            if (size == 0) {
                initialPosition = 1;
            } else if ((readOnlyArray.size > 0) && (readOnlyArray.pathNids[0] == 0)) {
                initialPosition = 1;
            } else if ((readOnlyArray.size == 0) && (mutableArray.pathNids[0] == 0)) {
                initialPosition = 1;
            } else {
                initialPosition = 0;
            }

            setupLatch.countDown();
        } catch (DatabaseException e) {
            throw new IOException(e);
        }
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
        initialPosition = -1;
        currentPaths = null;
        
    }

    @Override
    public void sync() throws IOException {
        expandPermit.acquireUninterruptibly();

        try {
            DatabaseEntry valueEntry = new DatabaseEntry();

            positionArrayBinder.objectToEntry(mutableArray, valueEntry);

            DatabaseEntry theKey = new DatabaseEntry();

            IntegerBinding.intToEntry(0, theKey);
            mutable.put(null, theKey, valueEntry);
            super.sync();
        } finally {
            expandPermit.release();
        }
    }


    //~--- get methods ---------------------------------------------------------
    public int getAuthorNid(int sapNid) {
        if (sapNid < 0) {
            return Integer.MIN_VALUE;
        }

        if (sapNid < readOnlyArraySize) {
            return readOnlyArray.authorNids[sapNid];
        } else {
            return mutableArray.authorNids[sapNid - readOnlyArraySize];
        }
    }
    
    public int getModuleNid(int sapNid) {
        if (sapNid < 0) {
            return Integer.MIN_VALUE;
        }

        if (sapNid < readOnlyArraySize) {
            return readOnlyArray.moduleNids[sapNid];
        } else {
            return mutableArray.moduleNids[sapNid - readOnlyArraySize];
        }
    }

    public static Set<Integer> getCurrentPaths() {
        try {
            currentPaths = Bdb.getPathManager().getPathNids();

            return currentPaths;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getDbName() {
        return "positionDb";
    }

    public static int getInitialPosition() {
        try {
            setupLatch.await();
        } catch (InterruptedException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }

        return initialPosition;
    }

    public int getPathNid(int index) {
        if (index < 0) {
            return Integer.MIN_VALUE;
        }

        if (index < readOnlyArraySize) {
            return readOnlyArray.pathNids[index];
        } else {
            return mutableArray.pathNids[index - readOnlyArraySize];
        }
    }

    public PositionBI getPosition(int sapNid)
            throws IOException, PathNotExistsException, TerminologyException {
        int status;
        int author;
        int pathNid;
        long time;

        if (sapNid < readOnlyArraySize) {
            pathNid = readOnlyArray.pathNids[sapNid];
            time = readOnlyArray.commitTimes[sapNid];
            status = readOnlyArray.statusNids[sapNid];
            author = readOnlyArray.authorNids[sapNid];
        } else {
            int mutableIndex = sapNid - readOnlyArraySize;

            pathNid = mutableArray.pathNids[mutableIndex];
            time = mutableArray.commitTimes[mutableIndex];
            status = mutableArray.statusNids[mutableIndex];
            author = mutableArray.authorNids[mutableIndex];
        }

        if (pathNid == 0) {
            AceLog.getAppLog().severe("readOnly: " + (sapNid < readOnlyArraySize) + " pathNid == 0 "
                    + "sapNid == " + sapNid + " time: " + time + " status == " + status
                    + " author: " + author);
        }

        PathBI path = Bdb.getPathManager().get(pathNid);

        return new Position(time, path);
    }

    public int getPositionCount() {
        return readOnlyArraySize + mutableArray.getSize();
    }

    public int getReadOnlyMax() {
        return readOnlyArray.size - 1;
    }

    public int getSapNid(StatusAuthorPosition tsp) {
        return getSapNid(tsp.getStatusNid(), tsp.getTime(), tsp.getAuthorNid(), tsp.getModuleNid(), tsp.getPathNid() );
    }

    public int getSapNid(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
        if (time == Long.MAX_VALUE) {
            UncommittedStatusForPath usp = new UncommittedStatusForPath(statusNid, authorNid, moduleNid, pathNid);

            if (uncomittedStatusPathEntries.containsKey(usp)) {
                return uncomittedStatusPathEntries.get(usp);
            } else {
                expandPermit.acquireUninterruptibly();

                try {
                    if (uncomittedStatusPathEntries.containsKey(usp)) {
                        return uncomittedStatusPathEntries.get(usp);
                    }

                    int statusAtPositionNid = sequence.getAndIncrement();

                    mutableArray.setSize(statusAtPositionNid - readOnlyArraySize + 1);
                    mutableArray.statusNids[statusAtPositionNid - readOnlyArraySize] = statusNid;
                    mutableArray.authorNids[statusAtPositionNid - readOnlyArraySize] = authorNid;
                    mutableArray.pathNids[statusAtPositionNid - readOnlyArraySize] = pathNid;
                    mutableArray.moduleNids[statusAtPositionNid - readOnlyArraySize] = moduleNid;
                    mutableArray.commitTimes[statusAtPositionNid - readOnlyArraySize] = time;
                    uncomittedStatusPathEntries.put(usp, statusAtPositionNid);
                    hits.incrementAndGet();

                    return statusAtPositionNid;
                } finally {
                    expandPermit.release();
                }
            }
        }

        if (sapToIntMap.containsKey(statusNid, time, authorNid, moduleNid, pathNid)) {
            hits.incrementAndGet();

            return sapToIntMap.get(statusNid, time, authorNid, moduleNid, pathNid);
        }

        expandPermit.acquireUninterruptibly();

        try {

            // Try one last time...
            if (sapToIntMap.containsKey(statusNid, time, authorNid, moduleNid, pathNid)) {
                hits.incrementAndGet();

                return sapToIntMap.get(statusNid, time, authorNid, moduleNid, pathNid);
            }

            int statusAtPositionNid = sequence.getAndIncrement();

            mutableArray.setSize(statusAtPositionNid - readOnlyArraySize + 1);
            mutableArray.statusNids[statusAtPositionNid - readOnlyArraySize] = statusNid;
            mutableArray.authorNids[statusAtPositionNid - readOnlyArraySize] = authorNid;
            mutableArray.pathNids[statusAtPositionNid - readOnlyArraySize] = pathNid;
            mutableArray.moduleNids[statusAtPositionNid - readOnlyArraySize] = moduleNid;
            mutableArray.commitTimes[statusAtPositionNid - readOnlyArraySize] = time;
            sapToIntMap.put(statusNid, time, authorNid, moduleNid, pathNid, statusAtPositionNid);
            misses.incrementAndGet();

            return statusAtPositionNid;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            expandPermit.release();
        }
    }
    /**
    * Iterates all the sap nids in the database and processes them according to
    * the implementation of <code>ProcessStampDataBI</code>.
    * @param processor contains the information about how the sap nids should be
    * processed.
    * @throws Exception 
    */
    public void iterateStampDataInSequence(ProcessStampDataBI processor) throws Exception {
        for (int sap = initialPosition; sap < sequence.get(); sap++) {
            processor.processStampData(new STAMP(sap));
        }
    }

    public IntSet getSpecifiedSapNids(IntSet pathIds, long startTime, long endTime) {
        IntSet specifiedSapNids = new IntSet();
        Collection<Integer> values = sapToIntMap.values();

        if ((pathIds != null) && (pathIds.size() > 0)) {
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
    
    /**
     * 
     * @param allowedStatuses can be null
     * @param startTime required
     * @param endTime required
     * @param authorNid can be null
     * @param moduleNid can be null
     * @param pathNid can be null
     * @return 
     */
    public IntSet getSpecifiedSapNids(IntSet allowedStatuses, long startTime, long endTime, 
            Integer authorNid, IntSet moduleNids, IntSet pathNids) {
        IntSet specifiedSapNids = new IntSet();
        Collection<Integer> values = sapToIntMap.values();
        for (int sapNid : values) {
            boolean passed = true;
            if (allowedStatuses != null) {
                if (!allowedStatuses.contains(getStatusNid(sapNid))) {
                    passed = false;
                }
            }
            if (authorNid != null) {
                if(authorNid != getAuthorNid(sapNid)){
                    passed = false;
                }
            }
            if (moduleNids != null) {
                if(!moduleNids.contains(getModuleNid(sapNid))){
                    passed = false;
                }
            }
            if (pathNids != null) {
                if(!pathNids.contains(getPathNid(sapNid))){
                    passed = false;
                }
            }
            
            if (passed) {
                checkTimeAndAdd(startTime, endTime, specifiedSapNids, sapNid);
            }
        }
        return specifiedSapNids;
    }

    public int getStatusNid(int index) {
        if (index < 0) {
            return Integer.MIN_VALUE;
        }

        if (index < readOnlyArraySize) {
            return readOnlyArray.statusNids[index];
        } else {
            return mutableArray.statusNids[index - readOnlyArraySize];
        }
    }

    public long getTime(int index) {
        if (index == Integer.MAX_VALUE) {
            throw new RuntimeException("index == Integer.MAX_VALUE");
        }

        if (index < 0) {
            return Long.MIN_VALUE;
        }

        if (index < readOnlyArraySize) {
            return readOnlyArray.commitTimes[index];
        } else {
            return mutableArray.commitTimes[index - readOnlyArraySize];
        }
    }

    public List<TimePathId> getTimePathList() {
        HashSet<TimePathId> returnValues = new HashSet<>();
        Collection<Integer> values = sapToIntMap.values();

        for (int sapNid : values) {
            returnValues.add(new TimePathId(getVersion(sapNid), getPathNid(sapNid)));
        }

        return new ArrayList<>(returnValues);
    }

    public int getVersion(int index) {
        if (index < 0) {
            return Integer.MIN_VALUE;
        }

        if (index < readOnlyArraySize) {
            return ThinVersionHelper.convert(readOnlyArray.commitTimes[index]);
        } else {
            return ThinVersionHelper.convert(mutableArray.commitTimes[index - readOnlyArraySize]);
        }
    }

    //~--- inner classes -------------------------------------------------------
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
                pa.moduleNids[i] = input.readInt();
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
                output.writeInt(pa.moduleNids[i]);
                output.writeLong(pa.commitTimes[i]);
            }
        }
    }

    private static class PositionArrays {

        int size = 0;
        int[] authorNids;
        long[] commitTimes;
        int[] pathNids;
        int[] moduleNids;
        int[] statusNids;

        //~--- constructors -----------------------------------------------------
        public PositionArrays() {
            statusNids = new int[MIN_ARRAY_SIZE];
            authorNids = new int[MIN_ARRAY_SIZE];
            pathNids = new int[MIN_ARRAY_SIZE];
            moduleNids = new int[MIN_ARRAY_SIZE];
            commitTimes = new long[MIN_ARRAY_SIZE];
            this.size = 0;
        }

        public PositionArrays(int size) {
            statusNids = new int[size];
            authorNids = new int[size];
            pathNids = new int[size];
            moduleNids = new int[size];
            commitTimes = new long[size];
            this.size = size;
        }

        //~--- methods ----------------------------------------------------------
        private void ensureCapacity(int size) {
            if (size > getCapacity()) {
                int newCapacity = pathNids.length + MIN_ARRAY_SIZE;
                int[] tempStatusNids = new int[newCapacity];

                System.arraycopy(statusNids, 0, tempStatusNids, 0, statusNids.length);
                statusNids = tempStatusNids;

                int[] tempAuthorhNids = new int[newCapacity];

                System.arraycopy(authorNids, 0, tempAuthorhNids, 0, authorNids.length);
                authorNids = tempAuthorhNids;

                int[] tempPathNids = new int[newCapacity];

                System.arraycopy(pathNids, 0, tempPathNids, 0, pathNids.length);
                pathNids = tempPathNids;
                
                int[] tempModuleNids = new int[newCapacity];

                System.arraycopy(moduleNids, 0, tempModuleNids, 0, moduleNids.length);
                moduleNids = tempModuleNids;

                long[] tempCommitTimes = new long[newCapacity];

                System.arraycopy(commitTimes, 0, tempCommitTimes, 0, commitTimes.length);
                commitTimes = tempCommitTimes;
            }
        }

        //~--- get methods ------------------------------------------------------
        private int getCapacity() {
            return pathNids.length;
        }

        private int getSize() {
            return size;
        }

        //~--- set methods ------------------------------------------------------
        private void setSize(int size) {
            this.size = size;
            ensureCapacity(size);
        }
    }
}
