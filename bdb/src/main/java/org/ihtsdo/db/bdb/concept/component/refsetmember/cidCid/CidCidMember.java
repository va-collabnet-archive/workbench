package org.ihtsdo.db.bdb.concept.component.refsetmember.cidCid;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetCidCidMember;
import org.ihtsdo.etypes.ERefsetCidCidVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidCidMember extends RefsetMember<CidCidVersion, CidCidMember> {

	private int c1Nid;
	private int c2Nid;

	public CidCidMember(Concept enclosingConcept, 
			TupleInput input) {
		super(enclosingConcept, 
				input);
	}

	public CidCidMember(ERefsetCidCidMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		c2Nid = Bdb.uuidToNid(refsetMember.getC2Uuid());
		if (refsetMember.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<CidCidVersion>(refsetMember.getExtraVersionsList().size());
			for (ERefsetCidCidVersion eVersion: refsetMember.getExtraVersionsList()) {
				additionalVersions.add(new CidCidVersion(eVersion, this));
			}
		}
	}
	@Override
	protected boolean membersEqual(
			ConceptComponent<CidCidVersion, CidCidMember> obj) {
		if (CidCidMember.class.isAssignableFrom(obj.getClass())) {
			CidCidMember another = (CidCidMember) obj;
			return this.c1Nid == another.c1Nid && this.c2Nid == another.c2Nid;
		}
		return false;
	}

	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (additionalVersions == null) {
			additionalVersions = new ArrayList<CidCidVersion>(
					additionalVersionCount);
		} else {
			additionalVersions.ensureCapacity(additionalVersions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			additionalVersions.add(new CidCidVersion(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		c1Nid = input.readInt();
		c2Nid = input.readInt();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
		output.writeInt(c2Nid);
	}

	@Override
	protected String getTypeFieldsString() {
		StringBuffer buf = new StringBuffer();
		buf.append("c1Nid: ");
		addNidToBuffer(buf, c1Nid);
		buf.append(" c2Nid: ");
		addNidToBuffer(buf, c2Nid);
		return buf.toString();
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
	@Override
	public int getTypeId() {
		return REFSET_TYPES.CID_CID.getTypeNid();
	}

}
