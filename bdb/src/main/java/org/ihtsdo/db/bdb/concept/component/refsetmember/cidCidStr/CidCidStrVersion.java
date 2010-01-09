package org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidStr;

import java.io.IOException;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetVersion;
import org.ihtsdo.etypes.ERefsetCidCidStrVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidCidStrVersion extends RefsetVersion<CidCidStrVersion, CidCidStrMember>
	implements I_ThinExtByRefPartConceptConceptString {

	private int c1Nid;
	private int c2Nid;
	private String strValue;
	
	
	public String toString() {
		return " c1Nid: " + c1Nid + " c2Nid: " + c2Nid + " strValue: " + strValue + " " +super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (CidCidStrVersion.class.isAssignableFrom(obj.getClass())) {
			CidCidStrVersion another = (CidCidStrVersion) obj;
			if (this.c1Nid != another.c1Nid) {
				return false;
			}
			if (this.c2Nid != another.c2Nid) {
				return false;
			}
			if (this.strValue.equals(another.strValue)) {
				return false;
			}
			return super.equals(obj);
		}
		return false;
	}

	public CidCidStrVersion(int statusNid, int pathNid,
			long time, 
			CidCidStrMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	public CidCidStrVersion(int statusAtPositionNid, 
			CidCidStrMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
	}

	public CidCidStrVersion(TupleInput input, 
			CidCidStrMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		c2Nid = input.readInt();
		strValue = input.readString();
	}

	public CidCidStrVersion(ERefsetCidCidStrVersion eVersion,
			CidCidStrMember member) {
		super(eVersion, member);
		c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
		c2Nid = Bdb.uuidToNid(eVersion.getC2Uuid());
		strValue = eVersion.getStringValue();
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
		return strValue;
	}

	@Override
	public String getStringValue() {
		return strValue;
	}

	@Override
	public void setStr(String str) {
		this.strValue = str;
	}

	@Override
	public void setStringValue(String value) {
		this.strValue = value;
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

	public String getStrValue() {
		return strValue;
	}

	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}

	@Override
	public int getC1id() {
		return c1Nid;
	}

	@Override
	public int getC2id() {
		return c2Nid;
	}

	@Override
	public void setC1id(int c1id) {
		this.c1Nid = c1id;
	}

	@Override
	public void setC2id(int c2id) {
		this.c2Nid = c2id;
	}

	@Override
	public I_AmPart getMutablePart() {
		return this;
	}

}
