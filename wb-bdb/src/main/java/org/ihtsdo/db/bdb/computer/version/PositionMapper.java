package org.ihtsdo.db.bdb.computer.version;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
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

import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbPathManager;
import org.ihtsdo.time.TimeUtil;

import cern.colt.bitvector.BitMatrix;

/**
 * Assumptions: 1. Each path can participate as an origin only once 2. The
 * version/time element of each position reflects actual calendar time, and
 * future dates can be no greater than 1 year in the future.
 * 
 * Thoughts: a. Should the conflict matrix be a sparse matrix? Space vs
 * efficiency tradeoff.
 * 
 * @author kec
 * 
 */
public class PositionMapper {
	
	public static final BigInteger BIG_MINUS_ONE = BigInteger.valueOf(-1); 

	/**
	 * Possible results when comparing two positions with respect to a
	 * destination position.
	 * 
	 * @author kec
	 * 
	 */
	public enum RELATIVE_POSITION {
		BEFORE, EQUAL, AFTER, CONFLICTING, UNREACHABLE
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
				} catch (Throwable e) {
					AceLog.getAppLog().alertAndLogException(e);
				} 
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
	public <V extends ConceptComponent<?, ?>.Version> boolean onRoute(V version)  {
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
		return positionDistance[version.getSapNid()] >= 0;
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
	public <V extends ConceptComponent<?, ?>.Version> RELATIVE_POSITION 
		relativePosition(V v1, V v2)
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
			if (conflictMatrix.get(v1.getSapNid(), v2.getSapNid())) {
				return RELATIVE_POSITION.CONFLICTING;
			} else if (positionDistance[v1.getSapNid()] > 
					positionDistance[v2.getSapNid()]) {
				return RELATIVE_POSITION.BEFORE;
			} else if (positionDistance[v1.getSapNid()] < 
					positionDistance[v2.getSapNid()]) {
				return RELATIVE_POSITION.AFTER;
			} else if (positionDistance[v1.getSapNid()] == 
				positionDistance[v2.getSapNid()]) {
				return RELATIVE_POSITION.EQUAL;
			}
		}
		return RELATIVE_POSITION.UNREACHABLE;
	}
	/**
	 * Bypasses the onRoute test of <code>relativePosition</code>
	 * @param <T>
	 * @param part1
	 * @param part2
	 * @return
	 * @throws IOException
	 */
	public <V extends ConceptComponent<?, ?>.Version> RELATIVE_POSITION 
	  fastRelativePosition(V part1, V part2) {
		queryCount++;
		lastRequestTime = System.currentTimeMillis();
		// Forms a barrier to ensure that the setup is complete prior to use
		try {
			completeLatch.await();
			assert part1.getSapNid() < conflictMatrix.rows():
				"SapNid: " + part1.getSapNid() + " out of range; " + 
				  " rows: " + conflictMatrix.rows() + 
				  " columns: " + conflictMatrix.columns() +
				  " time: " + new Date(Bdb.getSapDb().getTime(part1.getSapNid())) +
				  " status: " + Concept.get(Bdb.getSapDb().getStatusId(part1.getSapNid())) +
				  " path: " + Concept.get(Bdb.getSapDb().getPathId(part1.getSapNid())) + 
				  " destination: " + destination + " latch: " + completeLatch.getCount() +
				  " positionCount: " + positionCount;
			assert part2.getSapNid() < conflictMatrix.rows():
				"SapNid: " + part2.getSapNid() + " out of range; " + 
				  " rows: " + conflictMatrix.rows() + 
				  " columns: " + conflictMatrix.columns() + 
				  " time: " + new Date(Bdb.getSapDb().getTime(part2.getSapNid())) +
				  " status: " + Concept.get(Bdb.getSapDb().getStatusId(part2.getSapNid())) +
				  " path: " + Concept.get(Bdb.getSapDb().getPathId(part2.getSapNid())) + 
				  " destination: " + destination + " latch: " + completeLatch.getCount() +
				  " positionCount: " + positionCount;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		if (conflictMatrix.get(part1.getSapNid(), part2.getSapNid())) {
			return RELATIVE_POSITION.CONFLICTING;
		} else if (positionDistance[part1.getSapNid()] > 
			positionDistance[part2.getSapNid()]) {
			return RELATIVE_POSITION.BEFORE;
		} else if (positionDistance[part1.getSapNid()] < 
			positionDistance[part2.getSapNid()]) {
			return RELATIVE_POSITION.AFTER;
		} 
		return RELATIVE_POSITION.EQUAL;
	}

	/**
	 * A bit matrix of the combinations of position identifiers that are
	 * unreachable from each other. Hence, these combinations result in a
	 * conflict.
	 */
	private BitMatrix conflictMatrix;

	/**
	 * The position this class uses to determining if another position is on
	 * route to this <code>destination</code>, and to also determine the
	 * relative location of positions on that route to one another.
	 */
	private I_Position destination;
	
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
		Concept pathConcept = Bdb.getConceptDb().getConcept(destination.getPath().getConceptId());
		String pathDesc = pathConcept.getDescriptions().iterator().next().getText();
		writeLock.lock();
		if (completeLatch.getCount() == 1) {
			try {
				if (pathManager == null) {
					pathManager = new BdbPathManager();
				}
				AceLog.getAppLog().info(
						"Creating new PositionMapper for: "
								+ pathConcept.getNid() + ": " 
								+ pathDesc + " time: "
								+ TimeUtil.formatDate(destination.getTime()) + 
								" thread: " + Thread.currentThread().getName());
				Collection<I_Position> origins = 
					pathManager.getAllPathOrigins(destination.getPath().getConceptId());
				origins.add(this.destination);

				// Map of the origin position's path id, to the origin position... See
				// assumption 1.
				Map<Integer, I_Position> originMap = new TreeMap<Integer, I_Position>();

				// Map of the origin position's path to it's 'depth' (how many origins
				// below the destination it is)
				TreeMap<Integer, BigInteger> depthMap = new TreeMap<Integer, BigInteger>();

				// Map of the origin's position path to the set of paths that precede it
				// (including itself).
				TreeMap<Integer, Set<Integer>> precedingPathIdMap = new TreeMap<Integer, Set<Integer>>();
				for (I_Position o : origins) {
					originMap.put(o.getPath().getConceptId(), o);
					depthMap.put(o.getPath().getConceptId(),
							getDepth(o, destination, 0));
					precedingPathIdMap.put(o.getPath().getConceptId(),
							getPreceedingPathSet(o));
				}

				BigInteger timeUpperBound = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TEN);

				positionCount  = Bdb.getSapDb().getPositionCount();
				positionDistance = new int[positionCount];
				Arrays.fill(positionDistance, Integer.MIN_VALUE);
				BigInteger[] positionComputedDistance = new BigInteger[positionCount];
				Arrays.fill(positionComputedDistance, BIG_MINUS_ONE);
				conflictMatrix = new BitMatrix(positionCount, positionCount);
				for (int p1index = 0; p1index < positionCount; p1index++) {
					I_Position p1 = Bdb.getSapDb().getPosition(p1index);
					Integer p1pathId = p1.getPath().getConceptId();
					Set<Integer> precedingPathIdSet = precedingPathIdMap.get(p1pathId);
					// see if position may be in route to the destination
					if (originMap.containsKey(p1pathId)) {
						// compute the distance to the destination
						BigInteger pathDepth = depthMap
								.get(p1.getPath().getConceptId());

						if (destination.getPath().getConceptId() == p1.getPath()
								.getConceptId()) {
							// On the same path as the destination...
							if (p1.getTime() <= destination.getTime()) {
								positionComputedDistance[p1index] = BigInteger
										.valueOf(destination.getTime() - p1.getTime());
							} else {
								positionNotReachable(positionComputedDistance, p1index);
							}
							for (int p2index = 0; p2index < positionCount; p2index++) {
								// no conflicts with any position
								conflictMatrix.putQuick(p1index, p2index, false);
							}
						} else {
							// On a different path than the destination
							// compute the distance to the destination
							positionComputedDistance[p1index] = BigInteger.valueOf(
									p1.getTime()).add(
									timeUpperBound.multiply(pathDepth));

							// iterate to compute conflicts...
							for (int p2index = 0; p2index < positionCount; p2index++) {
								I_Position p2 = Bdb.getSapDb()
										.getPosition(p2index);
								Integer p2pathId = p2.getPath().getConceptId();
								if (originMap.containsKey(p2pathId)
										&& p2.getTime() <= originMap.get(p2pathId)
												.getTime()) {
								    Set<Integer> p2PrecedingPathIdSet = precedingPathIdMap.get(p2pathId);
									if (precedingPathIdSet.contains(p2pathId) || p2PrecedingPathIdSet.contains(p1pathId)) {
										conflictMatrix
												.putQuick(p1index, p2index, false);
									} else {
										conflictMatrix.putQuick(p1index, p2index, true);
									}
								} else {
									positionNotReachable(positionComputedDistance,
											p1index);
								}
							}
						}
					} else {
						positionNotReachable(positionComputedDistance, p1index);
					}
					
				}

				// Copy positionComputedDistance to positionDistance.
				// Step 1: sort

				TreeSet<BigInteger> sortedPositionComputedDistanceTreeSet = new TreeSet<BigInteger>(
						Arrays.asList(positionComputedDistance));
				BigInteger[] sortedPositionComputedDistance = sortedPositionComputedDistanceTreeSet
						.toArray(new BigInteger[sortedPositionComputedDistanceTreeSet
								.size()]);

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
								+ TimeUtil.formatDate(destination.getTime()) + 
								" thread: " + Thread.currentThread().getName());
			} catch (Throwable e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} else {
			AceLog.getAppLog().info(
					"Suppressed reinitilization of PositionMapper for: "
							+ pathConcept.getNid() + ": " 
							+ pathDesc + " time: "
							+ destination.getTime() + 
							" thread: " + Thread.currentThread().getName());

		}
		writeLock.unlock();
	}

	/**
	 * Set the data structures to indicate that the position is not reachable.
	 * 
	 * @param positionCount
	 * @param positionComputedDistance
	 *            array to put the distance into
	 * @param positionIndex
	 *            index of the path that is not reachable
	 */
	private void positionNotReachable(BigInteger[] positionComputedDistance,
			int positionIndex) {
		for (int p2index = 0; p2index < positionComputedDistance.length; p2index++) {
			// No conflicts can arise from an unreachable path
			conflictMatrix.putQuick(positionIndex, p2index, false);
		}
	}

	/**
	 * 
	 * @param path
	 *            the path to get all the preceding paths for.
	 * @return the set of all path identifiers that precede this path
	 */
	private Set<Integer> getPreceedingPathSet(I_Position path) {
		return getPreceedingPathSetRecursion(path, new TreeSet<Integer>());
	}

	/**
	 * Recursive call to support getting the preceding path set.
	 * 
	 * @param path
	 * @param preceedingPaths
	 * @return the set of all path identifiers that precede this path
	 */
	private Set<Integer> getPreceedingPathSetRecursion(I_Position path,
			Set<Integer> preceedingPaths) {
		preceedingPaths.add(path.getPath().getConceptId());
		for (I_Position origin : path.getPath().getOrigins()) {
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
	private BigInteger getDepth(I_Position testPath, I_Position depthFinder,
			int depthSeed) {
		if (testPath.getPath().getConceptId() == depthFinder.getPath()
				.getConceptId()) {
			return BigInteger.valueOf(depthSeed);
		}
		for (I_Position child : depthFinder.getPath().getOrigins()) {
			BigInteger depth = getDepth(testPath, child, depthSeed + 1);
			if (depth.compareTo(BigInteger.ZERO) > 0) {
				return depth;
			}
		}
		return BIG_MINUS_ONE;
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
	public PositionMapper(I_Position destination) {
		this.destination = destination;
	}

	public I_Position getDestination() {
		return destination;
	}

	int lengthToPrint = 150;
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		Formatter f = new Formatter(buf);
        buf.append(this.getClass().getSimpleName() + ": ");
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
						Bdb.getSapDb().getPathId(i)));
				buf.append("|");
				buf.append(Bdb.getConceptDb().getConcept( // status
						Bdb.getSapDb().getStatusId(i)));
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
				buf.append(Boolean.toString(conflictMatrix.get(i, j)).charAt(0));
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
	
	
}
