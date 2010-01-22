package org.ihtsdo.db.bdb.concept.component.refsetmember.cidFloat;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetRevision;
import org.ihtsdo.etypes.ERefsetCidFloatVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidFloatRevision extends RefsetRevision<CidFloatRevision, CidFloatMember> {

	private int c1Nid;
	private float floatValue;

	public String toString() {
		return " c1Nid: " + c1Nid + " floatValue: " + floatValue + " " +super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (CidFloatRevision.class.isAssignableFrom(obj.getClass())) {
			CidFloatRevision another = (CidFloatRevision) obj;
			if (this.c1Nid != another.c1Nid) {
				return false;
			}
			if (this.floatValue != another.floatValue) {
				return false;
			}
			return super.equals(obj);
		}
		return false;
	}

	public CidFloatRevision(int statusNid, int pathNid, long time, 
			CidFloatMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	public CidFloatRevision(int statusAtPositionNid, 
			CidFloatMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
	}

	public CidFloatRevision(TupleInput input, 
			CidFloatMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		floatValue = input.readFloat();
	}

	public CidFloatRevision(ERefsetCidFloatVersion eVersion,
			CidFloatMember member) {
		super(eVersion, member);
		c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
		floatValue = eVersion.getFloatValue();
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
	public CidFloatRevision getMutablePart() {
		return this;
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
	}

	public float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
	}

}
