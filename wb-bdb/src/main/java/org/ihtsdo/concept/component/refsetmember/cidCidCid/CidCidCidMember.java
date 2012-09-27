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
import org.ihtsdo.etypes.ERefsetCidCidCidRevision;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_nid_nid_nid.RefexNidNidNidAnalogBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_uuid.TkRefexUuidUuidUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_uuid.TkRefexUuidUuidUuidRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.RevisionHandling;

public class CidCidCidMember extends RefsetMember<CidCidCidRevision, CidCidCidMember>
        implements I_ExtendByRefPartCidCidCid<CidCidCidRevision>,
        RefexNidNidNidVersionBI<CidCidCidRevision>,
        RefexNidNidNidAnalogBI<CidCidCidRevision> {

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

    public CidCidCidMember(TkRefexUuidUuidUuidMember refsetMember, int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        c1Nid = Bdb.uuidToNid(refsetMember.getUuid1());
        c2Nid = Bdb.uuidToNid(refsetMember.getUuid2());
        c3Nid = Bdb.uuidToNid(refsetMember.getUuid3());

        if (refsetMember.getRevisionList() != null) {
            revisions = new RevisionSet<CidCidCidRevision, CidCidCidMember>(primordialSapNid);

            for (TkRefexUuidUuidUuidRevision eVersion : refsetMember.getRevisionList()) {
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
        rcs.with(RefexProperty.CNID1, getNid1());
        rcs.with(RefexProperty.CNID2, getNid2());
        rcs.with(RefexProperty.CNID3, getNid3());
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
        return new CidCidCidRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
    }

    @Override
    public CidCidCidRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
        CidCidCidRevision newR = new CidCidCidRevision(statusNid, time, authorNid, moduleNid, pathNid,this);

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
        if(RefexNidNidNidVersionBI.class.isAssignableFrom(another.getClass())){
            RefexNidNidNidVersionBI cv = (RefexNidNidNidVersionBI) another;
            return (this.c1Nid == cv.getNid1()) && (this.c2Nid == cv.getNid2())
                    && (this.c3Nid == cv.getNid3());
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
    public int getNid1() {
        return c1Nid;
    }

    @Override
    public int getNid2() {
        return c2Nid;
    }

    @Override
    public int getNid3() {
        return c3Nid;
    }

    @Override
    public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
            Map<UUID, UUID> conversionMap)
            throws ContradictionException, IOException {
        return new TkRefexUuidUuidUuidMember(this, exclusionSet, conversionMap, 0, true, vc);
    }

    protected TK_REFEX_TYPE getTkRefsetType() {
        return TK_REFEX_TYPE.CID_CID_CID;
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
    public void setNid1(int cnid1) throws PropertyVetoException {
        this.c1Nid = cnid1;
        modified();
    }

    @Override
    public void setNid2(int cnid2) throws PropertyVetoException {
        this.c2Nid = cnid2;
        modified();
    }

    @Override
    public void setNid3(int cnid) throws PropertyVetoException {
        this.c3Nid = cnid;
        modified();
    }

    //~--- inner classes -------------------------------------------------------
    public class Version extends RefsetMember<CidCidCidRevision, CidCidCidMember>.Version
            implements I_ExtendByRefVersion<CidCidCidRevision>, I_ExtendByRefPartCidCidCid<CidCidCidRevision>,
            RefexNidNidNidVersionBI<CidCidCidRevision> {

        private Version(RefexNidNidNidAnalogBI cv) {
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
            return getCv().getNid1();
        }

        @Override
        public int getC2id() {
            return getCv().getNid2();
        }

        @Override
        public int getC3id() {
            return getCv().getNid3();
        }

        RefexNidNidNidAnalogBI getCv() {
            return (RefexNidNidNidAnalogBI) cv;
        }

        @Override
        public TkRefexUuidUuidUuidMember getERefsetMember() throws IOException {
            return new TkRefexUuidUuidUuidMember(this, RevisionHandling.EXCLUDE_REVISIONS);
        }

        @Override
        public ERefsetCidCidCidRevision getERefsetRevision() throws IOException {
            return new ERefsetCidCidCidRevision(this);
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setC1id(int c1id) throws PropertyVetoException {
            getCv().setNid1(c1id);
        }

        @Override
        public void setC2id(int c2id) throws PropertyVetoException {
            getCv().setNid2(c2id);
        }

        @Override
        public void setC3id(int c3id) throws PropertyVetoException {
            getCv().setNid3(c3id);
        }

        @Override
        public int getNid3() {
            return getCv().getNid3();
        }

        @Override
        public int getNid2() {
            return getCv().getNid2();
        }

        @Override
        public int getNid1() {
            return getCv().getNid1();
        }
    }
}
