package org.ihtsdo.concept.component.refsetmember.Boolean;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.util.HashFunction;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetBooleanRevision;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

public class BooleanMember extends RefsetMember<BooleanRevision, BooleanMember>
        implements I_ExtendByRefPartBoolean<BooleanRevision>, RefexBooleanAnalogBI<BooleanRevision> {

    private static VersionComputer<RefsetMember<BooleanRevision, BooleanMember>.Version> computer =
            new VersionComputer<RefsetMember<BooleanRevision, BooleanMember>.Version>();
    //~--- fields --------------------------------------------------------------
    private boolean booleanValue;

    //~--- constructors --------------------------------------------------------
    public BooleanMember() {
        super();
    }

    public BooleanMember(int enclosingConceptNid, TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    public BooleanMember(TkRefexBooleanMember refsetMember, int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        booleanValue = refsetMember.getBoolean1();

        if (refsetMember.getRevisionList() != null) {
            revisions = new RevisionSet(primordialSapNid);

            for (TkRefexBooleanRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new BooleanRevision(eVersion, this));
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addRefsetTypeNids(Set<Integer> allNids) {
        // ;
    }

    @Override
    protected void addSpecProperties(RefexCAB rcs) {
        rcs.with(RefexProperty.BOOLEAN1, getBoolean1());
    }

    @Override
    public I_ExtendByRefPart<BooleanRevision> duplicate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (BooleanMember.class.isAssignableFrom(obj.getClass())) {
            BooleanMember another = (BooleanMember) obj;

            return this.nid == another.nid;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[]{nid});
    }

    @Override
    public BooleanRevision makeAnalog() {
        BooleanRevision newR = new BooleanRevision(getStatusNid(), getTime(),
                getAuthorNid(), getModuleNid(), getPathNid(), this);

        return newR;
    }
    
    @Override
    public BooleanRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
         BooleanRevision newR = new BooleanRevision(statusNid, time,
                authorNid, moduleNid, pathNid, this);

        addRevision(newR);

        return newR;
    }

    @Override
    public I_ExtendByRefPart<BooleanRevision> makePromotionPart(PathBI promotionPath, int authorNid) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean refexFieldsEqual(ConceptComponent<BooleanRevision, BooleanMember> obj) {
        if (BooleanMember.class.isAssignableFrom(obj.getClass())) {
            BooleanMember another = (BooleanMember) obj;

            return this.booleanValue = another.booleanValue;
        }

        return false;
    }

    @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexBooleanVersionBI.class.isAssignableFrom(another.getClass())){
            RefexBooleanVersionBI bv = (RefexBooleanVersionBI) another;
            return this.booleanValue = bv.getBoolean1();
        }
        return false;
    }

    @Override
    protected void readMemberFields(TupleInput input) {
        booleanValue = input.readBoolean();
    }

    @Override
    protected final BooleanRevision readMemberRevision(TupleInput input) {
        return new BooleanRevision(input, this);
    }

    @Override
    public boolean readyToWriteRefsetMember() {
        return true;
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(this.getClass().getSimpleName()).append(":{");
        buf.append(" booleanValue:").append(this.booleanValue);
        buf.append("; ");
        buf.append(super.toString());

        return buf.toString();
    }

    @Override
    protected void writeMember(TupleOutput output) {
        output.writeBoolean(booleanValue);
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public boolean getBoolean1() {
        return this.booleanValue;
    }

    @Override
    public boolean getBooleanValue() {
        return booleanValue;
    }

    @Override
    public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
            Map<UUID, UUID> conversionMap)
            throws ContradictionException, IOException {
        return new TkRefexBooleanMember(this, exclusionSet, conversionMap, 0, true, vc);
    }

    @Override
    protected TK_REFEX_TYPE getTkRefsetType() {
        return TK_REFEX_TYPE.BOOLEAN;
    }

    @Override
    public int getTypeId() {
        return REFSET_TYPES.BOOLEAN.getTypeNid();
    }

    @Override
    public int getTypeNid() {
        return REFSET_TYPES.BOOLEAN.getTypeNid();
    }

    @Override
    protected ArrayIntList getVariableVersionNids() {

        // TODO Auto-generated method stub
        return null;
    }

    protected VersionComputer<RefsetMember<BooleanRevision, BooleanMember>.Version> getVersionComputer() {
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
                for (BooleanRevision br : revisions) {
                    if (br.getTime() != Long.MIN_VALUE) {
                        list.add(new Version(br));
                    }
                }
            }

            versions = list;
        }

        return Collections.unmodifiableList((List<Version>) versions);
    }

    //~--- set methods ---------------------------------------------------------
    @Override
    public void setBoolean1(boolean l) throws PropertyVetoException {
        this.booleanValue = l;
        modified();
    }

    @Override
    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
        modified();
    }

    //~--- inner classes -------------------------------------------------------
    public class Version extends RefsetMember<BooleanRevision, BooleanMember>.Version
            implements I_ExtendByRefVersion<BooleanRevision>, I_ExtendByRefPartBoolean<BooleanRevision>,
            RefexBooleanAnalogBI<BooleanRevision> {

        private Version(RefexBooleanAnalogBI<BooleanRevision> cv) {
            super(cv);
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public int compareTo(RefexVersionBI o) {
            if (I_ExtendByRefPartBoolean.class.isAssignableFrom(o.getClass())) {
                I_ExtendByRefPartBoolean another = (I_ExtendByRefPartBoolean) o;

                if (this.getBooleanValue() == another.getBooleanValue()) {
                    return super.compareTo(another);
                }

                if (this.getBooleanValue()) {
                    return 1;
                }

                return -1;
            }

            return super.compareTo(o);
        }

        @Override
        public I_ExtendByRefPartBoolean<BooleanRevision> duplicate() {
            return (I_ExtendByRefPartBoolean<BooleanRevision>) super.duplicate();
        }

        @Override
        public int hashCodeOfParts() {
            if (getBoolean1()) {
                return Integer.MAX_VALUE;
            } else {
                return Integer.MIN_VALUE;
            }
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public boolean getBoolean1() {
            return getCv().getBoolean1();
        }

        @Override
        @Deprecated
        public boolean getBooleanValue() {
            return getCv().getBoolean1();
        }

        RefexBooleanAnalogBI<BooleanRevision> getCv() {
            return (RefexBooleanAnalogBI<BooleanRevision>) cv;
        }

        @Override
        public TkRefexBooleanMember getERefsetMember() throws IOException {
            return new TkRefexBooleanMember(this, RevisionHandling.EXCLUDE_REVISIONS);
        }

        @Override
        public ERefsetBooleanRevision getERefsetRevision() throws IOException {
            return new ERefsetBooleanRevision(this);
        }

        @Override
        public ArrayIntList getVariableVersionNids() {
            return new ArrayIntList();
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setBoolean1(boolean value) throws PropertyVetoException {
            getCv().setBoolean1(value);
        }

        @Override
        public void setBooleanValue(boolean value) throws PropertyVetoException {
            getCv().setBoolean1(value);
        }
    }
}
