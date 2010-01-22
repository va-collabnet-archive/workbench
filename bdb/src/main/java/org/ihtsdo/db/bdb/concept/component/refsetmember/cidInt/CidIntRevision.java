package org.ihtsdo.db.bdb.concept.component.refsetmember.cidInt;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetRevision;
import org.ihtsdo.etypes.ERefsetCidIntVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidIntRevision  
				extends RefsetRevision<CidIntRevision, CidIntMember>
	implements I_ThinExtByRefPartConceptInt {

	private int c1Nid;
	private int intValue;
	
	public String toString() {
		return " c1Nid: " + c1Nid + " intValue: " + intValue + " " +super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (CidIntRevision.class.isAssignableFrom(obj.getClass())) {
			CidIntRevision another = (CidIntRevision) obj;
			if (this.c1Nid != another.intValue) {
				return false;
			}
			if (this.intValue != another.intValue) {
				return false;
			}
			return super.equals(obj);
		}
		return false;
	}
	protected CidIntRevision(int statusNid, int pathNid, long time, 
			CidIntMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	protected CidIntRevision(int statusAtPositionNid, 
			CidIntMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
	}

	public CidIntRevision(TupleInput input, 
			CidIntMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		intValue = input.readInt();
	}

	public CidIntRevision(ERefsetCidIntVersion eVersion,
			CidIntMember member) {
		super(eVersion, member);
		c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
		intValue = eVersion.getIntValue();
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

}
