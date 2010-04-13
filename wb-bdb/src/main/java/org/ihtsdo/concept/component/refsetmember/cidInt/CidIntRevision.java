package org.ihtsdo.concept.component.refsetmember.cidInt;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.ERefsetCidIntRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidIntRevision  
				extends RefsetRevision<CidIntRevision, CidIntMember>
	implements I_ExtendByRefPartCidInt {

	private int c1Nid;
	private int intValue;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid: ");
        ConceptComponent.addNidToBuffer(buf, c1Nid);
        buf.append(" intValue:" + this.intValue);
        buf.append(super.toString());
        return buf.toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidIntRevision.class.isAssignableFrom(obj.getClass())) {
            CidIntRevision another = (CidIntRevision) obj;
             return this.c1Nid == another.c1Nid && 
             	this.intValue == another.intValue &&
             	super.equals(obj);
        }
        return false;
    }

    
    protected CidIntRevision(int statusNid, int pathNid, long time, 
			CidIntMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		intValue = primoridalMember.getIntValue();
	}

	protected CidIntRevision(int statusAtPositionNid, 
			CidIntMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		intValue = primoridalMember.getIntValue();
	}

	protected CidIntRevision(int statusNid, int pathNid, long time, 
			CidIntRevision another) {
		super(statusNid, pathNid, time, another.primordialComponent);
		c1Nid = another.c1Nid;
		intValue = another.intValue;
	}

	@Override
	public CidIntRevision makeAnalog(int statusNid, int pathNid, long time) {
		return new CidIntRevision(statusNid, pathNid, time, this);
	}

	public CidIntRevision(TupleInput input, 
			CidIntMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		intValue = input.readInt();
	}

	public CidIntRevision(ERefsetCidIntRevision eVersion,
			CidIntMember member) {
		super(eVersion, member);
		c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
		intValue = eVersion.getIntValue();
	}

    public CidIntRevision() {
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

	@Override
	public int getIntValue() {
		return intValue;
	}

	@Override
	public void setIntValue(int intValue) {
		this.intValue = intValue;
        modified();
        modified();
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
        modified();
	}

	@Override
	public int getC1id() {
		return c1Nid;
	}

	@Override
	public void setC1id(int c1id) {
		this.c1Nid = c1id;
        modified();
	}
    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeInt(c1Nid);
        output.writeInt(intValue);
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(3);
        variableNids.add(getC1Nid());
        return variableNids;
    }

}
