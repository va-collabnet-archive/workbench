package org.dwfa.vodb.bind;

import org.dwfa.ace.api.TimePathId;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class BranchTimeBinder extends TupleBinding {

	@Override
	public Object entryToObject(TupleInput ti) {
		int pathId = ti.readInt();
		int time = ti.readInt();
		return new TimePathId(time, pathId);
	}

	@Override
	public void objectToEntry(Object obj, TupleOutput to) {
		TimePathId tb = (TimePathId) obj;
		to.writeInt(tb.getPathId());
		to.writeInt(tb.getTime());
	}

}
