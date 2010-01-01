package org.ihtsdo.db.bdb.concept.component.refsetmember.language;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMemberMutablePart;

import com.sleepycat.bind.tuple.TupleInput;

public class LangVersion extends RefsetMemberMutablePart {

	public LangVersion(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
	}

	public LangVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public LangVersion(TupleInput input) {
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
	public LangVersion getMutablePart() {
		return this;
	}

}
