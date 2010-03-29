package org.ihtsdo.concept;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntIterator;
import org.ihtsdo.db.util.NidPair;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IntListPairsBinder extends TupleBinding<List<? extends NidPair>> {
	
	private HashSet<NidPair> priorEntries;

	@Override
	public List<NidPair> entryToObject(TupleInput input) {
		int size = input.readInt();
		List<NidPair> newPairs = new ArrayList<NidPair>(size);
		for (int i = 0; i < size; i = i + 2) {
			newPairs.add(new NidPair(input.readInt(), input.readInt()));
		}
		if (priorEntries != null) {
		    priorEntries.addAll(newPairs);
		    newPairs.clear();
			newPairs.addAll(priorEntries);
	        priorEntries = null;
		}
		return newPairs;
	}

	@Override
	public void objectToEntry(List<? extends NidPair> object, TupleOutput output) {
		if (priorEntries != null && priorEntries.size() > 0) {
			ArrayIntList listToWrite = new ArrayIntList(object.size() * 2);
			for (NidPair pair: object) {
				if (!priorEntries.contains(pair)) {
					listToWrite.add(pair.getNid1());
					listToWrite.add(pair.getNid2());
				}
			}
			output.writeInt(listToWrite.size());
			IntIterator itr = listToWrite.iterator();
			while (itr.hasNext()) {
				output.writeInt(itr.next());
			}
		} else {
			output.writeInt(object.size() * 2);
            for (NidPair pair: object) {
                if (!priorEntries.contains(pair)) {
                    output.writeInt(pair.getNid1());
                    output.writeInt(pair.getNid2());
                }
            }
		}
		priorEntries = null;
	}

	public void setReadOnlyList(List<? extends NidPair> roList) {
		if (roList == null) {
			priorEntries = new HashSet<NidPair>();
		} else {
			priorEntries = new HashSet<NidPair>(roList);
		}
	}

	public byte[] getBytes(List<? extends NidPair> roList, List<? extends NidPair> rwList) {
		setReadOnlyList(roList);
		TupleOutput output = new TupleOutput();
		objectToEntry(rwList, output);
		return output.toByteArray();
	}
}
