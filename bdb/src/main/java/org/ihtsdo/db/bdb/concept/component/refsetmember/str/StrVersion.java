package org.ihtsdo.db.bdb.concept.component.refsetmember.str;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class StrVersion extends RefsetVersion<StrVersion, StrMember> 
	//implements I_ThinExtByRefPartString 
	{

	private String stringValue;
	
	public StrVersion(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
	}

	public StrVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public StrVersion(TupleInput input) {
		super(input);
		stringValue = input.readString();
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

	@Override
	public StrVersion getMutablePart() {
		return this;
	}

}
