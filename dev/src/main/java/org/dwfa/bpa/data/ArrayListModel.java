/*
 * Created on Mar 9, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.data;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * @author kec
 *  
 */
public class ArrayListModel<T> implements ListModel, List<T>, Serializable {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private List<T> data;

	private transient Collection<ListDataListener> listeners;

     private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(data);
     }

    @SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
                this.data = (List<T>) in.readObject();
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
	public ArrayListModel(Collection<T> data) {
		super();
		this.data = Collections.synchronizedList(new ArrayList<T>(data));
	}

	public ArrayListModel() {
		super();
		this.data = Collections.synchronizedList(new ArrayList<T>());
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
		return this.data.get(index);
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

	public List<T> getList() {
		return data;
	}
	/**
	 * @param o
	 * @return
	 * @return
	 */
	
	
	public boolean add(T o) {
		boolean rv = data.add(o);
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, data.size(), data.size());
        notifyListenersContentChanged(e);
        return rv;
	}
	
	
	/**
	 * @param index
	 * @param element
	 */
	public void add(int index, T element) {
		data.add(index, element);
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, data.size());
        notifyListenersContentChanged(e);
	}
	/**
	 * @param index
	 * @param c
	 * @return
	 */
	public boolean addAll(int index, Collection<? extends T> c) {
		boolean rv = data.addAll(index, c);
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, data.size());
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
        int index = data.size();
        boolean rv = data.addAll(index, c);
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, data.size());
        notifyListenersContentChanged(e);
        return rv;
	}
	/**
	 * 
	 */
	public void clear() {
		data.clear();
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
	public boolean containsAll(Collection c) {
		return data.containsAll(c);
	}
	/**
	 * @param index
	 * @return
	 */
	public T get(int index) {
		return data.get(index);
	}
	/**
	 * @param o
	 * @return
	 */
	public int indexOf(Object o) {
		return data.indexOf(o);
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
		return data.lastIndexOf(o);
	}
	/**
	 * @return
	 */
	public ListIterator<T> listIterator() {
		return data.listIterator();
	}
	/**
	 * @param index
	 * @return
	 */
	public ListIterator<T> listIterator(int index) {
		return data.listIterator(index);
	}

    /**
     * @param index
     * @return
     */
    public T remove(int index) {
        T removedObj = data.remove(index);
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index);
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
	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}
	/**
	 * @param c
	 * @return
	 */
	public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
	}
	/**
	 * @param index
	 * @param element
	 * @return
	 */
	public T set(int index, T element) {
		T rv = data.set(index, element);
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
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	public List<T> subList(int fromIndex, int toIndex) {
		return Collections.unmodifiableList(data.subList(fromIndex, toIndex));
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

}