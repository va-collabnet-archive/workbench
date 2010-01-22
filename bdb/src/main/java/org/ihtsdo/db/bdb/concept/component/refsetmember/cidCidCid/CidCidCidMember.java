package org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidCid;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetCidCidCidMember;
import org.ihtsdo.etypes.ERefsetCidCidCidVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidCidCidMember extends RefsetMember<CidCidCidRevision, CidCidCidMember> {

	private int c1Nid;
	private int c2Nid;
	private int c3Nid;

	public CidCidCidMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public CidCidCidMember(ERefsetCidCidCidMember refsetMember, 
			Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		c2Nid = Bdb.uuidToNid(refsetMember.getC2Uuid());
		c3Nid = Bdb.uuidToNid(refsetMember.getC3Uuid());
		if (refsetMember.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<CidCidCidRevision>(refsetMember.getExtraVersionsList().size());
			for (ERefsetCidCidCidVersion eVersion: refsetMember.getExtraVersionsList()) {
				additionalVersions.add(new CidCidCidRevision(eVersion, this));
			}
		}
	}

	@Override
	protected boolean membersEqual(
			ConceptComponent<CidCidCidRevision, CidCidCidMember> obj) {
		if (CidCidCidMember.class.isAssignableFrom(obj.getClass())) {
			CidCidCidMember another = (CidCidCidMember) obj;
			return this.c1Nid == another.c1Nid && this.c2Nid == another.c2Nid && this.c3Nid == another.c3Nid;
		}
		return false;
	}

	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (additionalVersions == null) {
			additionalVersions = new ArrayList<CidCidCidRevision>(
					additionalVersionCount);
		} else {
			additionalVersions.ensureCapacity(additionalVersions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			additionalVersions.add(new CidCidCidRevision(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		c1Nid = input.readInt();
		c2Nid = input.readInt();
		c3Nid = input.readInt();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
		output.writeInt(c2Nid);
		output.writeInt(c3Nid);
	}

	@Override
	protected String getTypeFieldsString() {
		StringBuffer buf = new StringBuffer();
		buf.append("c1Nid: ");
		addNidToBuffer(buf, c1Nid);
		buf.append(" c2Nid: ");
		addNidToBuffer(buf, c2Nid);
		buf.append(" c3Nid: ");
		addNidToBuffer(buf, c3Nid);
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

	public int getC3Nid() {
		return c3Nid;
	}

	public void setC3Nid(int c3Nid) {
		this.c3Nid = c3Nid;
	}
	@Override
	public int getTypeId() {
		return REFSET_TYPES.CID_CID_CID.getTypeNid();
	}

}
