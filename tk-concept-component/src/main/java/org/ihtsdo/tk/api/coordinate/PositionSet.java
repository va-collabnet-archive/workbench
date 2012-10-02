/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.api.coordinate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;

// TODO: Auto-generated Javadoc
/**
 * The Class PositionSet.
 */
public class PositionSet implements PositionSetBI, Serializable {
  
  /** The Constant dataVersion. */
  private static final int dataVersion = 1;

   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = 1L;
  
  /**
   * Read object.
   *
   * @param in the in
   * @throws IOException signals that an I/O exception has occurred.
   * @throws ClassNotFoundException the class not found exception
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();

      if (objDataVersion == 1) {
          positions = (PositionBI[]) in.readObject();
          pathNids = (NidSetBI) in.readObject();
      }
      else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   /**
    * Write object.
    *
    * @param out the out
    * @throws IOException signals that an I/O exception has occurred.
    */
   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(positions);
      out.writeObject(pathNids);
   }


    /** The positions. */
    PositionBI[] positions = new PositionBI[0];
    
    /** The path nids. */
    NidSetBI pathNids = new NidSet();

    /**
     * Instantiates a new position set.
     *
     * @param positionSet the position set
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
     * Instantiates a new position set.
     *
     * @param viewPosition the view position
     */
    public PositionSet(PositionBI viewPosition) {
        if (viewPosition != null) {
            positions = new PositionBI[]{viewPosition};
            pathNids.add(viewPosition.getPath().getConceptNid());
        }
    }

    
    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.PositionSetBI#getViewPathNidSet()
     */
    @Override
    public NidSetBI getViewPathNidSet() {
        return pathNids;
    }

    /* (non-Javadoc)
     * @see java.util.Set#add(java.lang.Object)
     */
    @Override
    public boolean add(PositionBI e) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Set#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(Collection<? extends PositionBI> c) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Set#clear()
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Set#contains(java.lang.Object)
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

    /* (non-Javadoc)
     * @see java.util.Set#containsAll(java.util.Collection)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Set#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return positions.length == 0;
    }

    /**
     * The Class PositionIterator.
     */
    private class PositionIterator implements Iterator<PositionBI> {

        /** The index. */
        int index = 0;

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return index < positions.length;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public PositionBI next() {
            return positions[index++];
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /* (non-Javadoc)
     * @see java.util.Set#iterator()
     */
    @Override
    public Iterator<PositionBI> iterator() {
        return new PositionIterator();
    }

    /* (non-Javadoc)
     * @see java.util.Set#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object obj) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Set#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Set#retainAll(java.util.Collection)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Set#size()
     */
    @Override
    public int size() {
        return positions.length;
    }

    /* (non-Javadoc)
     * @see java.util.Set#toArray()
     */
    @Override
    public Object[] toArray() {
        return positions.clone();
    }

    /* (non-Javadoc)
     * @see java.util.Set#toArray(T[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        return (T[]) positions.clone();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Arrays.asList(positions).toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
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
     * @see org.ihtsdo.tk.api.PositionSetBI#getPositionArray()
     */
    @Override
    public PositionBI[] getPositionArray() {
        return positions;
    }

}
