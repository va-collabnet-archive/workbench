package org.ihtsdo.concept.component.refsetmember.cidStr;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.concept.component.refsetmember.cidLong.CidLongRevision;
import org.ihtsdo.concept.component.refsetmember.str.StrRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.ERefsetCidStrRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidStrRevision extends RefsetRevision<CidStrRevision, CidStrMember> 
	implements I_ExtendByRefPartCidString {

	private int c1Nid;
	private String strValue;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid: ");
        ConceptComponent.addNidToBuffer(buf, c1Nid);
        buf.append(" strValue:" + "'" + this.strValue + "'");
        buf.append(super.toString());
        return buf.toString();
    }



    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidStrRevision.class.isAssignableFrom(obj.getClass())) {
            CidStrRevision another = (CidStrRevision) obj;
            return this.c1Nid == another.c1Nid &&
            	this.strValue.equals(another.strValue) &&
            	super.equals(obj);
        }
        return false;
    }

    public CidStrRevision(int statusNid, int pathNid, long time, 
			CidStrMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		strValue = primoridalMember.getStringValue();
	}

	public CidStrRevision(int statusAtPositionNid, 
			CidStrMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		strValue = primoridalMember.getStringValue();
	}

	protected CidStrRevision(int statusNid, int pathNid, long time, 
			CidStrRevision another) {
		super(statusNid, pathNid, time, another.primordialComponent);
		c1Nid = another.c1Nid;
		strValue = another.strValue;
	}

	@Override
	public CidStrRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathId() == pathNid) {
            this.setStatusId(statusNid);
            return this;
        }
		return new CidStrRevision(statusNid, pathNid, time, this);
	}

    @Override
    public CidStrRevision makeAnalog() {
         return new CidStrRevision(getStatusId(), getPathId(), getTime(), this);
    }

	public CidStrRevision(TupleInput input, 
			CidStrMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		strValue = input.readString();
	}

	public CidStrRevision(ERefsetCidStrRevision eVersion,
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
	public I_ExtendByRefPart makePromotionPart(I_Path promotionPath) {
		// TODO
		throw new UnsupportedOperationException();
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
	}

	public String getStringValue() {
		return strValue;
	}

	public void setStringValue(String strValue) {
		this.strValue = strValue;
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
	public StrRevision duplicate() {
		throw new UnsupportedOperationException();
	}
    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeInt(c1Nid);
        output.writeString(strValue);
    }
    
    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(3);
        variableNids.add(getC1id());
        return variableNids;
    }

}
