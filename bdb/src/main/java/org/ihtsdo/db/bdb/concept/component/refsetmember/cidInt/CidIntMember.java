package org.ihtsdo.db.bdb.concept.component.refsetmember.cidInt;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetCidIntMember;
import org.ihtsdo.etypes.ERefsetCidIntVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidIntMember extends RefsetMember<CidIntVersion, CidIntMember> {

	private int c1Nid;
	private int intValue;

	public CidIntMember(int nid, int partCount, 
			Concept enclosingConcept, 
			UUID primordialUuid) {
		super(nid, partCount, enclosingConcept, 
				primordialUuid);
	}

	public CidIntMember(ERefsetCidIntMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		intValue = refsetMember.getIntValue();
		if (refsetMember.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<CidIntVersion>(refsetMember.getExtraVersionsList().size());
			for (ERefsetCidIntVersion eVersion: refsetMember.getExtraVersionsList()) {
				additionalVersions.add(new CidIntVersion(eVersion, this));
			}
		}
	}

	@Override
	protected final void readMemberParts(TupleInput input) {
		c1Nid = input.readInt();
		intValue = input.readInt();
		if (additionalVersions != null) {
			for (int i = 0; i < additionalVersions.size(); i++) {
				additionalVersions.add(new CidIntVersion(input, this));
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

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

}
