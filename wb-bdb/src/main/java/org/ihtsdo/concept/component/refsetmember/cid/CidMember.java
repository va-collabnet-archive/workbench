package org.ihtsdo.concept.component.refsetmember.cid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.ERefsetCidMember;
import org.ihtsdo.etypes.ERefsetCidRevision;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidMember extends RefsetMember<CidRevision, CidMember> implements I_ExtendByRefPartCid {
	
	private static VersionComputer<RefsetMember<CidRevision, CidMember>.Version> computer = 
		new VersionComputer<RefsetMember<CidRevision, CidMember>.Version>();

	protected VersionComputer<RefsetMember<CidRevision, CidMember>.Version> getVersionComputer() {
		return computer;
	}

	public class Version 
	extends RefsetMember<CidRevision, CidMember>.Version 
	implements I_ExtendByRefVersion, I_ExtendByRefPartCid {

		private Version() {
			super();
		}

		private Version(int index) {
			super(index);
		}
		@Override
		public int compareTo(I_ExtendByRefPart o) {
			if (I_ExtendByRefPartCid.class.isAssignableFrom(o.getClass())) {
				I_ExtendByRefPartCid another = (I_ExtendByRefPartCid) o;
				if (this.getC1id() != another.getC1id()) {
					return this.getC1id() - another.getC1id();
				}
			}
			return super.compareTo(o);
		}

		@Override
		public ArrayIntList getVariableVersionNids() {
		    ArrayIntList variableNids = new ArrayIntList(3);
		    variableNids.add(getC1id());
		    return variableNids;
		}

	      
		@Override
		public int getC1id() {
			if (index >= 0) {
				return revisions.get(index).getC1id();
			}
			return CidMember.this.getC1Nid();
		}

		@Override
		public void setC1id(int c1id) {
			if (index >= 0) {
				revisions.get(index).setC1id(c1id);
			}
			CidMember.this.setC1Nid(c1id);
		}
		
		@Override
		public ERefsetCidMember getERefsetMember() throws TerminologyException, IOException {
			return new ERefsetCidMember(this, CidMember.this);
		}

		@Override
		public ERefsetCidRevision getERefsetRevision() throws TerminologyException, IOException {
			return new ERefsetCidRevision(this);
		}
	}

	private int c1Nid;

	public CidMember(Concept enclosingConcept, 
			TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}


	public CidMember(TkRefsetCidMember refsetMember, Concept enclosingConcept) throws IOException {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		if (refsetMember.getRevisionList() != null) {
			revisions = new CopyOnWriteArrayList<CidRevision>();
			for (TkRefsetCidRevision eVersion: refsetMember.getRevisionList()) {
				revisions.add(new CidRevision(eVersion, this));
			}
		}
	}

	public CidMember() {
		super();
	}


	@Override
	protected boolean membersEqual(
			ConceptComponent<CidRevision, CidMember> obj) {
		if (CidMember.class.isAssignableFrom(obj.getClass())) {
			CidMember another = (CidMember) obj;
			return this.c1Nid == another.c1Nid;
		}
		return false;
	}


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidMember.class.isAssignableFrom(obj.getClass())) {
            CidMember another = (CidMember) obj;
            return this.c1Nid == another.c1Nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { c1Nid });
    }
    
    
    
    @Override
	protected final CidRevision readMemberRevision(TupleInput input) {
		return new CidRevision(input, this);
	}
	@Override
	protected void readMemberFields(TupleInput input) {
		c1Nid = input.readInt();

	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        if (getTime() == time && getPathId() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
		CidRevision newR = new CidRevision(statusNid, pathNid, time, this);
		addRevision(newR);
		return newR;
	}

	@Override
	public CidRevision makeAnalog() {
	    CidRevision newR = new CidRevision(getStatusId(), getPathId(), getTime(), this);
	    return newR;
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
        modified();
	}
	
	@Override
	public int getTypeId() {
		return REFSET_TYPES.CID.getTypeNid();
	}


	@Override
	@Deprecated
	public int getC1id() {
		return getC1Nid();
	}



	@Override
	@Deprecated
	public void setC1id(int c1id) {
		setC1Nid(c1id);
	}

	@Override
	public I_ExtendByRefPart makePromotionPart(I_Path promotionPath) {
		throw new UnsupportedOperationException();
	}
	
	public I_ExtendByRefPart duplicate() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append("c1Nid: ");
		addNidToBuffer(buf, c1Nid);
        buf.append(super.toString());
        return buf.toString();
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
