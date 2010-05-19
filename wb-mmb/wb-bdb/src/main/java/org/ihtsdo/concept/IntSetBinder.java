package org.ihtsdo.concept;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IntSetBinder extends TupleBinding<Set<Integer>> {

	@Override
	public CopyOnWriteArraySet<Integer> entryToObject(TupleInput input) {
		int size = input.readInt();
		List<Integer> setValues = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++) {
			setValues.add(input.readInt());
		}
		return new CopyOnWriteArraySet<Integer>(setValues);
	}

	@Override
	public void objectToEntry(Set<Integer> set, TupleOutput output) {
		output.writeInt(set.size());
		for (int nid: set) {
			output.writeInt(nid);
		}
		
	}

}
