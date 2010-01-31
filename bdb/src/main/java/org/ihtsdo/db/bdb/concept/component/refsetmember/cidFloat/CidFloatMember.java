package org.ihtsdo.db.bdb.concept.component.refsetmember.cidFloat;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidStr.CidCidStrMember;
import org.ihtsdo.etypes.ERefsetCidFloatMember;
import org.ihtsdo.etypes.ERefsetCidFloatVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidFloatMember extends RefsetMember<CidFloatRevision, CidFloatMember> {

	private int c1Nid;
	private float floatValue;

	public CidFloatMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public CidFloatMember(ERefsetCidFloatMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		floatValue = refsetMember.getFloatValue();
		if (refsetMember.getExtraVersionsList() != null) {
			revisions = new ArrayList<CidFloatRevision>(refsetMember.getExtraVersionsList().size());
			for (ERefsetCidFloatVersion eVersion: refsetMember.getExtraVersionsList()) {
				revisions.add(new CidFloatRevision(eVersion, this));
			}
		}
	}

    public CidFloatMember() {
        super();
    }
    
    @Override
	protected boolean membersEqual(
			ConceptComponent<CidFloatRevision, CidFloatMember> obj) {
		if (CidFloatMember.class.isAssignableFrom(obj.getClass())) {
			CidFloatMember another = (CidFloatMember) obj;
			return this.c1Nid == another.c1Nid && this.floatValue == another.floatValue;
		}
		return false;
	}


	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (revisions == null) {
			revisions = new ArrayList<CidFloatRevision>(
					additionalVersionCount);
		} else {
			revisions.ensureCapacity(revisions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			revisions.add(new CidFloatRevision(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		c1Nid = input.readInt();
		floatValue = input.readFloat();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
		output.writeFloat(floatValue);
	}

	@Override
	protected String getTypeFieldsString() {
		StringBuffer buf = new StringBuffer();
		buf.append("c1Nid: ");
		addNidToBuffer(buf, c1Nid);
		buf.append(" floatValue: ");
		buf.append(floatValue);
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

	public float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
	}

	@Override
	public int getTypeId() {
		return REFSET_TYPES.CID_FLOAT.getTypeNid();
	}

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ": ");
        buf.append(" c1Nid:" + this.c1Nid);
        buf.append(" floatValue:" + this.floatValue);
        buf.append("; ");
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidFloatMember.class.isAssignableFrom(obj.getClass())) {
            CidFloatMember another = (CidFloatMember) obj;
            return this.c1Nid == another.c1Nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { c1Nid });
    }  
}
