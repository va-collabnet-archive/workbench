package org.ihtsdo.concept.component.refsetmember.cidFloat;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.api.blueprint.RefexCUB;
import org.ihtsdo.tk.api.blueprint.RefexCUB.RefexProperty;
import org.ihtsdo.tk.api.refex.type_cnid_float.RefexCnidFloatAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetType;
import org.ihtsdo.tk.dto.concept.component.refset.cidflt.TkRefsetCidFloatMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidflt.TkRefsetCidFloatRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidFloatMember extends RefsetMember<CidFloatRevision, CidFloatMember>
        implements RefexCnidFloatAnalogBI<CidFloatRevision> {

    private static VersionComputer<RefsetMember<CidFloatRevision, CidFloatMember>.Version> computer =
            new VersionComputer<RefsetMember<CidFloatRevision, CidFloatMember>.Version>();

    protected VersionComputer<RefsetMember<CidFloatRevision, CidFloatMember>.Version> getVersionComputer() {
        return computer;
    }
    private int c1Nid;
    private float floatValue;

    public CidFloatMember(int enclosingConceptNid, TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    public CidFloatMember(TkRefsetCidFloatMember refsetMember, int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
        floatValue = refsetMember.getFloatValue();
        if (refsetMember.getRevisionList() != null) {
            revisions = new CopyOnWriteArrayList<CidFloatRevision>();
            for (TkRefsetCidFloatRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new CidFloatRevision(eVersion, this));
            }
        }
    }

    public CidFloatMember() {
        super();
    }

    @Override
    protected boolean membersEqual(
            ConceptComponent<CidFloatRevision, CidFloatMember> obj) {
        if (CidFloatMember.class.isAssignableFrom(obj.getClass())) {
            CidFloatMember another = (CidFloatMember) obj;
            return this.c1Nid == another.c1Nid && this.floatValue == another.floatValue;
        }
        return false;
    }

    @Override
    protected final CidFloatRevision readMemberRevision(TupleInput input) {
        return new CidFloatRevision(input, this);
    }

    @Override
    protected void readMemberFields(TupleInput input) {
        c1Nid = input.readInt();
        floatValue = input.readFloat();
    }

    @Override
    protected void writeMember(TupleOutput output) {
        output.writeInt(c1Nid);
        output.writeFloat(floatValue);
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(3);
        variableNids.add(getC1Nid());
        return variableNids;
    }

    @Override
    public CidFloatRevision makeAnalog(int statusNid, int pathNid, long time) {
        CidFloatRevision newR = new CidFloatRevision(statusNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public CidFloatRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        CidFloatRevision newR = new CidFloatRevision(statusNid, authorNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public CidFloatRevision makeAnalog() {
        return new CidFloatRevision(getStatusNid(), getPathNid(), getTime(), this);
    }

    public int getC1Nid() {
        return c1Nid;
    }

    public void setC1Nid(int c1Nid) {
        this.c1Nid = c1Nid;
        modified();
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
        modified();
    }

    @Override
    public int getTypeId() {
        return REFSET_TYPES.CID_FLOAT.getTypeNid();
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
        buf.append(" floatValue:" + this.floatValue);
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (CidFloatMember.class.isAssignableFrom(obj.getClass())) {
            CidFloatMember another = (CidFloatMember) obj;
            return this.c1Nid == another.c1Nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[]{c1Nid});
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
    public void setCnid1(int cnid) throws PropertyVetoException {
        this.c1Nid = cnid;
        modified();
    }

    @Override
    public int getCnid1() {
        return c1Nid;
    }

    @Override
    public void setFloat1(float f) throws PropertyVetoException {
        this.floatValue = f;
        modified();
    }

    @Override
    public float getFloat1() {
        return this.floatValue;
    }

    protected TkRefsetType getTkRefsetType() {
        return TkRefsetType.CID_FLOAT;
    }

    protected void addSpecProperties(RefexCUB rcs) {
        rcs.with(RefexProperty.CNID1, getCnid1());
        rcs.with(RefexProperty.FLOAT1, getFloat1());
    }
}
