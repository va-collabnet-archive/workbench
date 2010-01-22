package org.ihtsdo.db.bdb.concept.component.refsetmember.cidLong;

import java.io.IOException;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptLong;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetRevision;
import org.ihtsdo.etypes.ERefsetCidLongVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidLongRevision extends RefsetRevision<CidLongRevision, CidLongMember>
	implements I_ThinExtByRefPartConceptLong {

	private int c1Nid;
	private long longValue;
	
	public String toString() {
		return " c1Nid: " + c1Nid + " longValue: " + longValue + " " +super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (CidLongRevision.class.isAssignableFrom(obj.getClass())) {
			CidLongRevision another = (CidLongRevision) obj;
			if (this.c1Nid != another.c1Nid) {
				return false;
			}
			if (this.longValue != another.longValue) {
				return false;
			}
			return super.equals(obj);
		}
		return false;
	}

	protected CidLongRevision(int statusNid, int pathNid, long time, 
			CidLongMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	protected CidLongRevision(int statusAtPositionNid, 
			CidLongMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
	}

	public CidLongRevision(TupleInput input, 
			CidLongMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		longValue = input.readLong();
	}

	public CidLongRevision(ERefsetCidLongVersion eVersion,
			CidLongMember member) {
		super(eVersion, member);
		c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
		longValue = eVersion.getLongValue();
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

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
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
