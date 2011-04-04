package org.ihtsdo.concept.component.refsetmember.cidStr;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetCidStrMember;
import org.ihtsdo.etypes.ERefsetCidStrRevision;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.refex.type_cnid_str.RefexCnidStrAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.cidstr.TkRefsetCidStrMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidstr.TkRefsetCidStrRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidStrMember extends RefsetMember<CidStrRevision, CidStrMember> implements
        I_ExtendByRefPartCidString<CidStrRevision>, RefexCnidStrAnalogBI<CidStrRevision> {

    private static VersionComputer<RefsetMember<CidStrRevision, CidStrMember>.Version> computer =
            new VersionComputer<RefsetMember<CidStrRevision, CidStrMember>.Version>();

    protected VersionComputer<RefsetMember<CidStrRevision, CidStrMember>.Version> getVersionComputer() {
        return computer;
    }

    public class Version
            extends RefsetMember<CidStrRevision, CidStrMember>.Version
            implements I_ExtendByRefVersion<CidStrRevision>,
            I_ExtendByRefPartCidString<CidStrRevision>,
            RefexCnidStrAnalogBI<CidStrRevision> {

        private Version() {
            super();
        }

        private Version(int index) {
            super(index);
        }

        public int compareTo(I_ExtendByRefPart<CidStrRevision> o) {
            if (I_ExtendByRefPartCidString.class.isAssignableFrom(o.getClass())) {
                I_ExtendByRefPartCidString<CidStrRevision> another =
                        (I_ExtendByRefPartCidString<CidStrRevision>) o;
                if (this.getC1id() != another.getC1id()) {
                    return this.getC1id() - another.getC1id();
                }
                if (!this.getStringValue().equals(another.getStringValue())) {
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
            return CidStrMember.this.getC1Nid();
        }

        @Override
        public int getCnid1() {
            if (index >= 0) {
                return revisions.get(index).getCnid1();
            }
            return CidStrMember.this.getCnid1();
        }

        @Override
        public void setC1id(int c1id) {
            if (index >= 0) {
                revisions.get(index).setC1id(c1id);
            }
            CidStrMember.this.setC1Nid(c1id);
        }

        @Override
        public void setCnid1(int c1id) throws PropertyVetoException {
            if (index >= 0) {
                revisions.get(index).setCnid1(c1id);
            }
            CidStrMember.this.setCnid1(c1id);
        }

        @Override
        public I_ExtendByRefPartCidString<CidStrRevision> duplicate() {
            return (I_ExtendByRefPartCidString<CidStrRevision>) super.duplicate();
        }

        @Override
        public String getStringValue() {
            if (index >= 0) {
                return revisions.get(index).getStringValue();
            }
            return CidStrMember.this.getStringValue();
        }

        @Override
        public void setStringValue(String value) {
            if (index >= 0) {
                revisions.get(index).setStringValue(value);
            }
            CidStrMember.this.setStringValue(value);
        }

        @Override
        public String getStr1() {
            if (index >= 0) {
                return revisions.get(index).getStr1();
            }
            return CidStrMember.this.getStr1();
        }

        @Override
        public void setStr1(String value) {
            if (index >= 0) {
                revisions.get(index).setStringValue(value);
            }
            CidStrMember.this.setStringValue(value);
        }

        @Override
        public ERefsetCidStrMember getERefsetMember() throws TerminologyException, IOException {
            return new ERefsetCidStrMember(this);
        } 

        @Override
        public ERefsetCidStrRevision getERefsetRevision() throws TerminologyException, IOException {
            return new ERefsetCidStrRevision(this);
        }

        @Override
        public int hashCodeOfParts() {
            return HashFunction.hashCode(new int[]{getC1Nid(), getStringValue().hashCode()});
        }
    }
    private int c1Nid;
    private String strValue;

    public CidStrMember(int enclosingConceptNid, TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    public CidStrMember(TkRefsetCidStrMember refsetMember, int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
        strValue = refsetMember.getStrValue();
        if (refsetMember.getRevisionList() != null) {
            revisions = new CopyOnWriteArrayList<CidStrRevision>();
            for (TkRefsetCidStrRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new CidStrRevision(eVersion, this));
            }
        }
    }

    public CidStrMember() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (CidStrMember.class.isAssignableFrom(obj.getClass())) {
            CidStrMember another = (CidStrMember) obj;
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
            ConceptComponent<CidStrRevision, CidStrMember> obj) {
        if (CidStrMember.class.isAssignableFrom(obj.getClass())) {
            CidStrMember another = (CidStrMember) obj;
            return this.c1Nid == another.c1Nid && this.strValue.equals(another.strValue);
        }
        return false;
    }

    @Override
    protected final CidStrRevision readMemberRevision(TupleInput input) {
        return new CidStrRevision(input, this);
    }

    @Override
    protected void readMemberFields(TupleInput input) {
        c1Nid = input.readInt();
        strValue = input.readString();
    }

    @Override
    protected void writeMember(TupleOutput output) {
        output.writeInt(c1Nid);
        output.writeString(strValue);
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(3);
        variableNids.add(getC1id());
        return variableNids;
    }

    @Override
    public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        CidStrRevision newR = new CidStrRevision(statusNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public CidStrRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        CidStrRevision newR = new CidStrRevision(statusNid, authorNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public CidStrRevision makeAnalog() {
        CidStrRevision newR = new CidStrRevision(getStatusNid(), getPathNid(), getTime(), this);
        return newR;
    }

    public int getC1Nid() {
        return c1Nid;
    }

    public void setC1Nid(int c1Nid) {
        this.c1Nid = c1Nid;
        modified();
    }

    public String getStringValue() {
        return strValue;
    }

    public String getStr1() {
        return strValue;
    }

    public void setStringValue(String strValue) {
        this.strValue = strValue;
        modified();
    }

    @Override
    public int getTypeId() {
        return REFSET_TYPES.CID_STR.getTypeNid();
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
        buf.append(" strValue:" + "'" + this.strValue + "'");
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public CidStrRevision duplicate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getC1id() {
        return getC1Nid();
    }

    @Override
    public void setC1id(int c1id) {
        setC1Nid(c1id);
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

    @Override
    public void setCnid1(int cnid) throws PropertyVetoException {
        this.c1Nid = cnid;
        modified();
    }

    @Override
    public void setStr1(String str) throws PropertyVetoException {
        this.strValue = str;
        modified();
    }

    @Override
    public int getCnid1() {
        return c1Nid;
    }

    protected TK_REFSET_TYPE getTkRefsetType() {
        return TK_REFSET_TYPE.CID_STR;
    }

    protected void addSpecProperties(RefexCAB rcs) {
        rcs.with(RefexProperty.CNID1, getCnid1());
        rcs.with(RefexProperty.STRING1, getStr1());
    }
}
