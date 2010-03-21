package org.ihtsdo.concept.component.refsetmember.membership;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.ERefsetMemberMember;
import org.ihtsdo.etypes.ERefsetRevision;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class MembershipMember extends RefsetMember<MembershipRevision, MembershipMember> {

	private static VersionComputer<RefsetMember<MembershipRevision, MembershipMember>.Version> computer = 
		new VersionComputer<RefsetMember<MembershipRevision, MembershipMember>.Version>();

	protected VersionComputer<RefsetMember<MembershipRevision, MembershipMember>.Version> getVersionComputer() {
		return computer;
	}

	public MembershipMember(Concept enclosingConcept, TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}

	public MembershipMember(ERefsetMemberMember refsetMember, Concept enclosingConcept) throws IOException {
		super(refsetMember, enclosingConcept);
		if (refsetMember.getRevisionList() != null) {
			revisions = new ArrayList<MembershipRevision>(refsetMember.getRevisionList().size());
			for (ERefsetRevision eVersion: refsetMember.getRevisionList()) {
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
	protected final MembershipRevision readMemberRevision(TupleInput input) {
	    return new MembershipRevision(input, this);
	}
	@Override
	protected void readMemberFields(TupleInput input) {
		// nothing to read...
	}
	@Override
	protected void writeMember(TupleOutput output) {
		// nothing to write
	}

    @Override
    protected ArrayIntList getVariableVersionNids() {
        return new ArrayIntList(2);
    }

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		MembershipRevision newR = new MembershipRevision(statusNid, pathNid, time, this);
		addRevision(newR);
		return newR;
	}
	
	@Override
	public int getTypeId() {
		return REFSET_TYPES.MEMBER.getTypeNid();
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
