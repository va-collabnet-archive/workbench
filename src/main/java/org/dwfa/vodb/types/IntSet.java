package org.dwfa.vodb.types;

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
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.NoMappingException;

import com.sleepycat.je.DatabaseException;

public class IntSet implements ListDataListener, I_IntSet {
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

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#contains(int)
	 */
	public boolean contains(int key) {
		return Arrays.binarySearch(setValues, key) >= 0;
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#getSetValues()
	 */
	public int[] getSetValues() {
		return setValues;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#add(int)
	 */
	public void add(int key) {
		addNoIntervalAdded(key);
		intervalAdded(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, setValues.length));
	}
	private void addNoIntervalAdded(int key) {
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
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#remove(int)
	 */
	public void remove(int key) {
		removeNoIntervalRemoved(key);
		intervalRemoved(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, setValues.length));
	}
	private void removeNoIntervalRemoved(int key) {
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

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#addAll(int[])
	 */
	public void addAll(int[] keys) {
		for (int i = 0; i < keys.length; i++) {
			addNoIntervalAdded(keys[i]);
		}
		intervalAdded(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, setValues.length));
	}
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#removeAll(int[])
	 */
	public void removeAll(int[] keys) {
		for (int i = 0; i < keys.length; i++) {
			remove(keys[i]);
		}
		intervalRemoved(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, setValues.length));
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#clear()
	 */
	public void clear() {
		setValues = new int[0];
		intervalRemoved(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, setValues.length));
	}
	
	public static void writeIntSet(ObjectOutputStream out, I_IntSet set) throws IOException {
		if (set == null) {
			out.writeInt(Integer.MIN_VALUE);
			return;
		}
		out.writeInt(set.getSetValues().length);
        for (int i: set.getSetValues()) {
        	try {
				out.writeObject(AceConfig.getVodb().nativeToUuid(i));
			} catch (DatabaseException e) {
				IOException newEx = new IOException();
				newEx.initCause(e);
				throw newEx;
			}
        }
	}
	public static IntSet readIntSetIgnoreMapErrors(ObjectInputStream in) throws IOException, ClassNotFoundException {
		return readIntSet(in, true);
	}
	public static IntSet readIntSetStrict(ObjectInputStream in) throws IOException, ClassNotFoundException {
		return readIntSet(in, false);
	}
	
	@SuppressWarnings("unchecked")
	private static IntSet readIntSet(ObjectInputStream in, boolean ignore) throws IOException, ClassNotFoundException {
		int unmappedIds = 0;
		int size = in.readInt();
		if (size == Integer.MIN_VALUE) {
			return new IntSet();
		}
		int[] set = new int[size];
        for (int i = 0; i < size; i++) {
        	try {
        		if (ignore) {
            		try {
						set[i] = AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject());
					} catch (NoMappingException e) {
						AceLog.getAppLog().alertAndLogException(e);
						unmappedIds++;
						set[i] = Integer.MAX_VALUE;
					}
        			
        		} else {
            		set[i] = AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject());
        		}
			} catch (Exception e) {
				IOException newEx = new IOException();
				newEx.initCause(e);
				throw newEx;
			} 
        }
        if (unmappedIds > 0) {
        	int[] setMinusUnmapped = new int[size - unmappedIds];
        	int i = 0;
        	for (int j = 0; j < setMinusUnmapped.length; j++) {
        		setMinusUnmapped[j] = set[i];
        		while (setMinusUnmapped[j] == Integer.MAX_VALUE) {
        			i++;
        			setMinusUnmapped[j] = set[i];
        		}
        		i++;
        	}
        	set = setMinusUnmapped;
        }

        IntSet returnSet = new IntSet(set);
        return returnSet;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#contentsChanged(javax.swing.event.ListDataEvent)
	 */
	public void contentsChanged(ListDataEvent e) {
		if (e.getSource() != this) {
			handleChange(e);
		}
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
	 * @see org.dwfa.ace.api.I_IntSet#intervalAdded(javax.swing.event.ListDataEvent)
	 */
	public void intervalAdded(ListDataEvent e) {
		if (e.getSource() != this) {
			handleChange(e);
		}
		for (ListDataListener l: listeners) {
			l.intervalAdded(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_IntSet#intervalRemoved(javax.swing.event.ListDataEvent)
	 */
	public void intervalRemoved(ListDataEvent e) {
		if (e.getSource() != this) {
			handleChange(e);
		}
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
