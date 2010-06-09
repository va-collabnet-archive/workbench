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

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.OpenBitSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class IdentifierSet implements I_RepresentIdSet {
	
	// TODO Consider using an implementation that uses
	// AtomicLongArray rather than simply a long[]...

	// Find power-of-two sizes best matching arguments
	private static int concurrencyLevel = 128;
	private static int sshift = 0;
	private static int ssize = 1;
	static {
		while (ssize < concurrencyLevel) {
			++sshift;
			ssize <<= 1;
		}
	}
	private static int segmentShift = 32 - sshift;
	private static int segmentMask = ssize - 1;
	private static ReentrantLock[] locks = new ReentrantLock[concurrencyLevel];
	static {
		for (int i = 0; i < concurrencyLevel; i++) {
			locks[i] = new ReentrantLock();
		}
	}

	protected OpenBitSet bitSet;
	private int offset = Integer.MIN_VALUE;
	private int toStringMax = 10;

	public IdentifierSet(OpenBitSet bitSet) {
		super();
		this.bitSet = bitSet;
	}

	public IdentifierSet(IdentifierSet anotherSet) {
		super();
		this.bitSet = (OpenBitSet) anotherSet.bitSet.clone();
	}

	public IdentifierSet() {
		bitSet = new OpenBitSet();
	}

	public IdentifierSet(int numBits) {
		bitSet = new OpenBitSet(numBits);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.ace.api.I_RepresentIdSet#isMember(int)
	 */
	public boolean isMember(int nid) {
		int index = nid + offset;
		bitSet.ensureCapacity(index);
		return bitSet.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.ace.api.I_RepresentIdSet#setMember(int)
	 */
	public void setMember(int nid) {
		int word = (nid >>> segmentShift) & segmentMask;
		locks[word].lock();
		try {
			int index = nid + offset;
			bitSet.ensureCapacity(index);
			bitSet.set(index);
		} finally {
			locks[word].unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.ace.api.I_RepresentIdSet#setNotMember(int)
	 */
	public void setNotMember(int nid) {
		int word = (nid >>> segmentShift) & segmentMask;
		locks[word].lock();
		try {
			int index = nid + offset;
			bitSet.ensureCapacity(index);
			bitSet.clear(index);
		} finally {
			locks[word].unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dwfa.ace.api.I_RepresentIdSet#and(org.dwfa.ace.api.IdentifierSet)
	 */
	public void and(I_RepresentIdSet other) {
		bitSet.and(((IdentifierSet) other).bitSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.ace.api.I_RepresentIdSet#or(org.dwfa.ace.api.IdentifierSet)
	 */
	public void or(I_RepresentIdSet other) {
		bitSet.or(((IdentifierSet) other).bitSet);
	}

	public I_RepresentIdSet duplicate() {
		return new IdentifierSet((OpenBitSet) bitSet.clone());
	}

	public I_IterateIds iterator() {
		return new NidIterator(bitSet.iterator());
	}

	private class NidIterator implements I_IterateIds {
		private DocIdSetIterator docIterator;

		private NidIterator(DocIdSetIterator docIterator) {
			super();
			this.docIterator = docIterator;
		}

		public boolean next() throws IOException {
			return docIterator.nextDoc() != DocIdSetIterator.NO_MORE_DOCS;
		}

		public int nid() {
			return docIterator.docID() + offset;
		}

		public boolean skipTo(int target) throws IOException {
			return docIterator.advance(target + offset) != DocIdSetIterator.NO_MORE_DOCS;
		}

		public String toString() {
			StringBuffer buff = new StringBuffer();
			buff.append("NidIterator: nid: ");
			buff.append(nid());
			buff.append("component: ");
			try {
				buff.append(Terms.get().getComponent(nid()).toString());
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			return buff.toString();
		}
	}

	public int size() {
		return (int) bitSet.cardinality();
	}

	public int cardinality() {
		return (int) bitSet.cardinality();
	}

	public int totalBits() {
		return bitSet.getNumWords() * 64;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("IdentifierSet: cardinality: ");
		buff.append(bitSet.cardinality());
		buff.append(" ");
		I_IterateIds idIterator = iterator();
		int count = 0;
		int cardinality = (int) bitSet.cardinality();
		try {
			buff.append("[");
			while (count < toStringMax && idIterator.next()) {
				try {
					buff.append(Terms.get().getComponent(idIterator.nid())
							.toString());
				} catch (TerminologyException e) {
					buff.append(e.toString());
				}
				count++;
				if (count == 10 && count < cardinality) {
					buff.append(", ...");
				} else if (count < cardinality) {
					buff.append(", ");
				}
			}
			buff.append("]");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buff.toString();
	}

	public int getToStringMax() {
		return toStringMax;
	}

	public void setToStringMax(int toStringMax) {
		this.toStringMax = toStringMax;
	}

	public void clear() {
		bitSet.clear(0, bitSet.capacity());
	}

	@Override
	public boolean equals(Object obj) {
		if (IdentifierSet.class.isAssignableFrom(obj.getClass())) {
			IdentifierSet another = (IdentifierSet) obj;
			return this.bitSet.equals(another.bitSet);
		}
		return super.equals(obj);
	}

	public String getDifferences(IdentifierSet that) throws IOException {
		StringBuffer buff = new StringBuffer();
		buff.append("this->that differences: \n");
		I_IterateIds thisItr = this.iterator();
		while (thisItr.next()) {
			if (!that.isMember(thisItr.nid())) {
				buff.append("   that missing: ");
				buff.append(thisItr.nid());
				buff.append("\n");
			}
		}

		buff.append("\n\nthat->this differences: \n");
		I_IterateIds thatItr = this.iterator();
		while (thatItr.next()) {
			if (!this.isMember(thatItr.nid())) {
				buff.append("   this missing: ");
				buff.append(thatItr.nid());
				buff.append("\n");
			}
		}
		return buff.toString();
	}
}
