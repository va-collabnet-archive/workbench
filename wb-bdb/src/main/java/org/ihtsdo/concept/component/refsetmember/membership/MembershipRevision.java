package org.ihtsdo.concept.component.refsetmember.membership;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.dto.concept.component.refset.member.TkRefsetRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.util.Collection;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.refset.RefsetMemberVersionBI;

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
        if (obj == null) {
            return false;
        }
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

    public MembershipRevision(int statusNid, int authorNid, int pathNid, long time,
            MembershipMember primoridalMember) {
        super(statusNid, authorNid, pathNid, time,
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

    protected MembershipRevision(int statusNid, int authorNid, int pathNid, long time,
            MembershipRevision another) {
        super(statusNid, pathNid, time, another.primordialComponent);
    }

    @Override
    public MembershipRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        MembershipRevision newR = new MembershipRevision(statusNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
    }

    @Override
    public MembershipRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        MembershipRevision newR = new MembershipRevision(statusNid, authorNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
    }

    @Override
    public MembershipRevision makeAnalog() {
        return new MembershipRevision(getStatusNid(), getPathNid(), getTime(), this);
    }

    public MembershipRevision(TkRefsetRevision eVersion,
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
    public RefsetRevision<MembershipRevision, MembershipMember> makePromotionPart(PathBI promotionPath) {
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
    
    
    @Override
    public MembershipMember.Version getVersion(Coordinate c)
            throws ContraditionException {
        return (MembershipMember.Version) ((MembershipMember) primordialComponent).getVersion(c);
    }

    @Override
    public Collection<MembershipMember.Version> getVersions() {
        return ((MembershipMember) primordialComponent).getVersions();
    }

    @Override
    public Collection<? extends RefsetMemberVersionBI> getVersions(
            Coordinate c) {
        return ((MembershipMember) primordialComponent).getVersions(c);
    }

}
