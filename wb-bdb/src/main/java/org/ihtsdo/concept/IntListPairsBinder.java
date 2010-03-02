package org.ihtsdo.concept;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntIterator;
import org.dwfa.util.HashFunction;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IntListPairsBinder extends TupleBinding<List<? extends Integer>> {
	
	private static class Pair {
		int first;
		int second;
		public Pair(int first, int second) {
			super();
			this.first = first;
			this.second = second;
		}

		@Override
		public boolean equals(Object obj) {
			if (Pair.class.isAssignableFrom(obj.getClass())) {
				Pair another = (Pair) obj;
				return first == another.first && second == another.second;
			}
			return super.equals(obj);
		}
		
		@Override
		public int hashCode() {
			return HashFunction.hashCode(new int[] { first, second });
		}
		
		public String toString() {
			return "first: " + first + " second: " + second;
		}
	}
	
	private HashSet<Pair> priorEntries;

	@Override
	public List<Integer> entryToObject(TupleInput input) {
		int size = input.readInt();
		HashSet<Pair> newPairs = new HashSet<Pair>(size);
		for (int i = 0; i < size; i = i + 2) {
			newPairs.add(new Pair(input.readInt(), input.readInt()));
		}
		if (priorEntries != null) {
			newPairs.addAll(priorEntries);
		} 
		priorEntries = null;
		List<Integer> newList = new ArrayList<Integer>(newPairs.size() * 2);
		for (Pair p: newPairs) {
			newList.add(p.first);
			newList.add(p.second);
		}
		return newList;
	}

	@Override
	public void objectToEntry(List<? extends Integer> object, TupleOutput output) {
		if (priorEntries != null && priorEntries.size() > 0) {
			ArrayIntList listToWrite = new ArrayIntList(object.size());
			Iterator<? extends Integer> roListItr = object.iterator();
			while (roListItr.hasNext()) {
				int part1 = roListItr.next();
				int part2 = roListItr.next();
				if (!priorEntries.contains(new Pair(part1, part2))) {
					listToWrite.add(part1);
					listToWrite.add(part2);
				}
			}
			output.writeInt(listToWrite.size());
			IntIterator itr = listToWrite.iterator();
			while (itr.hasNext()) {
				output.writeInt(itr.next());
			}
		} else {
			output.writeInt(object.size());
			Iterator<? extends Integer> itr = object.iterator();
			while (itr.hasNext()) {
				output.writeInt(itr.next());
			}
		}
		priorEntries = null;
	}

	public void setReadOnlyList(List<? extends Integer> roList) {
		if (roList == null) {
			priorEntries = new HashSet<Pair>();
		} else {
			Iterator<? extends Integer> itr = roList.iterator();
			priorEntries = new HashSet<Pair>(roList.size() / 2);
			while (itr.hasNext()) {
				priorEntries.add(new Pair(itr.next(), itr.next()));
			}
		}
	}

	public byte[] getBytes(List<? extends Integer> roList, List<? extends Integer> rwList) {
		setReadOnlyList(roList);
		TupleOutput output = new TupleOutput();
		objectToEntry(rwList, output);
		return output.toByteArray();
	}
}
