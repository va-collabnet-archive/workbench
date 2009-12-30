package org.ihtsdo.db.bdb.concept.component.refsetmember.cid;

import java.io.IOException;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refsetmember.RefsetMemberMutablePart;

import com.sleepycat.bind.tuple.TupleInput;

public class ConceptVersion extends RefsetMemberMutablePart
	implements I_ThinExtByRefPartConcept {
	
	private int c1Nid;

	protected ConceptVersion(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
	}

	protected ConceptVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public ConceptVersion(TupleInput input) {
		super(input);
		c1Nid = input.readInt();
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
	public ConceptVersion getMutablePart() {
		return this;
	}

}
