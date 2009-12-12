package org.ihtsdo.db.bdb.concept;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RefsetNidMemberNidForComponentNidArrayBinder extends TupleBinding<RefsetNidMemberNidForComponentNidArray> {

	@Override
	public RefsetNidMemberNidForComponentNidArray entryToObject(TupleInput input) {
		RefsetNidMemberNidForComponentNidArray result = new RefsetNidMemberNidForComponentNidArray();
		int length = input.readInt();
		result.refsetNid = new int[length];
		result.memberNid = new int[length];
		result.componentNid = new int[length];
		for (int i = 0; i < length; i++) {
			result.refsetNid[i] = input.readInt();
			result.memberNid[i] = input.readInt();
			result.componentNid[i] = input.readInt();
		}
		return result;
	}

	@Override
	public void objectToEntry(RefsetNidMemberNidForComponentNidArray object,
			TupleOutput output) {
		output.writeInt(object.refsetNid.length);
		for (int i = 0; i < object.refsetNid.length; i++) {
			output.writeInt(object.refsetNid[i]);
			output.writeInt(object.memberNid[i]);
			output.writeInt(object.componentNid[i]);
		}
	}
}
