package org.ihtsdo.concept.component.refsetmember.cid;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.ERefsetCidRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidRevision extends RefsetRevision<CidRevision, CidMember>
	implements I_ExtendByRefPartCid {
	
	private int c1Nid;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid: ");
        ConceptComponent.addNidToBuffer(buf, this.c1Nid);
        buf.append(super.toString());
        return buf.toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidRevision.class.isAssignableFrom(obj.getClass())) {
            CidRevision another = (CidRevision) obj;
            if (this.c1Nid == another.c1Nid) {
                return super.equals(obj);
            }
        }
        return false;
    }

    
    protected CidRevision(int statusNid, int pathNid, long time, 
			CidMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
	}

	protected CidRevision(int statusNid, int pathNid, long time, 
			CidRevision another) {
		super(statusNid, pathNid, time, another.primordialComponent);
		c1Nid = another.c1Nid;
	}

	@Override
	public CidRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathId() == pathNid) {
            this.setStatusId(statusNid);
            return this;
        }
        CidRevision newR = new CidRevision(statusNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
	}

    @Override
    public CidRevision makeAnalog() {
         return new CidRevision(getStatusId(), getPathId(), getTime(), this);
    }

	protected CidRevision(int statusAtPositionNid, 
			CidMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
	}

	public CidRevision(TupleInput input, 
			CidMember primoridalMember) {
		super(input, primoridalMember);
        c1Nid = input.readInt();
	}

	public CidRevision(ERefsetCidRevision eVersion,
			CidMember member) {
		super(eVersion, member);
		c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
	}

    public CidRevision() {
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
	public int getC1id() {
		return c1Nid;
	}

	@Override
	public void setC1id(int c1Nid) {
		this.c1Nid = c1Nid;
        modified();
	}
	
    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeInt(c1Nid);
    }
    
    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(3);
        variableNids.add(getC1id());
        return variableNids;
    }


}
