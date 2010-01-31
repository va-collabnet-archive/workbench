package org.ihtsdo.db.bdb.concept.component.refsetmember.integer;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidStr.CidStrRevision;
import org.ihtsdo.etypes.ERefsetIntVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class IntRevision extends RefsetRevision<IntRevision, IntMember>
	implements I_ThinExtByRefPartInteger {

	private int intValue;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" intValue:" + this.intValue);
        buf.append(" }=> ");
        buf.append(super.toString());
        return buf.toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (IntRevision.class.isAssignableFrom(obj.getClass())) {
            IntRevision another = (IntRevision) obj;
            return this.sapNid == another.sapNid;
        }
        return false;
    }

    public IntRevision(int statusNid, int pathNid, long time, 
			IntMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
	}

	public IntRevision(int statusAtPositionNid, 
			IntMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
	}

	public IntRevision(TupleInput input, 
			IntMember primoridalMember) {
		super(input, primoridalMember);
		intValue = input.readInt();
	}

	public IntRevision(ERefsetIntVersion eVersion,
			IntMember member) {
		super(eVersion, member);
		this.intValue = eVersion.getIntValue();
	}

    public IntRevision() {
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
	public int getIntValue() {
		return intValue;
	}

	@Override
	public int getValue() {
		return intValue;
	}

	@Override
	public void setIntValue(int value) {
		this.intValue = value;
	}

	@Override
	public void setValue(int value) {
		this.intValue = value;
	}

}
