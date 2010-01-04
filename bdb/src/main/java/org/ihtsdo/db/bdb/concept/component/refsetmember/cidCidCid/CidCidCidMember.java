package org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidCid;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetCidCidCidMember;
import org.ihtsdo.etypes.ERefsetCidCidCidVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidCidCidMember extends RefsetMember<CidCidCidVersion, CidCidCidMember> {

	private int c1Nid;
	private int c2Nid;
	private int c3Nid;

	public CidCidCidMember(int nid, int partCount, 
			Concept enclosingConcept, 
			UUID primordialUuid) {
		super(nid, partCount, enclosingConcept, 
				primordialUuid);
	}

	public CidCidCidMember(ERefsetCidCidCidMember refsetMember, 
			Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		c2Nid = Bdb.uuidToNid(refsetMember.getC2Uuid());
		c3Nid = Bdb.uuidToNid(refsetMember.getC3Uuid());
		if (refsetMember.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<CidCidCidVersion>(refsetMember.getExtraVersionsList().size());
			for (ERefsetCidCidCidVersion eVersion: refsetMember.getExtraVersionsList()) {
				additionalVersions.add(new CidCidCidVersion(eVersion, this));
			}
		}
	}

	@Override
	protected final void readMemberParts(TupleInput input) {
		c1Nid = input.readInt();
		c2Nid = input.readInt();
		c3Nid = input.readInt();
		if (additionalVersions != null) {
			for (int i = 0; i < additionalVersions.size(); i++) {
				additionalVersions.add(new CidCidCidVersion(input, this));
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

	public int getC2Nid() {
		return c2Nid;
	}

	public void setC2Nid(int c2Nid) {
		this.c2Nid = c2Nid;
	}

	public int getC3Nid() {
		return c3Nid;
	}

	public void setC3Nid(int c3Nid) {
		this.c3Nid = c3Nid;
	}

}
