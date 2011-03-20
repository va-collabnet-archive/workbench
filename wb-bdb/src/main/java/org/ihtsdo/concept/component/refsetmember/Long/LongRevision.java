package org.ihtsdo.concept.component.refsetmember.Long;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartLong;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.concept.component.refsetmember.Long.LongMember.Version;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetType;
import org.ihtsdo.tk.dto.concept.component.refset.Long.TkRefsetLongRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class LongRevision extends RefsetRevision<LongRevision, LongMember>
        implements I_ExtendByRefPartLong<LongRevision>, RefexLongAnalogBI<LongRevision> {

    private long longValue;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getClass().getSimpleName()).append(":{");
        buf.append(" longValue:").append(this.longValue);
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (LongRevision.class.isAssignableFrom(obj.getClass())) {
            LongRevision another = (LongRevision) obj;
            return this.longValue == another.longValue
                    && super.equals(obj);
        }
        return false;
    }

    public LongRevision(int statusNid, int pathNid, long time,
            LongMember primoridalMember) {
        super(statusNid, pathNid, time,
                primoridalMember);
        longValue = primoridalMember.getLongValue();
    }

    public LongRevision(int statusNid, int authorNid, int pathNid, long time,
            LongMember primoridalMember) {
        super(statusNid, authorNid, pathNid, time,
                primoridalMember);
        longValue = primoridalMember.getLongValue();
    }

    public LongRevision(int statusAtPositionNid,
            LongMember primoridalMember) {
        super(statusAtPositionNid, primoridalMember);
        longValue = primoridalMember.getLongValue();
    }

    protected LongRevision(int statusNid, int authorNid, int pathNid, long time,
            LongRevision another) {
        super(statusNid, authorNid, pathNid, time, another.primordialComponent);
        longValue = another.longValue;
    }

    protected LongRevision(int statusNid, int pathNid, long time,
            LongRevision another) {
        super(statusNid, pathNid, time, another.primordialComponent);
        longValue = another.longValue;
    }

    @Override
    public LongRevision makeAnalog(int statusNid, int pathNid, long time) {
        LongRevision newR = new LongRevision(statusNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
    }

    @Override
    public LongRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        LongRevision newR = new LongRevision(statusNid, authorNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
    }

    @Override
    public LongRevision makeAnalog() {
        return new LongRevision(getStatusNid(), getPathNid(), getTime(), this);
    }

    public LongRevision(TupleInput input,
            LongMember primoridalMember) {
        super(input, primoridalMember);
        longValue = input.readLong();
    }

    public LongRevision(TkRefsetLongRevision eVersion,
            LongMember member) {
        super(eVersion, member);
        this.longValue = eVersion.getLongValue();
    }

    public LongRevision() {
        super();
    }

    @Override
    public UniversalAceExtByRefPart getUniversalPart()
            throws TerminologyException, IOException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public I_ExtendByRefPart<LongRevision> makePromotionPart(PathBI promotionPath) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
        modified();
    }

    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeLong(longValue);
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        return new ArrayIntList(2);
    }

    @Override
    public LongMember.Version getVersion(ViewCoordinate c)
            throws ContraditionException {
        return (Version) ((LongMember) primordialComponent).getVersion(c);
    }

    @Override
    public Collection<LongMember.Version> getVersions() {
        return ((LongMember) primordialComponent).getVersions();
    }

    @Override
    public Collection<? extends RefexVersionBI<LongRevision>> getVersions(
            ViewCoordinate c) {
        return ((LongMember) primordialComponent).getVersions(c);
    }

    @Override
    public void setLong1(long l) throws PropertyVetoException {
        this.longValue = l;
        modified();
    }

    @Override
    public long getLong1() {
        return longValue;
    }

    @Override
    protected TkRefsetType getTkRefsetType() {
        return TkRefsetType.LONG;
    }

    @Override
    protected void addSpecProperties(RefexCAB rcs) {
        rcs.with(RefexProperty.LONG1, getLong1());
    }

    @Override
    public int getPartsHashCode() {
        return HashFunction.hashCode(new int[]{new Long(getLongValue()).hashCode()});
    }
}
