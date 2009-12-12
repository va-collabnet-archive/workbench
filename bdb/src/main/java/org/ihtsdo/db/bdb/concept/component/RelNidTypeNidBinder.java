package org.ihtsdo.db.bdb.concept.component;

import cern.colt.map.OpenIntIntHashMap;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RelNidTypeNidBinder extends TupleBinding<OpenIntIntHashMap>  {

	private static RelNidTypeNidBinder binder = new RelNidTypeNidBinder();
	
	public static TupleBinding<OpenIntIntHashMap> getBinder() {
		return binder;
	}

	@Override
	public OpenIntIntHashMap entryToObject(TupleInput input) {
		int size = input.readInt();
		OpenIntIntHashMap relNidToTypeNidMap = new OpenIntIntHashMap(size);
		for (int i = 0; i < size; i++) {
			relNidToTypeNidMap.put(input.readInt(), input.readInt());
		}
		return relNidToTypeNidMap;
	}

	@Override
	public void objectToEntry(OpenIntIntHashMap relNidToTypeNidMap, TupleOutput output) {
		output.writeInt(relNidToTypeNidMap.size());
		for (int relNid: relNidToTypeNidMap.keys().elements()) {
			output.writeInt(relNid);
			output.writeInt(relNidToTypeNidMap.get(relNid));
		}
	}
	
}
