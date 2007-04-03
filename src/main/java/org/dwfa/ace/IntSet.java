package org.dwfa.ace;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.AbstractListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.config.AceConfig;

import com.sleepycat.je.DatabaseException;

public class IntSet implements ListDataListener {
	private Set<ListDataListener> listeners = new HashSet<ListDataListener>();
	
	private int[] setValues = new int[0];
	
	public IntSet(int[] values) {
		super();
		this.setValues = values;
	}
	public IntSet() {
		super();
		this.setValues = new int[0];
	}

	public boolean contains(int key) {
		return Arrays.binarySearch(setValues, key) >= 0;
	}
	
	public int[] getSetValues() {
		return setValues;
	}
	public void add(int key) {
		if (setValues.length == 0) {
			setValues = new int[1];
			setValues[0] = key;
		} else {
			int insertionPoint = Arrays.binarySearch(setValues, key);
			if (insertionPoint >= 0) {
				return;
			}
			insertionPoint = -insertionPoint - 1;
			int[] newSet = new int[setValues.length + 1];
			for (int i = 0; i < insertionPoint; i++) {
				newSet[i] = setValues[i];
			}
			newSet[insertionPoint] = key;
			for (int i = insertionPoint + 1; i < newSet.length; i++) {
				newSet[i] = setValues[i-1];
			}
			setValues = newSet;
		}
	}
	public void remove(int key) {
		int insertionPoint = Arrays.binarySearch(setValues, key);
		if (insertionPoint < 0) {
			return;
		}
		int[] newSet = new int[setValues.length - 1];
		for (int i = 0; i < insertionPoint; i++) {
			newSet[i] = setValues[i];
		}
		for (int i = insertionPoint + 1; i < setValues.length; i++) {
			newSet[i-1] = setValues[i];
		}
		setValues = newSet;
	}

	public void addAll(int[] keys) {
		for (int i = 0; i < keys.length; i++) {
			add(keys[i]);
		}
	}
	public void removeAll(int[] keys) {
		for (int i = 0; i < keys.length; i++) {
			remove(keys[i]);
		}
	}
	
	public void clear() {
		setValues = new int[0];
	}
	
	public static void writeIntSet(ObjectOutputStream out, IntSet set) throws IOException {
		if (set == null) {
			out.writeInt(Integer.MIN_VALUE);
			return;
		}
		out.writeInt(set.getSetValues().length);
        for (int i: set.getSetValues()) {
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
	public static IntSet readIntSet(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int size = in.readInt();
		if (size == Integer.MIN_VALUE) {
			return null;
		}
		int[] set = new int[size];
        for (int i = 0; i < size; i++) {
        	try {
        		set[i] = AceConfig.vodb.uuidToNative((List<UUID>) in.readObject());
			} catch (Exception e) {
				IOException newEx = new IOException();
				newEx.initCause(e);
				throw newEx;
			} 
        }
        IntSet returnSet = new IntSet(set);
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

}
