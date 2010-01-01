package org.ihtsdo.db.bdb.concept.component.refsetmember.cidFloat;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidFloatVersion extends RefsetVersion<CidFloatVersion, CidFloatMember> {

	private int c1Nid;
	private float floatValue;

	public CidFloatVersion(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
	}

	public CidFloatVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public CidFloatVersion(TupleInput input) {
		super(input);
		c1Nid = input.readInt();
		floatValue = input.readFloat();
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
	public CidFloatVersion getMutablePart() {
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
