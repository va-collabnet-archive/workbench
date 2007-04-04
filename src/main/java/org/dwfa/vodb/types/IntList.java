package org.dwfa.vodb.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;

import javax.swing.AbstractListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.tapi.TerminologyException;

import com.sleepycat.je.DatabaseException;

public class IntList implements ListDataListener, I_IntList {
	private Set<ListDataListener> listeners = new HashSet<ListDataListener>();
	
	private List<Integer> listValues = new ArrayList<Integer>(2);
	
	public IntList(int[] values) {
		super();
		for (int i: values) {
			this.listValues.add(i);
		}
	}
	public IntList() {
		super();
	}

	
	public static void writeIntList(ObjectOutputStream out, IntList list) throws IOException {
		if (list == null) {
			out.writeInt(Integer.MIN_VALUE);
			return;
		}
		out.writeInt(list.size());
        for (int i: list.listValues) {
        	try {
				out.writeObject(AceConfig.vodb.nativeToUuid(i));
			} catch (DatabaseException e) {
				IOException newEx = new IOException();
				newEx.initCause(e);
				throw newEx;
			}
        }
	}

	@SuppressWarnings("unchecked")
	public static IntList readIntList(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int size = in.readInt();
		if (size == Integer.MIN_VALUE) {
			return new IntList();
		}
		int[] list = new int[size];
        for (int i = 0; i < size; i++) {
        	try {
        		list[i] = AceConfig.vodb.uuidToNative((List<UUID>) in.readObject());
			} catch (TerminologyException e) {
				IOException newEx = new IOException();
				newEx.initCause(e);
				throw newEx;
			} 
        }
        IntList returnSet = new IntList(list);
        return returnSet;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#contentsChanged(javax.swing.event.ListDataEvent)
	 */
	public void contentsChanged(ListDataEvent e) {
		handleChange(e);
		for (ListDataListener l: listeners) {
			l.contentsChanged(e);
		}
	}

	private void handleChange(ListDataEvent e) {
		AbstractListModel model = (AbstractListModel) e.getSource();
		clear();
		for (int i = 0; i < model.getSize(); i++) {
			I_GetConceptData cb = (I_GetConceptData) model.getElementAt(i);
			add(cb.getConceptId());
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#intervalAdded(javax.swing.event.ListDataEvent)
	 */
	public void intervalAdded(ListDataEvent e) {
		handleChange(e);
		for (ListDataListener l: listeners) {
			l.intervalAdded(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#intervalRemoved(javax.swing.event.ListDataEvent)
	 */
	public void intervalRemoved(ListDataEvent e) {
		handleChange(e);
		for (ListDataListener l: listeners) {
			l.intervalRemoved(e);
		}
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#addListDataListener(javax.swing.event.ListDataListener)
	 */
	public boolean addListDataListener(ListDataListener o) {
		return listeners.add(o);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#removeListDataListener(javax.swing.event.ListDataListener)
	 */
	public boolean removeListDataListener(ListDataListener o) {
		return listeners.remove(o);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#add(int, java.lang.Integer)
	 */
	public void add(int index, Integer element) {
		listValues.add(index, element);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#add(java.lang.Integer)
	 */
	public boolean add(Integer o) {
		return listValues.add(o);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends Integer> c) {
		return listValues.addAll(c);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends Integer> c) {
		return listValues.addAll(index, c);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#clear()
	 */
	public void clear() {
		listValues.clear();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return listValues.contains(o);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return listValues.containsAll(c);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#get(int)
	 */
	public Integer get(int index) {
		return listValues.get(index);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return listValues.indexOf(o);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#isEmpty()
	 */
	public boolean isEmpty() {
		return listValues.isEmpty();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#iterator()
	 */
	public Iterator<Integer> iterator() {
		return listValues.iterator();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return listValues.lastIndexOf(o);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#listIterator()
	 */
	public ListIterator<Integer> listIterator() {
		return listValues.listIterator();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#listIterator(int)
	 */
	public ListIterator<Integer> listIterator(int index) {
		return listValues.listIterator(index);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#remove(int)
	 */
	public Integer remove(int index) {
		return listValues.remove(index);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return listValues.remove(o);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return listValues.removeAll(c);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return listValues.retainAll(c);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#set(int, java.lang.Integer)
	 */
	public Integer set(int index, Integer element) {
		return listValues.set(index, element);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#size()
	 */
	public int size() {
		return listValues.size();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#subList(int, int)
	 */
	public List<Integer> subList(int fromIndex, int toIndex) {
		return listValues.subList(fromIndex, toIndex);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#toArray()
	 */
	public Object[] toArray() {
		return listValues.toArray();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#toArray(T[])
	 */
	public <T> T[] toArray(T[] a) {
		return listValues.toArray(a);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IntList#getListValues()
	 */
	public List<Integer> getListValues() {
		return listValues;
	}

}
