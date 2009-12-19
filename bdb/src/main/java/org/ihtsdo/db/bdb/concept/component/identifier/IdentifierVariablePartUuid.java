package org.ihtsdo.db.bdb.concept.component.identifier;

import java.util.UUID;

import org.ihtsdo.db.bdb.concept.component.identifier.Identifier.VARIABLE_PART_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVariablePartUuid extends IdentifierVariablePart {
	private long mostSigBits;
	private long leastSigBits;
	
	public IdentifierVariablePartUuid(TupleInput input) {
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
