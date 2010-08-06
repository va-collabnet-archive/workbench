package org.ihtsdo.concept.component.refsetmember.cidCidCid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.ERefsetCidCidCidMember;
import org.ihtsdo.etypes.ERefsetCidCidCidRevision;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.dto.concept.component.refset.cidcidcid.TkRefsetCidCidCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcidcid.TkRefsetCidCidCidRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidCidCidMember extends RefsetMember<CidCidCidRevision, CidCidCidMember>
	implements I_ExtendByRefPartCidCidCid {

	private static VersionComputer<RefsetMember<CidCidCidRevision, CidCidCidMember>.Version> computer = 
		new VersionComputer<RefsetMember<CidCidCidRevision, CidCidCidMember>.Version>();

	protected VersionComputer<RefsetMember<CidCidCidRevision, CidCidCidMember>.Version> getVersionComputer() {
		return computer;
	}

	
	
	public class Version 
	extends RefsetMember<CidCidCidRevision, CidCidCidMember>.Version 
	implements I_ExtendByRefVersion, I_ExtendByRefPartCidCidCid {

		private Version() {
			super();
		}

		private Version(int index) {
			super(index);
		}
		public int compareTo(I_ExtendByRefPart o) {
			if (I_ExtendByRefPartCidCidCid.class.isAssignableFrom(o.getClass())) {
				I_ExtendByRefPartCidCidCid another = (I_ExtendByRefPartCidCidCid) o;
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
		public int getC1id() {
			if (index >= 0) {
				return revisions.get(index).getC1id();
			}
			return CidCidCidMember.this.getC1Nid();
		}

		@Override
		public void setC1id(int c1id) {
			if (index >= 0) {
				revisions.get(index).setC1id(c1id);
			}
			CidCidCidMember.this.setC1Nid(c1id);
		}

		@Override
		public int getC2id() {
			if (index >= 0) {
				return revisions.get(index).getC2id();
			}
			return CidCidCidMember.this.getC2Nid();
		}

		@Override
		public void setC2id(int c2id) {
			if (index >= 0) {
				revisions.get(index).setC2id(c2id);
			}
			CidCidCidMember.this.setC2Nid(c2id);
		}

		@Override
		public int getC3id() {
			if (index >= 0) {
				return revisions.get(index).getC3id();
			}
			return CidCidCidMember.this.getC3Nid();
		}

		@Override
		public void setC3id(int c3id) {
			if (index >= 0) {
				revisions.get(index).setC3id(c3id);
			}
			CidCidCidMember.this.setC3Nid(c3id);
		}
		
		@Override
		public ERefsetCidCidCidMember getERefsetMember() throws TerminologyException, IOException {
			return new ERefsetCidCidCidMember(this);
		}

		@Override
		public ERefsetCidCidCidRevision getERefsetRevision() throws TerminologyException, IOException {
			return new ERefsetCidCidCidRevision(this);
		}

	
	}

	private int c1Nid;
	private int c2Nid;
	private int c3Nid;

	public CidCidCidMember(Concept enclosingConcept, TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}

	public CidCidCidMember(TkRefsetCidCidCidMember refsetMember, 
			Concept enclosingConcept) throws IOException {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		c2Nid = Bdb.uuidToNid(refsetMember.getC2Uuid());
		c3Nid = Bdb.uuidToNid(refsetMember.getC3Uuid());
		if (refsetMember.getRevisionList() != null) {
			revisions = new CopyOnWriteArrayList<CidCidCidRevision>();
			for (TkRefsetCidCidCidRevision eVersion: refsetMember.getRevisionList()) {
				revisions.add(new CidCidCidRevision(eVersion, this));
			}
		}
	}

    
    public CidCidCidMember() {
		super();
	}

	@Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidCidCidMember.class.isAssignableFrom(obj.getClass())) {
            CidCidCidMember another = (CidCidCidMember) obj;
            return this.c1Nid == another.c1Nid 
                && this.c2Nid == another.c2Nid
                && this.c3Nid == another.c3Nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { c1Nid, c2Nid, c3Nid });
    }	
    
    @Override
	protected boolean membersEqual(
			ConceptComponent<CidCidCidRevision, CidCidCidMember> obj) {
		if (CidCidCidMember.class.isAssignableFrom(obj.getClass())) {
			CidCidCidMember another = (CidCidCidMember) obj;
			return this.c1Nid == another.c1Nid && this.c2Nid == another.c2Nid && this.c3Nid == another.c3Nid;
		}
		return false;
	}

	@Override
	protected final CidCidCidRevision readMemberRevision(TupleInput input) {
		return new CidCidCidRevision(input, this);
	}
	@Override
	protected void readMemberFields(TupleInput input) {
		c1Nid = input.readInt();
		c2Nid = input.readInt();
		c3Nid = input.readInt();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
		output.writeInt(c2Nid);
		output.writeInt(c3Nid);
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
	public CidCidCidRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (getTime() == time && getPathId() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
		CidCidCidRevision newR = new CidCidCidRevision(statusNid, pathNid, time, this);
		addRevision(newR);
		return newR;
	}

    @Override
    public CidCidCidRevision makeAnalog() {
        return new CidCidCidRevision(getStatusId(), getPathId(), getTime(), this);
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

	public int getC3Nid() {
		return c3Nid;
	}

	public void setC3Nid(int c3Nid) {
		this.c3Nid = c3Nid;
        modified();
	}
	@Override
	public int getTypeId() {
		return REFSET_TYPES.CID_CID_CID.getTypeNid();
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
        buf.append(" c3Nid: ");
		addNidToBuffer(buf, c3Nid);
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
	public CidCidCidMember duplicate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getC3id() {
		return getC3Nid();
	}

	@Override
	public void setC3id(int c3id) {
		setC3Nid(c3id);
	}
	@SuppressWarnings("unchecked")
	protected List<Version> getVersions() {
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

}
