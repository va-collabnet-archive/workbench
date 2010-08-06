package org.ihtsdo.concept.component.refsetmember.Long;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartLong;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.tk.dto.concept.component.refset.Long.TkRefsetLongRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class LongRevision extends RefsetRevision<LongRevision, LongMember>
	implements I_ExtendByRefPartLong {

	private long longValue;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" longValue:" + this.longValue);
        buf.append(super.toString());
        return buf.toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (LongRevision.class.isAssignableFrom(obj.getClass())) {
            LongRevision another = (LongRevision) obj;
            return this.longValue == another.longValue && 
            super.equals(obj);
        }
        return false;
    }

    
    public LongRevision(int statusNid, int pathNid, long time, 
			LongMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
		longValue = primoridalMember.getLongValue();
	}

	public LongRevision(int statusAtPositionNid, 
			LongMember primoridalMember) {
		super(statusAtPositionNid, primoridalMember);
		longValue = primoridalMember.getLongValue();
	}

	protected LongRevision(int statusNid, int pathNid, long time, 
			LongRevision another) {
		super(statusNid, pathNid, time, another.primordialComponent);
		longValue = another.longValue;
	}

	@Override
	public LongRevision makeAnalog(int statusNid, int pathNid, long time) {
        LongRevision newR = new LongRevision(statusNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
	}


    @Override
    public LongRevision makeAnalog() {
         return new LongRevision(getStatusId(), getPathId(), getTime(), this);
    }

	public LongRevision(TupleInput input, 
			LongMember primoridalMember) {
		super(input, primoridalMember);
		longValue = input.readLong();
	}

	public LongRevision(TkRefsetLongRevision eVersion,
			LongMember member) {
		super(eVersion, member);
		this.longValue = eVersion.getLongValue();
	}

    public LongRevision() {
        super();
    }
    
    @Override
	public UniversalAceExtByRefPart getUniversalPart()
			throws TerminologyException, IOException {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ExtendByRefPart makePromotionPart(I_Path promotionPath) {
		// TODO
		throw new UnsupportedOperationException();
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
        modified();
	}
    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeLong(longValue);
    }
    
    @Override
    public ArrayIntList getVariableVersionNids() {
        return new ArrayIntList(2);
    }

}
