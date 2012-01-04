package org.ihtsdo.concept.component.refsetmember.cidCidCid;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetCidCidCidMember;
import org.ihtsdo.etypes.ERefsetCidCidCidRevision;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_cnid_cnid_cnid.RefexCnidCnidCnidAnalogBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid_cnid.RefexCnidCnidCnidVersionBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcidcid.TkRefsetCidCidCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcidcid.TkRefsetCidCidCidRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.refex.RefexVersionBI;

public class CidCidCidMember extends RefsetMember<CidCidCidRevision, CidCidCidMember>
        implements I_ExtendByRefPartCidCidCid<CidCidCidRevision>,
        RefexCnidCnidCnidVersionBI<CidCidCidRevision>,
        RefexCnidCnidCnidAnalogBI<CidCidCidRevision> {

    private static VersionComputer<RefsetMember<CidCidCidRevision, CidCidCidMember>.Version> computer =
            new VersionComputer<RefsetMember<CidCidCidRevision, CidCidCidMember>.Version>();
    //~--- fields --------------------------------------------------------------
    private int c1Nid;
    private int c2Nid;
    private int c3Nid;

    //~--- constructors --------------------------------------------------------
    public CidCidCidMember() {
        super();
    }

    public CidCidCidMember(int enclosingConceptNid, TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    public CidCidCidMember(TkRefsetCidCidCidMember refsetMember, int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
        c2Nid = Bdb.uuidToNid(refsetMember.getC2Uuid());
        c3Nid = Bdb.uuidToNid(refsetMember.getC3Uuid());

        if (refsetMember.getRevisionList() != null) {
            revisions = new RevisionSet<CidCidCidRevision, CidCidCidMember>(primordialSapNid);

            for (TkRefsetCidCidCidRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new CidCidCidRevision(eVersion, this));
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addRefsetTypeNids(Set<Integer> allNids) {
        allNids.add(c1Nid);
        allNids.add(c2Nid);
        allNids.add(c3Nid);
    }

    protected void addSpecProperties(RefexCAB rcs) {
        rcs.with(RefexProperty.CNID1, getCnid1());
        rcs.with(RefexProperty.CNID2, getCnid2());
        rcs.with(RefexProperty.CNID3, getCnid3());
    }

    @Override
    public CidCidCidMember duplicate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (CidCidCidMember.class.isAssignableFrom(obj.getClass())) {
            CidCidCidMember another = (CidCidCidMember) obj;

            return (this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid)
                    && (this.c3Nid == another.c3Nid) && (this.nid == another.nid)
                    && (this.referencedComponentNid == another.referencedComponentNid);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Hashcode.compute(new int[]{c1Nid, c2Nid, c3Nid});
    }

    @Override
    public CidCidCidRevision makeAnalog() {
        return new CidCidCidRevision(getStatusNid(), getPathNid(), getTime(), this);
    }

    @Override
    public CidCidCidRevision makeAnalog(int statusNid, int pathNid, long time) {
        CidCidCidRevision newR = new CidCidCidRevision(statusNid, pathNid, time, this);

        addRevision(newR);

        return newR;
    }

    @Override
    public CidCidCidRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        CidCidCidRevision newR = new CidCidCidRevision(statusNid, authorNid, pathNid, time, this);

        addRevision(newR);

        return newR;
    }

    @Override
    protected boolean refexFieldsEqual(ConceptComponent<CidCidCidRevision, CidCidCidMember> obj) {
        if (CidCidCidMember.class.isAssignableFrom(obj.getClass())) {
            CidCidCidMember another = (CidCidCidMember) obj;

            return (this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid)
                    && (this.c3Nid == another.c3Nid);
        }

        return false;
    }
    
    
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexCnidCnidCnidVersionBI.class.isAssignableFrom(another.getClass())){
            RefexCnidCnidCnidVersionBI cv = (RefexCnidCnidCnidVersionBI) another;
            return (this.c1Nid == cv.getCnid1()) && (this.c2Nid == cv.getCnid2())
                    && (this.c3Nid == cv.getCnid3());
        }
        return false;
    }

    @Override
    protected void readMemberFields(TupleInput input) {
        c1Nid = input.readInt();
        c2Nid = input.readInt();
        c3Nid = input.readInt();
    }

    @Override
    protected final CidCidCidRevision readMemberRevision(TupleInput input) {
        return new CidCidCidRevision(input, this);
    }

    @Override
    public boolean readyToWriteRefsetMember() {
        assert c1Nid != Integer.MAX_VALUE;
        assert c2Nid != Integer.MAX_VALUE;
        assert c3Nid != Integer.MAX_VALUE;

        return true;
    }

    /*
    *  (non-Javadoc)
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
        buf.append(" c3Nid: ");
        addNidToBuffer(buf, c3Nid);
        buf.append(super.toString());

        return buf.toString();
    }

    @Override
    protected void writeMember(TupleOutput output) {
        output.writeInt(c1Nid);
        output.writeInt(c2Nid);
        output.writeInt(c3Nid);
    }

    //~--- get methods ---------------------------------------------------------
    public int getC1Nid() {
        return c1Nid;
    }

    @Override
    public int getC1id() {
        return getC1Nid();
    }

    public int getC2Nid() {
        return c2Nid;
    }

    @Override
    public int getC2id() {
        return getC2Nid();
    }

    public int getC3Nid() {
        return c3Nid;
    }

    @Override
    public int getC3id() {
        return getC3Nid();
    }

    @Override
    public int getCnid1() {
        return c1Nid;
    }

    @Override
    public int getCnid2() {
        return c2Nid;
    }

    @Override
    public int getCnid3() {
        return c3Nid;
    }

    @Override
    public TkRefsetAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
            Map<UUID, UUID> conversionMap)
            throws ContradictionException, IOException {
        return new TkRefsetCidCidCidMember(this, exclusionSet, conversionMap, 0, true, vc);
    }

    protected TK_REFSET_TYPE getTkRefsetType() {
        return TK_REFSET_TYPE.CID_CID_CID;
    }

    @Override
    public int getTypeId() {
        return REFSET_TYPES.CID_CID_CID.getTypeNid();
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(5);

        variableNids.add(getC1id());
        variableNids.add(getC2id());
        variableNids.add(getC3id());

        return variableNids;
    }

    @Override
    protected VersionComputer<RefsetMember<CidCidCidRevision, CidCidCidMember>.Version> getVersionComputer() {
        return computer;
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
                list.add(new Version(this));
            }

            if (revisions != null) {
                for (CidCidCidRevision r : revisions) {
                    if (r.getTime() != Long.MIN_VALUE) {
                        list.add(new Version(r));
                    }
                }
            }

            versions = list;
        }

        return Collections.unmodifiableList((List<Version>) versions);
    }

    //~--- set methods ---------------------------------------------------------
    public void setC1Nid(int c1Nid) {
        this.c1Nid = c1Nid;
        modified();
    }

    @Override
    public void setC1id(int c1id) {
        setC1Nid(c1id);
    }

    public void setC2Nid(int c2Nid) {
        this.c2Nid = c2Nid;
        modified();
    }

    @Override
    public void setC2id(int c2id) {
        setC2Nid(c2id);
    }

    public void setC3Nid(int c3Nid) {
        this.c3Nid = c3Nid;
        modified();
    }

    @Override
    public void setC3id(int c3id) {
        setC3Nid(c3id);
    }

    @Override
    public void setCnid1(int cnid1) throws PropertyVetoException {
        this.c1Nid = cnid1;
        modified();
    }

    @Override
    public void setCnid2(int cnid2) throws PropertyVetoException {
        this.c2Nid = cnid2;
        modified();
    }

    @Override
    public void setCnid3(int cnid) throws PropertyVetoException {
        this.c3Nid = cnid;
        modified();
    }

    //~--- inner classes -------------------------------------------------------
    public class Version extends RefsetMember<CidCidCidRevision, CidCidCidMember>.Version
            implements I_ExtendByRefVersion<CidCidCidRevision>, I_ExtendByRefPartCidCidCid<CidCidCidRevision>,
            RefexCnidCnidCnidVersionBI<CidCidCidRevision> {

        private Version(RefexCnidCnidCnidAnalogBI cv) {
            super(cv);
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public int compareTo(RefexVersionBI o) {
            if (I_ExtendByRefPartCidCidCid.class.isAssignableFrom(o.getClass())) {
                I_ExtendByRefPartCidCidCid<CidCidCidRevision> another =
                        (I_ExtendByRefPartCidCidCid<CidCidCidRevision>) o;

                if (this.getC1id() != another.getC1id()) {
                    return this.getC1id() - another.getC1id();
                }

                if (this.getC2id() != another.getC2id()) {
                    return this.getC2id() - another.getC2id();
                }

                if (this.getC3id() != another.getC3id()) {
                    return this.getC2id() - another.getC2id();
                }
            }

            return super.compareTo(o);
        }

        @Override
        public int hashCodeOfParts() {
            return Hashcode.compute(new int[]{getC1Nid(), getC2Nid(), getC3Nid()});
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public int getC1id() {
            return getCv().getCnid1();
        }

        @Override
        public int getC2id() {
            return getCv().getCnid2();
        }

        @Override
        public int getC3id() {
            return getCv().getCnid3();
        }

        RefexCnidCnidCnidAnalogBI getCv() {
            return (RefexCnidCnidCnidAnalogBI) cv;
        }

        @Override
        public ERefsetCidCidCidMember getERefsetMember() throws IOException {
            return new ERefsetCidCidCidMember(this);
        }

        @Override
        public ERefsetCidCidCidRevision getERefsetRevision() throws IOException {
            return new ERefsetCidCidCidRevision(this);
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setC1id(int c1id) throws PropertyVetoException {
            getCv().setCnid1(c1id);
        }

        @Override
        public void setC2id(int c2id) throws PropertyVetoException {
            getCv().setCnid2(c2id);
        }

        @Override
        public void setC3id(int c3id) throws PropertyVetoException {
            getCv().setCnid3(c3id);
        }

        @Override
        public int getCnid3() {
            return getCv().getCnid3();
        }

        @Override
        public int getCnid2() {
            return getCv().getCnid2();
        }

        @Override
        public int getCnid1() {
            return getCv().getCnid1();
        }
    }
}
