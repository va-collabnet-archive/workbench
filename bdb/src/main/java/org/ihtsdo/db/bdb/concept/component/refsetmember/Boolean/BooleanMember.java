package org.ihtsdo.db.bdb.concept.component.refsetmember.Boolean;

import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetBooleanMember;

import com.sleepycat.bind.tuple.TupleInput;

public class BooleanMember extends RefsetMember<BooleanVersion, BooleanMember> {

	private boolean booleanValue;
	
	public BooleanMember(int nid, int partCount, 
			Concept enclosingConcept, 
			UUID primordialUuid) {
		super(nid, partCount, enclosingConcept, 
				primordialUuid);
	}

	public BooleanMember(ERefsetBooleanMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		booleanValue = refsetMember.getBooleanValue();
	}

	@Override
	protected final void readMemberParts(TupleInput input) {
		booleanValue = input.readBoolean();
		if (additionalVersions != null) {
			for (int i = 0; i < additionalVersions.size(); i++) {
				additionalVersions.add(new BooleanVersion(input));
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



	public boolean getBooleanValue() {
		return booleanValue;
	}



	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}
}
