package org.ihtsdo.db.bdb.concept.component.identifier;

import org.ihtsdo.db.bdb.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.etypes.EIdentifierVersionString;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVersionString extends IdentifierVersion {

	private String stringDenotation;

	public IdentifierVersionString(TupleInput input) {
		super(input);
		stringDenotation = input.readString();
	}

	public IdentifierVersionString(EIdentifierVersionString idv) {
		super(idv);
		stringDenotation = idv.getDenotation();
	}

	@Override
	public IDENTIFIER_PART_TYPES getType() {
		return IDENTIFIER_PART_TYPES.STRING;
	}

	@Override
	protected void writeSourceIdToBdb(TupleOutput output) {
		output.writeString(stringDenotation);
	}

	@Override
	public Object getDenotation() {
		return stringDenotation;
	}

	@Override
	public void setDenotation(Object sourceDenotation) {
		stringDenotation = (String) sourceDenotation;
	}

	@Override
	public String toString() {
		return "denotation: " + stringDenotation + " " + super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (IdentifierVersionString.class.isAssignableFrom(obj.getClass())) {
			IdentifierVersionString another = (IdentifierVersionString) obj;
			return this.stringDenotation.equals(another.stringDenotation) && 
				super.equals(another);
		}
		return false;
	}

}
