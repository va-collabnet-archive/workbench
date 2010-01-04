package org.ihtsdo.db.bdb.concept.component.refsetmember.integer;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetIntMember;
import org.ihtsdo.etypes.ERefsetIntVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class IntMember extends RefsetMember<IntVersion, IntMember> {
	private int intValue;

	public IntMember(int nid, int partCount, Concept enclosingConcept, 
			UUID primordialUuid) {
		super(nid, partCount, enclosingConcept, 
				primordialUuid);
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
	protected final void readMemberParts(TupleInput input) {
		intValue = input.readInt();
		if (additionalVersions != null) {
			for (int i = 0; i < additionalVersions.size(); i++) {
				additionalVersions.add(new IntVersion(input, this));
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

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

}
