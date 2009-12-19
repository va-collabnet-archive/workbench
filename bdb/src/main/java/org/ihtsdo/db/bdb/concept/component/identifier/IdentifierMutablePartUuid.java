package org.ihtsdo.db.bdb.concept.component.identifier;

import java.util.UUID;

import org.ihtsdo.db.bdb.concept.component.identifier.Identifier.VARIABLE_PART_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierMutablePartUuid extends IdentifierMutablePart {
	private long mostSigBits;
	private long leastSigBits;
	
	public IdentifierMutablePartUuid(TupleInput input) {
		super(input.readInt());
		mostSigBits = input.readLong();
		leastSigBits = input.readLong();
	}

	@Override
	protected VARIABLE_PART_TYPES getType() {
		return VARIABLE_PART_TYPES.UUID;
	}

	@Override
	protected void writeSourceIdToBdb(TupleOutput output) {
		output.writeInt(statusAtPositionNid);
		output.writeLong(mostSigBits);
		output.writeLong(leastSigBits);
	}

	public UUID getUuid() {
		return new UUID(mostSigBits, leastSigBits);
	}

}
