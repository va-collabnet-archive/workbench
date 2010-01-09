package org.ihtsdo.db.bdb.concept.component.identifier;

import java.util.UUID;

import org.ihtsdo.db.bdb.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.etypes.EIdentifierVersionUuid;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVersionUuid extends IdentifierVersion {
	private long mostSigBits;
	private long leastSigBits;
	
	public IdentifierVersionUuid(TupleInput input) {
		super(input);
		mostSigBits = input.readLong();
		leastSigBits = input.readLong();
	}

	public IdentifierVersionUuid(EIdentifierVersionUuid idv) {
		super(idv);
		mostSigBits = idv.getDenotation().getMostSignificantBits();
		leastSigBits = idv.getDenotation().getLeastSignificantBits();
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

	@Override
	public String toString() {
		return "denotation: " + getUuid() + " " + super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (IdentifierVersionUuid.class.isAssignableFrom(obj.getClass())) {
			IdentifierVersionUuid another = (IdentifierVersionUuid) obj;
			return this.mostSigBits == another.mostSigBits && 
			this.leastSigBits == another.leastSigBits && super.equals(another);
		}
		return false;
	}

}
