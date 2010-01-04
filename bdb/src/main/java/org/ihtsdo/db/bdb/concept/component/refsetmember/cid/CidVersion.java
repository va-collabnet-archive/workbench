package org.ihtsdo.db.bdb.concept.component.refsetmember.cid;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetVersion;
import org.ihtsdo.etypes.ERefsetCidVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidVersion extends RefsetVersion<CidVersion, CidMember>
	implements I_ThinExtByRefPartConcept {
	
	private int c1Nid;

	protected CidVersion(int statusNid, int pathNid, long time, 
			CidMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	protected CidVersion(int statusAtPositionNid, 
			CidMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
	}

	public CidVersion(TupleInput input, 
			CidMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
	}

	public CidVersion(ERefsetCidVersion eVersion,
			CidMember member) {
		super(eVersion, member);
		c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
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
	public int getC1id() {
		return c1Nid;
	}

	@Override
	public void setC1id(int c1Nid) {
		this.c1Nid = c1Nid;
	}

	@Override
	@Deprecated
	public int getConceptId() {
		return c1Nid;
	}

	@Override
	@Deprecated
	public void setConceptId(int conceptId) {
		this.c1Nid = conceptId;
	}

	@Override
	public CidVersion getMutablePart() {
		return this;
	}

}
