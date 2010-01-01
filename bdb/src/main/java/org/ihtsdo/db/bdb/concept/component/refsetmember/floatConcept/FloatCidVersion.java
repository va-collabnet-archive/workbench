package org.ihtsdo.db.bdb.concept.component.refsetmember.floatConcept;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMemberMutablePart;

import com.sleepycat.bind.tuple.TupleInput;

public class FloatCidVersion extends RefsetMemberMutablePart {

	public FloatCidVersion(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
	}

	public FloatCidVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public FloatCidVersion(TupleInput input) {
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
	public FloatCidVersion getMutablePart() {
		return this;
	}

}
