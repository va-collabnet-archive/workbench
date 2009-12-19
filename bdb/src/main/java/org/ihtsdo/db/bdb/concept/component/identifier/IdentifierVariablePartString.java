package org.ihtsdo.db.bdb.concept.component.identifier;

import org.ihtsdo.db.bdb.concept.component.identifier.Identifier.VARIABLE_PART_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVariablePartString extends IdentifierVariablePart {

	private String stringId;

	public IdentifierVariablePartString(TupleInput input) {
		super(input.readInt());
		stringId = input.readString();
	}

	@Override
	protected VARIABLE_PART_TYPES getType() {
		return VARIABLE_PART_TYPES.STRING;
	}

	@Override
	protected void writeSourceIdToBdb(TupleOutput output) {
		output.writeInt(statusAtPositionNid);
		output.writeString(stringId);
	}

}
