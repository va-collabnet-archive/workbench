package org.dwfa.ace;

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
import org.dwfa.ace.config.AceConfig;
import org.dwfa.tapi.TerminologyException;

import com.sleepycat.je.DatabaseException;

public class IntList implements ListDataListener {
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

	public void intervalAdded(ListDataEvent e) {
		handleChange(e);
		for (ListDataListener l: listeners) {
			l.intervalAdded(e);
		}
	}

	public void intervalRemoved(ListDataEvent e) {
		handleChange(e);
		for (ListDataListener l: listeners) {
			l.intervalRemoved(e);
		}
	}
	public boolean addListDataListener(ListDataListener o) {
		return listeners.add(o);
	}
	public boolean removeListDataListener(ListDataListener o) {
		return listeners.remove(o);
	}
	public void add(int index, Integer element) {
		listValues.add(index, element);
	}
	public boolean add(Integer o) {
		return listValues.add(o);
	}
	public boolean addAll(Collection<? extends Integer> c) {
		return listValues.addAll(c);
	}
	public boolean addAll(int index, Collection<? extends Integer> c) {
		return listValues.addAll(index, c);
	}
	public void clear() {
		listValues.clear();
	}
	public boolean contains(Object o) {
		return listValues.contains(o);
	}
	public boolean containsAll(Collection<?> c) {
		return listValues.containsAll(c);
	}
	public Integer get(int index) {
		return listValues.get(index);
	}
	public int indexOf(Object o) {
		return listValues.indexOf(o);
	}
	public boolean isEmpty() {
		return listValues.isEmpty();
	}
	public Iterator<Integer> iterator() {
		return listValues.iterator();
	}
	public int lastIndexOf(Object o) {
		return listValues.lastIndexOf(o);
	}
	public ListIterator<Integer> listIterator() {
		return listValues.listIterator();
	}
	public ListIterator<Integer> listIterator(int index) {
		return listValues.listIterator(index);
	}
	public Integer remove(int index) {
		return listValues.remove(index);
	}
	public boolean remove(Object o) {
		return listValues.remove(o);
	}
	public boolean removeAll(Collection<?> c) {
		return listValues.removeAll(c);
	}
	public boolean retainAll(Collection<?> c) {
		return listValues.retainAll(c);
	}
	public Integer set(int index, Integer element) {
		return listValues.set(index, element);
	}
	public int size() {
		return listValues.size();
	}
	public List<Integer> subList(int fromIndex, int toIndex) {
		return listValues.subList(fromIndex, toIndex);
	}
	public Object[] toArray() {
		return listValues.toArray();
	}
	public <T> T[] toArray(T[] a) {
		return listValues.toArray(a);
	}
	public List<Integer> getListValues() {
		return listValues;
	}

}
