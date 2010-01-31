package org.ihtsdo.db.bdb.concept.component.refsetmember.str;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membership.MembershipMember;
import org.ihtsdo.etypes.ERefsetStrMember;
import org.ihtsdo.etypes.ERefsetStrVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class StrMember extends RefsetMember<StrRevision, StrMember> {

	private String stringValue;

	public StrMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public StrMember(ERefsetStrMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		stringValue = refsetMember.getStrValue();
		if (refsetMember.getExtraVersionsList() != null) {
			revisions = new ArrayList<StrRevision>(refsetMember.getExtraVersionsList().size());
			for (ERefsetStrVersion eVersion: refsetMember.getExtraVersionsList()) {
				revisions.add(new StrRevision(eVersion, this));
			}
		}
	}

    public StrMember() {
        super();
    }

	@Override
	protected boolean membersEqual(
			ConceptComponent<StrRevision, StrMember> obj) {
		if (StrMember.class.isAssignableFrom(obj.getClass())) {
			StrMember another = (StrMember) obj;
			return this.stringValue.equals(another.stringValue);
		}
		return false;
	}

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (StrMember.class.isAssignableFrom(obj.getClass())) {
            StrMember another = (StrMember) obj;
            return this.nid == another.nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { this.nid });
    } 
    
    @Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (revisions == null) {
			revisions = new ArrayList<StrRevision>(
					additionalVersionCount);
		} else {
			revisions.ensureCapacity(revisions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			revisions.add(new StrRevision(input, this));
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

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ": ");
        buf.append(" stringValue:" + "'" + this.stringValue + "'");
        buf.append("; ");
        buf.append(super.toString());
        return buf.toString();
    }

}
