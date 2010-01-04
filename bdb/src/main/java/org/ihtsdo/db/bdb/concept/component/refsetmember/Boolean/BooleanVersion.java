package org.ihtsdo.db.bdb.concept.component.refsetmember.Boolean;

import java.io.IOException;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class BooleanVersion extends RefsetVersion<BooleanVersion, BooleanMember>
	implements I_ThinExtByRefPartBoolean {
	
	private boolean booleanValue;

	protected BooleanVersion(int statusNid, int pathNid, long time, 
			BooleanMember primoridalMember) {
		super(statusNid, pathNid, time, primoridalMember);
	}

	protected BooleanVersion(int statusAtPositionNid, 
			BooleanMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
	}

	public BooleanVersion(TupleInput input, 
			BooleanMember primoridalMember) {
		super(input, primoridalMember);
		booleanValue = input.readBoolean();
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

	public boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	@Override
	public I_AmPart getMutablePart() {
		return this;
	}
}
