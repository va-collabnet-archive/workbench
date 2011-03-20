package org.ihtsdo.concept.component.refsetmember.cidCid;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetCidCidMember;
import org.ihtsdo.etypes.ERefsetCidCidRevision;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec.RefexProperty;
import org.ihtsdo.tk.api.refex.type_cnid_cnid.RefexCnidCnidAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetType;
import org.ihtsdo.tk.dto.concept.component.refset.cidcid.TkRefsetCidCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcid.TkRefsetCidCidRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidCidMember extends RefsetMember<CidCidRevision, CidCidMember>
        implements I_ExtendByRefPartCidCid<CidCidRevision>, RefexCnidCnidAnalogBI<CidCidRevision> {

    private static VersionComputer<RefsetMember<CidCidRevision, CidCidMember>.Version> computer =
            new VersionComputer<RefsetMember<CidCidRevision, CidCidMember>.Version>();

    protected VersionComputer<RefsetMember<CidCidRevision, CidCidMember>.Version> getVersionComputer() {
        return computer;
    }

    public class Version
            extends RefsetMember<CidCidRevision, CidCidMember>.Version
            implements I_ExtendByRefVersion<CidCidRevision>, I_ExtendByRefPartCidCid<CidCidRevision>,
            RefexCnidCnidAnalogBI<CidCidRevision> {

        private Version() {
            super();
        }

        private Version(int index) {
            super(index);
        }

        public int compareTo(I_ExtendByRefPart<CidCidRevision> o) {
            if (I_ExtendByRefPartCidCid.class.isAssignableFrom(o.getClass())) {
                I_ExtendByRefPartCidCid<CidCidRevision> another = (I_ExtendByRefPartCidCid<CidCidRevision>) o;
                if (this.getC1id() != another.getC1id()) {
                    return this.getC1id() - another.getC1id();
                }
                if (this.getC2id() != another.getC2id()) {
                    return this.getC2id() - another.getC2id();
                }
            }
            return super.compareTo(o);
        }

        @Override
        public int getC1id() {
            if (index >= 0) {
                return revisions.get(index).getC1id();
            }
            return CidCidMember.this.getC1Nid();
        }

        @Override
        public void setC1id(int c1id) {
            if (index >= 0) {
                revisions.get(index).setC1id(c1id);
            }
            CidCidMember.this.setC1Nid(c1id);
        }

        @Override
        public int getC2id() {
            if (index >= 0) {
                return revisions.get(index).getC2id();
            }
            return CidCidMember.this.getC2Nid();
        }

        @Override
        public void setC2id(int c2id) {
            if (index >= 0) {
                revisions.get(index).setC2id(c2id);
            }
            CidCidMember.this.setC2Nid(c2id);
        }

        @Override
        public int getCnid2() {
            if (index >= 0) {
                return revisions.get(index).getCnid2();
            }
            return CidCidMember.this.getCnid2();
        }

        @Override
        public void setCnid1(int cnid1) throws PropertyVetoException {
            if (index >= 0) {
                revisions.get(index).setCnid1(cnid1);
            }
            CidCidMember.this.setCnid1(cnid1);
        }

        @Override
        public int getCnid1() {
            if (index >= 0) {
                return revisions.get(index).getCnid1();
            }
            return CidCidMember.this.getCnid1();
        }

        @Override
        public void setCnid2(int cnid2) throws PropertyVetoException {
            if (index >= 0) {
                revisions.get(index).setCnid2(cnid2);
            }
            CidCidMember.this.setCnid2(cnid2);
        }

        @Override
        public ERefsetCidCidMember getERefsetMember() throws TerminologyException, IOException {
            return new ERefsetCidCidMember(this);
        }

        @Override
        public ERefsetCidCidRevision getERefsetRevision() throws TerminologyException, IOException {
            return new ERefsetCidCidRevision(this);
        }
    }
    private int c1Nid;
    private int c2Nid;

    public CidCidMember(int enclosingConceptNid,
            TupleInput input) throws IOException {
        super(enclosingConceptNid,
                input);
    }

    public CidCidMember(TkRefsetCidCidMember refsetMember,
            int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
        c2Nid = Bdb.uuidToNid(refsetMember.getC2Uuid());
        if (refsetMember.getRevisionList() != null) {
            revisions = new CopyOnWriteArrayList<CidCidRevision>();
            for (TkRefsetCidCidRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new CidCidRevision(eVersion, this));
            }
        }
    }

    public CidCidMember() {
        super();
    }

    @Override
    protected boolean membersEqual(
            ConceptComponent<CidCidRevision, CidCidMember> obj) {
        if (CidCidMember.class.isAssignableFrom(obj.getClass())) {
            CidCidMember another = (CidCidMember) obj;
            return this.c1Nid == another.c1Nid && this.c2Nid == another.c2Nid;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (CidCidMember.class.isAssignableFrom(obj.getClass())) {
            CidCidMember another = (CidCidMember) obj;
            return this.c1Nid == another.c1Nid && this.c2Nid == another.c2Nid &&
            this.nid == another.nid &&
            this.referencedComponentNid == another.referencedComponentNid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[]{c1Nid, c2Nid});
    }

    @Override
    protected final CidCidRevision readMemberRevision(TupleInput input) {
        return new CidCidRevision(input, this);
    }

    @Override
    protected void readMemberFields(TupleInput input) {
        c1Nid = input.readInt();
        c2Nid = input.readInt();
    }

    @Override
    protected void writeMember(TupleOutput output) {
        output.writeInt(c1Nid);
        output.writeInt(c2Nid);
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(4);
        variableNids.add(getC1id());
        variableNids.add(getC2id());
        return variableNids;
    }

    @Override
    public CidCidRevision makeAnalog(int statusNid, int pathNid, long time) {
        CidCidRevision newR = new CidCidRevision(statusNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public CidCidRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        CidCidRevision newR = new CidCidRevision(statusNid, authorNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public CidCidRevision makeAnalog() {
        return new CidCidRevision(getStatusNid(), getPathNid(), getTime(), this);
    }

    public int getC1Nid() {
        return c1Nid;
    }

    public void setC1Nid(int c1Nid) {
        this.c1Nid = c1Nid;
        modified();
    }

    public int getC2Nid() {
        return c2Nid;
    }

    public void setC2Nid(int c2Nid) {
        this.c2Nid = c2Nid;
        modified();
    }

    @Override
    public void setCnid1(int cnid1) throws PropertyVetoException {
        this.c1Nid = cnid1;
        modified();
    }

    @Override
    public int getCnid2() {
        return c2Nid;
    }

    @Override
    public int getCnid1() {
        return c1Nid;
    }

    @Override
    public void setCnid2(int cnid2) throws PropertyVetoException {
        this.c2Nid = cnid2;
        modified();
    }

    @Override
    public int getTypeId() {
        return REFSET_TYPES.CID_CID.getTypeNid();
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
        buf.append(" c2Nid: ");
        addNidToBuffer(buf, c2Nid);
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public int getC2id() {
        return getC2Nid();
    }

    @Override
    public void setC2id(int c2id) {
        setC2Nid(c2id);
    }

    @Override
    public int getC1id() {
        return getC1Nid();
    }

    @Override
    public void setC1id(int c1id) {
        setC1Nid(c1id);
    }

    @Override
    public CidCidMember duplicate() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
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

    protected TkRefsetType getTkRefsetType() {
        return TkRefsetType.CID_CID;
    }

    @Override
    public int getPartsHashCode() {
        return HashFunction.hashCode(new int[]{getC1id(), getC2id()});
    }

    @Override
    protected void addSpecProperties(RefexAmendmentSpec rcs) {
        rcs.with(RefexProperty.CNID1, getCnid1());
        rcs.with(RefexProperty.CNID2, getCnid2());
    }
}
