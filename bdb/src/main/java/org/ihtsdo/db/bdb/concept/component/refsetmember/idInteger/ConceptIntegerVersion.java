package org.ihtsdo.db.bdb.concept.component.refsetmember.idInteger;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refsetmember.id.ConceptVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class ConceptIntegerVersion extends ConceptVersion
	implements I_ThinExtByRefPartConceptInt {

	private int intValue;
	
	protected ConceptIntegerVersion(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
		// TODO Auto-generated constructor stub
	}

	protected ConceptIntegerVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
		// TODO Auto-generated constructor stub
	}

	public ConceptIntegerVersion(TupleInput input) {
		super(input);
		intValue = input.readInt();
	}

	@Override
	public UniversalAceExtByRefPart getUniversalPart()
			throws TerminologyException, IOException {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefPart makePromotionPart(I_Path promotionPath) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(I_ThinExtByRefPart o) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public int getIntValue() {
		return intValue;
	}

	@Override
	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

}
