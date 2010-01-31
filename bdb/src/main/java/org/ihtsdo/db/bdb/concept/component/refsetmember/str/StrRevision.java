package org.ihtsdo.db.bdb.concept.component.refsetmember.str;

import java.io.IOException;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.concept.component.refsetmember.integer.IntRevision;
import org.ihtsdo.etypes.ERefsetStrVersion;

import com.sleepycat.bind.tuple.TupleInput;

public class StrRevision extends RefsetRevision<StrRevision, StrMember> 
	//implements I_ThinExtByRefPartString 
	{

	private String stringValue;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" stringValue:" + "'" + this.stringValue + "'");
        buf.append(" }=> ");
        buf.append(super.toString());
        return buf.toString();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (StrRevision.class.isAssignableFrom(obj.getClass())) {
            StrRevision another = (StrRevision) obj;
            return this.sapNid == another.sapNid;
        }
        return false;
    }

    public StrRevision(int statusNid, int pathNid, long time, 
			StrMember primoridalMember) {
		super(statusNid, pathNid, time, 
				 primoridalMember);
	}

	public StrRevision(int statusAtPositionNid, 
			StrMember primoridalMember) {
		super(statusAtPositionNid, primoridalMember);
	}

	public StrRevision(TupleInput input, 
			StrMember primoridalMember) {
		super(input, primoridalMember);
		stringValue = input.readString();
	}

	public StrRevision(ERefsetStrVersion eVersion,
			StrMember primoridalMember) {
		super(eVersion, primoridalMember);
		this.stringValue = eVersion.getStringValue();
	}
	
    public StrRevision() {
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

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
}
