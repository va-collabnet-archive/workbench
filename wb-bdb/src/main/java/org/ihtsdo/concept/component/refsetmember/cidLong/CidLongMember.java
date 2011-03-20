package org.ihtsdo.concept.component.refsetmember.cidLong;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec.RefexProperty;
import org.ihtsdo.tk.api.refex.type_cnid_long.RefexCnidLongAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetType;
import org.ihtsdo.tk.dto.concept.component.refset.cidlong.TkRefsetCidLongMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidlong.TkRefsetCidLongRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidLongMember
        extends RefsetMember<CidLongRevision, CidLongMember>
        implements RefexCnidLongAnalogBI<CidLongRevision> {

    private static VersionComputer<RefsetMember<CidLongRevision, CidLongMember>.Version> computer =
            new VersionComputer<RefsetMember<CidLongRevision, CidLongMember>.Version>();

    protected VersionComputer<RefsetMember<CidLongRevision, CidLongMember>.Version> getVersionComputer() {
        return computer;
    }
    private int c1Nid;
    private long longValue;

    public CidLongMember(int enclosingConceptNid,
            TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    public CidLongMember(TkRefsetCidLongMember refsetMember,
            int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
        longValue = refsetMember.getLongValue();
        if (refsetMember.getRevisionList() != null) {
            revisions = new CopyOnWriteArrayList<CidLongRevision>();
            for (TkRefsetCidLongRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new CidLongRevision(eVersion, this));
            }
        }
    }

    public CidLongMember() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (CidLongMember.class.isAssignableFrom(obj.getClass())) {
            CidLongMember another = (CidLongMember) obj;
            return this.c1Nid == another.c1Nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[]{c1Nid});
    }

    @Override
    protected boolean membersEqual(
            ConceptComponent<CidLongRevision, CidLongMember> obj) {
        if (CidLongMember.class.isAssignableFrom(obj.getClass())) {
            CidLongMember another = (CidLongMember) obj;
            return this.c1Nid == another.c1Nid && this.longValue == another.longValue;
        }
        return false;
    }

    @Override
    protected final CidLongRevision readMemberRevision(TupleInput input) {
        return new CidLongRevision(input, this);
    }

    @Override
    protected void readMemberFields(TupleInput input) {
        c1Nid = input.readInt();
        longValue = input.readLong();
    }

    @Override
    protected void writeMember(TupleOutput output) {
        output.writeInt(c1Nid);
        output.writeLong(longValue);
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(3);
        variableNids.add(getC1Nid());
        return variableNids;
    }

    @Override
    public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        CidLongRevision newR = new CidLongRevision(statusNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public CidLongRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        CidLongRevision newR = new CidLongRevision(statusNid, authorNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public CidLongRevision makeAnalog() {
        CidLongRevision newR = new CidLongRevision(getStatusNid(), getPathNid(), getTime(), this);
        return newR;
    }

    public int getC1Nid() {
        return c1Nid;
    }

    public void setC1Nid(int c1Nid) {
        this.c1Nid = c1Nid;
        modified();
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
        modified();
    }

    @Override
    public int getTypeId() {
        return REFSET_TYPES.CID_LONG.getTypeNid();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid: ");
        addNidToBuffer(buf, c1Nid);
        buf.append(" longValue:" + this.longValue);
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

    @Override
    public long getLong1() {
        return this.longValue;
    }

    @Override
    public void setLong1(long l) throws PropertyVetoException {
        this.longValue = l;
        modified();
    }

    @Override
    public void setCnid1(int cnid1) throws PropertyVetoException {
        this.c1Nid = cnid1;
        modified();
    }

    @Override
    public int getCnid1() {
        return c1Nid;
    }

    protected TkRefsetType getTkRefsetType() {
        return TkRefsetType.CID_LONG;
    }

    protected void addSpecProperties(RefexAmendmentSpec rcs) {
        rcs.with(RefexProperty.CNID1, getCnid1());
        rcs.with(RefexProperty.LONG1, getLong1());
    }
}
