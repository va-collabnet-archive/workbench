package org.ihtsdo.concept.component.refsetmember.cidCidStr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.ERefsetCidCidStrMember;
import org.ihtsdo.etypes.ERefsetCidCidStrRevision;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidCidStrMember extends RefsetMember<CidCidStrRevision, CidCidStrMember>
	implements I_ExtendByRefPartCidCidString {
	
	private static VersionComputer<RefsetMember<CidCidStrRevision, CidCidStrMember>.Version> computer = 
		new VersionComputer<RefsetMember<CidCidStrRevision, CidCidStrMember>.Version>();

	protected VersionComputer<RefsetMember<CidCidStrRevision, CidCidStrMember>.Version> getVersionComputer() {
		return computer;
	}

	public class Version 
	extends RefsetMember<CidCidStrRevision, CidCidStrMember>.Version 
	implements I_ExtendByRefVersion, I_ExtendByRefPartCidCidString {

		private Version() {
			super();
		}

		private Version(int index) {
			super(index);
		}

		public int compareTo(I_ExtendByRefPart o) {
			if (I_ExtendByRefPartCidCidString.class.isAssignableFrom(o.getClass())) {
				I_ExtendByRefPartCidCidString another = (I_ExtendByRefPartCidCidString) o;
				if (this.getC1id() != another.getC1id()) {
					return this.getC1id() - another.getC1id();
				}
				if (this.getC2id() != another.getC2id()) {
					return this.getC2id() - another.getC2id();
				}
				if (this.getStringValue().equals(another.getStringValue())) {
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
			return CidCidStrMember.this.getC1Nid();
		}

		@Override
		public void setC1id(int c1id) {
			if (index >= 0) {
				revisions.get(index).setC1id(c1id);
			}
			CidCidStrMember.this.setC1Nid(c1id);
		}

		@Override
		public int getC2id() {
			if (index >= 0) {
				return revisions.get(index).getC2id();
			}
			return CidCidStrMember.this.getC2Nid();
		}

		@Override
		public void setC2id(int c2id) {
			if (index >= 0) {
				revisions.get(index).setC2id(c2id);
			}
			CidCidStrMember.this.setC2Nid(c2id);
		}

		@Override
		public String getStringValue() {
			if (index >= 0) {
				return revisions.get(index).getStringValue();
			}
			return CidCidStrMember.this.getStringValue();
		}

		@Override
		public void setStringValue(String value) {
			if (index >= 0) {
				revisions.get(index).setStringValue(value);
			}
			CidCidStrMember.this.setStringValue(value);
		}

		@Override
		public I_ExtendByRefPartCidCidString duplicate() {
			return (I_ExtendByRefPartCidCidString) super.duplicate();
		}
		@Override
		public ERefsetCidCidStrMember getERefsetMember() throws TerminologyException, IOException {
			return new ERefsetCidCidStrMember(this);
		}

		@Override
		public ERefsetCidCidStrRevision getERefsetRevision() throws TerminologyException, IOException {
			return new ERefsetCidCidStrRevision(this);
		}
	}

	private int c1Nid;
	private int c2Nid;
	private String strValue;

	public CidCidStrMember(Concept enclosingConcept, TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}

	public CidCidStrMember(ERefsetCidCidStrMember refsetMember, Concept enclosingConcept) throws IOException {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		c2Nid = Bdb.uuidToNid(refsetMember.getC2Uuid());
		strValue = refsetMember.getStrValue();
		if (refsetMember.getRevisionList() != null) {
			revisions = new CopyOnWriteArrayList<CidCidStrRevision>();
			for (ERefsetCidCidStrRevision eVersion: refsetMember.getRevisionList()) {
				revisions.add(new CidCidStrRevision(eVersion, this));
			}
		}
	}

    public CidCidStrMember() {
        super();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidCidStrMember.class.isAssignableFrom(obj.getClass())) {
            CidCidStrMember another = (CidCidStrMember) obj;
            return this.c1Nid == another.c1Nid 
                && this.c2Nid == another.c2Nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { c1Nid, c2Nid });
    }   
    

	@Override
	protected boolean membersEqual(
			ConceptComponent<CidCidStrRevision, CidCidStrMember> obj) {
		if (CidCidStrMember.class.isAssignableFrom(obj.getClass())) {
			CidCidStrMember another = (CidCidStrMember) obj;
			return this.c1Nid == another.c1Nid && this.c2Nid == another.c2Nid && this.strValue.equals(another.strValue);
		}
		return false;
	}

	@Override
	protected final CidCidStrRevision readMemberRevision(TupleInput input) {
		return new CidCidStrRevision(input, this);
	}
	@Override
	protected void readMemberFields(TupleInput input) {
		c1Nid = input.readInt();
		c2Nid = input.readInt();
		strValue = input.readString();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
		output.writeInt(c2Nid);
		output.writeString(strValue);
	}

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(4);
        variableNids.add(getC1id());
        variableNids.add(getC2id());
        return variableNids;
    }

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        if (getTime() == time && getPathId() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
		CidCidStrRevision newR = new CidCidStrRevision(statusNid, pathNid, time, this);
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

	public int getC2Nid() {
		return c2Nid;
	}

	public void setC2Nid(int c2Nid) {
		this.c2Nid = c2Nid;
        modified();
	}

	public String getStringValue() {
		return strValue;
	}
	
    public String getStrValue() {
        return strValue;
    }

    public void setStringValue(String strValue) {
        this.strValue = strValue;
        modified();
    }
    
    public void setStrValue(String strValue) {
        this.strValue = strValue;
        modified();
    }

	@Override
	public int getTypeId() {
		return REFSET_TYPES.CID_CID_STR.getTypeNid();
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
        buf.append(" strValue:" + "'" + this.strValue + "'");
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
	public CidCidStrMember duplicate() {
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
