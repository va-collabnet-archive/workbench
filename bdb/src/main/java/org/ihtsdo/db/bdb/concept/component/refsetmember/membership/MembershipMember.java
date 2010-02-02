package org.ihtsdo.db.bdb.concept.component.refsetmember.membership;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.Long.LongMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.integer.IntRevision;
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
			revisions = new ArrayList<MembershipRevision>(refsetMember.getExtraVersionsList().size());
			for (ERefsetVersion eVersion: refsetMember.getExtraVersionsList()) {
				revisions.add(new MembershipRevision(eVersion, this));
			}
		}
	}

    public MembershipMember() {
        super();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (MembershipMember.class.isAssignableFrom(obj.getClass())) {
            MembershipMember another = (MembershipMember) obj;
            return this.nid == another.nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { this.nid });
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
		if (revisions == null) {
			revisions = new ArrayList<MembershipRevision>(
					additionalVersionCount);
		} else {
			revisions.ensureCapacity(revisions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			revisions.add(new MembershipRevision(input, this));
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
		if (enclosingConcept.isEditable()) {
			MembershipRevision newR = new MembershipRevision(statusNid, pathNid, time, this);
			addVersion(newR);
			return newR;
		}
		throw new UnsupportedOperationException("enclosingConcept is not editable");
	}
	
	@Override
	public int getTypeId() {
		return REFSET_TYPES.MEMBER.getTypeNid();
	}

	@Override
	protected String getTypeFieldsString() {
		return "";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" }=> ");
        buf.append(super.toString());
        return buf.toString();
    }

}
