package org.ihtsdo.db.bdb.concept.component.refsetmember.Long;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.integer.IntRevision;
import org.ihtsdo.etypes.ERefsetLongMember;
import org.ihtsdo.etypes.ERefsetLongVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class LongMember extends RefsetMember<LongRevision, LongMember> {
	private long longValue;

	public LongMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public LongMember(ERefsetLongMember refsetMember, Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		longValue =refsetMember.getLongValue();
		if (refsetMember.getExtraVersionsList() != null) {
			revisions = new ArrayList<LongRevision>(refsetMember.getExtraVersionsList().size());
			for (ERefsetLongVersion eVersion: refsetMember.getExtraVersionsList()) {
				revisions.add(new LongRevision(eVersion, this));
			}
		}
	}

	public LongMember() {
        super();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (LongMember.class.isAssignableFrom(obj.getClass())) {
            LongMember another = (LongMember) obj;
            return this.nid == another.nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { this.nid });
    } 
    
	@Override
	protected boolean membersEqual(
			ConceptComponent<LongRevision, LongMember> obj) {
		if (LongMember.class.isAssignableFrom(obj.getClass())) {
			LongMember another = (LongMember) obj;
			return this.longValue == another.longValue;
		}
		return false;
	}
	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (revisions == null) {
			revisions = new ArrayList<LongRevision>(
					additionalVersionCount);
		} else {
			revisions.ensureCapacity(revisions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			revisions.add(new LongRevision(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		longValue = input.readLong();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeLong(longValue);
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		if (enclosingConcept.isEditable()) {
			LongRevision newR = new LongRevision(statusNid, pathNid, time, this);
			addVersion(newR);
			return newR;
		}
		throw new UnsupportedOperationException("enclosingConcept is not editable");
	}

	protected long getLongValue() {
		return longValue;
	}

	protected void setLongValue(long longValue) {
		this.longValue = longValue;
	}

	@Override
	protected String getTypeFieldsString() {
		StringBuffer buf = new StringBuffer();
		buf.append(" longValue:");
		buf.append(longValue);
		return buf.toString();
	}


	@Override
	public int getTypeId() {
		return REFSET_TYPES.LONG.getTypeNid();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
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
    public String validate(LongMember another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        if (this.longValue != another.longValue) {
            buf.append("\tLongMember.longValue not equal: \n" + 
                "\t\tthis.longValue = " + this.longValue + "\n" + 
                "\t\tanother.longValue = " + another.longValue + "\n");
        }
        // Compare the parents 
        buf.append(super.validate(another));
        return buf.toString();
    }
}
