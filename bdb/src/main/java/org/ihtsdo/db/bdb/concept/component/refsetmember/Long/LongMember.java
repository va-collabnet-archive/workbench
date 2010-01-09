package org.ihtsdo.db.bdb.concept.component.refsetmember.Long;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetLongMember;
import org.ihtsdo.etypes.ERefsetLongVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class LongMember extends RefsetMember<LongVersion, LongMember> {
	private long longValue;

	public LongMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public LongMember(ERefsetLongMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		longValue =refsetMember.getLongValue();
		if (refsetMember.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<LongVersion>(refsetMember.getExtraVersionsList().size());
			for (ERefsetLongVersion eVersion: refsetMember.getExtraVersionsList()) {
				additionalVersions.add(new LongVersion(eVersion, this));
			}
		}
	}

	@Override
	protected boolean membersEqual(
			ConceptComponent<LongVersion, LongMember> obj) {
		if (LongMember.class.isAssignableFrom(obj.getClass())) {
			LongMember another = (LongMember) obj;
			return this.longValue == another.longValue;
		}
		return false;
	}
	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (additionalVersions == null) {
			additionalVersions = new ArrayList<LongVersion>(
					additionalVersionCount);
		} else {
			additionalVersions.ensureCapacity(additionalVersions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			additionalVersions.add(new LongVersion(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		longValue = input.readLong();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeLong(longValue);
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

	protected long getLongValue() {
		return longValue;
	}

	protected void setLongValue(long longValue) {
		this.longValue = longValue;
	}


}
