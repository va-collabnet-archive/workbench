package org.ihtsdo.db.bdb.concept;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RefsetNidMemberNidForConceptArrayBinder extends TupleBinding<RefsetNidMemberNidForConceptArray> {

	@Override
	public RefsetNidMemberNidForConceptArray entryToObject(TupleInput input) {
		RefsetNidMemberNidForConceptArray result = new RefsetNidMemberNidForConceptArray();
		int length = input.readInt();
		result.refsetNid = new int[length];
		result.memberNid = new int[length];
		for (int i = 0; i < length; i++) {
			result.refsetNid[i] = input.readInt();
			result.memberNid[i] = input.readInt();
		}
		return result;
	}

	@Override
	public void objectToEntry(RefsetNidMemberNidForConceptArray object, TupleOutput output) {
		output.writeInt(object.refsetNid.length);
		for (int i = 0; i < object.refsetNid.length; i++) {
			output.writeInt(object.refsetNid[i]);
			output.writeInt(object.memberNid[i]);
		}
	}

}
