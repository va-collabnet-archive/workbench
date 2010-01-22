package org.ihtsdo.db.bdb.concept.component.refsetmember.membership;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetMember;
import org.ihtsdo.etypes.ERefsetVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class MembershipMember extends RefsetMember<MembershipRevision, MembershipMember> {

	public MembershipMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public MembershipMember(ERefsetMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		if (refsetMember.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<MembershipRevision>(refsetMember.getExtraVersionsList().size());
			for (ERefsetVersion eVersion: refsetMember.getExtraVersionsList()) {
				additionalVersions.add(new MembershipRevision(eVersion, this));
			}
		}
	}

	@Override
	protected boolean membersEqual(
			ConceptComponent<MembershipRevision, MembershipMember> obj) {
		if (MembershipMember.class.isAssignableFrom(obj.getClass())) {
			return true;
		}
		return false;
	}

	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (additionalVersions == null) {
			additionalVersions = new ArrayList<MembershipRevision>(
					additionalVersionCount);
		} else {
			additionalVersions.ensureCapacity(additionalVersions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			additionalVersions.add(new MembershipRevision(input, this));
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
	
	@Override
	public int getTypeId() {
		return REFSET_TYPES.MEMBER.getTypeNid();
	}

	@Override
	protected String getTypeFieldsString() {
		return "";
	}

}
