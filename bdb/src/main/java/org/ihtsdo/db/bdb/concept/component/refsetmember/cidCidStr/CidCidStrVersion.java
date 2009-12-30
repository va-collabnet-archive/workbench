package org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidStr;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidCid.CidCidVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidCidStrVersion extends CidCidVersion
	implements I_ThinExtByRefPartConceptConceptString {

	private String str;
	
	public CidCidStrVersion(int statusNid, int pathNid,
			long time) {
		super(statusNid, pathNid, time);
	}

	public CidCidStrVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}

	public CidCidStrVersion(TupleInput input) {
		super(input);
		this.str = input.readString();
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
	public String getStr() {
		return str;
	}

	@Override
	public String getStringValue() {
		return str;
	}

	@Override
	public void setStr(String str) {
		this.str = str;
	}

	@Override
	public void setStringValue(String value) {
		this.str = value;
	}

}
