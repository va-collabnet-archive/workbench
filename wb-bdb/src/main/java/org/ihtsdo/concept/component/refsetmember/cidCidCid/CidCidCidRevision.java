package org.ihtsdo.concept.component.refsetmember.cidCidCid;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.ERefsetCidCidCidRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidCidCidRevision extends RefsetRevision<CidCidCidRevision, CidCidCidMember> 
	implements I_ExtendByRefPartCidCidCid {

	private int c1Nid;
	private int c2Nid;
	private int c3Nid;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid:" + this.c1Nid);
        buf.append(" c2Nid:" + this.c2Nid);
        buf.append(" c3Nid:" + this.c3Nid);
        buf.append(super.toString());
        return buf.toString();
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidCidCidRevision.class.isAssignableFrom(obj.getClass())) {
            CidCidCidRevision another = (CidCidCidRevision) obj;
            return this.c1Nid == another.c1Nid 
                && this.c2Nid == another.c2Nid
                && this.c3Nid == another.c3Nid &&
                super.equals(obj);
        }
        return false;
    }

	public CidCidCidRevision(int statusNid, int pathNid,
			long time, 
			CidCidCidMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		c2Nid = primoridalMember.getC2Nid();
		c3Nid = primoridalMember.getC3Nid();
	}

	public CidCidCidRevision(int statusAtPositionNid, 
			CidCidCidMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		c2Nid = primoridalMember.getC2Nid();
		c3Nid = primoridalMember.getC3Nid();
	}

	protected CidCidCidRevision(int statusNid, int pathNid, long time, 
			CidCidCidRevision another) {
		super(statusNid, pathNid, time, another.primordialComponent);
		c1Nid = another.c1Nid;
		c2Nid = another.c2Nid;
		c3Nid = another.c3Nid;
	}

	@Override
	public CidCidCidRevision makeAnalog(int statusNid, int pathNid, long time) {
		return new CidCidCidRevision(statusNid, pathNid, time, this);
	}
	
	public CidCidCidRevision(TupleInput input, 
			CidCidCidMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		c2Nid = input.readInt();
		c3Nid = input.readInt();
	}

	public CidCidCidRevision(ERefsetCidCidCidRevision eVersion,
			CidCidCidMember member) {
		super(eVersion, member);
		c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
		c2Nid = Bdb.uuidToNid(eVersion.getC2Uuid());
		c3Nid = Bdb.uuidToNid(eVersion.getC3Uuid());
	}

    public CidCidCidRevision() {
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
	public int getC3id() {
		return c3Nid;
	}

	@Override
	public void setC3id(int c3nid) {
		this.c3Nid = c3nid;
        modified();
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
        modified();
	}

	public int getC2Nid() {
		return c2Nid;
	}

	public void setC2Nid(int c2Nid) {
		this.c2Nid = c2Nid;
        modified();
	}

	public int getC3Nid() {
		return c3Nid;
	}

	public void setC3Nid(int c3Nid) {
		this.c3Nid = c3Nid;
        modified();
	}

	@Override
	public int getC2id() {
		return c2Nid;
	}

	@Override
	public void setC2id(int c2id) {
		this.c2Nid = c2id;
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
        output.writeInt(c2Nid);
        output.writeInt(c3Nid);
    }
    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(5);
        variableNids.add(getC1id());
        variableNids.add(getC2id());
        variableNids.add(getC3id());
        return variableNids;
    }

}
