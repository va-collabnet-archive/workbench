package org.ihtsdo.db.bdb.concept.component.refsetmember.Boolean;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetRevision;
import org.ihtsdo.etypes.ERefsetBooleanVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class BooleanRevision extends RefsetRevision<BooleanRevision, BooleanMember>
	implements I_ThinExtByRefPartBoolean {
	
	private boolean booleanValue;

	public String toString() {
		return " booleanValue: " + booleanValue + " " + super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (BooleanRevision.class.isAssignableFrom(obj.getClass())) {
			BooleanRevision another = (BooleanRevision) obj;
			if (this.booleanValue != another.booleanValue) {
				return false;
			}
			return super.equals(obj);
		}
		return false;
	}

	protected BooleanRevision(int statusNid, int pathNid, long time, 
			BooleanMember primoridalMember) {
		super(statusNid, pathNid, time, primoridalMember);
	}

	protected BooleanRevision(int statusAtPositionNid, 
			BooleanMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
	}

	public BooleanRevision(TupleInput input, 
			BooleanMember primoridalMember) {
		super(input, primoridalMember);
		booleanValue = input.readBoolean();
	}

	public BooleanRevision(ERefsetBooleanVersion eVersion,
			BooleanMember booleanMember) {
		super(eVersion, booleanMember);
		this.booleanValue = eVersion.isBooleanValue();
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
}
