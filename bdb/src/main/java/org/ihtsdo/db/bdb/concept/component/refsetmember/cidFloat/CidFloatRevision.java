package org.ihtsdo.db.bdb.concept.component.refsetmember.cidFloat;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidStr.CidCidStrRevision;
import org.ihtsdo.etypes.ERefsetCidFloatVersion;

import com.sleepycat.bind.tuple.TupleInput;

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
        buf.append(" }=> ");
        buf.append(super.toString());
        return buf.toString();
    }


    /**
     * Test method to check to see if two objects are equal in all respects. 
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures. 
     * @throws IOException 
     */
    public String validate(CidFloatRevision another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        if (this.c1Nid != another.c1Nid) {
            buf.append("\tCidFloatRevision.c1Nid not equal: \n" + 
                "\t\tthis.c1Nid = " + this.c1Nid + "\n" + 
                "\t\tanother.c1Nid = " + another.c1Nid + "\n");
        }
        if (this.floatValue != another.floatValue) {
            buf.append("\tCidFloatRevision.floatValue not equal: \n" + 
                "\t\tthis.floatValue = " + this.floatValue + "\n" + 
                "\t\tanother.floatValue = " + another.floatValue + "\n");
        }
        // Compare the parents 
        buf.append(super.validate(another));
        return buf.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidFloatRevision.class.isAssignableFrom(obj.getClass())) {
            CidFloatRevision another = (CidFloatRevision) obj;
            return this.c1Nid == another.c1Nid;
        }
        return false;
    }
    
    public CidFloatRevision(int statusNid, int pathNid, long time, 
			CidFloatMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	public CidFloatRevision(int statusAtPositionNid, 
			CidFloatMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
	}

	public CidFloatRevision(TupleInput input, 
			CidFloatMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		floatValue = input.readFloat();
	}

	public CidFloatRevision(ERefsetCidFloatVersion eVersion,
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

	public float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
	}

}
