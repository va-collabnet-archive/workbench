package org.ihtsdo.db.bdb.concept.component.refsetmember.cidCid;

import java.io.IOException;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetVersion;
import org.ihtsdo.etypes.ERefsetCidCidVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidCidVersion extends RefsetVersion<CidCidVersion, CidCidMember> 
	implements I_ThinExtByRefPartConceptConcept {

	private int c1Nid;
	private int c2Nid;

	public String toString() {
		return " c1Nid: " + c1Nid + " c2Nid: " + c2Nid + " " +super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (CidCidVersion.class.isAssignableFrom(obj.getClass())) {
			CidCidVersion another = (CidCidVersion) obj;
			if (this.c1Nid != another.c1Nid) {
				return false;
			}
			if (this.c2Nid != another.c2Nid) {
				return false;
			}
			return super.equals(obj);
		}
		return false;
	}

	public CidCidVersion(int statusNid, int pathNid, long time, 
			CidCidMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	public CidCidVersion(int statusAtPositionNid, 
			CidCidMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
	}

	public CidCidVersion(TupleInput input, 
			CidCidMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		c2Nid = input.readInt();
	}

	public CidCidVersion(ERefsetCidCidVersion eVersion,
			CidCidMember member) {
		super(eVersion, member);
		c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
		c2Nid = Bdb.uuidToNid(eVersion.getC2Uuid());
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
	public int getC2id() {
		return c2Nid;
	}

	@Override
	public void setC2id(int c2id) {
		this.c2Nid = c2id;
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
	}

	public int getC2Nid() {
		return c2Nid;
	}

	public void setC2Nid(int c2Nid) {
		this.c2Nid = c2Nid;
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
