package org.ihtsdo.db.bdb.concept.component.refsetmember.Long;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetLongMember;
import org.ihtsdo.etypes.ERefsetLongVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class LongMember extends RefsetMember<LongVersion, LongMember> {
	private long longValue;

	public LongMember(int nid, int partCount, 
			Concept enclosingConcept, 
			UUID primordialUuid) {
		super(nid, partCount, enclosingConcept, 
				primordialUuid);
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
	protected final void readMemberParts(TupleInput input) {
		longValue = input.readLong();
		if (additionalVersions != null) {
			for (int i = 0; i < additionalVersions.size(); i++) {
				additionalVersions.add(new LongVersion(input, this));
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

	protected long getLongValue() {
		return longValue;
	}

	protected void setLongValue(long longValue) {
		this.longValue = longValue;
	}


}
