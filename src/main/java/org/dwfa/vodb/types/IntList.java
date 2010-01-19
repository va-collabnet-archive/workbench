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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.list.TerminologyIntListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;

import com.sleepycat.je.DatabaseException;

public class IntList implements ListDataListener, I_IntList {
    private Set<ListDataListener> listeners = new HashSet<ListDataListener>();

    private List<Integer> listValues = new ArrayList<Integer>(2);

    public IntList(int[] values) {
        super();
        for (int i : values) {
            this.listValues.add(i);
        }
    }

    public IntList() {
        super();
    }

    public static void writeIntList(ObjectOutputStream out, I_IntList list) throws IOException {
        if (list == null) {
            out.writeInt(Integer.MIN_VALUE);
            return;
        }

        ArrayList<List<UUID>> outList = new ArrayList<List<UUID>>();
        for (int i : list.getListValues()) {
            try {
                outList.add(AceConfig.getVodb().nativeToUuid(i));
            } catch (DatabaseException e) {
                AceLog.getAppLog().log(Level.WARNING, e.toString(), e);
            }
        }

        out.writeInt(outList.size());
        for (List<UUID> i : outList) {
            out.writeObject(i);
        }
    }

    public static IntList readIntListIgnoreMapErrors(ObjectInputStream in) throws IOException, ClassNotFoundException {
        return readIntList(in, true);
    }

    public static IntList readIntListStrict(ObjectInputStream in) throws IOException, ClassNotFoundException {
        return readIntList(in, true);
    }

    @SuppressWarnings("unchecked")
    private static IntList readIntList(ObjectInputStream in, boolean ignoreMappingErrors) throws IOException,
            ClassNotFoundException {
        int unmappedIds = 0;
        int size = in.readInt();
        if (size == Integer.MIN_VALUE) {
            return new IntList();
        }
        int[] list = new int[size];
        for (int i = 0; i < size; i++) {
            try {
                if (ignoreMappingErrors) {
                    try {
                        Object uuidObj = in.readObject();
                        if (List.class.isAssignableFrom(uuidObj.getClass())) {
                            list[i] = AceConfig.getVodb().uuidToNative((List<UUID>) uuidObj);
                        } else {
                            AceLog.getAppLog().alertAndLogException(
                                new Exception("<html>Expecting List<UUID>. Found:<br>" + uuidObj));
                        }
                    } catch (NoMappingException e) {
                        AceLog.getAppLog().log(Level.FINE, e.getLocalizedMessage(), e);
                        unmappedIds++;
                        list[i] = Integer.MAX_VALUE;
                    }

                } else {
                    list[i] = AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject());
                }
            } catch (TerminologyException e) {
                IOException newEx = new IOException();
                newEx.initCause(e);
                throw newEx;
            }
        }
        if (unmappedIds > 0) {
            int[] listMinusUnmapped = new int[size - unmappedIds];
            int i = 0;
            for (int j = 0; j < listMinusUnmapped.length; j++) {
                listMinusUnmapped[j] = list[i];
                while (listMinusUnmapped[j] == Integer.MAX_VALUE) {
                    i++;
                    listMinusUnmapped[j] = list[i];
                }
                i++;
            }
            list = listMinusUnmapped;
        }
        IntList returnSet = new IntList(list);
        return returnSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * 
     * 
     * 
     * 
     * 
     * org.dwfa.vodb.types.I_IntList#contentsChanged(javax.swing.event.ListDataEvent
     * )
     */
    public void contentsChanged(ListDataEvent e) {
        handleChange(e);
        for (ListDataListener l : listeners) {
            l.contentsChanged(e);
        }
    }

    private void handleChange(ListDataEvent e) {
        if (e.getSource() == this) {
            return;
        }
        ListModel model = (ListModel) e.getSource();
        if (TerminologyIntListModel.class.isAssignableFrom(e.getSource().getClass())) {
            TerminologyIntListModel intListModel = (TerminologyIntListModel) e.getSource();
            if (intListModel.getElements() == this) {
                return;
            }
        }
        clear();
        for (int i = 0; i < model.getSize(); i++) {
            I_GetConceptData cb = (I_GetConceptData) model.getElementAt(i);
            add(cb.getConceptId());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * 
     * 
     * 
     * 
     * 
     * org.dwfa.vodb.types.I_IntList#intervalAdded(javax.swing.event.ListDataEvent
     * )
     */
    public void intervalAdded(ListDataEvent e) {
        handleChange(e);
        for (ListDataListener l : listeners) {
            l.intervalAdded(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * 
     * 
     * 
     * 
     * 
     * org.dwfa.vodb.types.I_IntList#intervalRemoved(javax.swing.event.ListDataEvent
     * )
     */
    public void intervalRemoved(ListDataEvent e) {
        handleChange(e);
        for (ListDataListener l : listeners) {
            l.intervalRemoved(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.vodb.types.I_IntList#addListDataListener(javax.swing.event.
     * ListDataListener)
     */
    public boolean addListDataListener(ListDataListener o) {
        return listeners.add(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_IntList#removeListDataListener(javax.swing.event
     * .ListDataListener)
     */
    public boolean removeListDataListener(ListDataListener o) {
        return listeners.remove(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#add(int, java.lang.Integer)
     */
    public void add(int index, Integer element) {
        listValues.add(index, element);
        handleChange(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index - 1, listValues.size()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#add(java.lang.Integer)
     */
    public boolean add(Integer o) {
        boolean returnValue = listValues.add(o);
        intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, listValues.size() - 1, listValues.size()));
        return returnValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends Integer> c) {
        boolean returnValue = listValues.addAll(c);
        intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, listValues.size()));
        return returnValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#addAll(int, java.util.Collection)
     */
    public boolean addAll(int index, Collection<? extends Integer> c) {
        boolean returnValue = listValues.addAll(index, c);
        intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, listValues.size()));
        return returnValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#clear()
     */
    public void clear() {
        int oldSize = listValues.size();
        listValues.clear();
        intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, oldSize));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        return listValues.contains(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c) {
        return listValues.containsAll(c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#get(int)
     */
    public Integer get(int index) {
        return listValues.get(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#indexOf(java.lang.Object)
     */
    public int indexOf(Object o) {
        return listValues.indexOf(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#isEmpty()
     */
    public boolean isEmpty() {
        return listValues.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#iterator()
     */
    public Iterator<Integer> iterator() {
        return listValues.iterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object o) {
        return listValues.lastIndexOf(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#listIterator()
     */
    public ListIterator<Integer> listIterator() {
        return listValues.listIterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#listIterator(int)
     */
    public ListIterator<Integer> listIterator(int index) {
        return listValues.listIterator(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#remove(int)
     */
    public Integer remove(int index) {
        int oldSize = listValues.size();
        Integer returnValue = listValues.remove(index);
        intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, oldSize));
        return returnValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        int oldSize = listValues.size();
        boolean returnValue = listValues.remove(o);
        intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, oldSize));
        return returnValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
        int oldSize = listValues.size();
        boolean returnValue = listValues.removeAll(c);
        intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, oldSize));
        return returnValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
        int oldSize = listValues.size();
        boolean returnValue = listValues.retainAll(c);
        intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, oldSize));
        return returnValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#set(int, java.lang.Integer)
     */
    public Integer set(int index, Integer element) {
        Integer old = listValues.set(index, element);
        contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index));
        return old;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#size()
     */
    public int size() {
        return listValues.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#subList(int, int)
     */
    public List<Integer> subList(int fromIndex, int toIndex) {
        return listValues.subList(fromIndex, toIndex);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#toArray()
     */
    public Object[] toArray() {
        return listValues.toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#toArray(T[])
     */
    public <T> T[] toArray(T[] a) {
        return listValues.toArray(a);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IntList#getListValues()
     */
    public List<Integer> getListValues() {
        return listValues;
    }

    public int[] getListArray() {
        int[] listArray = new int[listValues.size()];
        for (int i = 0; i < listArray.length; i++) {
            listArray[i] = listValues.get(i);
        }
        return listArray;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        int count = 0;
        for (int i : listValues) {
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
            if (count++ < listValues.size() - 1) {
                buf.append(", ");
            }
        }
        buf.append("]");
        return buf.toString();
    }

}
