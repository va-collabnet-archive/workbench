package org.ihtsdo.db.bdb.concept.component.identifier;

import java.util.UUID;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.etypes.EIdentifierVersionUuid;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVersionUuid extends IdentifierVersion {
	private int uNid;
	
	public IdentifierVersionUuid(TupleInput input) {
		super(input);
		uNid = input.readInt();
	}

	public IdentifierVersionUuid(EIdentifierVersionUuid idv) {
		super(idv);
		uNid = Bdb.getUuidsToNidMap().getUNid(idv.getDenotation());
	}

	@Override
	public IDENTIFIER_PART_TYPES getType() {
		return IDENTIFIER_PART_TYPES.UUID;
	}

	@Override
	protected void writeSourceIdToBdb(TupleOutput output) {
		output.writeInt(uNid);
	}

	public UUID getUuid() {
		return Bdb.getUuidDb().getUuid(uNid);
	}

	@Override
	public Object getDenotation() {
		return Bdb.getUuidDb().getUuid(uNid);
	}

	@Override
	public void setDenotation(Object sourceDenotation) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "denotation: " + getUuid() + " " + super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (IdentifierVersionUuid.class.isAssignableFrom(obj.getClass())) {
			IdentifierVersionUuid another = (IdentifierVersionUuid) obj;
			return this.uNid == another.uNid && super.equals(another);
		}
		return false;
	}

}
