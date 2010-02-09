package org.ihtsdo.db.bdb.concept.component.refsetmember.cidLong;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidInt.CidIntMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidInt.CidIntRevision;
import org.ihtsdo.etypes.ERefsetCidLongMember;
import org.ihtsdo.etypes.ERefsetCidLongVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidLongMember 
				extends RefsetMember<CidLongRevision, CidLongMember> {

	private int c1Nid;
	private long longValue;

	public CidLongMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public CidLongMember(ERefsetCidLongMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
		longValue = refsetMember.getLongValue();
		if (refsetMember.getExtraVersionsList() != null) {
			revisions = new ArrayList<CidLongRevision>(refsetMember.getExtraVersionsList().size());
			for (ERefsetCidLongVersion eVersion: refsetMember.getExtraVersionsList()) {
				revisions.add(new CidLongRevision(eVersion, this));
			}
		}
	}

    public CidLongMember() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidLongMember.class.isAssignableFrom(obj.getClass())) {
            CidLongMember another = (CidLongMember) obj;
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
			ConceptComponent<CidLongRevision, CidLongMember> obj) {
		if (CidLongMember.class.isAssignableFrom(obj.getClass())) {
			CidLongMember another = (CidLongMember) obj;
			return this.c1Nid == another.c1Nid && this.longValue == another.longValue;
		}
		return false;
	}

	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (revisions == null) {
			revisions = new ArrayList<CidLongRevision>(
					additionalVersionCount);
		} else {
			revisions.ensureCapacity(revisions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			revisions.add(new CidLongRevision(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		c1Nid = input.readInt();
		longValue = input.readLong();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(c1Nid);
		output.writeLong(longValue);
	}

	@Override
	protected String getTypeFieldsString() {
		StringBuffer buf = new StringBuffer();
		buf.append("c1Nid:");
		addNidToBuffer(buf, c1Nid);
		buf.append(" longValue:");
		buf.append(longValue);
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
			CidLongRevision newR = new CidLongRevision(statusNid, pathNid, time, this);
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

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

	@Override
	public int getTypeId() {
		return REFSET_TYPES.CID_LONG.getTypeNid();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid:" + this.c1Nid);
        buf.append(" longValue:" + this.longValue);
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
    public String validate(CidLongMember another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        if (this.c1Nid != another.c1Nid) {
            buf.append("\tCidLongMember.c1Nid not equal: \n" + 
                "\t\tthis.c1Nid = " + this.c1Nid + "\n" + 
                "\t\tanother.c1Nid = " + another.c1Nid + "\n");
        }
        if (this.longValue != another.longValue) {
            buf.append("\tCidLongMember.intValue not equal: \n" + 
                "\t\tthis.longValue = " + this.longValue + "\n" + 
                "\t\tanother.longValue = " + another.longValue + "\n");
        }
        // Compare the parents 
        buf.append(super.validate(another));
        return buf.toString();
    }

}
