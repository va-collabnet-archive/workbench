package org.ihtsdo.db.bdb.concept.component.refsetmember.cidInt;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidFloat.CidFloatMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidFloat.CidFloatRevision;
import org.ihtsdo.etypes.ERefsetCidIntMember;
import org.ihtsdo.etypes.ERefsetCidIntVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidIntMember extends RefsetMember<CidIntRevision, CidIntMember> {

	private int c1Nid;
	private int intValue;

	public CidIntMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public CidIntMember(ERefsetCidIntMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		intValue = refsetMember.getIntValue();
		if (refsetMember.getExtraVersionsList() != null) {
			revisions = new ArrayList<CidIntRevision>(refsetMember.getExtraVersionsList().size());
			for (ERefsetCidIntVersion eVersion: refsetMember.getExtraVersionsList()) {
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
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (revisions == null) {
			revisions = new ArrayList<CidIntRevision>(
					additionalVersionCount);
		} else {
			revisions.ensureCapacity(revisions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			revisions.add(new CidIntRevision(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		c1Nid = input.readInt();
		intValue = input.readInt();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
		output.writeInt(intValue);
	}


	@Override
	protected String getTypeFieldsString() {
		StringBuffer buf = new StringBuffer();
		buf.append("c1Nid:");
		addNidToBuffer(buf, c1Nid);
		buf.append(" intValue:");
		buf.append(intValue);
		return buf.toString();
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		if (enclosingConcept.isEditable()) {
			CidIntRevision newR = new CidIntRevision(statusNid, pathNid, time, this);
			addVersion(newR);
			return newR;
		}
		throw new UnsupportedOperationException("enclosingConcept is not editable");
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
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
        buf.append(" c1Nid:" + this.c1Nid);
        buf.append(" intValue:" + this.intValue);
        buf.append(" }=> ");
        buf.append(super.toString());
        return buf.toString();
    }

    /**
     * Test method to check to see if two objects are equal in all respects. 
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures. 
     * @throws IOException 
     */
    public String validate(CidIntMember another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        if (this.c1Nid != another.c1Nid) {
            buf.append("\tCidIntMember.c1Nid not equal: \n" + 
                "\t\tthis.c1Nid = " + this.c1Nid + "\n" + 
                "\t\tanother.c1Nid = " + another.c1Nid + "\n");
        }
        if (this.intValue != another.intValue) {
            buf.append("\tCidIntMember.intValue not equal: \n" + 
                "\t\tthis.intValue = " + this.intValue + "\n" + 
                "\t\tanother.intValue = " + another.intValue + "\n");
        }
        // Compare the parents 
        buf.append(super.validate(another));
        return buf.toString();
    }
    
}
