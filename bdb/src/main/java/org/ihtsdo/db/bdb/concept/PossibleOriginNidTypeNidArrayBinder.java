package org.ihtsdo.db.bdb.concept;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class PossibleOriginNidTypeNidArrayBinder extends TupleBinding<PossibleOriginNidTypeNidArray> {

	@Override
	public PossibleOriginNidTypeNidArray entryToObject(TupleInput input) {
		PossibleOriginNidTypeNidArray originNidTypeNidArray = new PossibleOriginNidTypeNidArray();
		int length = input.readInt();
		originNidTypeNidArray.originNids = new int[length];
		originNidTypeNidArray.typeNids = new int[length];
		for (int i = 0; i < length; i++) {
			originNidTypeNidArray.originNids[i] = input.readInt();
			originNidTypeNidArray.typeNids[i] = input.readInt();
		}
		return originNidTypeNidArray;
	}

	@Override
	public void objectToEntry(PossibleOriginNidTypeNidArray originNidTypeNidArray, TupleOutput output) {
		output.writeInt(originNidTypeNidArray.originNids.length);
		for (int i = 0; i < originNidTypeNidArray.originNids.length; i++) {
			output.writeInt(originNidTypeNidArray.originNids[i]);
			output.writeInt(originNidTypeNidArray.typeNids[i]);
		}
	}
}
