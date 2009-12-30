package org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidCid;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidCid.CidCidVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidCidCidVersion extends CidCidVersion
	implements I_ThinExtByRefPartConceptConceptConcept {

	private int c3nid;
	
	public CidCidCidVersion(int statusNid, int pathNid,
			long time) {
		super(statusNid, pathNid, time);
	}

	public CidCidCidVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public CidCidCidVersion(TupleInput input) {
		super(input);
		// TODO Auto-generated constructor stub
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
		return c3nid;
	}

	@Override
	public void setC3id(int c3nid) {
		this.c3nid = c3nid;
	}

}
