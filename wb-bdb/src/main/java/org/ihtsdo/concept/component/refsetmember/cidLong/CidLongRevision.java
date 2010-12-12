package org.ihtsdo.concept.component.refsetmember.cidLong;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidLong;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.dto.concept.component.refset.cidlong.TkRefsetCidLongRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.util.Collection;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.refset.RefsetMemberVersionBI;

public class CidLongRevision extends RefsetRevision<CidLongRevision, CidLongMember>
        implements I_ExtendByRefPartCidLong {

    private int c1Nid;
    private long longValue;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid: ");
        ConceptComponent.addNidToBuffer(buf, c1Nid);
        buf.append(" longValue:" + this.longValue);
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (CidLongRevision.class.isAssignableFrom(obj.getClass())) {
            CidLongRevision another = (CidLongRevision) obj;
            return this.c1Nid == another.c1Nid
                    && longValue == another.longValue
                    && super.equals(obj);
        }
        return false;
    }

    protected CidLongRevision(int statusNid, int pathNid, long time,
            CidLongMember primoridalMember) {
        super(statusNid, pathNid, time,
                primoridalMember);
        c1Nid = primoridalMember.getC1Nid();
        longValue = primoridalMember.getLongValue();
    }

    protected CidLongRevision(int statusNid, int authorNid, int pathNid, long time,
            CidLongMember primoridalMember) {
        super(statusNid, authorNid, pathNid, time,
                primoridalMember);
        c1Nid = primoridalMember.getC1Nid();
        longValue = primoridalMember.getLongValue();
    }

    protected CidLongRevision(int statusAtPositionNid,
            CidLongMember primoridalMember) {
        super(statusAtPositionNid,
                primoridalMember);
        c1Nid = primoridalMember.getC1Nid();
        longValue = primoridalMember.getLongValue();
    }

    protected CidLongRevision(int statusNid, int pathNid, long time,
            CidLongRevision another) {
        super(statusNid, pathNid, time, another.primordialComponent);
        c1Nid = another.c1Nid;
        longValue = another.longValue;
    }

    protected CidLongRevision(int statusNid, int authorNid, int pathNid, long time,
            CidLongRevision another) {
        super(statusNid, authorNid, pathNid, time, another.primordialComponent);
        c1Nid = another.c1Nid;
        longValue = another.longValue;
    }

    @Override
    public CidLongRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        CidLongRevision newR = new CidLongRevision(statusNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
    }

    @Override
    public CidLongRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        CidLongRevision newR = new CidLongRevision(statusNid, authorNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
    }

    @Override
    public CidLongRevision makeAnalog() {
        return new CidLongRevision(getStatusNid(), getPathNid(), getTime(), this);
    }

    public CidLongRevision(TupleInput input,
            CidLongMember primoridalMember) {
        super(input, primoridalMember);
        c1Nid = input.readInt();
        longValue = input.readLong();
    }

    public CidLongRevision(TkRefsetCidLongRevision eVersion,
            CidLongMember member) {
        super(eVersion, member);
        c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
        longValue = eVersion.getLongValue();
    }

    public CidLongRevision() {
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

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
        modified();
    }

    @Override
    public int getC1id() {
        return c1Nid;
    }

    @Override
    public void setC1id(int c1id) {
        this.c1Nid = c1id;
        modified();
    }

    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeInt(c1Nid);
        output.writeLong(longValue);
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(3);
        variableNids.add(getC1id());
        return variableNids;
    }
    
    
    @Override
    public CidLongMember.Version getVersion(Coordinate c)
            throws ContraditionException {
        return (CidLongMember.Version) ((CidLongMember) primordialComponent).getVersion(c);
    }

    @Override
    public Collection<CidLongMember.Version> getVersions() {
        return ((CidLongMember) primordialComponent).getVersions();
    }

    @Override
    public Collection<? extends RefsetMemberVersionBI> getVersions(
            Coordinate c) {
        return ((CidLongMember) primordialComponent).getVersions(c);
    }

}
