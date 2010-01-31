package org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidStr;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidCid.CidCidCidMember;
import org.ihtsdo.etypes.ERefsetCidCidStrMember;
import org.ihtsdo.etypes.ERefsetCidCidStrVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidCidStrMember extends RefsetMember<CidCidStrRevision, CidCidStrMember> {

	private int c1Nid;
	private int c2Nid;
	private String strValue;

	public CidCidStrMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public CidCidStrMember(ERefsetCidCidStrMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		c2Nid = Bdb.uuidToNid(refsetMember.getC2Uuid());
		strValue = refsetMember.getStrValue();
		if (refsetMember.getExtraVersionsList() != null) {
			revisions = new ArrayList<CidCidStrRevision>(refsetMember.getExtraVersionsList().size());
			for (ERefsetCidCidStrVersion eVersion: refsetMember.getExtraVersionsList()) {
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
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (revisions == null) {
			revisions = new ArrayList<CidCidStrRevision>(
					additionalVersionCount);
		} else {
			revisions.ensureCapacity(revisions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			revisions.add(new CidCidStrRevision(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
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
	protected String getTypeFieldsString() {
		StringBuffer buf = new StringBuffer();
		buf.append("c1Nid: ");
		addNidToBuffer(buf, c1Nid);
		buf.append(" c2Nid: ");
		addNidToBuffer(buf, c2Nid);
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

	public int getC2Nid() {
		return c2Nid;
	}

	public void setC2Nid(int c2Nid) {
		this.c2Nid = c2Nid;
	}

	public String getStrValue() {
		return strValue;
	}

	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}

	@Override
	public int getTypeId() {
		return REFSET_TYPES.CID_CID_STR.getTypeNid();
	}

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ": ");
        buf.append(" c1Nid:" + this.c1Nid);
        buf.append(" c2Nid:" + this.c2Nid);
        buf.append(" strValue:" + "'" + this.strValue + "'");
        buf.append("; ");
        buf.append(super.toString());
        return buf.toString();
    }

}
