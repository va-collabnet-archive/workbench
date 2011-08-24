package org.ihtsdo.db.bdb.sap;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class SapToIntHashMap {
   private static final int defaultCapacity = 277;

   //~--- fields --------------------------------------------------------------

   private java.util.concurrent.ConcurrentHashMap<StatusAuthorPosition, Integer> map;

   //~--- constructors --------------------------------------------------------

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
      setup(initialCapacity);
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
    *             <tt>initialCapacity < 0 || (minLoadFactor < 0.0 || minLoadFactor >= 1.0)
    *                 || (maxLoadFactor <= 0.0 || maxLoadFactor >= 1.0)
    *                 || (minLoadFactor >= maxLoadFactor)</tt>
    *             .
    */
   public SapToIntHashMap(int initialCapacity, double minLoadFactor, double maxLoadFactor) {
      setup(initialCapacity);
   }

   //~--- methods -------------------------------------------------------------

   public boolean containsKey(int statusNid, int authorNid, int pathNid, long time) {
      return map.containsKey(new StatusAuthorPosition(statusNid, authorNid, pathNid, time));
   }

   public boolean put(StatusAuthorPosition tsp, int statusAtPositionNid) {
      return map.put(tsp, statusAtPositionNid) == null;
   }

   public boolean put(int statusNid, int authorNid, int pathNid, long time, int statusAtPositionNid) {
      return put(new StatusAuthorPosition(statusNid, authorNid, pathNid, time), statusAtPositionNid);
   }

   private void setup(int initialCapacity) {
      map = new ConcurrentHashMap<StatusAuthorPosition, Integer>(initialCapacity);
   }

   public Collection<Integer> values() {
      return map.values();
   }

   //~--- get methods ---------------------------------------------------------

   public int get(StatusAuthorPosition tsp) {
      return map.get(tsp);
   }

   public int get(int statusNid, int authorNid, int pathNid, long time) {
      return map.get(new StatusAuthorPosition(statusNid, authorNid, pathNid, time));
   }
}
