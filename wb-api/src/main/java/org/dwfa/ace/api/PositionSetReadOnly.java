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
package org.dwfa.ace.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class PositionSetReadOnly  implements Set<I_Position> {
	I_Position[] positions = new I_Position[0];

	public PositionSetReadOnly(Set<I_Position> positionSet) {
		super();
		if (positionSet != null) {
			this.positions = positionSet.toArray(this.positions);
		}
	}

	public PositionSetReadOnly(I_Position viewPosition) {
		if (viewPosition != null) {
			positions = new I_Position[] { viewPosition};
		}
	}

	@Override
	public boolean add(I_Position e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends I_Position> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
		for (I_Position p: positions) {
			if (p.equals(o)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		return positions.length == 0;
	}
	
	private class PositionIterator implements Iterator<I_Position> {
		int index = 0;
		@Override
		public boolean hasNext() {
			return index < positions.length;
		}

		@Override
		public I_Position next() {
			return positions[index++];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

	@Override
	public Iterator<I_Position> iterator() {
		return new PositionIterator();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return positions.length;
	}

	@Override
	public Object[] toArray() {
		return positions.clone();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		return (T[]) positions.clone();
	}

	@Override
	public String toString() {
		return Arrays.asList(positions).toString();
	}
	
	
}
