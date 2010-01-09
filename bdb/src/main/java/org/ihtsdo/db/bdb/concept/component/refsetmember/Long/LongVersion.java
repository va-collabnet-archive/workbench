package org.ihtsdo.db.bdb.concept.component.refsetmember.Long;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLong;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetVersion;
import org.ihtsdo.etypes.ERefsetLongVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class LongVersion extends RefsetVersion<LongVersion, LongMember>
	implements I_ThinExtByRefPartLong {

	private long longValue;
	
	public String toString() {
		return " longValue: " + longValue + " " +super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (LongVersion.class.isAssignableFrom(obj.getClass())) {
			LongVersion another = (LongVersion) obj;
			if (this.longValue != another.longValue) {
				return false;
			}
			return super.equals(obj);
		}
		return false;
	}

	public LongVersion(int statusNid, int pathNid, long time, 
			LongMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	public LongVersion(int statusAtPositionNid, 
			LongMember primoridalMember) {
		super(statusAtPositionNid, primoridalMember);
	}

	public LongVersion(TupleInput input, 
			LongMember primoridalMember) {
		super(input, primoridalMember);
		longValue = input.readLong();
	}

	public LongVersion(ERefsetLongVersion eVersion,
			LongMember member) {
		super(eVersion, member);
		this.longValue = eVersion.getLongValue();
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
	public LongVersion getMutablePart() {
		return this;
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

}
