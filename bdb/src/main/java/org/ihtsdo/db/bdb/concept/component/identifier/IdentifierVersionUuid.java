package org.ihtsdo.db.bdb.concept.component.identifier;

import java.util.UUID;

import org.ihtsdo.db.bdb.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVersionUuid extends IdentifierVersion {
	private long mostSigBits;
	private long leastSigBits;
	
	public IdentifierVersionUuid(TupleInput input) {
		super(input.readInt());
		mostSigBits = input.readLong();
		leastSigBits = input.readLong();
	}

	@Override
	public IDENTIFIER_PART_TYPES getType() {
		return IDENTIFIER_PART_TYPES.UUID;
	}

	@Override
	protected void writeSourceIdToBdb(TupleOutput output) {
		output.writeLong(mostSigBits);
		output.writeLong(leastSigBits);
	}

	public UUID getUuid() {
		return new UUID(mostSigBits, leastSigBits);
	}

	@Override
	public Object getDenotation() {
		return new UUID(mostSigBits, leastSigBits);
	}

	@Override
	public void setDenotation(Object sourceDenotation) {
		UUID uuid = (UUID) sourceDenotation;
		mostSigBits = uuid.getMostSignificantBits();
		leastSigBits = uuid.getLeastSignificantBits();
	}

}
