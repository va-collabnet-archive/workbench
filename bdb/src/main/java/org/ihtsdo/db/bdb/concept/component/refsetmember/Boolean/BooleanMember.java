package org.ihtsdo.db.bdb.concept.component.refsetmember.Boolean;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.etypes.ERefsetBooleanMember;
import org.ihtsdo.etypes.ERefsetBooleanVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class BooleanMember extends RefsetMember<BooleanRevision, BooleanMember> {

	private boolean booleanValue;

	public BooleanMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public BooleanMember(ERefsetBooleanMember refsetMember,
			Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		booleanValue = refsetMember.getBooleanValue();
		if (refsetMember.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<BooleanRevision>(refsetMember
					.getExtraVersionsList().size());
			for (ERefsetBooleanVersion eVersion : refsetMember
					.getExtraVersionsList()) {
				additionalVersions.add(new BooleanRevision(eVersion, this));
			}
		}
	}

    public BooleanMember() {
        super();
    }

    @Override
	protected boolean membersEqual(
			ConceptComponent<BooleanRevision, BooleanMember> obj) {
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
			additionalVersions = new ArrayList<BooleanRevision>(
					additionalVersionCount);
		} else {
			additionalVersions.ensureCapacity(additionalVersions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			additionalVersions.add(new BooleanRevision(input, this));
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

	@Override
	public int getTypeId() {
		return REFSET_TYPES.BOOLEAN.getTypeNid();
	}

	@Override
	protected String getTypeFieldsString() {
		return Boolean.toString(booleanValue);
	}

	/**
	 * Returns a string representation of the object.
	 */
	@Override
	public String toString() {
	    StringBuffer buf = new StringBuffer();  
	    buf.append(this.getClass().getSimpleName() + ": ");
	    buf.append(" booleanValue:" + this.booleanValue);
	    buf.append("; ");
	    buf.append(super.toString());
	    return buf.toString();
	}

	@Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (BooleanMember.class.isAssignableFrom(obj.getClass())) {
            BooleanMember another = (BooleanMember) obj;
            return this.nid == another.nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { nid });
    }

}
