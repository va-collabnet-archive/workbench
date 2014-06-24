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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.ihtsdo.tk.Ts;


/**
 * The Class NidSet represents a serializable set of nids.
 */
public class NidSet implements NidSetBI, Serializable {

    private static final int dataVersion = 1;
    private static final long serialVersionUID = 1L;

    /**
     * Reads a
     * <code>NidSet</code> object from an external source.
     *
     * @param in the input stream
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion == 1) {
            setValues = (int[]) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /**
     * Write a
     * <code>NidSet</code> object to an external source.
     *
     * @param out the output stream
     * @throws IOException signals that an I/O exception has occurred
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(setValues);
    }
    private int[] setValues = new int[0];

    /**
     * Instantiates a new native id set based on
     * <code>another</code> nid set.
     *
     * @param another the other nid set specifying the values to use in
     * constructing this nid set
     */
    public NidSet(NidSet another) {
        this(another.setValues);
    }

    /**
     * Instantiates a new native id set based on the given
     * <code>values</code>.
     *
     * @param values an array specifying the values to use in constructing this
     * nid set
     */
    public NidSet(int[] values) {
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

    /**
     * Instantiates a new native id set.
     */
    public NidSet() {
        super();
        this.setValues = new int[0];
    }

    /**
     * Instantiates a new native id set based on the given
     * <code>paths</code>.
     *
     * @param paths the path concepts to use in constructing this nid set
     */
    public NidSet(Collection<PathBI> paths) {
        super();
        setValues = new int[paths.size()];
        int i = 0;
        for (PathBI p : paths) {
            setValues[i++] = p.getConceptNid();
        }
        Arrays.sort(setValues);
    }

    /**
     * Checks if this nid set contains the specified
     * <code>nid</code>.
     *
     * @param nid the nid in question
     * @return true if this nid set contains the specified nid
     */
    @Override
    public boolean contains(int nid) {
        return Arrays.binarySearch(setValues, nid) >= 0;
    }

    /**
     * Gets an array representing the nids in this set.
     *
     * @return an array representing the nids in this set
     */
    @Override
    public int[] getSetValues() {
        return setValues;
    }

    /**
     * Adds the specified
     * <code>nid</code> to this nid set.
     *
     * @param nid the nid to add
     */
    @Override
    public synchronized void add(int nid) {
        if (setValues.length == 0) {
            setValues = new int[1];
            setValues[0] = nid;
        } else {
            int insertionPoint = Arrays.binarySearch(setValues, nid);
            if (insertionPoint >= 0) {
                return;
            }
            insertionPoint = -insertionPoint - 1;
            int[] newSet = new int[setValues.length + 1];
            System.arraycopy(setValues, 0, newSet, 0, insertionPoint);
            newSet[insertionPoint] = nid;
            for (int i = insertionPoint + 1; i < newSet.length; i++) {
                newSet[i] = setValues[i - 1];
            }
            setValues = newSet;
        }
    }

    /**
     * Removes the specified
     * <code>nid</code> from this nid set.
     *
     * @param nid the nid to remove
     */
    @Override
    public void remove(int nid) {
        int insertionPoint = Arrays.binarySearch(setValues, nid);
        if (insertionPoint < 0) {
            return;
        }
        int[] newSet = new int[setValues.length - 1];
        System.arraycopy(setValues, 0, newSet, 0, insertionPoint);
        for (int i = insertionPoint + 1; i < setValues.length; i++) {
            newSet[i - 1] = setValues[i];
        }
        setValues = newSet;
    }

    /**
     * Adds all of the given
     * <code>nids</code> to this nid set.
     *
     * @param nids an array of nids to add
     * @return this nid set with the additional nids
     */
    @Override
    public synchronized NidSet addAll(int[] nids) {
        HashSet<Integer> members = getAsSet();
        for (int key : nids) {
            members.add(key);
        }
        replaceWithSet(members);
        return this;
    }

    /**
     * Removes all of the given
     * <code>nids</code> from this nid set.
     *
     * @param nids an array of nids to remove
     */
    @Override
    public synchronized void removeAll(int[] nids) {
        HashSet<Integer> members = getAsSet();
        for (int key : nids) {
            members.remove(key);
        }
        replaceWithSet(members);
    }

    /**
     * Returns this nid set as a
     * <code>HashSet</code>.
     *
     * @return a hash set representing this nid set
     */
    public HashSet<Integer> getAsSet() {
        HashSet<Integer> members = new HashSet<Integer>();
        for (int elem : setValues) {
            members.add(elem);
        }
        return members;
    }

    /**
     * Replaces the nids in this nid set with the given
     * <code>nids</code>.
     *
     * @param nids a hash set of the nids to replace with
     */
    public void replaceWithSet(HashSet<Integer> nids) {
        setValues = new int[nids.size()];
        int i = 0;
        for (int elem : nids) {
            setValues[i++] = elem;
        }
        Arrays.sort(setValues);
    }

    /**
     * Clears the nids in this set.
     */
    @Override
    public void clear() {
        setValues = new int[0];
    }

    /**
     * Checks to see if this nid set is equal to another nid set.
     *
     * @param obj the nid set to check
     * @return <code>true</code> if the nid sets are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (NidSetBI.class.isAssignableFrom(obj.getClass())) {
            NidSetBI another = (NidSetBI) obj;
            if (setValues.length != another.getSetValues().length) {
                return false;
            }
            for (int i = 0; i < setValues.length; i++) {
                if (setValues[i] != another.getSetValues()[i]) {
                    return false;
                }
            }
            return true;
        }
        return super.equals(obj);
    }

    /**
     * Returns the hash code value for this set.
     *
     * @return the hash code value for this set
     *
     * @see Set#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     *
     *
     * @return an int representing the number of nids in this nid set
     */
    @Override
    public int size() {
        return setValues.length;
    }

    /**
     *
     * @return the max value of a nid in this nid set
     */
    @Override
    public int getMax() {
        if (setValues.length == 0) {
            return Integer.MAX_VALUE;
        }
        return setValues[setValues.length - 1];
    }

    /**
     *
     * @return the min value of a nid in this nid set
     */
    @Override
    public int getMin() {
        if (setValues.length == 0) {
            return Integer.MIN_VALUE;
        }
        return setValues[0];
    }

    /**
     *
     * @return <code>true</code>, if the nids in this set are contiguous
     */
    @Override
    public boolean contiguous() {
        if (setValues.length == 0) {
            return true;
        }
        int prev = setValues[0] - 1;
        for (int i : setValues) {
            if (prev != i - 1) {
                return false;
            }
            prev = i;
        }
        return true;
    }

    /**
     * A string representing all of the concepts associated with the nids in
     * this set.
     *
     * @return a string representing all of the concepts associated with the
     * nids in this set
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        int count = 0;
        for (int i : setValues) {
            try {
                if (i < 0 && Ts.get().getConceptNidForNid(i) == i) {
                    buf.append(Ts.get().getConcept(i).toString());
                } else {
                    buf.append(i);
                }
            } catch (IOException e) {
                buf.append(i);
            }
            if (count++ < setValues.length - 1) {
                buf.append(", ");
            }
        }
        buf.append("]");
        return buf.toString();
    }
}
