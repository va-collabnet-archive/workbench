package org.ihtsdo.db.bdb.concept.component.refsetmember.cidStr;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidLong.CidLongRevision;
import org.ihtsdo.etypes.ERefsetCidStrVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class CidStrRevision extends RefsetRevision<CidStrRevision, CidStrMember> 
	implements I_ThinExtByRefPartConceptString {

	private int c1Nid;
	private String strValue;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid:" + this.c1Nid);
        buf.append(" strValue:" + "'" + this.strValue + "'");
        buf.append(" }=> ");
        buf.append(super.toString());
        return buf.toString();
    }



    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidStrRevision.class.isAssignableFrom(obj.getClass())) {
            CidStrRevision another = (CidStrRevision) obj;
            return this.sapNid == another.sapNid;
        }
        return false;
    }

    public CidStrRevision(int statusNid, int pathNid, long time, 
			CidStrMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	public CidStrRevision(int statusAtPositionNid, 
			CidStrMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
	}

	public CidStrRevision(TupleInput input, 
			CidStrMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		strValue = input.readString();
	}

	public CidStrRevision(ERefsetCidStrVersion eVersion,
			CidStrMember member) {
		super(eVersion, member);
		c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
		strValue = eVersion.getStrValue();
	}

    public CidStrRevision() {
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


	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
	}

	public String getStrValue() {
		return strValue;
	}

	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}

	@Override
	public int getC1id() {
		return c1Nid;
	}

	@Override
	public String getStr() {
		return strValue;
	}

	@Override
	public void setC1id(int c1id) {
		this.c1Nid = c1id;
	}

	@Override
	public void setStr(String str) {
		this.strValue = str;
	}
}
