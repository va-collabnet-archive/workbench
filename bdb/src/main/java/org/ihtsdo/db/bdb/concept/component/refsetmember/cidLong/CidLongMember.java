package org.ihtsdo.db.bdb.concept.component.refsetmember.cidLong;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetCidLongMember;
import org.ihtsdo.etypes.ERefsetCidLongVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidLongMember 
				extends RefsetMember<CidLongVersion, CidLongMember> {

	private int c1Nid;
	private long longValue;

	public CidLongMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public CidLongMember(ERefsetCidLongMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		longValue = refsetMember.getLongValue();
		if (refsetMember.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<CidLongVersion>(refsetMember.getExtraVersionsList().size());
			for (ERefsetCidLongVersion eVersion: refsetMember.getExtraVersionsList()) {
				additionalVersions.add(new CidLongVersion(eVersion, this));
			}
		}
	}
	@Override
	protected boolean membersEqual(
			ConceptComponent<CidLongVersion, CidLongMember> obj) {
		if (CidLongMember.class.isAssignableFrom(obj.getClass())) {
			CidLongMember another = (CidLongMember) obj;
			return this.c1Nid == another.c1Nid && this.longValue == another.longValue;
		}
		return false;
	}

	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (additionalVersions == null) {
			additionalVersions = new ArrayList<CidLongVersion>(
					additionalVersionCount);
		} else {
			additionalVersions.ensureCapacity(additionalVersions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			additionalVersions.add(new CidLongVersion(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		c1Nid = input.readInt();
		longValue = input.readLong();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
		output.writeLong(longValue);
	}

	@Override
	protected String getTypeFieldsString() {
		StringBuffer buf = new StringBuffer();
		buf.append("c1Nid: ");
		addNidToBuffer(buf, c1Nid);
		buf.append(" longValue: ");
		buf.append(longValue);
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

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

	@Override
	public int getTypeId() {
		return REFSET_TYPES.CID_LONG.getTypeNid();
	}

}
