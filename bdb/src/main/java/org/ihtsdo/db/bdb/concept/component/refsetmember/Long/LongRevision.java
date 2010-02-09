package org.ihtsdo.db.bdb.concept.component.refsetmember.Long;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLong;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.concept.component.refsetmember.integer.IntRevision;
import org.ihtsdo.etypes.ERefsetLongVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class LongRevision extends RefsetRevision<LongRevision, LongMember>
	implements I_ThinExtByRefPartLong {

	private long longValue;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" longValue:" + this.longValue);
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
    public String validate(LongRevision another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        if (this.longValue != another.longValue) {
            buf.append("\tLongRevision.longValue not equal: \n" + 
                "\t\tthis.longValue = " + this.longValue + "\n" + 
                "\t\tanother.longValue = " + another.longValue + "\n");
        }
        // Compare the parents 
        buf.append(super.validate(another));
        return buf.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (LongRevision.class.isAssignableFrom(obj.getClass())) {
            LongRevision another = (LongRevision) obj;
            return this.sapNid == another.sapNid;
        }
        return false;
    }

    
    public LongRevision(int statusNid, int pathNid, long time, 
			LongMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	public LongRevision(int statusAtPositionNid, 
			LongMember primoridalMember) {
		super(statusAtPositionNid, primoridalMember);
	}

	public LongRevision(TupleInput input, 
			LongMember primoridalMember) {
		super(input, primoridalMember);
		longValue = input.readLong();
	}

	public LongRevision(ERefsetLongVersion eVersion,
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
	public I_ThinExtByRefPart makePromotionPart(I_Path promotionPath) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(I_ThinExtByRefPart o) {
		// TODO
		throw new UnsupportedOperationException();
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

}
