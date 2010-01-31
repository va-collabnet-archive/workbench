package org.ihtsdo.db.bdb.concept.component.refsetmember.cidStr;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidLong.CidLongMember;
import org.ihtsdo.etypes.ERefsetCidStrMember;
import org.ihtsdo.etypes.ERefsetCidStrVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidStrMember extends RefsetMember<CidStrRevision, CidStrMember> {

	private int c1Nid;
	private String strValue;
	
	public CidStrMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public CidStrMember(ERefsetCidStrMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		strValue = refsetMember.getStrValue();
		if (refsetMember.getExtraVersionsList() != null) {
			revisions = new ArrayList<CidStrRevision>(refsetMember.getExtraVersionsList().size());
			for (ERefsetCidStrVersion eVersion: refsetMember.getExtraVersionsList()) {
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
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (revisions == null) {
			revisions = new ArrayList<CidStrRevision>(
					additionalVersionCount);
		} else {
			revisions.ensureCapacity(revisions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			revisions.add(new CidStrRevision(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		c1Nid = input.readInt();
		strValue = input.readString();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
		output.writeString(strValue);
	}
	@Override
	protected String getTypeFieldsString() {
		StringBuffer buf = new StringBuffer();
		buf.append("c1Nid: ");
		addNidToBuffer(buf, c1Nid);
		buf.append(" strValue: ");
		buf.append(strValue);
		return buf.toString();
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
	}

	public String getStrValue() {
		return strValue;
	}

	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}

	@Override
	public int getTypeId() {
		return REFSET_TYPES.CID_STR.getTypeNid();
	}

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ": ");
        buf.append(" c1Nid:" + this.c1Nid);
        buf.append(" strValue:" + "'" + this.strValue + "'");
        buf.append("; ");
        buf.append(super.toString());
        return buf.toString();
    }

}
