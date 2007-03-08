package org.dwfa.vodb.bind;

import java.util.UUID;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class UuidBinding extends TupleBinding {

	public UUID entryToObject(TupleInput ti) {
		return new UUID(ti.readLong(),ti.readLong());
	}

	public void objectToEntry(Object obj, TupleOutput to) {
		UUID id = (UUID) obj;
		to.writeLong(id.getMostSignificantBits());
		to.writeLong(id.getLeastSignificantBits());
	}

}
