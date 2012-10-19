/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * The Interface NidListBI represents a serializable list of nids. All
 * implementations must serialize the set using uuids.
 */
public interface NidListBI {

    /**
     * Adds the given
     * <code>nid</code> in the specified
     * <code>index</code>.
     *
     * @param index the index at which the specified nid is to be inserted
     * @param nid the nid to add
     */
    public void add(int index, Integer nid);

    /**
     * Adds the specified
     * <code>nid</code> to the end of this nid list.
     *
     * @param nid the nid to add
     * @return <code>true</code>, if the collection changed as a result
     */
    public boolean add(Integer nid);

    /**
     * Adds the specified
     * <code>nids</code> to the end of this nid list.
     *
     * @param nids a collection of nids to add
     * @return <code>true</code>, if the collection changed as a result
     */
    public boolean addAll(Collection<? extends Integer> nids);

    /**
     * Adds the given
     * <code>nids</code> in the specified
     * <code>index</code>.
     *
     * @param index the index at which the specified nid is to be inserted
     * @param nids a collection of nids to add
     * @return <code>true</code>, if successful
     */
    public boolean addAll(int index, Collection<? extends Integer> nids);

    /**
     * Removes all of the nids from this nid list.
     */
    public void clear();

    /**
     * Checks if this nid list contains the specified nid.
     *
     * @param obj the nid in question
     * @return <code>true</code>, if this nid list contains the specified nid
     */
    public boolean contains(Object obj);

    /**
     * Checks if this nid list contains the specified nids.
     *
     * @param c the nids in question
     * @return <code>true</code>, if this nid list contains the specified nids
     */
    public boolean containsAll(Collection<?> c);

    /**
     * Gets the nid at the specified
     * <code>index</code> in this nid list.
     *
     * @param index the index of the nid to return
     * @return the nid at the specified position
     */
    public Integer get(int index);

    /**
     * Gets the index of the first occurrence of the specified nid, or -1 if
     * this nid list does not contain the nid.
     *
     * @param obj the nid to search for
     * @return the index of the first occurrence of the specified nid, or -1 if
     * this nid list does not contain the nid
     */
    public int indexOf(Object obj);

    /**
     * Checks if this nid list is empty.
     *
     * @return <code>true</code>, if this nid list contains no nids
     */
    public boolean isEmpty();

    /**
     * Returns an iterator that will iterate of the nids in this nid list in the
     * proper sequence.
     *
     * @return an iterator that will iterate of the nids in this nid list in the
     * proper sequence
     */
    public Iterator<Integer> iterator();

    /**
     * Returns the index of the last occurrence of the specified nid in this nid
     * list, or -1 if this nid list does not contain the nid.
     *
     * @param obj the nid to search for
     * @return the index of the last occurrence of the specified nid in this nid
     * list, or -1 if this nid list does not contain the nid
     */
    public int lastIndexOf(Object obj);

    /**
     * Returns a list iterator over the nids in this nid list (in proper
     * sequence).
     *
     * @return Returns a list iterator over the nids in this nid list (in proper
     * sequence)
     */
    public ListIterator<Integer> listIterator();

    /**
     * Returns a list iterator of the nids in this nid list (in proper
     * sequence), starting at the specified position in this nid list.
     *
     * @param index the index of the first nid to be returned
     * @return a list iterator of the nids in this nid list (in proper
     * sequence), starting at the specified position in this nid list
     */
    public ListIterator<Integer> listIterator(int index);

    /**
     * Removes the nid at the specified position in this nid list.
     *
     * @param index the index of the nid to remove
     * @return the nid previously at the specified position
     */
    public Integer remove(int index);

    /**
     * Removes the first occurrence of the specified nid from this nid list, if
     * it is present.
     *
     * @param obj nid to be removed from this list
     * @return <code>true</code>, if this nid list contained the specified nid
     */
    public boolean remove(Object obj);

    /**
     * Removes from this nid list all of its nids that are contained in the
     * specified collection.
     *
     * @param c the nids to remove
     * @return <code>true</code>, if this nid list changed as a result
     */
    public boolean removeAll(Collection<?> c);

    /**
     * Retains only the nids in this nid list that are contained in the
     * specified collection.
     *
     * @param c the nids to retain
     * @return <code>true</code>, if this nid list changed as a result
     */
    public boolean retainAll(Collection<?> c);

    /**
     * Replaces the nid at the specified position in this nid list with the
     * specified nid.
     *
     * @param index the index of the nid to replace
     * @param nid the new nid
     * @return the nid previously at the specified position
     */
    public Integer set(int index, Integer nid);

    /**
     * The number of nids in this nid list.
     *
     * @return an int representing the number of nids in this nid list
     */
    public int size();

    /**
     * Returns a view of the portion of this nid list between the specified
     * <code>fromIndex</code>, inclusive, and <code>toIndex</code>, exclusive.
     *
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex high endpoint (exclusive) of the subList
     * @return a view of the specified range within this nid list
     * @see List#subList(int, int) 
     */
    public List<Integer> subList(int fromIndex, int toIndex);

    /**
     * Returns an array representing the nids in this nid list.
     *
     * @return an array representing the nids in this nid list
     */
    public Object[] toArray();

    /**
     * Returns an array representing the nids in this nid list.
     * @param <T> the type of the new array
     * @param a the array to store the new elements
     * @return an array representing the set of positions
     * @see List#toArray(T[]) 
     */
    public <T> T[] toArray(T[] a);

    /**
     * Gets a list representing the nids in this nid list.
     *
     * @return a list representing the nids in this nid list
     */
    public List<Integer> getListValues();

    /**
     *  Gets an array representing the nids in this nid list.
     *
     * @return an array representing the nids in this nid list
     */
    public int[] getListArray();
}
