/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.tk.Ts;

/**
 * The Class NidList represents represents a serializable list of nids.
 */
public class NidList implements NidListBI, Serializable {

    private static final int dataVersion = 1;
    private List<Integer> listValues = new ArrayList<Integer>(2);

    /**
     * Instantiates a new nid list using the specified
     * <code>values</code>.
     *
     * @param values the values to add to this nid list
     */
    public NidList(int[] values) {
        super();
        for (int i : values) {
            this.listValues.add(i);
        }
    }

    /**
     * Instantiates a new nid list.
     */
    public NidList() {
        super();
    }

    /**
     * Writes a
     * <code>NidList</code> object to an external source.
     *
     * @param out the output stream
     * @param nidList the nid list to write
     * @throws IOException signals that an I/O exception has occurred
     */
    public static void writeIntList(ObjectOutputStream out, NidList nidList) throws IOException {
        if (nidList == null) {
            out.writeInt(Integer.MIN_VALUE);
            return;
        }

        ArrayList<List<UUID>> outList = new ArrayList<List<UUID>>();
        for (int i : nidList.getListValues()) {
            if (i != 0 && i != Integer.MAX_VALUE) {
                outList.add(Ts.get().getUuidsForNid(i));
            }
        }

        out.writeInt(outList.size());
        for (List<UUID> i : outList) {
            out.writeObject(i);
        }
    }

    /**
     * Reads a
     * <code>NidList</code> object from an external source. Ignores nids that
     * don't map to a uuid in the current environment.
     *
     * @param in the input stream
     * @return a nid list representing the imported nids
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public static NidListBI readIntListIgnoreMapErrors(ObjectInputStream in) throws IOException, ClassNotFoundException {
        return readIntList(in, true);
    }

    /**
     * Reads a
     * <code>NidList</code> object from an external source. Returns a nid list
     * of the same size as the one read. If the nid does not map to a uuid the
     * current, a
     * <code>null</code> value will be used.
     *
     * @param in the input stream
     * @return a nid list representing the imported nids
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public static NidListBI readIntListStrict(ObjectInputStream in) throws IOException, ClassNotFoundException {
        return readIntList(in, true);
    }

    /**
     * Reads a
     * <code>NidList</code> object from an external source. Can specify if
     * mapping errors should be ignored.
     *
     * @param in the input stream
     * @param ignoreMappingErrors set to <code>true</code> to skip nids which do
     * not map to a uuid in the current environment, <code>false</code> will add
     * a <code>null</code> element if no mapping is found
     * @return a nid list representing the imported nids
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @SuppressWarnings("unchecked")
    private static NidListBI readIntList(ObjectInputStream in, boolean ignoreMappingErrors) throws IOException,
            ClassNotFoundException {
        int unmappedIds = 0;
        int size = in.readInt();
        if (size == Integer.MIN_VALUE) {
            return new NidList();
        }
        int[] list = new int[size];
        for (int i = 0; i < size; i++) {
            if (ignoreMappingErrors) {
                Object uuidObj = in.readObject();
                if (uuidObj != null) {
                    if (List.class.isAssignableFrom(uuidObj.getClass())) {
                        list[i] = Ts.get().getNidForUuids((List<UUID>) uuidObj);
                    }
                }


            } else {
                list[i] = Ts.get().getNidForUuids((List<UUID>) in.readObject());
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
        NidListBI returnSet = new NidList(list);
        return returnSet;
    }

    /**
     *
     * @param index the index at which the specified nid is to be inserted
     * @param nid the nid to add
     */
    @Override
    public void add(int index, Integer element) {
        listValues.add(index, element);
    }

    /**
     *
     * @param nid the nid to add
     * @return <code>true</code>, if the collection changed as a result
     */
    @Override
    public boolean add(Integer nid) {
        boolean returnValue = listValues.add(nid);
        return returnValue;
    }

    /**
     *
     * @param nids a collection of nids to add
     * @return <code>true</code>, if the collection changed as a result
     */
    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        boolean returnValue = listValues.addAll(c);
        return returnValue;
    }

    /**
     *
     * @param index the index at which the specified nid is to be inserted
     * @param nids a collection of nids to add
     * @return <code>true</code>, if successful
     */
    @Override
    public boolean addAll(int index, Collection<? extends Integer> c) {
        boolean returnValue = listValues.addAll(index, c);
        return returnValue;
    }

    /**
     *
     */
    @Override
    public void clear() {
        int oldSize = listValues.size();
        listValues.clear();
    }

    /**
     *
     * @param obj the nid in question
     * @return <code>true</code>, if this nid list contains the specified nid
     */
    @Override
    public boolean contains(Object obj) {
        return listValues.contains(obj);
    }

    /**
     *
     * @param c the nids in question
     * @return <code>true</code>, if this nid list contains the specified nids
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return listValues.containsAll(c);
    }

    /**
     *
     * @param index the index of the nid to return
     * @return the nid at the specified position
     */
    @Override
    public Integer get(int index) {
        return listValues.get(index);
    }

    /**
     *
     * @param obj the nid to search for
     * @return the index of the first occurrence of the specified nid, or -1 if
     * this nid list does not contain the nid
     */
    @Override
    public int indexOf(Object obj) {
        return listValues.indexOf(obj);
    }

    /**
     *
     * @return <code>true</code>, if this nid list contains no nids
     */
    @Override
    public boolean isEmpty() {
        return listValues.isEmpty();
    }

    /**
     *
     * @return an iterator that will iterate of the nids in this nid list in the
     * proper sequence
     */
    @Override
    public Iterator<Integer> iterator() {
        return listValues.iterator();
    }

    /**
     *
     * @param obj the nid to search for
     * @return the index of the last occurrence of the specified nid in this nid
     * list, or -1 if this nid list does not contain the nid
     */
    @Override
    public int lastIndexOf(Object obj) {
        return listValues.lastIndexOf(obj);
    }

    /**
     *
     * @return Returns a list iterator over the nids in this nid list (in proper
     * sequence)
     */
    @Override
    public ListIterator<Integer> listIterator() {
        return listValues.listIterator();
    }

    /**
     *
     * @param index the index of the first nid to be returned
     * @return a list iterator of the nids in this nid list (in proper
     * sequence), starting at the specified position in this nid list
     */
    @Override
    public ListIterator<Integer> listIterator(int index) {
        return listValues.listIterator(index);
    }

    /**
     *
     * @param index the index of the nid to remove
     * @return the nid previously at the specified position
     */
    @Override
    public Integer remove(int index) {
        int oldSize = listValues.size();
        Integer returnValue = listValues.remove(index);
        return returnValue;
    }

    /**
     *
     * @param obj nid to be removed from this list
     * @return <code>true</code>, if this nid list contained the specified nid
     */
    @Override
    public boolean remove(Object obj) {
        int oldSize = listValues.size();
        boolean returnValue = listValues.remove(obj);
        return returnValue;
    }

    /**
     *
     * @param c the nids to remove
     * @return <code>true</code>, if this nid list changed as a result
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        int oldSize = listValues.size();
        boolean returnValue = listValues.removeAll(c);
        return returnValue;
    }

    /**
     *
     * @param c the nids to retain
     * @return <code>true</code>, if this nid list changed as a result
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        int oldSize = listValues.size();
        boolean returnValue = listValues.retainAll(c);
        return returnValue;
    }

    /**
     *
     * @param index the index of the nid to replace
     * @param nid the new nid
     * @return the nid previously at the specified position
     */
    @Override
    public Integer set(int index, Integer element) {
        Integer old = listValues.set(index, element);
        return old;
    }

    /**
     *
     * @return an int representing the number of nids in this nid list
     */
    @Override
    public int size() {
        return listValues.size();
    }

    /**
     *
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex high endpoint (exclusive) of the subList
     * @return a view of the specified range within this nid list
     * @see List#subList(int, int)
     */
    @Override
    public List<Integer> subList(int fromIndex, int toIndex) {
        return listValues.subList(fromIndex, toIndex);
    }

    /**
     *
     * @return an array representing the nids in this nid list
     */
    @Override
    public Object[] toArray() {
        return listValues.toArray();
    }

    /**
     *
     * @param <T> the type of the new array
     * @param a the array to store the new elements
     * @return an array representing the set of positions
     * @see List#toArray(T[])
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return listValues.toArray(a);
    }

    /**
     *
     * @return a list representing the nids in this nid list
     */
    @Override
    public List<Integer> getListValues() {
        return listValues;
    }

    /**
     *
     * @return an array representing the nids in this nid list
     */
    @Override
    public int[] getListArray() {
        int[] listArray = new int[listValues.size()];
        for (int i = 0; i < listArray.length; i++) {
            listArray[i] = listValues.get(i);
        }
        return listArray;
    }

    /**
     * Generates a string representation the concepts associated with the nids
     * in this nid list.
     *
     * @return a string representation the concepts associated with the nids
     * in this nid list
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        int count = 0;
        for (int i : listValues) {
            try {
                buf.append(Ts.get().getConcept(i).toString());
                if (count++ < listValues.size() - 1) {
                    buf.append(", ");
                }
            } catch (IOException ex) {
                Logger.getLogger(NidList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        buf.append("]");
        return buf.toString();
    }
}
