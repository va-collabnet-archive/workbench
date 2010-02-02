package org.ihtsdo.db.bdb.concept.component.refsetmember.integer;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidStr.CidStrMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidStr.CidStrRevision;
import org.ihtsdo.etypes.ERefsetIntMember;
import org.ihtsdo.etypes.ERefsetIntVersion;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IntMember extends RefsetMember<IntRevision, IntMember> {
	private int intValue;

	public IntMember(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public IntMember(ERefsetIntMember refsetMember, 
			Concept enclosingConcept) {
		super(refsetMember, enclosingConcept);
		intValue =refsetMember.getIntValue();
		if (refsetMember.getExtraVersionsList() != null) {
			revisions = new ArrayList<IntRevision>(refsetMember.getExtraVersionsList().size());
			for (ERefsetIntVersion eVersion: refsetMember.getExtraVersionsList()) {
				revisions.add(new IntRevision(eVersion, this));
			}
		}
	}

    public IntMember() {
        super();
    }

	
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (IntMember.class.isAssignableFrom(obj.getClass())) {
            IntMember another = (IntMember) obj;
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
			ConceptComponent<IntRevision, IntMember> obj) {
		if (IntMember.class.isAssignableFrom(obj.getClass())) {
			IntMember another = (IntMember) obj;
			return this.intValue == another.intValue;
		}
		return false;
	}

	@Override
	protected final void readMemberParts(TupleInput input,
			int additionalVersionCount) {
		if (revisions == null) {
			revisions = new ArrayList<IntRevision>(
					additionalVersionCount);
		} else {
			revisions.ensureCapacity(revisions.size()
					+ additionalVersionCount);
		}
		for (int i = 0; i < additionalVersionCount; i++) {
			revisions.add(new IntRevision(input, this));
		}
	}
	@Override
	protected void readMember(TupleInput input) {
		intValue = input.readInt();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeInt(intValue);
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		if (enclosingConcept.isEditable()) {
			IntRevision newR = new IntRevision(statusNid, pathNid, time, this);
			addVersion(newR);
			return newR;
		}
		throw new UnsupportedOperationException("enclosingConcept is not editable");
	}

	public int getIntValue() {
		return intValue;
	}
	@Override
	protected String getTypeFieldsString() {
		StringBuffer buf = new StringBuffer();
		buf.append(" intValue:");
		buf.append(intValue);
		return buf.toString();
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}
	@Override
	public int getTypeId() {
		return REFSET_TYPES.INT.getTypeNid();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" intValue:" + this.intValue);
        buf.append(" }=> ");
        buf.append(super.toString());
        return buf.toString();
    }

}
