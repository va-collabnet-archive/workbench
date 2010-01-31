package org.ihtsdo.db.bdb.concept.component.refsetmember.cidCid;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.concept.component.refsetmember.Boolean.BooleanRevision;
import org.ihtsdo.etypes.ERefsetCidCidVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidCidRevision extends RefsetRevision<CidCidRevision, CidCidMember> 
	implements I_ThinExtByRefPartConceptConcept {

	private int c1Nid;
	private int c2Nid;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid:" + this.c1Nid);
        buf.append(" c2Nid:" + this.c2Nid);
        buf.append(" }=> ");
        buf.append(super.toString());
        return buf.toString();
    }
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidCidRevision.class.isAssignableFrom(obj.getClass())) {
            CidCidRevision another = (CidCidRevision) obj;
            if ((this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid)) {
                return true;
            }
        }
        return false;
    }

    public CidCidRevision(int statusNid, int pathNid, long time, 
			CidCidMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	public CidCidRevision(int statusAtPositionNid, 
			CidCidMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
	}

	public CidCidRevision(TupleInput input, 
			CidCidMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		c2Nid = input.readInt();
	}

	public CidCidRevision(ERefsetCidCidVersion eVersion,
			CidCidMember member) {
		super(eVersion, member);
		c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
		c2Nid = Bdb.uuidToNid(eVersion.getC2Uuid());
	}

    public CidCidRevision() {
        super();
    }
	   

	@Override
	public UniversalAceExtByRefPart getUniversalPart()
			throws TerminologyException, IOException {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefPart makePromotionPart(I_Path promotionPath) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(I_ThinExtByRefPart o) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public int getC2id() {
		return c2Nid;
	}

	@Override
	public void setC2id(int c2id) {
		this.c2Nid = c2id;
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
	}

	public int getC2Nid() {
		return c2Nid;
	}

	public void setC2Nid(int c2Nid) {
		this.c2Nid = c2Nid;
	}

	@Override
	public int getC1id() {
		return c1Nid;
	}

	@Override
	public int getConceptId() {
		return c1Nid;
	}

	@Override
	public void setC1id(int c1id) {
		this.c1Nid = c1id;
	}

	@Override
	public void setConceptId(int conceptId) {
		this.c1Nid = conceptId;
	}

}
