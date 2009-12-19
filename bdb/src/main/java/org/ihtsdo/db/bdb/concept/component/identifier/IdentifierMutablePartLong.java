package org.ihtsdo.db.bdb.concept.component.identifier;

import org.ihtsdo.db.bdb.concept.component.identifier.Identifier.VARIABLE_PART_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierMutablePartLong extends IdentifierMutablePart {
	private long longId;

	public IdentifierMutablePartLong(TupleInput input) {
		super(input.readInt());
		longId = input.readLong();
	}

	@Override
	protected VARIABLE_PART_TYPES getType() {
		return VARIABLE_PART_TYPES.LONG;
	}

	@Override
	protected void writeSourceIdToBdb(TupleOutput output) {
		output.writeInt(statusAtPositionNid);
		output.writeLong(longId);
	}

}
