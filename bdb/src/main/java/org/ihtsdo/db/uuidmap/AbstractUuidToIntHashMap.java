package org.ihtsdo.db.uuidmap;

import java.util.UUID;

import cern.colt.list.IntArrayList;
import cern.colt.map.AbstractMap;

public abstract class AbstractUuidToIntHashMap extends AbstractMap {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// public static int hashCollisions = 0; // for debug only
	/**
	 * Makes this class non instantiable, but still let's others inherit from
	 * it.
	 */
	protected AbstractUuidToIntHashMap() {
	}

	/**
	 * Returns <tt>true</tt> if the receiver contains the specified key.
	 * 
	 * @return <tt>true</tt> if the receiver contains the specified key.
	 */
	public boolean containsKey(final long[] key) {
		return !forEachKey(new UuidProcedure() {
			public boolean apply(long[] iterKey) {
				return (key[0] != iterKey[0] || key[1] != iterKey[1]);
			}
		});
	}

	/**
	 * Returns a deep copy of the receiver; uses <code>clone()</code> and casts
	 * the result.
	 * 
	 * @return a deep copy of the receiver.
	 */
	public AbstractUuidToIntHashMap copy() {
		return (AbstractUuidToIntHashMap) clone();
	}

	/**
	 * Applies a procedure to each key of the receiver, if any. Note: Iterates
	 * over the keys in no particular order. Subclasses can define a particular
	 * order, for example, "sorted by key". All methods which <i>can</i> be
	 * expressed in terms of this method (most methods can) <i>must
	 * guarantee</i> to use the <i>same</i> order defined by this method, even
	 * if it is no particular order. This is necessary so that, for example,
	 * methods <tt>keys</tt> and <tt>values</tt> will yield association pairs,
	 * not two uncorrelated lists.
	 * 
	 * @param procedure
	 *            the procedure to be applied. Stops iteration if the procedure
	 *            returns <tt>false</tt>, otherwise continues.
	 * @return <tt>false</tt> if the procedure stopped before all keys where
	 *         iterated over, <tt>true</tt> otherwise.
	 */
	public abstract boolean forEachKey(UuidProcedure procedure);

	/**
	 * Applies a procedure to each (key,value) pair of the receiver, if any.
	 * Iteration order is guaranteed to be <i>identical</i> to the order used by
	 * method {@link #forEachKey(UuidProcedure)}.
	 * 
	 * @param procedure
	 *            the procedure to be applied. Stops iteration if the procedure
	 *            returns <tt>false</tt>, otherwise continues.
	 * @return <tt>false</tt> if the procedure stopped before all keys where
	 *         iterated over, <tt>true</tt> otherwise.
	 */
	public boolean forEachPair(final UuidIntProcedure procedure) {
		return forEachKey(new UuidProcedure() {
			public boolean apply(long[] key) {
				return procedure.apply(key, get(key));
			}
		});
	}

	/**
	 * Returns the value associated with the specified key. It is often a good
	 * idea to first check with {@link #containsKey(double)} whether the given
	 * key has a value associated or not, i.e. whether there exists an
	 * association for the given key or not.
	 * 
	 * @param key
	 *            the key to be searched for.
	 * @return the value associated with the specified key; <tt>0</tt> if no
	 *         such key is present.
	 */
	public abstract int get(long[] key);

	/**
	 * Returns the first key the given value is associated with. It is often a
	 * good idea to first check with {@link #containsValue(int)} whether there
	 * exists an association from a key to this value. Search order is
	 * guaranteed to be <i>identical</i> to the order used by method
	 * {@link #forEachKey(UuidProcedure)}.
	 * 
	 * @param value
	 *            the value to search for.
	 * @return the first key for which holds <tt>get(key) == value</tt>; returns
	 *         <tt>Double.NaN</tt> if no such key exists.
	 */
	public long[] keyOf(final int value) {
		final long[] foundKey = new long[2];
		boolean notFound = forEachPair(new UuidIntProcedure() {
			public boolean apply(long[] iterKey, int iterValue) {
				boolean found = value == iterValue;
				if (found) {
					foundKey[0] = iterKey[0];
					foundKey[1] = iterKey[1];
				}
				return !found;
			}
		});
		if (notFound)
			return null;
		return foundKey;
	}

	/**
	 * Returns a list filled with all keys contained in the receiver. The
	 * returned list has a size that equals <tt>this.size()</tt>. Note: Keys are
	 * filled into the list in no particular order. However, the order is
	 * <i>identical</i> to the order used by method
	 * {@link #forEachKey(UuidProcedure)}.
	 * <p>
	 * This method can be used to iterate over the keys of the receiver.
	 * 
	 * @return the keys.
	 */
	public UuidArrayList keys() {
		UuidArrayList list = new UuidArrayList(size());
		keys(list);
		return list;
	}

	/**
	 * Fills all keys contained in the receiver into the specified list. Fills
	 * the list, starting at index 0. After this call returns the specified list
	 * has a new size that equals <tt>this.size()</tt>. Iteration order is
	 * guaranteed to be <i>identical</i> to the order used by method
	 * {@link #forEachKey(UuidProcedure)}.
	 * <p>
	 * This method can be used to iterate over the keys of the receiver.
	 * 
	 * @param list
	 *            the list to be filled, can have any size.
	 */
	public void keys(final UuidArrayList list) {
		list.clear();
		forEachKey(new UuidProcedure() {
			public boolean apply(long[] key) {
				list.add(key);
				return true;
			}
		});
	}

	/**
	 * Fills all keys <i>sorted ascending by their associated value</i> into the
	 * specified list. Fills into the list, starting at index 0. After this call
	 * returns the specified list has a new size that equals
	 * <tt>this.size()</tt>. Primary sort criterium is "value", secondary sort
	 * criterium is "key". This means that if any two values are equal, the
	 * smaller key comes first.
	 * <p>
	 * <b>Example:</b> <br>
	 * <tt>keys = (8,7,6), values = (1,2,2) --> keyList = (8,6,7)</tt>
	 * 
	 * @param keyList
	 *            the list to be filled, can have any size.
	 */
	public void keysSortedByValue(final UuidArrayList keyList) {
		pairsSortedByValue(keyList, new IntArrayList(size()));
	}

	/**
	 * Fills all pairs satisfying a given condition into the specified lists.
	 * Fills into the lists, starting at index 0. After this call returns the
	 * specified lists both have a new size, the number of pairs satisfying the
	 * condition. Iteration order is guaranteed to be <i>identical</i> to the
	 * order used by method {@link #forEachKey(UuidProcedure)}.
	 * <p>
	 * <b>Example:</b> <br>
	 * 
	 * <pre>
	 * UuidIntProcedure condition = new UuidIntProcedure() { // match even values only
	 * 		public boolean apply(double key, int value) { return value%2==0; }
	 * 	}
	 * 	keys = (8,7,6), values = (1,2,2) --> keyList = (6,8), valueList = (2,1)</tt>
	 * </pre>
	 * 
	 * @param condition
	 *            the condition to be matched. Takes the current key as first
	 *            and the current value as second argument.
	 * @param keyList
	 *            the list to be filled with keys, can have any size.
	 * @param valueList
	 *            the list to be filled with values, can have any size.
	 */
	public void pairsMatching(final UuidIntProcedure condition,
			final UuidArrayList keyList, final IntArrayList valueList) {
		keyList.clear();
		valueList.clear();

		forEachPair(new UuidIntProcedure() {
			public boolean apply(long[] key, int value) {
				if (condition.apply(key, value)) {
					keyList.add(key);
					valueList.add(value);
				}
				return true;
			}
		});
	}

	/**
	 * Fills all keys and values <i>sorted ascending by key</i> into the
	 * specified lists. Fills into the lists, starting at index 0. After this
	 * call returns the specified lists both have a new size that equals
	 * <tt>this.size()</tt>.
	 * <p>
	 * <b>Example:</b> <br>
	 * <tt>keys = (8,7,6), values = (1,2,2) --> keyList = (6,7,8), valueList = (2,2,1)</tt>
	 * 
	 * @param keyList
	 *            the list to be filled with keys, can have any size.
	 * @param valueList
	 *            the list to be filled with values, can have any size.
	 */
	public void pairsSortedByKey(final UuidArrayList keyList,
			final IntArrayList valueList) {
		/*
		 * keys(keyList); values(valueList);
		 * 
		 * final double[] k = keyList.elements(); final int[] v =
		 * valueList.elements(); cern.colt.Swapper swapper = new
		 * cern.colt.Swapper() { public void swap(int a, int b) { int t1; double
		 * t2; t1 = v[a]; v[a] = v[b]; v[b] = t1; t2 = k[a]; k[a] = k[b]; k[b] =
		 * t2; } };
		 * 
		 * cern.colt.function.IntComparator comp = new
		 * cern.colt.function.IntComparator() { public int compare(int a, int b)
		 * { return k[a]<k[b] ? -1 : k[a]==k[b] ? 0 : 1; } };
		 * cern.colt.MultiSorting.sort(0,keyList.size(),comp,swapper);
		 */

		// this variant may be quicker
		// cern.colt.map.OpenDoubleIntHashMap.hashCollisions = 0;
		// System.out.println("collisions="+cern.colt.map.OpenDoubleIntHashMap.hashCollisions);
		keys(keyList);
		keyList.sort();
		valueList.setSize(keyList.size());
		for (int i = keyList.size(); --i >= 0;) {
			valueList.setQuick(i, get(keyList.getQuick(i)));
		}
		// System.out.println("collisions="+cern.colt.map.OpenDoubleIntHashMap.hashCollisions);

	}

	/**
	 * Fills all keys and values <i>sorted ascending by value</i> into the
	 * specified lists. Fills into the lists, starting at index 0. After this
	 * call returns the specified lists both have a new size that equals
	 * <tt>this.size()</tt>. Primary sort criterium is "value", secondary sort
	 * criterium is "key". This means that if any two values are equal, the
	 * smaller key comes first.
	 * <p>
	 * <b>Example:</b> <br>
	 * <tt>keys = (8,7,6), values = (1,2,2) --> keyList = (8,6,7), valueList = (1,2,2)</tt>
	 * 
	 * @param keyList
	 *            the list to be filled with keys, can have any size.
	 * @param valueList
	 *            the list to be filled with values, can have any size.
	 */
	public void pairsSortedByValue(final UuidArrayList keyList,
			final IntArrayList valueList) {
		keys(keyList);
		values(valueList);

		final long[] k = keyList.elements();
		final int[] v = valueList.elements();
		cern.colt.Swapper swapper = new cern.colt.Swapper() {
			public void swap(int a, int b) {
				int t1;
				long[] t2 = new long[2];
				int aMsb = a * 2;
				int aLsb = aMsb + 1;
				int bMsb = b * 2;
				int bLsb = bMsb + 1;
				t1 = v[a];
				v[a] = v[b];
				v[b] = t1;
				t2[0] = k[aMsb];
				t2[1] = k[aLsb];
				k[aMsb] = k[bMsb];
				k[aLsb] = k[bLsb];
				k[bMsb] = t2[0];
				k[bLsb] = t2[1];
			}
		};

		cern.colt.function.IntComparator comp = new cern.colt.function.IntComparator() {
			public int compare(int a, int b) {
				int aMsb = a * 2;
				int aLsb = aMsb + 1;
				int bMsb = b * 2;
				int bLsb = bMsb + 1;
				return v[a] < v[b] ? -1 : v[a] > v[b] ? 1 : ((k[aMsb] < k[bMsb] || k[aLsb] < k[bLsb]) ? -1
						: ((k[aMsb] == k[bMsb] && k[aLsb] == k[bLsb]) ? 0 : 1));
			}
		};

		// cern.colt.map.OpenDoubleIntHashMap.hashCollisions = 0;
		cern.colt.GenericSorting.quickSort(0, keyList.size(), comp, swapper);
		// System.out.println("collisions="+cern.colt.map.OpenDoubleIntHashMap.hashCollisions);
	}

	/**
	 * Associates the given key with the given value. Replaces any old
	 * <tt>(key,someOtherValue)</tt> association, if existing.
	 * 
	 * @param key
	 *            the key the value shall be associated with.
	 * @param value
	 *            the value to be associated.
	 * @return <tt>true</tt> if the receiver did not already contain such a key;
	 *         <tt>false</tt> if the receiver did already contain such a key -
	 *         the new value has now replaced the formerly associated value.
	 */
	public abstract boolean put(long[] key, int value);

	/**
	 * Removes the given key with its associated element from the receiver, if
	 * present.
	 * 
	 * @param key
	 *            the key to be removed from the receiver.
	 * @return <tt>true</tt> if the receiver contained the specified key,
	 *         <tt>false</tt> otherwise.
	 */
	public abstract boolean removeKey(long[] key);

	/**
	 * Returns a string representation of the receiver, containing the String
	 * representation of each key-value pair, sorted ascending by key.
	 */
	public String toString() {
		UuidArrayList theKeys = keys();
		theKeys.sort();

		StringBuffer buf = new StringBuffer();
		buf.append("[");
		int maxIndex = theKeys.size() - 1;
		for (int i = 0; i <= maxIndex; i++) {
			long[] key = theKeys.get(i);
			UUID uuidKey = new UUID(key[0], key[1]);
			buf.append(uuidKey.toString());
			buf.append("->");
			buf.append(String.valueOf(get(key)));
			if (i < maxIndex)
				buf.append(", ");
		}
		buf.append("]");
		return buf.toString();
	}

	/**
	 * Returns a string representation of the receiver, containing the String
	 * representation of each key-value pair, sorted ascending by value.
	 */
	public String toStringByValue() {
		UuidArrayList theKeys = new UuidArrayList();
		keysSortedByValue(theKeys);

		StringBuffer buf = new StringBuffer();
		buf.append("[");
		int maxIndex = theKeys.size() - 1;
		for (int i = 0; i <= maxIndex; i++) {
			long[] key = theKeys.get(i);
			UUID uuidKey = new UUID(key[0], key[1]);
			buf.append(uuidKey.toString());
			buf.append("->");
			buf.append(String.valueOf(get(key)));
			if (i < maxIndex)
				buf.append(", ");
		}
		buf.append("]");
		return buf.toString();
	}

	/**
	 * Returns a list filled with all values contained in the receiver. The
	 * returned list has a size that equals <tt>this.size()</tt>. Iteration
	 * order is guaranteed to be <i>identical</i> to the order used by method
	 * {@link #forEachKey(UuidProcedure)}.
	 * <p>
	 * This method can be used to iterate over the values of the receiver.
	 * 
	 * @return the values.
	 */
	public IntArrayList values() {
		IntArrayList list = new IntArrayList(size());
		values(list);
		return list;
	}

	/**
	 * Fills all values contained in the receiver into the specified list. Fills
	 * the list, starting at index 0. After this call returns the specified list
	 * has a new size that equals <tt>this.size()</tt>. Iteration order is
	 * guaranteed to be <i>identical</i> to the order used by method
	 * {@link #forEachKey(UuidProcedure)}.
	 * <p>
	 * This method can be used to iterate over the values of the receiver.
	 * 
	 * @param list
	 *            the list to be filled, can have any size.
	 */
	public void values(final IntArrayList list) {
		list.clear();
		forEachKey(new UuidProcedure() {
			public boolean apply(long[] key) {
				list.add(get(key));
				return true;
			}
		});
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}
}
