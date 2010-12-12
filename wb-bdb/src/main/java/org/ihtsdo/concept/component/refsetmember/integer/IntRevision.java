package org.ihtsdo.concept.component.refsetmember.integer;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.dto.concept.component.refset.integer.TkRefsetIntRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.util.Collection;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.refset.RefsetMemberVersionBI;

public class IntRevision extends RefsetRevision<IntRevision, IntMember>
        implements I_ExtendByRefPartInt {

    private int intValue;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" intValue:" + this.intValue);
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (IntRevision.class.isAssignableFrom(obj.getClass())) {
            IntRevision another = (IntRevision) obj;
            return intValue == another.intValue
                    && super.equals(obj);
        }
        return false;
    }

    public IntRevision(int statusNid, int pathNid, long time,
            IntMember primoridalMember) {
        super(statusNid, pathNid, time,
                primoridalMember);
        intValue = primoridalMember.getIntValue();
    }

    public IntRevision(int statusNid, int authorNid, int pathNid, long time,
            IntMember primoridalMember) {
        super(statusNid, authorNid, pathNid, time,
                primoridalMember);
        intValue = primoridalMember.getIntValue();
    }

    public IntRevision(int statusAtPositionNid,
            IntMember primoridalMember) {
        super(statusAtPositionNid,
                primoridalMember);
        intValue = primoridalMember.getIntValue();
    }

    protected IntRevision(int statusNid, int pathNid, long time,
            IntRevision another) {
        super(statusNid, pathNid, time, another.primordialComponent);
        intValue = another.intValue;
    }

    protected IntRevision(int statusNid, int authorNid, int pathNid, long time,
            IntRevision another) {
        super(statusNid, authorNid, pathNid, time, another.primordialComponent);
        intValue = another.intValue;
    }

    @Override
    public IntRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        IntRevision newR = new IntRevision(statusNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
    }

    @Override
    public IntRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        IntRevision newR = new IntRevision(statusNid, authorNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
    }

    @Override
    public IntRevision makeAnalog() {
        return new IntRevision(getStatusNid(), getPathNid(), getTime(), this);
    }

    public IntRevision(TupleInput input,
            IntMember primoridalMember) {
        super(input, primoridalMember);
        intValue = input.readInt();
    }

    public IntRevision(TkRefsetIntRevision eVersion,
            IntMember member) {
        super(eVersion, member);
        this.intValue = eVersion.getIntValue();
    }

    public IntRevision() {
        super();
    }

    @Override
    public UniversalAceExtByRefPart getUniversalPart()
            throws TerminologyException, IOException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public I_ExtendByRefPart makePromotionPart(PathBI promotionPath) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIntValue() {
        return intValue;
    }

    @Override
    public void setIntValue(int value) {
        this.intValue = value;
        modified();
    }

    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeInt(intValue);
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        return new ArrayIntList(2);
    }

    @Override
    public IntMember.Version getVersion(Coordinate c)
            throws ContraditionException {
        return (IntMember.Version) ((IntMember) primordialComponent).getVersion(c);
    }

    @Override
    public Collection<IntMember.Version> getVersions() {
        return ((IntMember) primordialComponent).getVersions();
    }

    @Override
    public Collection<? extends RefsetMemberVersionBI> getVersions(
            Coordinate c) {
        return ((IntMember) primordialComponent).getVersions(c);
    }
}
