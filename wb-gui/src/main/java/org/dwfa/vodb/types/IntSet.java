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
package org.dwfa.vodb.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.AbstractListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;

public class IntSet implements ListDataListener, I_IntSet, NidSetBI {
    private Set<ListDataListener> listeners = new HashSet<ListDataListener>();

    private int[] setValues = new int[0];
    
    private static class LastContains {
        public LastContains(int key, boolean result) {
            lastContains = key;
            lastResult = result;
        }
        public LastContains() {
        }
        private int lastContains = Integer.MAX_VALUE;
        private boolean lastResult = false;
    }
    private LastContains lastContains = new LastContains();

    public IntSet(int[] values) {
        super();
        this.setValues = new int[values.length];
        System.arraycopy(values, 0, this.setValues, 0, values.length);
        Arrays.sort(this.setValues);
        boolean duplicates = false;
        for (int i = 1; i < values.length; i++) {
            if (this.setValues[i - 1] == this.setValues[i]) {
                duplicates = true;
            }
        }
        if (duplicates) {
            AceLog.getAppLog().info("Set array contains duplicates: " + toString());
            HashSet<Integer> hashSetValues = new HashSet<Integer>();
            for (int i : values) {
                hashSetValues.add(i);
            }
            this.setValues = new int[hashSetValues.size()];
            int i = 0;
            for (Integer value : hashSetValues) {
                this.setValues[i] = value;
                i++;
            }
            Arrays.sort(this.setValues);
        }
    }

    public IntSet() {
        super();
        this.setValues = new int[0];
    }

    public IntSet(Collection<PathBI> pathSet) {
        super();
        setValues = new int[pathSet.size()];
        int i = 0;
        for (PathBI p: pathSet) {
        	setValues[i++] = p.getConceptNid();
        }
        Arrays.sort(setValues);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        int count = 0;
        for (int i : setValues) {
            try {
            	if (Terms.get().hasConcept(i)) {
                    buf.append(Terms.get().getConcept(i).getInitialText());
            	} else {
                    buf.append(i);
            	}
            } catch (IOException e) {
                buf.append(i);
            } catch (TerminologyException e) {
                buf.append(i);
			}
            if (count++ < setValues.length - 1) {
                buf.append(", ");
            }
        }
        buf.append("]");
        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.api.I_IntSet#contains(int)
     */
    public boolean contains(int key) {
        LastContains last = lastContains;
        if (last.lastContains == key) {
            return last.lastResult;
        }
        boolean result = Arrays.binarySearch(setValues, key) >= 0;
        lastContains = new LastContains(key, result);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.api.I_IntSet#getSetValues()
     */
    public int[] getSetValues() {
        return setValues;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.api.I_IntSet#add(int)
     */
    public void add(int key) {
        addNoIntervalAdded(key);
        intervalAdded(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, setValues.length));
    }

    private synchronized void addNoIntervalAdded(int key) {
        lastContains = new LastContains();
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
                newSet[i] = setValues[i - 1];
            }
            setValues = newSet;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.api.I_IntSet#remove(int)
     */
    public void remove(int key) {
        removeNoIntervalRemoved(key);
        intervalRemoved(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, setValues.length));
    }

    private synchronized void removeNoIntervalRemoved(int key) {
        int insertionPoint = Arrays.binarySearch(setValues, key);
        if (insertionPoint < 0) {
            return;
        }
        lastContains = new LastContains();
        int[] newSet = new int[setValues.length - 1];
        for (int i = 0; i < insertionPoint; i++) {
            newSet[i] = setValues[i];
        }
        for (int i = insertionPoint + 1; i < setValues.length; i++) {
            newSet[i - 1] = setValues[i];
        }
        setValues = newSet;
        lastContains = new LastContains();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.api.I_IntSet#addAll(int[])
     */
    public synchronized IntSet addAll(int[] keys) {
        lastContains = new LastContains();
    	HashSet<Integer> members = getAsSet();
    	for (int key: keys) {
    		members.add(key);
    	}
    	replaceWithSet(members);
        intervalAdded(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, setValues.length));
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.api.I_IntSet#removeAll(int[])
     */
    public synchronized void removeAll(int[] keys) {
    	HashSet<Integer> members = getAsSet();
    	lastContains = new LastContains();
    	for (int key: keys) {
    		members.remove(key);
    	}
    	replaceWithSet(members);
    	lastContains = new LastContains();
    	intervalRemoved(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, setValues.length));
    }

	public HashSet<Integer> getAsSet() {
		HashSet<Integer> members = new HashSet<Integer>();
    	for (int elem: setValues) {
    		members.add(elem);
    	}
		return members;
	}

	public void replaceWithSet(HashSet<Integer> members) {
        lastContains = new LastContains();
		setValues = new int[members.size()];
    	int i = 0;
    	for (int elem: members) {
    		setValues[i++] = elem;
    	}
    	Arrays.sort(setValues);
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.api.I_IntSet#clear()
     */
    public void clear() {
        lastContains = new LastContains();
        setValues = new int[0];
        intervalRemoved(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, setValues.length));
    }

    public static void writeIntSet(ObjectOutputStream out, I_IntSet set) throws IOException {
        if (set == null) {
            out.writeInt(Integer.MIN_VALUE);
            return;
        }

        ArrayList<List<UUID>> outList = new ArrayList<List<UUID>>();
        for (int i : set.getSetValues()) {
            List<UUID> uuids = Terms.get().nativeToUuid(i);
            if (uuids != null && uuids.size() > 0) {
                outList.add(uuids);
            }
        }

        out.writeInt(outList.size());
        for (List<UUID> i : outList) {
            out.writeObject(i);
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
                        set[i] = Terms.get().uuidToNative((List<UUID>) in.readObject());
                    } catch (NoMappingException e) {
                        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                            AceLog.getAppLog().log(Level.FINE, e.getLocalizedMessage(), e);
                        } else {
                            AceLog.getAppLog().info(e.getLocalizedMessage());
                        }
                        unmappedIds++;
                        set[i] = Integer.MAX_VALUE;
                    }

                } else {
                    set[i] = Terms.get().uuidToNative((List<UUID>) in.readObject());
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
        Arrays.sort(set);
        IntSet returnSet = new IntSet(set);
        return returnSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.ace.api.I_IntSet#contentsChanged(javax.swing.event.ListDataEvent
     * )
     */
    public void contentsChanged(ListDataEvent e) {
        if (e.getSource() != this) {
            handleChange(e);
        }
        for (ListDataListener l : listeners) {
            l.contentsChanged(e);
        }
    }

    private void handleChange(ListDataEvent e) {
        AbstractListModel model = (AbstractListModel) e.getSource();
        clear();
        for (int i = 0; i < model.getSize(); i++) {
            I_GetConceptData cb = (I_GetConceptData) model.getElementAt(i);
            add(cb.getConceptNid());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.ace.api.I_IntSet#intervalAdded(javax.swing.event.ListDataEvent)
     */
    public void intervalAdded(ListDataEvent e) {
        if (e.getSource() != this) {
            handleChange(e);
        }
        for (ListDataListener l : listeners) {
            l.intervalAdded(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.ace.api.I_IntSet#intervalRemoved(javax.swing.event.ListDataEvent
     * )
     */
    public void intervalRemoved(ListDataEvent e) {
        if (e.getSource() != this) {
            handleChange(e);
        }
        for (ListDataListener l : listeners) {
            l.intervalRemoved(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.api.I_IntSet#addListDataListener(javax.swing.event.
     * ListDataListener)
     */
    public boolean addListDataListener(ListDataListener o) {
        return listeners.add(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.api.I_IntSet#removeListDataListener(javax.swing.event.
     * ListDataListener)
     */
    public boolean removeListDataListener(ListDataListener o) {
        return listeners.remove(o);
    }

	@Override
	public boolean equals(Object obj) {
		if (IntSet.class.isAssignableFrom(obj.getClass())) {
			IntSet another = (IntSet) obj;
			if (setValues.length != another.setValues.length) {
				return false;
			}
			for (int i = 0; i < setValues.length; i++) {
				if (setValues[i] != another.setValues[i]) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public int size() {
		return setValues.length;
	}

	@Override
	public int getMax() {
		if (setValues.length == 0) {
			return Integer.MAX_VALUE;
		}
		return setValues[setValues.length - 1];
	}
	
	@Override
	public int getMin() {
		if (setValues.length == 0) {
			return Integer.MIN_VALUE;
		}
		return setValues[0];
	}

	public boolean contiguous() {
		if (setValues.length == 0) {
			return true;
		}
		int prev = setValues[0] -1;
		for (int i: setValues) {
			if (prev != i-1) {
				return false;
			}
			prev = i;
		}
		return true;
	}
}
