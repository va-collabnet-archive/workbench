package org.ihtsdo.db.bdb.concept.component.refsetmember.membertype;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refsetmember.RefsetMemberMutablePart;

import com.sleepycat.bind.tuple.TupleInput;

public class StringMutablePart extends RefsetMemberMutablePart 
	//implements I_ThinExtByRefPartString 
	{

	private String stringValue;
	
	public StringMutablePart(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
	}

	public StringMutablePart(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public StringMutablePart(TupleInput input) {
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

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

}
