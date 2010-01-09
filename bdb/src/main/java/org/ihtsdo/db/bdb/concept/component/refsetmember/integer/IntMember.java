package org.ihtsdo.db.bdb.concept.component.refsetmember.integer;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetIntMember;
import org.ihtsdo.etypes.ERefsetIntVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IntMember extends RefsetMember<IntVersion, IntMember> {
	private int intValue;

	public IntMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public IntMember(ERefsetIntMember refsetMember, 
			Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		intValue =refsetMember.getIntValue();
		if (refsetMember.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<IntVersion>(refsetMember.getExtraVersionsList().size());
			for (ERefsetIntVersion eVersion: refsetMember.getExtraVersionsList()) {
				additionalVersions.add(new IntVersion(eVersion, this));
			}
		}
	}
	@Override
	protected boolean membersEqual(
			ConceptComponent<IntVersion, IntMember> obj) {
		if (IntMember.class.isAssignableFrom(obj.getClass())) {
			IntMember another = (IntMember) obj;
			return this.intValue == another.intValue;
		}
		return false;
	}

	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (additionalVersions == null) {
			additionalVersions = new ArrayList<IntVersion>(
					additionalVersionCount);
		} else {
			additionalVersions.ensureCapacity(additionalVersions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			additionalVersions.add(new IntVersion(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		intValue = input.readInt();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(intValue);
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

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

}
