package org.ihtsdo.concept.component.refsetmember.str;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.etypes.ERefsetStrRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class StrRevision extends RefsetRevision<StrRevision, StrMember> 
	implements I_ExtendByRefPartStr 
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
        buf.append(super.toString());
        return buf.toString();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (StrRevision.class.isAssignableFrom(obj.getClass())) {
        	StrRevision another = (StrRevision) obj;
            return stringValue.equals(another.stringValue) &&
            	super.equals(obj);
        }
        return false;
    }

    public StrRevision(int statusNid, int pathNid, long time, 
			StrMember another) {
		super(statusNid, pathNid, time, 
		    another);
		stringValue = another.getStringValue();
		
	}

	public StrRevision(int statusAtPositionNid, 
			StrMember another) {
		super(statusAtPositionNid, another);
		stringValue = another.getStringValue();
	}


	protected StrRevision(int statusNid, int pathNid, long time, 
			StrRevision another) {
		super(statusNid, pathNid, time, another.primordialComponent);
		stringValue = another.stringValue;
	}

	@Override
	public StrRevision makeAnalog(int statusNid, int pathNid, long time) {
		return new StrRevision(statusNid, pathNid, time, this);
	}

	public StrRevision(TupleInput input, 
			StrMember primoridalMember) {
		super(input, primoridalMember);
		stringValue = input.readString();
	}

	public StrRevision(ERefsetStrRevision eVersion,
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
	public I_ExtendByRefPart makePromotionPart(I_Path promotionPath) {
		// TODO
		throw new UnsupportedOperationException();
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}


	@Override
	public StrRevision duplicate() {
		throw new UnsupportedOperationException();
	}
    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeString(stringValue);
    }
    
    @Override
    public ArrayIntList getVariableVersionNids() {
        return new ArrayIntList(2);
    }

}
