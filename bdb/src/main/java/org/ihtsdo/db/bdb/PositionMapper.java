package org.ihtsdo.db.bdb;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dwfa.ace.api.I_Position;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.Version;

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

	/**
	 * 
	 * @param <T>
	 *            the type of part being tested.
	 * @param part
	 *            the part to be tested to determine if it is on route to the
	 *            destination.
	 * @return true if the part's position is on the route to the destination of
	 *         the class's instance.
	 * @throws IOException
	 */
	public <V extends Version<V, ?>> boolean onRoute(V part) {
		return positionDistance[part.getStatusAtPositionNid()] >= 0;
	}

	/**
	 * 
	 * @param <T>
	 *            the type of part being tested.
	 * @param part1
	 *            the first part of the comparison.
	 * @param part2
	 *            the second part of the comparison.
	 * @return the <code>RELATIVE_POSITION</code> of part1 compared to part2
	 *         with respect to the destination position of the class's instance.
	 * @throws IOException
	 */
	public <V extends Version<V, ?>> RELATIVE_POSITION relativePosition(V part1, V part2)
			throws IOException {
		if (onRoute(part1) && onRoute(part2)) {
			if (conflictMatrix.get(part1.getStatusAtPositionNid(), part2
					.getStatusAtPositionNid())) {
				return RELATIVE_POSITION.CONFLICTING;
			} else if (positionDistance[part1.getStatusAtPositionNid()] > positionDistance[part2
					.getStatusAtPositionNid()]) {
				return RELATIVE_POSITION.BEFORE;
			} else if (positionDistance[part1.getStatusAtPositionNid()] < positionDistance[part2
					.getStatusAtPositionNid()]) {
				return RELATIVE_POSITION.AFTER;
			} else if (positionDistance[part1.getStatusAtPositionNid()] == positionDistance[part2
					.getStatusAtPositionNid()]) {
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
	public <V extends Version<V, ?>> RELATIVE_POSITION fastRelativePosition(V part1,
			V part2) {
		if (conflictMatrix.get(part1.getStatusAtPositionNid(), part2
				.getStatusAtPositionNid())) {
			return RELATIVE_POSITION.CONFLICTING;
		} else if (positionDistance[part1.getStatusAtPositionNid()] > positionDistance[part2
				.getStatusAtPositionNid()]) {
			return RELATIVE_POSITION.BEFORE;
		} else if (positionDistance[part1.getStatusAtPositionNid()] < positionDistance[part2
				.getStatusAtPositionNid()]) {
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

	private void setup() throws IOException, PathNotExistsException,
			TerminologyException {
		Collection<I_Position> origins = this.destination.getAllOrigins();
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

		BigInteger timeUpperBound = BigInteger.valueOf(
				System.currentTimeMillis()).multiply(BigInteger.valueOf(2));

		int positionCount = Bdb.getStatusAtPositionDb().getPositionCount();
		positionDistance = new int[positionCount];
		BigInteger[] positionComputedDistance = new BigInteger[positionCount];
		Arrays.fill(positionDistance, Integer.MIN_VALUE);
		conflictMatrix = new BitMatrix(positionCount, positionCount);
		for (int p1index = 0; p1index < positionCount; p1index++) {
			I_Position p1 = Bdb.getStatusAtPositionDb().getPosition(p1index);
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
							destination.getTime() - p1.getTime()).add(
							timeUpperBound.multiply(pathDepth));

					// iterate to compute conflicts...
					for (int p2index = 0; p2index < positionCount; p2index++) {
						I_Position p2 = Bdb.getStatusAtPositionDb()
								.getPosition(p2index);
						Integer p2pathId = p2.getPath().getConceptId();
						if (originMap.containsKey(p2pathId)
								&& p2.getTime() <= originMap.get(p2pathId)
										.getTime()) {
							if (precedingPathIdSet.contains(p2pathId)) {
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
		// Path is not on any route to the destination
		// No distance can lead to destination
		positionComputedDistance[positionIndex] = BigInteger.valueOf(-1);
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
			if (testPath.getTime() <= depthFinder.getTime()) {
				return BigInteger.valueOf(depthSeed);
			}
			return BigInteger.valueOf(-1);
		}
		for (I_Position child : testPath.getPath().getOrigins()) {
			BigInteger depth = getDepth(testPath, child, depthSeed + 1);
			if (depth.compareTo(BigInteger.ZERO) > 0) {
				return depth;
			}
		}
		return BigInteger.valueOf(-1);
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
		try {
			setup();
		} catch (PathNotExistsException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (TerminologyException e) {
			throw new RuntimeException(e);
		}
	}

	public I_Position getDestination() {
		return destination;
	}
}
