package org.ihtsdo.concept.component.refsetmember.Boolean;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.tk.dto.concept.component.refset.Boolean.TkRefsetBooleanRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class BooleanRevision extends RefsetRevision<BooleanRevision, BooleanMember>
	implements I_ExtendByRefPartBoolean {
	
	private boolean booleanValue;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" booleanValue:" + this.booleanValue);
        buf.append(super.toString());
        return buf.toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (BooleanRevision.class.isAssignableFrom(obj.getClass())) {
            BooleanRevision another = (BooleanRevision) obj;
            return this.booleanValue == another.booleanValue && 
    			super.equals(obj);
        }
        return false;
    }

	protected BooleanRevision(int statusNid, int pathNid, long time, 
			BooleanMember primoridalMember) {
		super(statusNid, pathNid, time, primoridalMember);
		this.booleanValue = primoridalMember.getBooleanValue();
	}

	
	protected BooleanRevision(int statusAtPositionNid, 
			BooleanMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
		this.booleanValue = primoridalMember.getBooleanValue();
	}
	protected BooleanRevision(int statusNid, int pathNid, long time, 
			BooleanRevision another) {
		super(statusNid, pathNid, time, another.primordialComponent);
		this.booleanValue = another.getBooleanValue();
	}

	@Override
	public BooleanRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathId() == pathNid) {
            this.setStatusId(statusNid);
            return this;
        }
		BooleanRevision newR = new BooleanRevision(statusNid, pathNid, time, this);
	    primordialComponent.addRevision(newR);
	    return newR;

	}

    @Override
    public BooleanRevision makeAnalog() {
        return new BooleanRevision(getStatusId(), getPathId(), getTime(), this);
    }
	public BooleanRevision(TupleInput input, 
			BooleanMember primoridalMember) {
		super(input, primoridalMember);
        booleanValue = input.readBoolean();
	}

	public BooleanRevision(TkRefsetBooleanRevision eVersion,
			BooleanMember booleanMember) {
		super(eVersion, booleanMember);
		this.booleanValue = eVersion.isBooleanValue();
	}

    public BooleanRevision() {
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

	public boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
        modified();
	}

    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeBoolean(booleanValue);
    }
    
    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(2);
        return variableNids;
    }


}
