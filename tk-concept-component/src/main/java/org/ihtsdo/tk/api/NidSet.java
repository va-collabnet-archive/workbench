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

// TODO: Auto-generated Javadoc
/**
 * The Class NidSet represents a bit set optimized for use with nids. It should
 * be used for sets with large numbers of nids. For example, the lists of all
 * concept identifiers when iterating the database.
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
     * Instantiates a new native id set based on the given <code>values</code>.
     *
     * @param values an array specifying the values to use in
     * constructing this nid set
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
     * Instantiates a new native id set based on the given <code>paths</code>.
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

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.ace.api.I_IntSet#contains(int)
     */
    @Override
    public boolean contains(int key) {
        return Arrays.binarySearch(setValues, key) >= 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.ace.api.I_IntSet#getSetValues()
     */
    @Override
    public int[] getSetValues() {
        return setValues;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.NidSetBI#add(int)
     */
    @Override
    public synchronized void add(int key) {
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
            System.arraycopy(setValues, 0, newSet, 0, insertionPoint);
            newSet[insertionPoint] = key;
            for (int i = insertionPoint + 1; i < newSet.length; i++) {
                newSet[i] = setValues[i - 1];
            }
            setValues = newSet;
        }
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.NidSetBI#remove(int)
     */
    @Override
    public void remove(int key) {
        int insertionPoint = Arrays.binarySearch(setValues, key);
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

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.ace.api.I_IntSet#addAll(int[])
     */
    @Override
    public synchronized NidSet addAll(int[] keys) {
        HashSet<Integer> members = getAsSet();
        for (int key : keys) {
            members.add(key);
        }
        replaceWithSet(members);
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.ace.api.I_IntSet#removeAll(int[])
     */
    @Override
    public synchronized void removeAll(int[] keys) {
        HashSet<Integer> members = getAsSet();
        for (int key : keys) {
            members.remove(key);
        }
        replaceWithSet(members);
    }

    /**
     * Gets the as set.
     *
     * @return the as set
     */
    public HashSet<Integer> getAsSet() {
        HashSet<Integer> members = new HashSet<Integer>();
        for (int elem : setValues) {
            members.add(elem);
        }
        return members;
    }

    /**
     * Replace with set.
     *
     * @param nids the nids
     */
    public void replaceWithSet(HashSet<Integer> nids) {
        setValues = new int[nids.size()];
        int i = 0;
        for (int elem : nids) {
            setValues[i++] = elem;
        }
        Arrays.sort(setValues);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.ace.api.I_IntSet#clear()
     */
    @Override
    public void clear() {
        setValues = new int[0];
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.NidSetBI#size()
     */
    @Override
    public int size() {
        return setValues.length;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.NidSetBI#getMax()
     */
    @Override
    public int getMax() {
        if (setValues.length == 0) {
            return Integer.MAX_VALUE;
        }
        return setValues[setValues.length - 1];
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.NidSetBI#getMin()
     */
    @Override
    public int getMin() {
        if (setValues.length == 0) {
            return Integer.MIN_VALUE;
        }
        return setValues[0];
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.NidSetBI#contiguous()
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
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
