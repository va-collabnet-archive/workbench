package org.ihtsdo.db.bdb.concept.component.refsetmember.cidInt;

import java.io.IOException;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidFloat.CidFloatMember;

import com.sleepycat.bind.tuple.TupleInput;

public class CidIntVersion  
				extends RefsetVersion<CidIntVersion, CidIntMember>
	implements I_ThinExtByRefPartConceptInt {

	private int c1Nid;
	private int intValue;
	
	protected CidIntVersion(int statusNid, int pathNid, long time, 
			CidIntMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	protected CidIntVersion(int statusAtPositionNid, 
			CidIntMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
	}

	public CidIntVersion(TupleInput input, 
			CidIntMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
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

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
	}

	@Override
	public int getC1id() {
		return c1Nid;
	}

	@Override
	public int getConceptId() {
		return c1Nid;
	}

	@Override
	public void setC1id(int c1id) {
		this.c1Nid = c1id;
	}

	@Override
	public void setConceptId(int conceptId) {
		this.c1Nid = conceptId;
	}

	@Override
	public I_AmPart getMutablePart() {
		return this;
	}

}
