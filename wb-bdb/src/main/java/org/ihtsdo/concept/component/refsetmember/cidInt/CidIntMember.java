package org.ihtsdo.concept.component.refsetmember.cidInt;

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
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refsetmember.cidFloat.CidFloatMember;
import org.ihtsdo.concept.component.refsetmember.str.StrRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.ERefsetCidIntMember;
import org.ihtsdo.etypes.ERefsetCidIntRevision;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidIntMember extends RefsetMember<CidIntRevision, CidIntMember> implements I_ExtendByRefPartCidInt {

	private static VersionComputer<RefsetMember<CidIntRevision, CidIntMember>.Version> computer = 
		new VersionComputer<RefsetMember<CidIntRevision, CidIntMember>.Version>();

	protected VersionComputer<RefsetMember<CidIntRevision, CidIntMember>.Version> getVersionComputer() {
		return computer;
	}

	public class Version 
	extends RefsetMember<CidIntRevision, CidIntMember>.Version 
	implements I_ExtendByRefVersion, I_ExtendByRefPartCidInt {

		private Version() {
			super();
		}

		private Version(int index) {
			super(index);
		}

		public int compareTo(I_ExtendByRefPart o) {
			if (I_ExtendByRefPartCidInt.class.isAssignableFrom(o.getClass())) {
				I_ExtendByRefPartCidInt another = (I_ExtendByRefPartCidInt) o;
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
		public I_ExtendByRefPartCidInt duplicate() {
			return (I_ExtendByRefPartCidInt) super.duplicate();
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

	public CidIntMember(Concept enclosingConcept, TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}

	public CidIntMember(ERefsetCidIntMember refsetMember, Concept enclosingConcept) throws IOException {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		intValue = refsetMember.getIntValue();
		if (refsetMember.getRevisionList() != null) {
			revisions = new CopyOnWriteArrayList<CidIntRevision>();
			for (ERefsetCidIntRevision eVersion: refsetMember.getRevisionList()) {
				revisions.add(new CidIntRevision(eVersion, this));
			}
		}
	}

    public CidIntMember() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidIntMember.class.isAssignableFrom(obj.getClass())) {
            CidIntMember another = (CidIntMember) obj;
            return this.c1Nid == another.c1Nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { c1Nid });
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
        if (this.getTime() == time && this.getPathId() == pathNid) {
            this.setStatusId(statusNid);
            return this;
        }
		CidIntRevision newR = new CidIntRevision(statusNid, pathNid, time, this);
		addRevision(newR);
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
	public StrRevision duplicate() {
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
	protected List<Version> getVersions() {
		if (versions == null) {
			int count = 1;
			if (revisions != null) {
				count = count + revisions.size();
			}
			ArrayList<Version> list = new ArrayList<Version>(count);
			list.add(new Version());
			if (revisions != null) {
				for (int i = 0; i < revisions.size(); i++) {
					list.add(new Version(i));
				}
			}
			versions = list;
		}
		return (List<Version>) versions;
	}

}
