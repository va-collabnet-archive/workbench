package org.ihtsdo.concept.component.refsetmember.cidInt;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refsetmember.cidFloat.CidFloatMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetCidIntMember;
import org.ihtsdo.etypes.ERefsetCidIntRevision;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec.RefexProperty;
import org.ihtsdo.tk.api.refex.type_cnid_int.RefexCnidIntAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.cidint.TkRefsetCidIntMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidint.TkRefsetCidIntRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidIntMember extends RefsetMember<CidIntRevision, CidIntMember> 
	implements I_ExtendByRefPartCidInt<CidIntRevision>, RefexCnidIntAnalogBI<CidIntRevision> {

    private static VersionComputer<RefsetMember<CidIntRevision, CidIntMember>.Version> computer =
            new VersionComputer<RefsetMember<CidIntRevision, CidIntMember>.Version>();

    protected VersionComputer<RefsetMember<CidIntRevision, CidIntMember>.Version> getVersionComputer() {
        return computer;
    }

    public class Version
            extends RefsetMember<CidIntRevision, CidIntMember>.Version
            implements I_ExtendByRefVersion<CidIntRevision>, 
            		   I_ExtendByRefPartCidInt<CidIntRevision>,
            		   RefexCnidIntAnalogBI<CidIntRevision> {

        private Version() {
            super();
        }

        private Version(int index) {
            super(index);
        }

        public int compareTo(I_ExtendByRefPart<CidIntRevision> o) {
            if (I_ExtendByRefPartCidInt.class.isAssignableFrom(o.getClass())) {
                I_ExtendByRefPartCidInt<CidIntRevision> another = 
                	(I_ExtendByRefPartCidInt<CidIntRevision>) o;
                if (this.getC1id() != another.getC1id()) {
                    return this.getC1id() - another.getC1id();
                }
                if (this.getIntValue() != another.getIntValue()) {
                    return this.getIntValue() - another.getIntValue();
                }
            }
            return super.compareTo(o);
        }

        @Override
        public int getC1id() {
            if (index >= 0) {
                return revisions.get(index).getC1id();
            }
            return CidIntMember.this.getC1Nid();
        }

        @Override
        public void setC1id(int c1id) {
            if (index >= 0) {
                revisions.get(index).setC1id(c1id);
            }
            CidIntMember.this.setC1Nid(c1id);
        }

        @Override
        public I_ExtendByRefPartCidInt<CidIntRevision> duplicate() {
            return (I_ExtendByRefPartCidInt<CidIntRevision>) super.duplicate();
        }

        @Override
        public int getIntValue() {
            if (index >= 0) {
                return revisions.get(index).getIntValue();
            }
            return CidIntMember.this.getIntValue();
        }

        @Override
        public void setIntValue(int intValue) {
            if (index >= 0) {
                revisions.get(index).setIntValue(intValue);
            }
            CidIntMember.this.setIntValue(intValue);
        }

        @Override
		public void setInt1(int l) throws PropertyVetoException {
            if (index >= 0) {
                revisions.get(index).setInt1(l);
            }
            CidIntMember.this.setInt1(l);
		}

		@Override
		public int getInt1() {
            if (index >= 0) {
                return revisions.get(index).getInt1();
            }
            return CidIntMember.this.getInt1();
		}

		@Override
		public void setCnid1(int cnid1) throws PropertyVetoException {
            if (index >= 0) {
                revisions.get(index).setCnid1(cnid1);
            }
            CidIntMember.this.setCnid1(cnid1);
		}

		@Override
		public int getCnid1() {
            if (index >= 0) {
                return revisions.get(index).getCnid1();
            }
            return CidIntMember.this.getCnid1();
		}

		@Override
        public ERefsetCidIntMember getERefsetMember() throws TerminologyException, IOException {
            return new ERefsetCidIntMember(this);
        }

        @Override
        public ERefsetCidIntRevision getERefsetRevision() throws TerminologyException, IOException {
            return new ERefsetCidIntRevision(this);
        }
    }
    private int c1Nid;
    private int intValue;

    public CidIntMember(int enclosingConceptNid,
            TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    public CidIntMember(TkRefsetCidIntMember refsetMember,
            int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
        intValue = refsetMember.getIntValue();
        if (refsetMember.getRevisionList() != null) {
            revisions = new CopyOnWriteArrayList<CidIntRevision>();
            for (TkRefsetCidIntRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new CidIntRevision(eVersion, this));
            }
        }
    }

    public CidIntMember() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (CidIntMember.class.isAssignableFrom(obj.getClass())) {
            CidIntMember another = (CidIntMember) obj;
            if (super.equals(another)) {
                return this.c1Nid == another.c1Nid && this.intValue == another.intValue;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[]{c1Nid});
    }

    @Override
    protected boolean membersEqual(
            ConceptComponent<CidIntRevision, CidIntMember> obj) {
        if (CidFloatMember.class.isAssignableFrom(obj.getClass())) {
            CidIntMember another = (CidIntMember) obj;
            return this.c1Nid == another.c1Nid && this.intValue == another.intValue;
        }
        return false;
    }

    @Override
    protected final CidIntRevision readMemberRevision(TupleInput input) {
        return new CidIntRevision(input, this);
    }

    @Override
    protected void readMemberFields(TupleInput input) {
        c1Nid = input.readInt();
        intValue = input.readInt();
    }

    @Override
    protected void writeMember(TupleOutput output) {
        output.writeInt(c1Nid);
        output.writeInt(intValue);
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(3);
        variableNids.add(getC1Nid());
        return variableNids;
    }

    @Override
    public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        CidIntRevision newR = new CidIntRevision(statusNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public CidIntRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (getTime() == time && getPathNid() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
        CidIntRevision newR = new CidIntRevision(statusNid, authorNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public CidIntRevision makeAnalog() {
        CidIntRevision newR = new CidIntRevision(getStatusNid(), getPathNid(), getTime(), this);
        return newR;
    }

    public int getC1Nid() {
        return c1Nid;
    }

    public void setC1Nid(int c1Nid) {
        this.c1Nid = c1Nid;
        modified();
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
        modified();
    }

    @Override
	public void setInt1(int l) throws PropertyVetoException {
        this.intValue = l;
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

	@Override
	public int getInt1() {
		return intValue;
	}

	@Override
    public int getTypeId() {
        return REFSET_TYPES.CID_INT.getTypeNid();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append("c1Nid: ");
        ConceptComponent.addNidToBuffer(buf, c1Nid);
        buf.append(" intValue: " + this.intValue);
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public CidIntRevision duplicate() {
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
		return TK_REFSET_TYPE.CID_INT;
	}

	protected void addSpecProperties(RefexAmendmentSpec rcs) {
		rcs.with(RefexProperty.CNID1, getCnid1());
		rcs.with(RefexProperty.INTEGER1, getInt1());
	}

}
