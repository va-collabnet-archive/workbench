package org.ihtsdo.concept.component.refsetmember.cidCid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.ERefsetCidCidMember;
import org.ihtsdo.etypes.ERefsetCidCidRevision;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidCidMember extends RefsetMember<CidCidRevision, CidCidMember> 
	implements I_ExtendByRefPartCidCid {

	private static VersionComputer<RefsetMember<CidCidRevision, CidCidMember>.Version> computer = 
		new VersionComputer<RefsetMember<CidCidRevision, CidCidMember>.Version>();

	protected VersionComputer<RefsetMember<CidCidRevision, CidCidMember>.Version> getVersionComputer() {
		return computer;
	}

	public class Version 
	extends RefsetMember<CidCidRevision, CidCidMember>.Version 
	implements I_ExtendByRefVersion, I_ExtendByRefPartCidCid {

		private Version() {
			super();
		}

		private Version(int index) {
			super(index);
		}
		public int compareTo(I_ExtendByRefPart o) {
			if (I_ExtendByRefPartCidCid.class.isAssignableFrom(o.getClass())) {
				I_ExtendByRefPartCidCid another = (I_ExtendByRefPartCidCid) o;
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

	public CidCidMember(Concept enclosingConcept, 
			TupleInput input) throws IOException {
		super(enclosingConcept, 
				input);
	}

	public CidCidMember(ERefsetCidCidMember refsetMember, Concept enclosingConcept) throws IOException {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		c2Nid = Bdb.uuidToNid(refsetMember.getC2Uuid());
		if (refsetMember.getRevisionList() != null) {
			revisions = new ArrayList<CidCidRevision>(refsetMember.getRevisionList().size());
			for (ERefsetCidCidRevision eVersion: refsetMember.getRevisionList()) {
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
        if (obj == null)
            return false;
        if (CidCidMember.class.isAssignableFrom(obj.getClass())) {
            CidCidMember another = (CidCidMember) obj;
            return this.c1Nid == another.c1Nid && this.c2Nid == another.c2Nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { c1Nid, c2Nid });
    }

	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (revisions == null) {
			revisions = new ArrayList<CidCidRevision>(
					additionalVersionCount);
		} else {
			revisions.ensureCapacity(revisions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			CidCidRevision r = new CidCidRevision(input, this);
			if (r.getTime() != Long.MIN_VALUE) {
				revisions.add(r);
			}
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		c1Nid = input.readInt();
		c2Nid = input.readInt();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
		output.writeInt(c2Nid);
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		CidCidRevision newR = new CidCidRevision(statusNid, pathNid, time, this);
		addRevision(newR);
		return newR;
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
	}

	public int getC2Nid() {
		return c2Nid;
	}

	public void setC2Nid(int c2Nid) {
		this.c2Nid = c2Nid;
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
