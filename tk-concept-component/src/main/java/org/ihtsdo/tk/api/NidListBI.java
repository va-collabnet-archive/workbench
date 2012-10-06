/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

// TODO: Auto-generated Javadoc
/**
 * The Interface NidListBI. All implementations must serialize the list as uuids.
 * TODO-javadoc: udpate
 */
public interface NidListBI {

    /**
     * Adds the.
     *
     * @param index the index
     * @param element the element
     */
    public void add(int index, Integer element);

    /**
     * Adds the.
     *
     * @param o the o
     * @return <code>true</code>, if successful
     */
    public boolean add(Integer o);

    /**
     * Adds the all.
     *
     * @param c the c
     * @return <code>true</code>, if successful
     */
    public boolean addAll(Collection<? extends Integer> c);

    /**
     * Adds the all.
     *
     * @param index the index
     * @param c the c
     * @return <code>true</code>, if successful
     */
    public boolean addAll(int index, Collection<? extends Integer> c);

    /**
     * Clear.
     */
    public void clear();

    /**
     * Contains.
     *
     * @param obj the obj
     * @return <code>true</code>, if successful
     */
    public boolean contains(Object obj);

    /**
     * Contains all.
     *
     * @param c the c
     * @return <code>true</code>, if successful
     */
    public boolean containsAll(Collection<?> c);

    /**
     * Gets the.
     *
     * @param index the index
     * @return the integer
     */
    public Integer get(int index);

    /**
     * Index of.
     *
     * @param obj the obj
     * @return the int
     */
    public int indexOf(Object obj);

    /**
     * Checks if is empty.
     *
     * @return <code>true</code>, if is empty
     */
    public boolean isEmpty();

    /**
     * Iterator.
     *
     * @return the iterator
     */
    public Iterator<Integer> iterator();

    /**
     * Last index of.
     *
     * @param obj the obj
     * @return the int
     */
    public int lastIndexOf(Object obj);

    /**
     * List iterator.
     *
     * @return the list iterator
     */
    public ListIterator<Integer> listIterator();

    /**
     * List iterator.
     *
     * @param index the index
     * @return the list iterator
     */
    public ListIterator<Integer> listIterator(int index);

    /**
     * Removes the.
     *
     * @param index the index
     * @return the integer
     */
    public Integer remove(int index);

    /**
     * Removes the.
     *
     * @param obj the obj
     * @return <code>true</code>, if successful
     */
    public boolean remove(Object obj);

    /**
     * Removes the all.
     *
     * @param c the c
     * @return <code>true</code>, if successful
     */
    public boolean removeAll(Collection<?> c);

    /**
     * Retain all.
     *
     * @param c the c
     * @return <code>true</code>, if successful
     */
    public boolean retainAll(Collection<?> c);

    /**
     * Sets the.
     *
     * @param index the index
     * @param element the element
     * @return the integer
     */
    public Integer set(int index, Integer element);

    /**
     * Size.
     *
     * @return the int
     */
    public int size();

    /**
     * Sub list.
     *
     * @param fromIndex the from index
     * @param toIndex the to index
     * @return the list
     */
    public List<Integer> subList(int fromIndex, int toIndex);

    /**
     * To array.
     *
     * @return the object[]
     */
    public Object[] toArray();

    /**
     * To array.
     *
     * @param <T> the generic type
     * @param a the a
     * @return the t[]
     */
    public <T> T[] toArray(T[] a);

    /**
     * Gets the list values.
     *
     * @return the list values
     */
    public List<Integer> getListValues();

    /**
     * Gets the list array.
     *
     * @return the list array
     */
    public int[] getListArray();

}
