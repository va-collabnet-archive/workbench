package org.ihtsdo.db.bdb.concept.component.refsetmember.str;

import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetStrMember;

import com.sleepycat.bind.tuple.TupleInput;

public class StrMember extends RefsetMember<StrVersion, StrMember> {

	private String stringValue;

	public StrMember(int nid, int partCount, Concept enclosingConcept, 
			UUID primordialUuid) {
		super(nid, partCount, enclosingConcept, 
				primordialUuid);
	}

	public StrMember(ERefsetStrMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		stringValue = refsetMember.getStrValue();
	}

	@Override
	protected final void readMemberParts(TupleInput input) {
		stringValue = input.readString();
		if (additionalVersions != null) {
			for (int i = 0; i < additionalVersions.size(); i++) {
				additionalVersions.add(new StrVersion(input));
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

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}


}
