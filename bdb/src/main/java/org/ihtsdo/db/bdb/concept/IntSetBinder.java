package org.ihtsdo.db.bdb.concept;

import org.dwfa.vodb.types.IntSet;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IntSetBinder extends TupleBinding<IntSet> {

	@Override
	public IntSet entryToObject(TupleInput input) {
		int size = input.readInt();
		int[] setValues = new int[size];
		for (int i = 0; i < size; i++) {
			setValues[i] = input.readInt();
		}
		return new IntSet(setValues);
	}

	@Override
	public void objectToEntry(IntSet set, TupleOutput output) {
		output.writeInt(set.getSetValues().length);
		for (int nid: set.getSetValues()) {
			output.writeInt(nid);
		}
		
	}

}
