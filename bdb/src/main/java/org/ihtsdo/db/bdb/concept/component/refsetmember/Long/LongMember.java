package org.ihtsdo.db.bdb.concept.component.refsetmember.Long;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.integer.IntMember;
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
		// TODO Auto-generated method stub
		return null;
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
		buf.append(" longValue: ");
		buf.append(longValue);
		return buf.toString();
	}


	@Override
	public int getTypeId() {
		return REFSET_TYPES.LONG.getTypeNid();
	}

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ": ");
        buf.append(" longValue:" + this.longValue);
        buf.append("; ");
        buf.append(super.toString());
        return buf.toString();
    }

}
