package org.ihtsdo.db.bdb.concept.component;

import org.ihtsdo.db.bdb.concept.ConceptData;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class FormatVersionBinder extends TupleBinding<Integer> {

	private static FormatVersionBinder binder = new FormatVersionBinder();
	
    public static FormatVersionBinder getBinder() {
        return binder;
    }

	@Override
	public Integer entryToObject(TupleInput ti) {
		ti.skipFast(ConceptData.OFFSETS.FORMAT_VERSION.getOffset());
		return ti.readInt();
	}

	@Override
	public void objectToEntry(Integer arg0, TupleOutput to) {
		throw new UnsupportedOperationException();
	}

}
