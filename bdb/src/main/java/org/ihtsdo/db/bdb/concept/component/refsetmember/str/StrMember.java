package org.ihtsdo.db.bdb.concept.component.refsetmember.str;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetStrMember;
import org.ihtsdo.etypes.ERefsetStrVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class StrMember extends RefsetMember<StrVersion, StrMember> {

	private String stringValue;

	public StrMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public StrMember(ERefsetStrMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		stringValue = refsetMember.getStrValue();
		if (refsetMember.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<StrVersion>(refsetMember.getExtraVersionsList().size());
			for (ERefsetStrVersion eVersion: refsetMember.getExtraVersionsList()) {
				additionalVersions.add(new StrVersion(eVersion, this));
			}
		}
	}

	@Override
	protected boolean membersEqual(
			ConceptComponent<StrVersion, StrMember> obj) {
		if (StrMember.class.isAssignableFrom(obj.getClass())) {
			StrMember another = (StrMember) obj;
			return this.stringValue.equals(another.stringValue);
		}
		return false;
	}

	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (additionalVersions == null) {
			additionalVersions = new ArrayList<StrVersion>(
					additionalVersionCount);
		} else {
			additionalVersions.ensureCapacity(additionalVersions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			additionalVersions.add(new StrVersion(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		// nothing to read...
	}
	@Override
	protected void writeMember(TupleOutput output) {
		// nothing to write
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

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	@Override
	public int getTypeId() {
		return REFSET_TYPES.STR.getTypeNid();
	}

	@Override
	protected String getTypeFieldsString() {
		StringBuffer buf = new StringBuffer();
		buf.append(" stringValue: ");
		buf.append(stringValue);
		return buf.toString();
	}

}
