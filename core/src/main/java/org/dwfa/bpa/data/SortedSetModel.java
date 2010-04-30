/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.bpa.data;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class SortedSetModel<T> implements ListModel, SortedSet<T>, Serializable {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private SortedSet<T> data;

    private transient Collection<ListDataListener> listeners;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(data);
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.data = (SortedSet<T>) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @return
     */
    public Iterator<T> iterator() {
        return data.iterator();
    }

    /**
	 *  
	 */
    public SortedSetModel(Collection<T> data) {
        super();
        this.data = Collections.synchronizedSortedSet(new TreeSet<T>(data));
    }

    public SortedSetModel() {
        super();
        this.data = Collections.synchronizedSortedSet(new TreeSet<T>());
    }

    /**
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
        return data.size();
    }

    /**
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
        return this.data.toArray()[index];
    }

    /**
     * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
     */
    public void addListDataListener(ListDataListener l) {
        if (this.listeners == null) {
            this.listeners = new HashSet<ListDataListener>();
        }
        this.listeners.add(l);

    }

    /**
     * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
     */
    public void removeListDataListener(ListDataListener l) {
        this.listeners.remove(l);
    }

    /**
     * @param o
     * @return
     * @return
     */

    public boolean add(T o) {
        boolean rv = data.add(o);
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, data.size() - 2, data.size());
        notifyListenersContentChanged(e);
        return rv;
    }

    /**
     * @param index
     * @param element
     */
    public void add(int index, T element) {
        data.add(element);
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, data.size());
        notifyListenersContentChanged(e);
    }

    /**
     * @param index
     * @param c
     * @return
     */
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean rv = data.addAll(c);
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, data.size());
        notifyListenersContentChanged(e);
        return rv;
    }

    /**
     * @param e
     */
    private void notifyListenersContentChanged(ListDataEvent e) {
        if (listeners != null) {
            for (Iterator<ListDataListener> listenerItr = listeners.iterator(); listenerItr.hasNext();) {
                ListDataListener l = listenerItr.next();
                l.contentsChanged(e);
            }
        }
    }

    /**
     * @param c
     * @return
     */
    public boolean addAll(Collection<? extends T> c) {
        boolean rv = data.addAll(c);
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, data.size());
        notifyListenersContentChanged(e);
        return rv;
    }

    /**
	 * 
	 */
    public void clear() {
        int size = data.size();
        data.clear();
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, size);
        notifyListenersContentChanged(e);
    }

    /**
     * @param o
     * @return
     */
    public boolean contains(Object o) {
        return data.contains(o);
    }

    /**
     * @param c
     * @return
     */
    public boolean containsAll(Collection<?> c) {
        return data.containsAll(c);
    }

    /**
     * @param index
     * @return
     */
    public T get(int index) {
        return new ArrayList<T>(data).get(index);
    }

    /**
     * @param o
     * @return
     */
    public int indexOf(Object o) {
        return new ArrayList<T>(data).indexOf(o);
    }

    /**
     * @return
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * @param o
     * @return
     */
    public int lastIndexOf(Object o) {
        return new ArrayList<T>(data).lastIndexOf(o);
    }

    /**
     * @return
     */
    public ListIterator<T> listIterator() {
        return new ArrayList<T>(data).listIterator();
    }

    /**
     * @param index
     * @return
     */
    public ListIterator<T> listIterator(int index) {
        return new ArrayList<T>(data).listIterator(index);
    }

    /**
     * @param index
     * @return
     */
    public T remove(int index) {
        List<T> dataAsList = new ArrayList<T>(data);
        T removedObj = dataAsList.remove(index);
        data = Collections.synchronizedSortedSet(new TreeSet<T>(dataAsList));
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, data.size());
        notifyListenersContentChanged(e);
        return removedObj;
    }

    /**
     * @param o
     * @return
     */
    public boolean remove(Object o) {
        int index = this.indexOf(o);
        return remove(index) != null;
    }

    /**
     * @param c
     * @return
     */
    public boolean removeAll(Collection<?> c) {
        boolean removed = false;
        for (Object o : c) {
            if (remove(o)) {
                removed = true;
            }
        }
        return removed;
    }

    /**
     * @param c
     * @return
     */
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param index
     * @param element
     * @return
     */
    public T set(int index, T element) {
        List<T> dataAsList = new ArrayList<T>(data);
        T rv = dataAsList.set(index, element);
        data = Collections.synchronizedSortedSet(new TreeSet<T>(dataAsList));
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index);
        notifyListenersContentChanged(e);
        return rv;
    }

    /**
     * @return
     */
    public int size() {
        return data.size();
    }

    /**
     * @return
     */
    public Object[] toArray() {
        return data.toArray();
    }

    /**
     * @param a
     * @return
     */
    @SuppressWarnings("hiding")
    public <T> T[] toArray(T[] a) {
        return data.toArray(a);
    }

    @Override
    public String toString() {
        return data.toString();
    }

    public Comparator<? super T> comparator() {
        return data.comparator();
    }

    public boolean equals(Object o) {
        return data.equals(o);
    }

    public T first() {
        return data.first();
    }

    public int hashCode() {
        return data.hashCode();
    }

    public SortedSet<T> headSet(T toElement) {
        return data.headSet(toElement);
    }

    public T last() {
        return data.last();
    }

    public SortedSet<T> subSet(T fromElement, T toElement) {
        return data.subSet(fromElement, toElement);
    }

    public SortedSet<T> tailSet(T fromElement) {
        return data.tailSet(fromElement);
    }

}
