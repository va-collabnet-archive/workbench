package org.ihtsdo.db.bdb.concept.component.refsetmember.idId;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refsetmember.id.ConceptVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class ConceptConceptVersion extends ConceptVersion 
	implements I_ThinExtByRefPartConceptConcept {

	private int c2nid;
	
	public ConceptConceptVersion(int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
		// TODO Auto-generated constructor stub
	}

	public ConceptConceptVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
		// TODO Auto-generated constructor stub
	}

	public ConceptConceptVersion(TupleInput input) {
		super(input);
		c2nid = input.readInt();
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
		return c2nid;
	}

	@Override
	public void setC2id(int c2id) {
		this.c2nid = c2id;
	}

}
