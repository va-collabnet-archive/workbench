package org.ihtsdo.concept.component.refsetmember.Boolean;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetBooleanMember;
import org.ihtsdo.etypes.ERefsetBooleanRevision;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec.RefexProperty;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.Boolean.TkRefsetBooleanMember;
import org.ihtsdo.tk.dto.concept.component.refset.Boolean.TkRefsetBooleanRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class BooleanMember extends RefsetMember<BooleanRevision, BooleanMember> 
	implements I_ExtendByRefPartBoolean<BooleanRevision>, RefexBooleanAnalogBI<BooleanRevision> {

    private static VersionComputer<RefsetMember<BooleanRevision, BooleanMember>.Version> computer =
            new VersionComputer<RefsetMember<BooleanRevision, BooleanMember>.Version>();

    protected VersionComputer<RefsetMember<BooleanRevision, BooleanMember>.Version> getVersionComputer() {
        return computer;
    }

    public class Version
            extends RefsetMember<BooleanRevision, BooleanMember>.Version
            implements I_ExtendByRefVersion<BooleanRevision>, 
            		I_ExtendByRefPartBoolean<BooleanRevision>,
            		RefexBooleanAnalogBI<BooleanRevision>
            	 {

        private Version() {
            super();
        }

        private Version(int index) {
            super(index);
        }

        @Override
        public ArrayIntList getVariableVersionNids() {
            return new ArrayIntList();
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
        public int compareTo(I_ExtendByRefPart o) {
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
        public boolean getBooleanValue() {
            if (index >= 0) {
                return revisions.get(index).getBooleanValue();
            }
            return BooleanMember.this.getBooleanValue();
        }

        @Override
        public void setBooleanValue(boolean value) {
            if (index >= 0) {
                revisions.get(index).setBooleanValue(value);
            }
            BooleanMember.this.setBooleanValue(value);
        }
        @Override
        public boolean getBoolean1() {
            if (index >= 0) {
                return revisions.get(index).getBoolean1();
            }
            return BooleanMember.this.getBoolean1();
        }

        @Override
        public void setBoolean1(boolean value) {
            if (index >= 0) {
                revisions.get(index).setBooleanValue(value);
            }
            BooleanMember.this.setBooleanValue(value);
        }

        @Override
        public ERefsetBooleanMember getERefsetMember() throws TerminologyException, IOException {
            return new ERefsetBooleanMember(this);
        }

        @Override
        public ERefsetBooleanRevision getERefsetRevision() throws TerminologyException, IOException {
            return new ERefsetBooleanRevision(this);
        }
    }
    private boolean booleanValue;

    public BooleanMember(int enclosingConceptNid, TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    public BooleanMember(TkRefsetBooleanMember refsetMember,
            int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        booleanValue = refsetMember.getBooleanValue();
        if (refsetMember.getRevisionList() != null) {
            revisions = new CopyOnWriteArrayList<BooleanRevision>();
            for (TkRefsetBooleanRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new BooleanRevision(eVersion, this));
            }
        }
    }

    public BooleanMember() {
        super();
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
    public I_ExtendByRefPart<BooleanRevision> makePromotionPart(PathBI promotionPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public I_ExtendByRefPart<BooleanRevision> duplicate() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean membersEqual(
            ConceptComponent<BooleanRevision, BooleanMember> obj) {
        if (BooleanMember.class.isAssignableFrom(obj.getClass())) {
            BooleanMember another = (BooleanMember) obj;
            return this.booleanValue = another.booleanValue;
        }
        return false;
    }

    @Override
    protected final BooleanRevision readMemberRevision(TupleInput input) {
        return new BooleanRevision(input, this);
    }

    @Override
    protected void readMemberFields(TupleInput input) {
        booleanValue = input.readBoolean();
    }

    @Override
    protected void writeMember(TupleOutput output) {
        output.writeBoolean(booleanValue);
    }

    @Override
    protected ArrayIntList getVariableVersionNids() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BooleanRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (getTime() == time && getPathNid() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
        BooleanRevision newR = new BooleanRevision(statusNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public BooleanRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (getTime() == time && getPathNid() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
        BooleanRevision newR = new BooleanRevision(statusNid, authorNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public BooleanRevision makeAnalog() {
        BooleanRevision newR = new BooleanRevision(getStatusNid(), getPathNid(), getTime(), this);
        return newR;
    }

    @Override
    public boolean getBooleanValue() {
        return booleanValue;
    }

    @Override
    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
        modified();
    }

    @Override
    public int getTypeId() {
        return REFSET_TYPES.BOOLEAN.getTypeNid();
    }

    @Override
    public int getTypeNid() {
        return REFSET_TYPES.BOOLEAN.getTypeNid();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getClass().getSimpleName()).append(":{");
        buf.append(" booleanValue:").append(this.booleanValue);
        return buf.toString();
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
	public boolean getBoolean1() {
		return this.booleanValue;
	}

	@Override
	public void setBoolean1(boolean l) throws PropertyVetoException {
		this.booleanValue = l;
		modified();
	}

	protected TK_REFSET_TYPE getTkRefsetType() {
		return TK_REFSET_TYPE.BOOLEAN;
	}

	protected void addSpecProperties(RefexAmendmentSpec rcs) {
		rcs.with(RefexProperty.BOOLEAN1, getBoolean1());
	}

}
