package org.ihtsdo.db.bdb.concept.component.refsetmember.Boolean;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetBooleanMember;
import org.ihtsdo.etypes.ERefsetBooleanVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class BooleanMember extends RefsetMember<BooleanVersion, BooleanMember> {

	private boolean booleanValue;

	public BooleanMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public BooleanMember(ERefsetBooleanMember refsetMember,
			Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		booleanValue = refsetMember.getBooleanValue();
		if (refsetMember.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<BooleanVersion>(refsetMember
					.getExtraVersionsList().size());
			for (ERefsetBooleanVersion eVersion : refsetMember
					.getExtraVersionsList()) {
				additionalVersions.add(new BooleanVersion(eVersion, this));
			}
		}
	}

	@Override
	protected boolean membersEqual(
			ConceptComponent<BooleanVersion, BooleanMember> obj) {
		if (BooleanMember.class.isAssignableFrom(obj.getClass())) {
			BooleanMember another = (BooleanMember) obj;
			return this.booleanValue = another.booleanValue;
		}
		return false;
	}

	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (additionalVersions == null) {
			additionalVersions = new ArrayList<BooleanVersion>(
					additionalVersionCount);
		} else {
			additionalVersions.ensureCapacity(additionalVersions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			additionalVersions.add(new BooleanVersion(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		booleanValue = input.readBoolean();

	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeBoolean(booleanValue);
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

	public boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

}
