package org.dwfa.bpa.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


public class LongSet implements ListDataListener {
	private Set<ListDataListener> listeners = new HashSet<ListDataListener>();
	
	private long[] setValues = new long[0];
	
	public LongSet(long[] values) {
		super();
		this.setValues = values;
	}
	public LongSet() {
		super();
		this.setValues = new long[0];
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#contains(int)
	 */
	public boolean contains(int key) {
		return Arrays.binarySearch(setValues, key) >= 0;
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#getSetValues()
	 */
	public long[] getSetValues() {
		return setValues;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#add(int)
	 */
	public void add(long value) {
		if (setValues.length == 0) {
			setValues = new long[1];
			setValues[0] = value;
		} else {
			int insertionPoint = Arrays.binarySearch(setValues, value);
			if (insertionPoint >= 0) {
				return;
			}
			insertionPoint = -insertionPoint - 1;
			long[] newSet = new long[setValues.length + 1];
			for (int i = 0; i < insertionPoint; i++) {
				newSet[i] = setValues[i];
			}
			newSet[insertionPoint] = value;
			for (int i = insertionPoint + 1; i < newSet.length; i++) {
				newSet[i] = setValues[i-1];
			}
			setValues = newSet;
		}
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#remove(int)
	 */
	public void remove(int key) {
		int insertionPoint = Arrays.binarySearch(setValues, key);
		if (insertionPoint < 0) {
			return;
		}
		long[] newSet = new long[setValues.length - 1];
		for (int i = 0; i < insertionPoint; i++) {
			newSet[i] = setValues[i];
		}
		for (int i = insertionPoint + 1; i < setValues.length; i++) {
			newSet[i-1] = setValues[i];
		}
		setValues = newSet;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#addAll(int[])
	 */
	public void addAll(int[] keys) {
		for (int i = 0; i < keys.length; i++) {
			add(keys[i]);
		}
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#removeAll(int[])
	 */
	public void removeAll(int[] keys) {
		for (int i = 0; i < keys.length; i++) {
			remove(keys[i]);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#clear()
	 */
	public void clear() {
		setValues = new long[0];
	}
	
	public static void writeLongSet(ObjectOutputStream out, LongSet set) throws IOException {
		if (set == null) {
			out.writeInt(Integer.MIN_VALUE);
			return;
		}
		out.writeInt(set.getSetValues().length);
        for (long i: set.getSetValues()) {
        	out.writeLong(i);
        }
	}

	public static LongSet readLongSet(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int size = in.readInt();
		if (size == Integer.MIN_VALUE) {
			return null;
		}
		long[] set = new long[size];
        for (int i = 0; i < size; i++) {
         	set[i] = in.readLong();
        }
        LongSet returnSet = new LongSet(set);
        return returnSet;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#contentsChanged(javax.swing.event.ListDataEvent)
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
			Long value = (Long) model.getElementAt(i);
			add(value);
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#intervalAdded(javax.swing.event.ListDataEvent)
	 */
	public void intervalAdded(ListDataEvent e) {
		handleChange(e);
		for (ListDataListener l: listeners) {
			l.intervalAdded(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#intervalRemoved(javax.swing.event.ListDataEvent)
	 */
	public void intervalRemoved(ListDataEvent e) {
		handleChange(e);
		for (ListDataListener l: listeners) {
			l.intervalRemoved(e);
		}
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#addListDataListener(javax.swing.event.ListDataListener)
	 */
	public boolean addListDataListener(ListDataListener o) {
		return listeners.add(o);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#removeListDataListener(javax.swing.event.ListDataListener)
	 */
	public boolean removeListDataListener(ListDataListener o) {
		return listeners.remove(o);
	}

}
