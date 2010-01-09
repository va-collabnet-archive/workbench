package org.ihtsdo.db.bdb.concept.component.refsetmember.cidStr;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetCidStrMember;
import org.ihtsdo.etypes.ERefsetCidStrVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidStrMember extends RefsetMember<CidStrVersion, CidStrMember> {

	private int c1Nid;
	private String strValue;
	
	public CidStrMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
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
	protected boolean membersEqual(
			ConceptComponent<CidStrVersion, CidStrMember> obj) {
		if (CidStrMember.class.isAssignableFrom(obj.getClass())) {
			CidStrMember another = (CidStrMember) obj;
			return this.c1Nid == another.c1Nid && this.strValue.equals(another.strValue);
		}
		return false;
	}
	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (additionalVersions == null) {
			additionalVersions = new ArrayList<CidStrVersion>(
					additionalVersionCount);
		} else {
			additionalVersions.ensureCapacity(additionalVersions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			additionalVersions.add(new CidStrVersion(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		c1Nid = input.readInt();
		strValue = input.readString();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
		output.writeString(strValue);
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
