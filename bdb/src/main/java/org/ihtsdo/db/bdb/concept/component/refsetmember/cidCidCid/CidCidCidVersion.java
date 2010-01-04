package org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidCid;

import java.io.IOException;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidCidCidVersion extends RefsetVersion<CidCidCidVersion, CidCidCidMember> 
	implements I_ThinExtByRefPartConceptConceptConcept {

	private int c1Nid;
	private int c2Nid;
	private int c3Nid;
	
	public CidCidCidVersion(int statusNid, int pathNid,
			long time, 
			CidCidCidMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	public CidCidCidVersion(int statusAtPositionNid, 
			CidCidCidMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
	}

	public CidCidCidVersion(TupleInput input, 
			CidCidCidMember primoridalMember) {
		super(input, primoridalMember);
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
	public int getC3id() {
		return c3Nid;
	}

	@Override
	public void setC3id(int c3nid) {
		this.c3Nid = c3nid;
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

	public int getC3Nid() {
		return c3Nid;
	}

	public void setC3Nid(int c3Nid) {
		this.c3Nid = c3Nid;
	}

	@Override
	public int getC2id() {
		return c2Nid;
	}

	@Override
	public void setC2id(int c2id) {
		this.c2Nid = c2id;
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
