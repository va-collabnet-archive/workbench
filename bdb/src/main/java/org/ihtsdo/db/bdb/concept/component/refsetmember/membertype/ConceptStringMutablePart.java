package org.ihtsdo.db.bdb.concept.component.refsetmember.membertype;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

import com.sleepycat.bind.tuple.TupleInput;

public class ConceptStringMutablePart extends ConceptMutablePart 
	implements I_ThinExtByRefPartConceptString {

	private String str;
	
	public ConceptStringMutablePart(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
	}

	public ConceptStringMutablePart(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public ConceptStringMutablePart(TupleInput input) {
		super(input);
		// TODO Auto-generated constructor stub
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
	public String getStr() {
		return str;
	}

	@Override
	public void setStr(String str) {
		this.str = str;
	}

}
