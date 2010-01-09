package org.ihtsdo.db.bdb.concept.component.identifier;

import org.ihtsdo.db.bdb.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.etypes.EIdentifierVersionLong;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVersionLong extends IdentifierVersion {
	private long longDenotation;

	public IdentifierVersionLong(TupleInput input) {
		super(input);
		longDenotation = input.readLong();
	}

	public IdentifierVersionLong(EIdentifierVersionLong idv) {
		super(idv);
		longDenotation = idv.getDenotation();
	}

	@Override
	public IDENTIFIER_PART_TYPES getType() {
		return IDENTIFIER_PART_TYPES.LONG;
	}

	@Override
	protected void writeSourceIdToBdb(TupleOutput output) {
		output.writeLong(longDenotation);
	}

	@Override
	public Object getDenotation() {
		return longDenotation;
	}

	@Override
	public void setDenotation(Object sourceDenotation) {
		longDenotation = (Long) sourceDenotation;
	}

	@Override
	public String toString() {
		return "denotation: " + longDenotation + " " + super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (IdentifierVersionLong.class.isAssignableFrom(obj.getClass())) {
			IdentifierVersionLong another = (IdentifierVersionLong) obj;
			return this.longDenotation == another.longDenotation && super.equals(another);
		}
		return false;
	}

}
