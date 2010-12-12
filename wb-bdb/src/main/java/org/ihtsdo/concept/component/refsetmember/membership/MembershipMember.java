package org.ihtsdo.concept.component.refsetmember.membership;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.dto.concept.component.refset.member.TkRefsetMember;
import org.ihtsdo.tk.dto.concept.component.refset.member.TkRefsetRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.util.ArrayList;
import java.util.List;

public class MembershipMember extends RefsetMember<MembershipRevision, MembershipMember> {

    private static VersionComputer<RefsetMember<MembershipRevision, MembershipMember>.Version> computer =
            new VersionComputer<RefsetMember<MembershipRevision, MembershipMember>.Version>();

    protected VersionComputer<RefsetMember<MembershipRevision, MembershipMember>.Version> getVersionComputer() {
        return computer;
    }

    public MembershipMember(int enclosingConceptNid, TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    public MembershipMember(TkRefsetMember refsetMember,
            int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        if (refsetMember.getRevisionList() != null) {
            revisions = new CopyOnWriteArrayList<MembershipRevision>();
            for (TkRefsetRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new MembershipRevision(eVersion, this));
            }
        }
    }

    public MembershipMember() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (MembershipMember.class.isAssignableFrom(obj.getClass())) {
            MembershipMember another = (MembershipMember) obj;
            return this.nid == another.nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[]{this.nid});
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
        if (getTime() == time && getPathNid() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
        MembershipRevision newR = new MembershipRevision(statusNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public I_AmPart makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (getTime() == time && getPathNid() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
        MembershipRevision newR = new MembershipRevision(statusNid, authorNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public MembershipRevision makeAnalog() {
        MembershipRevision newR = new MembershipRevision(getStatusNid(), getPathNid(), getTime(), this);
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
        StringBuilder buf = new StringBuilder();
        buf.append(this.getClass().getSimpleName()).append(":{");
        buf.append(" }=> ");
        buf.append(super.toString());
        return buf.toString();
    }
    
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Version> getVersions() {
        if (versions == null) {
            int count = 1;
            if (revisions != null) {
                count = count + revisions.size();
            }
            ArrayList<Version> list = new ArrayList<Version>(count);
            if (getTime() != Long.MIN_VALUE) {
                list.add(new Version());
            }
            if (revisions != null) {
                for (int i = 0; i < revisions.size(); i++) {
                    if (revisions.get(i).getTime() != Long.MIN_VALUE) {
                        list.add(new Version(i));
                    }
                }
            }
            versions = list;
        }
        return (List<Version>) versions;
    }

}
