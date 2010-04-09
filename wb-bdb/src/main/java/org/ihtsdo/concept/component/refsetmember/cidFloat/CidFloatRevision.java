package org.ihtsdo.concept.component.refsetmember.cidFloat;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.ERefsetCidFloatRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidFloatRevision extends RefsetRevision<CidFloatRevision, CidFloatMember> {

	private int c1Nid;
	private float floatValue;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid:" + this.c1Nid);
        buf.append(" floatValue:" + this.floatValue);
        buf.append(super.toString());
        return buf.toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidFloatRevision.class.isAssignableFrom(obj.getClass())) {
            CidFloatRevision another = (CidFloatRevision) obj;
            return this.c1Nid == another.c1Nid && 
            	this.floatValue == another.floatValue &&
            	super.equals(obj);
        }
        return false;
    }
    
    public CidFloatRevision(int statusNid, int pathNid, long time, 
			CidFloatMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		floatValue = primoridalMember.getFloatValue();
	}

	public CidFloatRevision(int statusAtPositionNid, 
			CidFloatMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		floatValue = primoridalMember.getFloatValue();
	}

	protected CidFloatRevision(int statusNid, int pathNid, long time, 
			CidFloatRevision another) {
		super(statusNid, pathNid, time, another.primordialComponent);
		c1Nid = another.c1Nid;
		floatValue = another.floatValue;
	}

	@Override
	public CidFloatRevision makeAnalog(int statusNid, int pathNid, long time) {
		return new CidFloatRevision(statusNid, pathNid, time, this);
	}

	public CidFloatRevision(TupleInput input, 
			CidFloatMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		floatValue = input.readFloat();
	}

	public CidFloatRevision(ERefsetCidFloatRevision eVersion,
			CidFloatMember member) {
		super(eVersion, member);
		c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
		floatValue = eVersion.getFloatValue();
	}

    public CidFloatRevision() {
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
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeInt(c1Nid);
        output.writeFloat(floatValue);
    }
    
    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(3);
        variableNids.add(getC1Nid());
        return variableNids;
    }

}
