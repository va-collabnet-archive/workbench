package org.ihtsdo.concept.component.refsetmember.Long;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refsetmember.integer.IntRevision;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.ERefsetLongMember;
import org.ihtsdo.etypes.ERefsetLongRevision;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class LongMember extends RefsetMember<LongRevision, LongMember> {

	private static VersionComputer<RefsetMember<LongRevision, LongMember>.Version> computer = 
		new VersionComputer<RefsetMember<LongRevision, LongMember>.Version>();

	protected VersionComputer<RefsetMember<LongRevision, LongMember>.Version> getVersionComputer() {
		return computer;
	}

	private long longValue;

	public LongMember(Concept enclosingConcept, TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}

	public LongMember(ERefsetLongMember refsetMember, Concept enclosingConcept) throws IOException {
		super(refsetMember, enclosingConcept);
		longValue =refsetMember.getLongValue();
		if (refsetMember.getRevisionList() != null) {
			revisions = new CopyOnWriteArrayList<LongRevision>();
			for (ERefsetLongRevision eVersion: refsetMember.getRevisionList()) {
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
	protected final LongRevision readMemberRevision(TupleInput input) {
	    return new LongRevision(input, this);
	}
	@Override
	protected void readMemberFields(TupleInput input) {
		longValue = input.readLong();
	}
	@Override
	protected void writeMember(TupleOutput output) {
		output.writeLong(longValue);
	}

    @Override
    protected ArrayIntList getVariableVersionNids() {
        return new ArrayIntList(2);
    }

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        if (getTime() == time && getPathId() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
		LongRevision newR = new LongRevision(statusNid, pathNid, time, this);
		addRevision(newR);
		return newR;
	}


    @Override
    public LongRevision makeAnalog() {
        LongRevision newR = new LongRevision(getStatusId(), getPathId(), getTime(), this);
        return newR;
    }

    protected long getLongValue() {
		return longValue;
	}

	protected void setLongValue(long longValue) {
		this.longValue = longValue;
        modified();
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

}
