package org.ihtsdo.concept.component.refsetmember.membership;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.etypes.ERefsetRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class MembershipRevision extends RefsetRevision<MembershipRevision, MembershipMember> {

	
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(super.toString());
        return buf.toString();
    }
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (MembershipRevision.class.isAssignableFrom(obj.getClass())) {
            return super.equals(obj);
        }
        return false;
    }

    public MembershipRevision(int statusNid, int pathNid, long time, 
			MembershipMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	public MembershipRevision(int statusAtPositionNid, 
			MembershipMember primoridalMember) {
		super(statusAtPositionNid, primoridalMember);
	}

	public MembershipRevision(TupleInput input, 
			MembershipMember primoridalMember) {
		super(input, primoridalMember);
	}

	protected MembershipRevision(int statusNid, int pathNid, long time, 
			MembershipRevision another) {
		super(statusNid, pathNid, time, another.primordialComponent);
	}

	@Override
	public MembershipRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathId() == pathNid) {
            this.setStatusId(statusNid);
            return this;
        }
		return new MembershipRevision(statusNid, pathNid, time, this);
	}

	public MembershipRevision(ERefsetRevision eVersion,
			MembershipMember member) {
		super(eVersion, member);
	}

    public MembershipRevision() {
        super();
    }

	@Override
	public UniversalAceExtByRefPart getUniversalPart()
			throws TerminologyException, IOException {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public RefsetRevision<MembershipRevision, MembershipMember> makePromotionPart(I_Path promotionPath) {
		// TODO
		throw new UnsupportedOperationException();
	}

    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        // nothing to write
    }
    
    @Override
    public ArrayIntList getVariableVersionNids() {
        return new ArrayIntList(2);
    }


}
