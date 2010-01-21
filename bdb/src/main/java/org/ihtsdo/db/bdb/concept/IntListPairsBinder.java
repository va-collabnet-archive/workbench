package org.ihtsdo.db.bdb.concept;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntIterator;

import cern.colt.map.OpenLongObjectHashMap;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IntListPairsBinder extends TupleBinding<ArrayIntList> {

	private OpenLongObjectHashMap priorEntries;
	private ArrayIntList roList;

	@Override
	public ArrayIntList entryToObject(TupleInput input) {
		int size = input.readInt();
		ArrayIntList list;
		if (roList != null) {
			list = new ArrayIntList(size + roList.size());
			IntIterator roListItr = roList.iterator();
			while (roListItr.hasNext()) {
				list.add(roListItr.next());
			}
		} else {
			list = new ArrayIntList(size);
		}
		if (priorEntries != null) {
			for (int i = 0; i < size; i = i + 2) {
				int part1 = input.readInt();
				int part2 = input.readInt();
				if (!priorEntries.containsKey(toLong(part1, part2))) {
					list.add(part1);
					list.add(part2);
				}
			}
		} else {
			for (int i = 0; i < size; i++) {
				list.add(input.readInt());
			}
		}
		roList = null;
		priorEntries = null;
		return list;
	}

	@Override
	public void objectToEntry(ArrayIntList object, TupleOutput output) {
		if (priorEntries != null && priorEntries.size() > 0) {
			ArrayIntList listToWrite = new ArrayIntList(object.size());
			IntIterator roListItr = roList.iterator();
			while (roListItr.hasNext()) {
				int part1 = roListItr.next();
				int part2 = roListItr.next();
				if (!priorEntries.containsKey(toLong(part1, part2))) {
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
			IntIterator itr = object.iterator();
			while (itr.hasNext()) {
				output.writeInt(itr.next());
			}
		}
		roList = null;
		priorEntries = null;
	}

	public void setReadOnlyList(ArrayIntList roList) {
		this.roList = roList;
		if (roList == null) {
			priorEntries = new OpenLongObjectHashMap(0);
		}
		IntIterator itr = roList.iterator();
		priorEntries = new OpenLongObjectHashMap(roList.size() / 2);
		while (itr.hasNext()) {
			priorEntries.put(toLong(itr.next(), itr.next()), null);
		}
	}

	private long toLong(int part1, int part2) {
		long part1LongValue = part1;
		part1LongValue = part1LongValue & 0x00000000FFFFFFFFL;
		part1LongValue = part1LongValue << 32;
		long part2LongValue = part2;
		part2LongValue = part2LongValue & 0x00000000FFFFFFFFL;
		long returnValue = part1LongValue | part2LongValue;
		return returnValue;
	}

	public byte[] getBytes(ArrayIntList roList, ArrayIntList rwList) {
		setReadOnlyList(roList);
		TupleOutput output = new TupleOutput(); 
		objectToEntry(rwList, output);
		return output.toByteArray();
	}
}
