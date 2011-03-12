package org.ihtsdo.db.bdb.computer.version;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.Formatter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbPathManager;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;

/**
 * Assumptions: <br>
 * 1. Each path can participate as an origin only once <br>
 * 2. The version/time element of each position reflects actual calendar time, and
 * future dates can be no greater than 1 year in the future. <br>
 * <br>
 * Thoughts: a. Should the conflict matrix be a sparse matrix? Space vs
 * efficiency tradeoff.
 *
 * @author kec
 *
 */
public class PositionMapper {

    public static final BigInteger BIG_MINUS_ONE = BigInteger.valueOf(-1);
    public static final int INT_MINUS_ONE = -1;

    /**
     * Possible results when comparing two positions with respect to a
     * destination position.
     *
     * @author kec
     *
     */
    public enum RELATIVE_POSITION {

        BEFORE, EQUAL, AFTER, CONTRADICTION, UNREACHABLE
    };
    private static boolean closed = false;

    public static void close() {
        closed = true;
    }

    private static class PositionMapperSetupManager extends Thread {

        public PositionMapperSetupManager() {
            super("PositionMapperSetupManager");
            start();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (closed) {
                        return;
                    }
                    PositionMapper m = mappersToSetup.take();
                    if (closed) {
                        return;
                    }
                    m.setup();
                    //m.setup__int();
                } catch (Throwable e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        }
    }
    
    public class SortableDistance implements Comparable<SortableDistance>{
        public int idx;
        public long distance;

        public SortableDistance(int i, long d) {
            idx = i;
            distance = d;
        }

        @Override
       public int compareTo(SortableDistance o) {
            SortableDistance other = (SortableDistance) o;
            if (this.distance > other.distance) {
                return 1; // this is greater than received
            } else if (this.distance < other.distance) {
                return -1; // this is less than received
            } else {
                return 0; // this == received
            }
        }

    }

    /**
     *
     * @param <T>
     *            the type of part being tested.
     * @param version
     *            the part to be tested to determine if it is on route to the
     *            destination.
     * @return true if the part's position is on the route to the destination of
     *         the class's instance.
     * @throws InterruptedException
     * @throws IOException
     */
    public <V extends ConceptComponent<?, ?>.Version> boolean onRoute(V version) {
        queryCount++;
        if (version.getSapNid() < 0) {
            return false;
        }
        // Forms a barrier to ensure that the setup is complete prior to use
        try {
            completeLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        lastRequestTime = System.currentTimeMillis();
        if (version.getTime() == Long.MAX_VALUE) {
            return true;
        }
        assert version.getSapNid() < positionDistance.length : "sapNid: " + version.getSapNid()
                + " length: " + positionDistance.length + " version: " + version;
        if (version.getTime() < Long.MAX_VALUE) {
            return positionDistance[version.getSapNid()] >= 0;
        } else if (destination.getTime() > System.currentTimeMillis()) {
            return positionDistance[version.getSapNid()] >= 0;
        }
        return false;
    }

    public boolean isSetup() {
        return completeLatch.getCount() == 0;
    }

    /**
     *
     * @param <T> the type of part being tested.
     * @param v1 the first part of the comparison.
     * @param v2 the second part of the comparison.
     * @return the <code>RELATIVE_POSITION</code> of part1 compared to part2
     *         with respect to the destination position of the class's instance.
     * @throws IOException
     * @throws InterruptedException
     */
    public <V extends ConceptComponent<?, ?>.Version> RELATIVE_POSITION relativePosition(V v1, V v2)
            throws IOException {
        queryCount++;
        if (v1.getSapNid() < 0) {
            if (v2.getSapNid() < 0) {
                return RELATIVE_POSITION.EQUAL;
            }
            return RELATIVE_POSITION.BEFORE;
        }
        if (v2.getSapNid() < 0) {
            if (v1.getSapNid() < 0) {
                return RELATIVE_POSITION.EQUAL;
            }
            return RELATIVE_POSITION.AFTER;
        }

        // Forms a barrier to ensure that the setup is complete prior to use
        try {
            completeLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        lastRequestTime = System.currentTimeMillis();
        if (v1.getTime() == Long.MAX_VALUE) {
            if (v2.getTime() == Long.MAX_VALUE) {
                return RELATIVE_POSITION.EQUAL;
            }
            return RELATIVE_POSITION.AFTER;
        } else if (v2.getTime() == Long.MAX_VALUE) {
            return RELATIVE_POSITION.BEFORE;
        }
        if (onRoute(v1) && onRoute(v2)) {
            if (inConflict(v1.getSapNid(), v2.getSapNid())) {
                return RELATIVE_POSITION.CONTRADICTION;
            } else if (positionDistance[v1.getSapNid()]
                    > positionDistance[v2.getSapNid()]) {
                return RELATIVE_POSITION.BEFORE;
            } else if (positionDistance[v1.getSapNid()]
                    < positionDistance[v2.getSapNid()]) {
                return RELATIVE_POSITION.AFTER;
            } else if (positionDistance[v1.getSapNid()]
                    == positionDistance[v2.getSapNid()]) {
                if (v1.getAuthorNid() != v2.getAuthorNid()) {
                    return RELATIVE_POSITION.CONTRADICTION;
                }
                return RELATIVE_POSITION.EQUAL;
            }
        }
        return RELATIVE_POSITION.UNREACHABLE;
    }

    /**
     * Bypasses the onRoute test of <code>relativePosition</code>
     * @param <T>
     * @param v1 the first part of the comparison.
     * @param v2 the second part of the comparison.
     * @return the <code>RELATIVE_POSITION</code> of part1 compared to part2
     *         with respect to the destination position of the class's instance.
     * @throws IOException
     */
    public <V extends ConceptComponent<?, ?>.Version> RELATIVE_POSITION fastRelativePosition(V part1, V part2, Precedence precedencePolicy) {
        queryCount++;
        lastRequestTime = System.currentTimeMillis();
        // Forms a barrier to ensure that the setup is complete prior to use
        try {
            completeLatch.await();
            assert part1.getSapNid() < conflictMatrix.length :
                    "SapNid: " + part1.getSapNid() + " out of range; "
                    + " rows: " + conflictMatrix.length
                    + " columns: " + conflictMatrix.length
                    + " time: " + new Date(Bdb.getSapDb().getTime(part1.getSapNid()))
                    + " status: " + Concept.get(Bdb.getSapDb().getStatusNid(part1.getSapNid()))
                    + " path: " + Concept.get(Bdb.getSapDb().getPathNid(part1.getSapNid()))
                    + " destination: " + destination + " latch: " + completeLatch.getCount()
                    + " positionCount: " + positionCount;
            assert part2.getSapNid() < conflictMatrix.length :
                    "SapNid: " + part2.getSapNid() + " out of range; "
                    + " rows: " + conflictMatrix.length
                    + " columns: " + conflictMatrix.length
                    + " time: " + new Date(Bdb.getSapDb().getTime(part2.getSapNid()))
                    + " status: " + Concept.get(Bdb.getSapDb().getStatusNid(part2.getSapNid()))
                    + " path: " + Concept.get(Bdb.getSapDb().getPathNid(part2.getSapNid()))
                    + " destination: " + destination + " latch: " + completeLatch.getCount()
                    + " positionCount: " + positionCount;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        switch (precedencePolicy) {
            case PATH:
                if (inConflict(part1.getSapNid(), part2.getSapNid())) {
                    return RELATIVE_POSITION.CONTRADICTION;
                } else if (positionDistance[part1.getSapNid()]
                        > positionDistance[part2.getSapNid()]) {
                    return RELATIVE_POSITION.BEFORE;
                } else if (positionDistance[part1.getSapNid()]
                        < positionDistance[part2.getSapNid()]) {
                    return RELATIVE_POSITION.AFTER;
                }
                if (part1.getAuthorNid() != part2.getAuthorNid()) {
                    return RELATIVE_POSITION.CONTRADICTION;
                }
                return RELATIVE_POSITION.EQUAL;
            case TIME:
                if (part1.getTime() == part2.getTime()) {
                    if (positionDistance[part1.getSapNid()]
                            > positionDistance[part2.getSapNid()]) {
                        return RELATIVE_POSITION.BEFORE;
                    } else if (positionDistance[part1.getSapNid()]
                            < positionDistance[part2.getSapNid()]) {
                        return RELATIVE_POSITION.AFTER;
                    }
                    return RELATIVE_POSITION.CONTRADICTION;
                } else if (part1.getTime() < part2.getTime()) {
                    return RELATIVE_POSITION.BEFORE;
                }
                return RELATIVE_POSITION.AFTER;
            default:
                throw new RuntimeException("Can't handle policy: " + precedencePolicy);
        }
    }
    /**
     * A bit matrix of the combinations of position identifiers that are
     * unreachable from each other. Hence, these combinations result in a
     * conflict.
     */
    private BitSet[] conflictMatrix;
    /**
     * The position this class uses to determining if another position is on
     * route to this <code>destination</code>, and to also determine the
     * relative location of positions on that route to one another.
     */
    private PositionBI destination;
    /**
     * An array with an index of position identifiers, and values that
     * correspond to the distance of the indexed position from the
     * <code>destination</code>. A value of -1 indicates that the indexed
     * position is not on a route that leads to the destination.
     */
    private int[] positionDistance;
    private static BdbPathManager pathManager;
    private static LinkedBlockingQueue<PositionMapper> mappersToSetup = new LinkedBlockingQueue<PositionMapper>();
    @SuppressWarnings("unused")
    private static PositionMapperSetupManager setupManager = new PositionMapperSetupManager();
    /**
     * Only need an approximate query count, so no need to incur
     * AtomicInt overhead.
     */
    private int queryCount = 0;
    private long lastRequestTime = System.currentTimeMillis();
    private CountDownLatch completeLatch = new CountDownLatch(1);
    private Lock writeLock = new ReentrantLock();
    private int positionCount = -1;

    public void queueForSetup() {
        mappersToSetup.add(this);
    }

    private void setup() throws IOException, PathNotExistsException,
            TerminologyException {
        if (Bdb.getConceptDb() == null) {
            return;
        }
        Concept pathConcept = Bdb.getConceptDb().getConcept(destination.getPath().getConceptNid());
        String pathDesc = pathConcept.getPrimUuid().toString();
        if (pathConcept.getDescriptions() != null && pathConcept.getDescriptions().size() > 0) {
            pathDesc = pathConcept.getDescriptions().iterator().next().getText();
        }
        writeLock.lock();
        if (completeLatch.getCount() == 1) {
            try {
                if (pathManager == null) {
                    pathManager = BdbPathManager.get();
                }
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine(
                            "Creating new PositionMapper for: "
                            + pathConcept.getNid() + ": "
                            + pathDesc + " time: "
                            + TimeHelper.formatDate(destination.getTime())
                            + " thread: " + Thread.currentThread().getName());
                }
                Collection<PositionBI> origins =
                        pathManager.getAllPathOrigins(destination.getPath().getConceptNid());
                origins.add(this.destination);

                // Map of the origin position's path id, to the origin position... See
                // assumption 1.
                Map<Integer, PositionBI> originMap = new TreeMap<Integer, PositionBI>();

                // Map of the origin position's path to it's 'depth' (how many origins
                // below the destination it is)
                TreeMap<Integer, BigInteger> depthMap = new TreeMap<Integer, BigInteger>();

                // Map of the origin's position path to the set of paths that precede it
                // (including itself).
                TreeMap<Integer, Set<Integer>> precedingPathIdMap = new TreeMap<Integer, Set<Integer>>();
                for (PositionBI o : origins) {
                    originMap.put(o.getPath().getConceptNid(), o);
                    depthMap.put(o.getPath().getConceptNid(),
                            getDepth(o, destination, 1));
                    precedingPathIdMap.put(o.getPath().getConceptNid(),
                            getPreceedingPathSet(o));
                }

                BigInteger timeUpperBound = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TEN);

                positionCount = Bdb.getSapDb().getPositionCount();
                positionDistance = new int[positionCount];
                Arrays.fill(positionDistance, Integer.MIN_VALUE);
                BigInteger[] positionComputedDistance = new BigInteger[positionCount];
                Arrays.fill(positionComputedDistance, BIG_MINUS_ONE);
                conflictMatrix = new BitSet[positionCount];
                for (int p1index = 0; p1index < positionCount; p1index++) {
                    try {
                        PositionBI p1 = Bdb.getSapDb().getPosition(p1index);
                        Integer p1pathId = p1.getPath().getConceptNid();
                        Set<Integer> precedingPathIdSet = precedingPathIdMap.get(p1pathId);
                        // see if position may be in route to the destination
                        if (originMap.containsKey(p1pathId)) {
                            // compute the distance to the destination
                            BigInteger pathDepth = depthMap.get(p1.getPath().getConceptNid());

                            if (destination.getPath().getConceptNid() == p1.getPath().getConceptNid()) {
                                // On the same path as the destination...
                                if (p1.getTime() <= destination.getTime()) {
                                    positionComputedDistance[p1index] = timeUpperBound.subtract(BigInteger.valueOf(p1.getTime()));
                                } else {
                                    conflictMatrix[p1index] = null;
                                }
                                conflictMatrix[p1index] = null;
                            } else {
                                // On a different path than the destination
                                // compute the distance to the destination
                                positionComputedDistance[p1index] = timeUpperBound.multiply(pathDepth).subtract(
                                        BigInteger.valueOf(p1.getTime()));

                                // iterate to compute conflicts...
                                for (int p2index = 0; p2index < positionCount; p2index++) {
                                    PositionBI p2 = Bdb.getSapDb().getPosition(p2index);
                                    Integer p2pathId = p2.getPath().getConceptNid();
                                    if (originMap.containsKey(p2pathId)
                                            && p2.getTime() <= originMap.get(p2pathId).getTime()) {
                                        Set<Integer> p2PrecedingPathIdSet = precedingPathIdMap.get(p2pathId);
                                        if (precedingPathIdSet.contains(p2pathId) || p2PrecedingPathIdSet.contains(p1pathId)) {
                                            if (conflictMatrix[p1index] != null) {
                                                // technically not required as default is to false.
                                                conflictMatrix[p1index].set(p2index, false);
                                            }
                                            if (conflictMatrix[p2index] != null) {
                                                // technically not required as default is to false.
                                                conflictMatrix[p2index].set(p1index, false);
                                            }
                                        } else {
                                            if (p1index < p2index) {
                                                if (conflictMatrix[p1index] == null) {
                                                    conflictMatrix[p1index] = new BitSet(positionCount);
                                                }
                                                conflictMatrix[p1index].set(p2index, true);
                                            } else {
                                                if (conflictMatrix[p2index] == null) {
                                                    conflictMatrix[p2index] = new BitSet(positionCount);
                                                }
                                                conflictMatrix[p2index].set(p1index, true);
                                            }
                                        }
                                    } else {
                                        conflictMatrix[p1index] = null;
                                    }
                                }
                            }
                        } else {
                            conflictMatrix[p1index] = null;
                        }
                    } catch (Exception e) {
                        AceLog.getAppLog().alertAndLogException(e);
                    }

                }

                // Copy positionComputedDistance to positionDistance.
                // Step 1: sort

                TreeSet<BigInteger> sortedPositionComputedDistanceTreeSet = new TreeSet<BigInteger>(
                        Arrays.asList(positionComputedDistance));
                BigInteger[] sortedPositionComputedDistance = sortedPositionComputedDistanceTreeSet.toArray(new BigInteger[sortedPositionComputedDistanceTreeSet.size()]);

                // Step 2: copy: if neg, distance = -1 if positive, distance = sort
                // sequence.
                for (int pid = 0; pid < positionCount; pid++) {
                    if (positionComputedDistance[pid].compareTo(BigInteger.ZERO) < 0) {
                        positionDistance[pid] = -1;
                    } else {
                        positionDistance[pid] = Arrays.binarySearch(
                                sortedPositionComputedDistance,
                                positionComputedDistance[pid]);
                    }
                }
                completeLatch.countDown();
                AceLog.getAppLog().info(
                        "Finished setup for new PositionMapper for: "
                        + pathConcept.getNid() + ": "
                        + pathDesc + " time: "
                        + TimeHelper.formatDate(destination.getTime())
                        + " thread: " + Thread.currentThread().getName());
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else {
            AceLog.getAppLog().info(
                    "Suppressed reinitilization of PositionMapper for: "
                    + pathConcept.getNid() + ": "
                    + pathDesc + " time: "
                    + destination.getTime()
                    + " thread: " + Thread.currentThread().getName());

        }
        writeLock.unlock();
    }
    
    private void setup__int() throws IOException, PathNotExistsException, TerminologyException {
        if (Bdb.getConceptDb() == null) {
            return;
        }
        Concept pathConcept = Bdb.getConceptDb().getConcept(destination.getPath().getConceptNid());
        String pathDesc = pathConcept.getPrimUuid().toString();
        if (pathConcept.getDescriptions() != null && pathConcept.getDescriptions().size() > 0) {
            pathDesc = pathConcept.getDescriptions().iterator().next().getText();
        }
        writeLock.lock();
        if (completeLatch.getCount() == 1) {
            try {
                if (pathManager == null) {
                    pathManager = BdbPathManager.get();
                }
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine(
                            "Creating new PositionMapper for: " + pathConcept.getNid() + ": "
                                    + pathDesc + " time: "
                                    + TimeHelper.formatDate(destination.getTime()) + " thread: "
                                    + Thread.currentThread().getName());
                }
                Collection<PositionBI> origins = pathManager.getAllPathOrigins(destination
                        .getPath().getConceptNid());
                origins.add(this.destination);

                // Map of the origin position's path id, to the origin position... See
                // assumption 1.
                Map<Integer, PositionBI> originMap = new TreeMap<Integer, PositionBI>();

                // Map of the origin position's path to it's 'depth' (how many origins
                // below the destination it is)
                TreeMap<Integer, Integer> depthMap = new TreeMap<Integer, Integer>();

                // Map of the origin's position path to the set of paths that precede it
                // (including itself).
                TreeMap<Integer, Set<Integer>> precedingPathIdMap = new TreeMap<Integer, Set<Integer>>();
                for (PositionBI o : origins) {
                    originMap.put(o.getPath().getConceptNid(), o);
                    if (depthMap.containsKey(o.getPath().getConceptNid()) == false)
                        depthMap.put(o.getPath().getConceptNid(), getDepth__int(o, destination, 1));
                    precedingPathIdMap.put(o.getPath().getConceptNid(), getPreceedingPathSet(o));
                }

                positionCount = Bdb.getSapDb().getPositionCount();

                // COMPUTE timeMax, timeMin, timeDelta
                long[] unscaledTimeData = new long[positionCount];
                for (int pIdx = 0; pIdx < positionCount; pIdx++)
                    unscaledTimeData[pIdx] = Bdb.getSapDb().getPosition(pIdx).getTime();
                Arrays.sort(unscaledTimeData);

                long timeMax = unscaledTimeData[positionCount - 1];
                long timeMin = unscaledTimeData[0];
                long timeDelta = Long.MAX_VALUE;

                for (int i = 0; i < positionCount - 1; i++) {
                    long delta = unscaledTimeData[i + 1] - unscaledTimeData[i];
                    if (delta < timeDelta)
                        timeDelta = delta;
                }

                if (timeDelta >= 4)
                    timeDelta = timeDelta / 2; // over sample to insure unique "time slot" after scaling
                else if (timeDelta == 0)
                    timeDelta = 1;
                unscaledTimeData = null;

                // COMPUTE DISTANCES
                long timeUpperBound = ((timeMax - timeMin) * 4) / timeDelta;

                AceLog.getAppLog().info(
                        "::: PositionMapper " + "\ttimeMax=" + timeMax + "\ttimeMin=" + timeMin
                                + "\ttimeDelta=" + timeDelta + "\ttimeUpperBound="
                                + timeUpperBound);

                positionDistance = new int[positionCount];
                Arrays.fill(positionDistance, Integer.MIN_VALUE);
                long[] positionComputedDistance__long = new long[positionCount];
                Arrays.fill(positionComputedDistance__long, INT_MINUS_ONE);
                conflictMatrix = new BitSet[positionCount];
                for (int p1index = 0; p1index < positionCount; p1index++) {
                    try {
                        PositionBI p1 = Bdb.getSapDb().getPosition(p1index);
                        Integer p1pathId = p1.getPath().getConceptNid();
                        Set<Integer> precedingPathIdSet = precedingPathIdMap.get(p1pathId);
                        // see if position may be in route to the destination
                        if (originMap.containsKey(p1pathId)) {
                            // compute the distance to the destination
                            int pathDepth = depthMap.get(p1.getPath().getConceptNid());

                            if (destination.getPath().getConceptNid() == p1.getPath()
                                    .getConceptNid()) {
                                // On the same path as the destination...
                                if (p1.getTime() <= destination.getTime()) {
                                    long scaledTime = (p1.getTime() - timeMin) / timeDelta;
                                    positionComputedDistance__long[p1index] = timeUpperBound - scaledTime;
                                } else {
                                    conflictMatrix[p1index] = null;
                                }
                                conflictMatrix[p1index] = null;
                            } else {
                                // On a different path than the destination
                                // compute the distance to the destination
                                long scaledTime = (p1.getTime() - timeMin) / timeDelta;
                                positionComputedDistance__long[p1index] = (timeUpperBound * pathDepth)
                                - scaledTime;

                                // iterate to compute conflicts...
                                for (int p2index = 0; p2index < positionCount; p2index++) {
                                    PositionBI p2 = Bdb.getSapDb().getPosition(p2index);
                                    Integer p2pathId = p2.getPath().getConceptNid();
                                    if (originMap.containsKey(p2pathId)
                                            && p2.getTime() <= originMap.get(p2pathId).getTime()) {
                                        Set<Integer> p2PrecedingPathIdSet = precedingPathIdMap
                                                .get(p2pathId);
                                        if (precedingPathIdSet.contains(p2pathId)
                                                || p2PrecedingPathIdSet.contains(p1pathId)) {
                                            if (conflictMatrix[p1index] != null) {
                                                // technically not required as default is to false.
                                                conflictMatrix[p1index].set(p2index, false);
                                            }
                                            if (conflictMatrix[p2index] != null) {
                                                // technically not required as default is to false.
                                                conflictMatrix[p2index].set(p1index, false);
                                            }
                                        } else {
                                            if (p1index < p2index) {
                                                if (conflictMatrix[p1index] == null) {
                                                    conflictMatrix[p1index] = new BitSet(
                                                            positionCount);
                                                }
                                                conflictMatrix[p1index].set(p2index, true);
                                            } else {
                                                if (conflictMatrix[p2index] == null) {
                                                    conflictMatrix[p2index] = new BitSet(
                                                            positionCount);
                                                }
                                                conflictMatrix[p2index].set(p1index, true);
                                            }
                                        }
                                    } else {
                                        conflictMatrix[p1index] = null;
                                    }
                                }
                            }
                        } else {
                            conflictMatrix[p1index] = null;
                        }
                    } catch (Exception e) {
                        AceLog.getAppLog().alertAndLogException(e);
                    }

                }

                /*
                // STEP 1: SORT DISTANCES
                SortableDistance[] distSortArray = new SortableDistance[positionCount];
                for (int p1index = 0; p1index < positionCount; p1index++)
                    distSortArray[p1index] = new SortableDistance(p1index,
                            positionComputedDistance__long[p1index]);
                Arrays.sort(distSortArray);

                // STEP 2:ASSIGN RELATIVE DISTANCES
                for (int pid = 0; pid < positionCount; pid++) {
                    SortableDistance sd = distSortArray[pid];
                    if (sd.distance < 0)
                        positionDistance[sd.idx] = -1;
                    else
                        positionDistance[sd.idx] = pid;
                }
                */
                
                // STEP 1: SORT DISTANCES
                long[] positionComputedDistanceSorted__long = new long[positionCount];
                System.arraycopy(positionComputedDistance__long, 0, positionComputedDistanceSorted__long, 0, positionCount);
                Arrays.sort(positionComputedDistanceSorted__long);

                // STEP 2:ASSIGN RELATIVE DISTANCES
                for (int pid = 0; pid < positionCount; pid++) {
                    if (positionComputedDistance__long[pid] < 0)
                        positionDistance[pid] = -1;
                    else
                        positionDistance[pid] = Arrays.binarySearch(positionComputedDistanceSorted__long,
                                positionComputedDistance__long[pid]);
                }

                completeLatch.countDown();
                AceLog.getAppLog().info(
                        "Finished setup for new PositionMapper for: " + pathConcept.getNid() + ": "
                                + pathDesc + " time: " + TimeHelper.formatDate(destination.getTime())
                                + " thread: " + Thread.currentThread().getName());
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else {
            AceLog.getAppLog().info(
                    "Suppressed reinitilization of PositionMapper for: " + pathConcept.getNid()
                            + ": " + pathDesc + " time: " + destination.getTime() + " thread: "
                            + Thread.currentThread().getName());

        }
        writeLock.unlock();
    }

    /**
     *
     * @param path
     *            the path to get all the preceding paths for.
     * @return the set of all path identifiers that precede this path
     */
    private Set<Integer> getPreceedingPathSet(PositionBI path) {
        return getPreceedingPathSetRecursion(path, new TreeSet<Integer>());
    }

    /**
     * Recursive call to support getting the preceding path set.
     *
     * @param path
     * @param preceedingPaths
     * @return the set of all path identifiers that precede this path
     */
    private Set<Integer> getPreceedingPathSetRecursion(PositionBI path,
            Set<Integer> preceedingPaths) {
        preceedingPaths.add(path.getPath().getConceptNid());
        for (PositionBI origin : path.getPath().getOrigins()) {
            getPreceedingPathSetRecursion(origin, preceedingPaths);
        }
        return preceedingPaths;
    }

    /**
     *
     * @param testPath
     *            the path to determine the depth of.
     * @param depthFinder
     *            the position to test for equality with the test path, or to
     *            provide any origins for further testing
     * @param depthSeed
     *            seed value that is incremented with each recursive call to
     *            compute the depth.
     * @return the depth of the testPath with respect to the destination
     *         specified by the instance of this class.
     */
    private BigInteger getDepth(PositionBI testPath, PositionBI depthFinder,
            int depthSeed) {
        if (testPath.getPath().getConceptNid() == depthFinder.getPath().getConceptNid()) {
            return BigInteger.valueOf(depthSeed);
        }
        for (PositionBI child : depthFinder.getPath().getOrigins()) {
            BigInteger depth = getDepth(testPath, child, depthSeed + 1);
            if (depth.compareTo(BigInteger.ZERO) > 0) {
                return depth;
            }
        }
        return BIG_MINUS_ONE; // not on path
    }

    private Integer getDepth__int(PositionBI testPath, PositionBI depthFinder,
            int depthSeed) {
        if (testPath.getPath().getConceptNid() == depthFinder.getPath().getConceptNid()) {
            return depthSeed;
        }
        for (PositionBI child : depthFinder.getPath().getOrigins()) {
             int depth = getDepth__int(testPath, child, depthSeed + 1);
            if (depth > 0) {
                return depth;
            }
        }
        return INT_MINUS_ONE; // not on path
    }

    /**
     *
     * @param position
     *            specifies the destination that this
     *            <class>PositionMapper</class> is constructed for.
     * @throws IOException
     * @throws TerminologyException
     * @throws PathNotExistsException
     */
    public PositionMapper(PositionBI destination) {
        this.destination = destination;
    }

    public PositionBI getDestination() {
        return destination;
    }
    int lengthToPrint = 150;

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        Formatter f = new Formatter(buf);
        buf.append(this.getClass().getSimpleName()).append(": ");
        buf.append(" destination:");
        buf.append(destination);
        buf.append("\nsapNid|distance|time|path|status\n");
        for (int i = 0; i < lengthToPrint && i < positionDistance.length; i++) {
            f.format("%1$2d|", i); // sapNid
            f.format("%1$2d|", positionDistance[i]); // distance
            try {
                buf.append(Revision.fileDateFormat.format( // time
                        new Date(Bdb.getSapDb().getPosition(i).getTime())));
                buf.append("|");
                buf.append(Bdb.getConceptDb().getConcept( // path
                        Bdb.getSapDb().getPathNid(i)));
                buf.append("|");
                buf.append(Bdb.getConceptDb().getConcept( // status
                        Bdb.getSapDb().getStatusNid(i)));
            } catch (PathNotExistsException e) {
                buf.append(e.getLocalizedMessage());
            } catch (IOException e) {
                buf.append(e.getLocalizedMessage());
            } catch (TerminologyException e) {
                buf.append(e.getLocalizedMessage());
            }
            buf.append("\n");
        }
        buf.append("\nconflict matrix: \n");
        buf.append("   ");

        for (int i = 0; i < lengthToPrint && i < positionDistance.length; i++) {
            f.format("%1$2d ", i);
        }
        buf.append("\n");
        for (int i = 0; i < lengthToPrint && i < positionDistance.length; i++) {
            f.format("%1$2d ", i);
            for (int j = 0; j < lengthToPrint && j < positionDistance.length; j++) {
                buf.append(" ");
                buf.append(Boolean.toString(inConflict(i, j)).charAt(0));
                buf.append(" ");
            }
            buf.append("\n");
        }
        return buf.toString();
    }

    public int getQueryCount() {
        return queryCount;
    }

    public long getLastRequestTime() {
        return lastRequestTime;
    }

    private boolean inConflict(int sap1, int sap2) {
        if (sap1 < sap2) {
            if (conflictMatrix[sap1] != null) {
                return conflictMatrix[sap1].get(sap2);
            }
            return false;
        }
        if (conflictMatrix[sap2] != null) {
            return conflictMatrix[sap2].get(sap1);
        }
        return false;

    }
}
