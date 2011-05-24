package org.ihtsdo.concept.component.refsetmember.cidCidStr;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetCidCidStrMember;
import org.ihtsdo.etypes.ERefsetCidCidStrRevision;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.refex.type_cnid_cnid_str.RefexCnidCnidStrAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.cidcidstr.TkRefsetCidCidStrMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcidstr.TkRefsetCidCidStrRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidCidStrMember extends RefsetMember<CidCidStrRevision, CidCidStrMember>
        implements I_ExtendByRefPartCidCidString<CidCidStrRevision>,
        RefexCnidCnidStrAnalogBI<CidCidStrRevision> {

    private static VersionComputer<RefsetMember<CidCidStrRevision, CidCidStrMember>.Version> computer =
            new VersionComputer<RefsetMember<CidCidStrRevision, CidCidStrMember>.Version>();

    protected VersionComputer<RefsetMember<CidCidStrRevision, CidCidStrMember>.Version> getVersionComputer() {
        return computer;
    }

    public class Version
            extends RefsetMember<CidCidStrRevision, CidCidStrMember>.Version
            implements I_ExtendByRefVersion<CidCidStrRevision>,
            I_ExtendByRefPartCidCidString<CidCidStrRevision>,
            RefexCnidCnidStrAnalogBI<CidCidStrRevision> {

        private Version() {
            super();
        }

        private Version(int index) {
            super(index);
        }

        public int compareTo(I_ExtendByRefPart<CidCidStrRevision> o) {
            if (I_ExtendByRefPartCidCidString.class.isAssignableFrom(o.getClass())) {
                I_ExtendByRefPartCidCidString<CidCidStrRevision> another =
                        (I_ExtendByRefPartCidCidString<CidCidStrRevision>) o;
                if (this.getC1id() != another.getC1id()) {
                    return this.getC1id() - another.getC1id();
                }
                if (this.getC2id() != another.getC2id()) {
                    return this.getC2id() - another.getC2id();
                }
                if (this.getStringValue().equals(another.getStringValue())) {
                    return this.getStringValue().compareTo(another.getStringValue());
                }
            }
            return super.compareTo(o);
        }

        @Override
        public int getC1id() {
            if (index >= 0) {
                return revisions.get(index).getC1id();
            }
            return CidCidStrMember.this.getC1Nid();
        }

        @Override
        public void setC1id(int c1id) {
            if (index >= 0) {
                revisions.get(index).setC1id(c1id);
            }
            CidCidStrMember.this.setC1Nid(c1id);
        }

        @Override
        public int getC2id() {
            if (index >= 0) {
                return revisions.get(index).getC2id();
            }
            return CidCidStrMember.this.getC2Nid();
        }

        @Override
        public void setC2id(int c2id) {
            if (index >= 0) {
                revisions.get(index).setC2id(c2id);
            }
            CidCidStrMember.this.setC2Nid(c2id);
        }

        @Override
        public void setCnid2(int cnid2) throws PropertyVetoException {
            if (index >= 0) {
                revisions.get(index).setCnid2(cnid2);
            }
            CidCidStrMember.this.setCnid2(cnid2);
        }

        @Override
        public int getCnid1() {
            if (index >= 0) {
                return revisions.get(index).getCnid1();
            }
            return CidCidStrMember.this.getCnid1();
        }

        @Override
        public int getCnid2() {
            if (index >= 0) {
                return revisions.get(index).getCnid2();
            }
            return CidCidStrMember.this.getCnid2();
        }

        @Override
        public void setStr1(String str) throws PropertyVetoException {
            if (index >= 0) {
                revisions.get(index).setStr1(str);
            }
            CidCidStrMember.this.setStr1(str);
        }

        @Override
        public void setCnid1(int cnid1) throws PropertyVetoException {
            if (index >= 0) {
                revisions.get(index).setCnid1(cnid1);
            }
            CidCidStrMember.this.setCnid1(cnid1);
        }

        @Override
        public String getStr1() {
            if (index >= 0) {
                return revisions.get(index).getStr1();
            }
            return CidCidStrMember.this.getStr1();
        }

        @Override
        public String getStringValue() {
            if (index >= 0) {
                return revisions.get(index).getStringValue();
            }
            return CidCidStrMember.this.getStringValue();
        }

        @Override
        public void setStringValue(String value) {
            if (index >= 0) {
                revisions.get(index).setStringValue(value);
            }
            CidCidStrMember.this.setStringValue(value);
        }

        @Override
        public I_ExtendByRefPartCidCidString<CidCidStrRevision> duplicate() {
            return (I_ExtendByRefPartCidCidString<CidCidStrRevision>) super.duplicate();
        }

        @Override
        public ERefsetCidCidStrMember getERefsetMember() throws TerminologyException, IOException {
            return new ERefsetCidCidStrMember(this);
        }

        @Override
        public ERefsetCidCidStrRevision getERefsetRevision() throws TerminologyException, IOException {
            return new ERefsetCidCidStrRevision(this);
        }

        @Override
        public int hashCodeOfParts() {
            return HashFunction.hashCode(new int[]{getC1Nid(), getC2Nid(), getStringValue().hashCode()});
        }
    }
    private int c1Nid;
    private int c2Nid;
    private String strValue;

    @Override
    public boolean readyToWriteRefsetMember() {
        assert c1Nid != Integer.MAX_VALUE;
        assert c2Nid != Integer.MAX_VALUE;
        assert strValue != null;
        return true;
    }

    public CidCidStrMember(int enclosingConceptNid,
            TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    public CidCidStrMember(TkRefsetCidCidStrMember refsetMember,
            int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
        c2Nid = Bdb.uuidToNid(refsetMember.getC2Uuid());
        strValue = refsetMember.getStrValue();
        if (refsetMember.getRevisionList() != null) {
            revisions = new CopyOnWriteArrayList<CidCidStrRevision>();
            for (TkRefsetCidCidStrRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new CidCidStrRevision(eVersion, this));
            }
        }
    }

    public CidCidStrMember() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (CidCidStrMember.class.isAssignableFrom(obj.getClass())) {
            CidCidStrMember another = (CidCidStrMember) obj;
            return this.c1Nid == another.c1Nid
                    && this.c2Nid == another.c2Nid && this.nid == another.nid
                    && this.referencedComponentNid == another.referencedComponentNid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[]{c1Nid, c2Nid});
    }

    @Override
    protected boolean membersEqual(
            ConceptComponent<CidCidStrRevision, CidCidStrMember> obj) {
        if (CidCidStrMember.class.isAssignableFrom(obj.getClass())) {
            CidCidStrMember another = (CidCidStrMember) obj;
            return this.c1Nid == another.c1Nid && this.c2Nid == another.c2Nid && this.strValue.equals(another.strValue);
        }
        return false;
    }

    @Override
    protected final CidCidStrRevision readMemberRevision(TupleInput input) {
        return new CidCidStrRevision(input, this);
    }

    @Override
    protected void readMemberFields(TupleInput input) {
        c1Nid = input.readInt();
        c2Nid = input.readInt();
        strValue = input.readString();
    }

    @Override
    protected void writeMember(TupleOutput output) {
        output.writeInt(c1Nid);
        output.writeInt(c2Nid);
        output.writeString(strValue);
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(4);
        variableNids.add(getC1id());
        variableNids.add(getC2id());
        return variableNids;
    }

    @Override
    public CidCidStrRevision makeAnalog(int statusNid, int pathNid, long time) {
        CidCidStrRevision newR = new CidCidStrRevision(statusNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public CidCidStrRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        CidCidStrRevision newR = new CidCidStrRevision(statusNid, authorNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public CidCidStrRevision makeAnalog() {
        return new CidCidStrRevision(getStatusNid(), getPathNid(), getTime(), this);
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
    public void setCnid2(int cnid2) throws PropertyVetoException {
        this.c2Nid = cnid2;
        modified();
    }

    @Override
    public String getStr1() {
        return this.strValue;
    }

    @Override
    public void setStr1(String str) throws PropertyVetoException {
        this.strValue = str;
        modified();
    }

    @Override
    public int getCnid2() {
        return c2Nid;
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

    public String getStringValue() {
        return strValue;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStringValue(String strValue) {
        this.strValue = strValue;
        modified();
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
        modified();
    }

    @Override
    public int getTypeId() {
        return REFSET_TYPES.CID_CID_STR.getTypeNid();
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
        buf.append(" strValue:" + "'" + this.strValue + "'");
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
    public CidCidStrMember duplicate() {
        throw new UnsupportedOperationException();
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

    protected TK_REFSET_TYPE getTkRefsetType() {
        return TK_REFSET_TYPE.CID_CID_STR;
    }

    protected void addSpecProperties(RefexCAB rcs) {
        rcs.with(RefexProperty.CNID1, getCnid1());
        rcs.with(RefexProperty.CNID2, getCnid2());
        rcs.with(RefexProperty.STRING1, getStr1());
    }
}
