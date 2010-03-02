package org.ihtsdo.db.bdb.sap;

import org.ihtsdo.db.uuidmap.UuidToIntHashMap;

import cern.colt.list.IntArrayList;

public class SapToIntHashMap {
	
	private UuidToIntHashMap map;

	private static final int defaultCapacity = 277;
	private static final double defaultMinLoadFactor = 0.2;
	private static final double defaultMaxLoadFactor = 0.5;

	/**
	 * Constructs an empty map with default capacity and default load factors.
	 */
	public SapToIntHashMap() {
		this(defaultCapacity);
	}

	/**
	 * Constructs an empty map with the specified initial capacity and default
	 * load factors.
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the map.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is less than zero.
	 */
	public SapToIntHashMap(int initialCapacity) {
		this(initialCapacity, defaultMinLoadFactor, defaultMaxLoadFactor);
	}

	/**
	 * Constructs an empty map with the specified initial capacity and the
	 * specified minimum and maximum load factor.
	 * 
	 * @param initialCapacity
	 *            the initial capacity.
	 * @param minLoadFactor
	 *            the minimum load factor.
	 * @param maxLoadFactor
	 *            the maximum load factor.
	 * @throws IllegalArgumentException
	 *             if
	 * 
	 *             <tt>initialCapacity < 0 || (minLoadFactor < 0.0 || minLoadFactor >= 1.0) || (maxLoadFactor <= 0.0 || maxLoadFactor >= 1.0) || (minLoadFactor >= maxLoadFactor)</tt>
	 *             .
	 */
	public SapToIntHashMap(int initialCapacity, double minLoadFactor,
			double maxLoadFactor) {
		setup(initialCapacity, minLoadFactor, maxLoadFactor);
	}

	private void setup(int initialCapacity, double minLoadFactor,
			double maxLoadFactor) {
		map = new UuidToIntHashMap(initialCapacity, minLoadFactor,
				maxLoadFactor);
	}

	public int get(long time, int statusNid, int pathNid) {
		return map.get(TimeStatusPosition.timeStatusPositionToUuid(time, statusNid, pathNid));
	}
	
	public int get(TimeStatusPosition tsp) {
		return map.get(tsp.toUuid());
	}
	
	public boolean put(long time, int statusNid, int pathNid, int statusAtPositionNid) {
		return map.put(TimeStatusPosition.timeStatusPositionToUuid(time, statusNid, pathNid), 
				statusAtPositionNid);
	}
	
	public boolean put(TimeStatusPosition tsp, int statusAtPositionNid) {
		return map.put(tsp.toUuid(), statusAtPositionNid);
	}

	public boolean containsKey(long time, int statusNid, int pathNid) {
		return map.containsKey(TimeStatusPosition.timeStatusPositionToUuid(time, statusNid, pathNid));
	}

	public IntArrayList values() {
		return map.values();
	}
}
