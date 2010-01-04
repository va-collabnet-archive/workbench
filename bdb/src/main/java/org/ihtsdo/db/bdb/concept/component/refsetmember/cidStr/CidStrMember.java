package org.ihtsdo.db.bdb.concept.component.refsetmember.cidStr;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetCidStrMember;
import org.ihtsdo.etypes.ERefsetCidStrVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidStrMember extends RefsetMember<CidStrVersion, CidStrMember> {

	private int c1Nid;
	private String strValue;
	
	public CidStrMember(int nid, int partCount, 
			Concept enclosingConcept, 
			UUID primordialUuid) {
		super(nid, partCount, enclosingConcept, 
				primordialUuid);
	}

	public CidStrMember(ERefsetCidStrMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		strValue = refsetMember.getStrValue();
		if (refsetMember.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<CidStrVersion>(refsetMember.getExtraVersionsList().size());
			for (ERefsetCidStrVersion eVersion: refsetMember.getExtraVersionsList()) {
				additionalVersions.add(new CidStrVersion(eVersion, this));
			}
		}
	}

	@Override
	protected final void readMemberParts(TupleInput input) {
		c1Nid = input.readInt();
		strValue = input.readString();
		if (additionalVersions != null) {
			for (int i = 0; i < additionalVersions.size(); i++) {
				additionalVersions.add(new CidStrVersion(input, this));
			}
		}
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
	}

	public String getStrValue() {
		return strValue;
	}

	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}

}
