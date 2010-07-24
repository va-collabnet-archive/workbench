package org.ihtsdo.concept.component.refsetmember.cidStr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refsetmember.str.StrRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.ERefsetCidStrMember;
import org.ihtsdo.etypes.ERefsetCidStrRevision;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.concept.component.refset.cidstr.TkRefsetCidStrMember;
import org.ihtsdo.tk.concept.component.refset.cidstr.TkRefsetCidStrRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidStrMember extends RefsetMember<CidStrRevision, CidStrMember> implements I_ExtendByRefPartCidString {

	private static VersionComputer<RefsetMember<CidStrRevision, CidStrMember>.Version> computer = 
		new VersionComputer<RefsetMember<CidStrRevision, CidStrMember>.Version>();

	protected VersionComputer<RefsetMember<CidStrRevision, CidStrMember>.Version> getVersionComputer() {
		return computer;
	}

	public class Version 
	extends RefsetMember<CidStrRevision, CidStrMember>.Version 
	implements I_ExtendByRefVersion, I_ExtendByRefPartCidString {

		private Version() {
			super();
		}

		private Version(int index) {
			super(index);
		}

		public int compareTo(I_ExtendByRefPart o) {
			if (I_ExtendByRefPartCidString.class.isAssignableFrom(o.getClass())) {
				I_ExtendByRefPartCidString another = (I_ExtendByRefPartCidString) o;
				if (this.getC1id() != another.getC1id()) {
					return this.getC1id() - another.getC1id();
				}
				if (!this.getStringValue().equals(another.getStringValue())) {
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
			return CidStrMember.this.getC1Nid();
		}

		@Override
		public void setC1id(int c1id) {
			if (index >= 0) {
				revisions.get(index).setC1id(c1id);
			}
			CidStrMember.this.setC1Nid(c1id);
		}


		@Override
		public I_ExtendByRefPartCidString duplicate() {
			return (I_ExtendByRefPartCidString) super.duplicate();
		}

		@Override
		public String getStringValue() {
			if (index >= 0) {
				return revisions.get(index).getStringValue();
			}
			return CidStrMember.this.getStringValue();
		}

		@Override
		public void setStringValue(String value) {
			if (index >= 0) {
				revisions.get(index).setStringValue(value);
			}
			CidStrMember.this.setStringValue(value);
		}
		@Override
		public ERefsetCidStrMember getERefsetMember() throws TerminologyException, IOException {
			return new ERefsetCidStrMember(this);
		}

		@Override
		public ERefsetCidStrRevision getERefsetRevision() throws TerminologyException, IOException {
			return new ERefsetCidStrRevision(this);
		}
	}

	private int c1Nid;
	private String strValue;
	
	public CidStrMember(Concept enclosingConcept, TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}

	public CidStrMember(TkRefsetCidStrMember refsetMember, Concept enclosingConcept) throws IOException {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		strValue = refsetMember.getStrValue();
		if (refsetMember.getRevisionList() != null) {
			revisions = new CopyOnWriteArrayList<CidStrRevision>();
			for (TkRefsetCidStrRevision eVersion: refsetMember.getRevisionList()) {
				revisions.add(new CidStrRevision(eVersion, this));
			}
		}
	}

    public CidStrMember() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidStrMember.class.isAssignableFrom(obj.getClass())) {
            CidStrMember another = (CidStrMember) obj;
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
			ConceptComponent<CidStrRevision, CidStrMember> obj) {
		if (CidStrMember.class.isAssignableFrom(obj.getClass())) {
			CidStrMember another = (CidStrMember) obj;
			return this.c1Nid == another.c1Nid && this.strValue.equals(another.strValue);
		}
		return false;
	}
	
	
	@Override
	protected final CidStrRevision readMemberRevision(TupleInput input) {
	    return new CidStrRevision(input, this);
	}
	@Override
	protected void readMemberFields(TupleInput input) {
		c1Nid = input.readInt();
		strValue = input.readString();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
		output.writeString(strValue);
	}

	@Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(3);
        variableNids.add(getC1id());
        return variableNids;
    }

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        if (getTime() == time && getPathId() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
		CidStrRevision newR = new CidStrRevision(statusNid, pathNid, time, this);
		addRevision(newR);
		return newR;
	}


    @Override
    public CidStrRevision makeAnalog() {
        CidStrRevision newR = new CidStrRevision(getStatusId(), getPathId(), getTime(), this);
        return newR;
    }

    public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
        modified();
	}

	public String getStringValue() {
		return strValue;
	}

	public void setStringValue(String strValue) {
		this.strValue = strValue;
        modified();
	}

	@Override
	public int getTypeId() {
		return REFSET_TYPES.CID_STR.getTypeNid();
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
        buf.append(" strValue:" + "'" + this.strValue + "'");
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
