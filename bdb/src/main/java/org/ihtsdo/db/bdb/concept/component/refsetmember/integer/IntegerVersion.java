package org.ihtsdo.db.bdb.concept.component.refsetmember.integer;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refsetmember.RefsetMemberMutablePart;

import com.sleepycat.bind.tuple.TupleInput;

public class IntegerVersion extends RefsetMemberMutablePart
	implements I_ThinExtByRefPartInteger {

	private int intValue;
	
	public IntegerVersion(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
	}

	public IntegerVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public IntegerVersion(TupleInput input) {
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
	public int getValue() {
		return intValue;
	}

	@Override
	public void setIntValue(int value) {
		this.intValue = value;
	}

	@Override
	public void setValue(int value) {
		this.intValue = value;
	}

	@Override
	public IntegerVersion getMutablePart() {
		return this;
	}

}
