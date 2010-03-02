package org.ihtsdo.concept.component.refsetmember.cidLong;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidLong;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.ERefsetCidLongRevision;

import com.sleepycat.bind.tuple.TupleInput;

public class CidLongRevision extends RefsetRevision<CidLongRevision, CidLongMember>
	implements I_ExtendByRefPartCidLong {

	private int c1Nid;
	private long longValue;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid:" + this.c1Nid);
        buf.append(" longValue:" + this.longValue);
        buf.append(" }=> ");
        buf.append(super.toString());
        return buf.toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidLongRevision.class.isAssignableFrom(obj.getClass())) {
            CidLongRevision another = (CidLongRevision) obj;
            return this.c1Nid == another.c1Nid && 
            	longValue == another.longValue &&
            	super.equals(obj);
        }
        return false;
    }
    
    protected CidLongRevision(int statusNid, int pathNid, long time, 
			CidLongMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		longValue = primoridalMember.getLongValue();
	}

	protected CidLongRevision(int statusAtPositionNid, 
			CidLongMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		longValue = primoridalMember.getLongValue();
	}

	protected CidLongRevision(int statusNid, int pathNid, long time, 
			CidLongRevision another) {
		super(statusNid, pathNid, time, another.primordialComponent);
		c1Nid = another.c1Nid;
		longValue = another.longValue;
	}

	@Override
	public CidLongRevision makeAnalog(int statusNid, int pathNid, long time) {
		return new CidLongRevision(statusNid, pathNid, time, this);
	}

	public CidLongRevision(TupleInput input, 
			CidLongMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		longValue = input.readLong();
	}

	public CidLongRevision(ERefsetCidLongRevision eVersion,
			CidLongMember member) {
		super(eVersion, member);
		c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
		longValue = eVersion.getLongValue();
	}

    public CidLongRevision() {
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
	public int compareTo(I_ExtendByRefPart o) {
		// TODO
		throw new UnsupportedOperationException();
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

	@Override
	public int getC1id() {
		return c1Nid;
	}

	@Override
	public void setC1id(int c1id) {
		this.c1Nid = c1id;
	}

}
