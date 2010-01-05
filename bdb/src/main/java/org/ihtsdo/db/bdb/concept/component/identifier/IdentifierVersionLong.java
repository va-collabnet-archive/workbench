package org.ihtsdo.db.bdb.concept.component.identifier;

import org.ihtsdo.db.bdb.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.etypes.EIdentifierVersionLong;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVersionLong extends IdentifierVersion {
	private long longDenotation;

	public IdentifierVersionLong(TupleInput input) {
		super(input.readInt());
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


}
