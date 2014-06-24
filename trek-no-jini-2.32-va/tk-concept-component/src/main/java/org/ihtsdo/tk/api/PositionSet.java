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
import java.util.Iterator;
import java.util.Set;


/**
 * The Class PositionSet represents a serializable set of
 * <code>PositionBI</code>. Additionally it contains a <code>NidSet</code> of view paths nids
 * associated with those positions and an array of <code>PositionBI</code>/
 */
public class PositionSet implements PositionSetBI, Serializable {

    private static final int dataVersion = 1;
    private static final long serialVersionUID = 1L;

    /**
     * Reads a <code>PositionSet</code> object from an external source, including the
     * positions and path nids.
     *
     * @param in the input stream
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion == 1) {
            positions = (PositionBI[]) in.readObject();
            pathNids = (NidSetBI) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /**
     * Write this <code>PositionSet</code> object to an external source, including the
     * positions and path nids.
     *
     * @param out the output stream
     * @throws IOException signals that an I/O exception has occurred
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(positions);
        out.writeObject(pathNids);
    }
    PositionBI[] positions = new PositionBI[0];
    NidSetBI pathNids = new NidSet();

    /**
     * Instantiates a new position set based on the given
     * <code>positionSet</code>.
     *
     * @param positionSet the position set values to add to this position set
     */
    public PositionSet(Set<? extends PositionBI> positionSet) {
        super();
        if (positionSet != null) {
            this.positions = positionSet.toArray(this.positions);
            for (PositionBI p : positionSet) {
                pathNids.add(p.getPath().getConceptNid());
            }
        }
    }

    /**
     * Instantiates a new position set based on the given
     * <code>viewPosition</code>.
     *
     * @param viewPosition the view position to add to this position set
     */
    public PositionSet(PositionBI viewPosition) {
        if (viewPosition != null) {
            positions = new PositionBI[]{viewPosition};
            pathNids.add(viewPosition.getPath().getConceptNid());
        }
    }

    /**
     *
     * @return a set of view path nids
     */
    @Override
    public NidSetBI getViewPathNidSet() {
        return pathNids;
    }

    /**
     * Not supported by this class.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean add(PositionBI e) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported by this class.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean addAll(Collection<? extends PositionBI> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported by this class.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if this set of positions contains the specified
     * <code>PositionBI</code>.
     *
     * @param obj the <code>PositionBI</code>
     * @return <code>true</code> if this position set contains the specified
     * position
     * @see Set#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object obj) {
        for (PositionBI p : positions) {
            if (p.equals(obj)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Not supported by this class.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if this set contains a position.
     *
     * @return <code>true</code> if this set does not contain any positions
     * @see Set#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return positions.length == 0;
    }

    /**
     * The Class PositionIterator represents an iterator designed to iterate
     * over the array
     * <code>PositionBI</code> type elements in a <code>PositionSet</code>.
     */
    private class PositionIterator implements Iterator<PositionBI> {

        int index = 0;

        /**
         * If the array of positions has more elements.
         *
         * @return <code>true</code> if the set of positions has more elements
         * @see Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return index < positions.length;
        }

        /**
         * Gets the next position element in the array of positions.
         *
         * @return the next element
         * @see Iterator#next()
         */
        @Override
        public PositionBI next() {
            return positions[index++];
        }

        /**
         * Not supported by this class.
         *
         * @throws UnsupportedOperationException
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Gets a new
     * <code>PositionBI</code> type iterator.
     *
     * @return a <code>PositionBI</code> type iterator
     *
     * @see PositionSet.PositionIterator
     */
    @Override
    public Iterator<PositionBI> iterator() {
        return new PositionIterator();
    }

    /**
     * Not supported by this class.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean remove(Object obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported by this class.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported by this class.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the size of the array of positions associated with this position set.
     * @return the size of the array of positions
     * @see Set#size() 
     */
    @Override
    public int size() {
        return positions.length;
    }

    /**
     * Returns the array of positions associated position set.
     * @return an array positions associated with this position set
     * @see Set#toArray() 
     */
    @Override
    public Object[] toArray() {
        return positions.clone();
    }

    /**
     * Converts the set of positions associated with this position set to an array.
     * @param <T> the type of the new array
     * @param a the array to store the new elements
     * @return an array representing the set of positions
     * @see Set#toArray(T[]) 
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        return (T[]) positions.clone();
    }

    /**
     * Generates a string representation of this position set.
     * @return a string representation of this position set
     */
    @Override
    public String toString() {
        return Arrays.asList(positions).toString();
    }

    /**
     * Checks to see if this position set is equal to another position set.
     * @param obj the position set to check
     * @return <code>true</code> if the position sets are equal
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * Returns the hash code value for this set.
     * @return the hash code value for this set
     * 
     * @see Set#hashCode() 
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns the array of positions associated with this position set
     * @return the array of positions
     */
    @Override
    public PositionBI[] getPositionArray() {
        return positions;
    }
}
