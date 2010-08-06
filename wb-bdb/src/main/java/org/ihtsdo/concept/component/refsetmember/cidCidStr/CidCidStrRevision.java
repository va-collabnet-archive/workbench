package org.ihtsdo.concept.component.refsetmember.cidCidStr;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.dto.concept.component.refset.cidcidstr.TkRefsetCidCidStrRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidCidStrRevision extends RefsetRevision<CidCidStrRevision, CidCidStrMember>
	implements I_ExtendByRefPartCidCidString {

	private int c1Nid;
	private int c2Nid;
	private String strValue;
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid:" + this.c1Nid);
        buf.append(" c2Nid:" + this.c2Nid);
        buf.append(" strValue:" + "'" + this.strValue + "'");
        buf.append(super.toString());
        return buf.toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (CidCidStrRevision.class.isAssignableFrom(obj.getClass())) {
            CidCidStrRevision another = (CidCidStrRevision) obj;
            return this.c1Nid == another.c1Nid && 
        		this.c2Nid == another.c2Nid &&
        		this.strValue.equals(another.strValue) &&
        		super.equals(obj);
        }
        return false;
    }

    public CidCidStrRevision(int statusNid, int pathNid,
			long time, 
			CidCidStrMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		c2Nid = primoridalMember.getC2Nid();
		strValue = primoridalMember.getStringValue();
	}

	public CidCidStrRevision(int statusAtPositionNid, 
			CidCidStrMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		c2Nid = primoridalMember.getC2Nid();
		strValue = primoridalMember.getStringValue();
	}

	protected CidCidStrRevision(int statusNid, int pathNid, long time, 
			CidCidStrRevision another) {
		super(statusNid, pathNid, time, another.primordialComponent);
		c1Nid = another.c1Nid;
		c2Nid = another.c2Nid;
		strValue = another.strValue;
	}

	@Override
	public CidCidStrRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathId() == pathNid) {
            this.setStatusId(statusNid);
            return this;
        }
        CidCidStrRevision newR = new CidCidStrRevision(statusNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
	}


    @Override
    public CidCidStrRevision makeAnalog() {
         return new CidCidStrRevision(getStatusId(), getPathId(), getTime(), this);
    }

	public CidCidStrRevision(TupleInput input, 
			CidCidStrMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		c2Nid = input.readInt();
		strValue = input.readString();
	}

	public CidCidStrRevision(TkRefsetCidCidStrRevision eVersion,
			CidCidStrMember member) {
		super(eVersion, member);
		c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
		c2Nid = Bdb.uuidToNid(eVersion.getC2Uuid());
		strValue = eVersion.getStringValue();
	}

    public CidCidStrRevision() {
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
	public String getStringValue() {
		return strValue;
	}

	@Override
	public void setStringValue(String value) {
		this.strValue = value;
        modified();
	}

	public int getC1Nid() {
		return c1Nid;
	}

	public void setC1Nid(int c1Nid) {
		this.c1Nid = c1Nid;
        modified();
	}

	public int getC2Nid() {
		return c2Nid;
	}

	public void setC2Nid(int c2Nid) {
		this.c2Nid = c2Nid;
        modified();
	}

	public String getStrValue() {
		return strValue;
	}

	public void setStrValue(String strValue) {
		this.strValue = strValue;
        modified();
	}

	@Override
	public int getC1id() {
		return c1Nid;
	}

	@Override
	public int getC2id() {
		return c2Nid;
	}

	@Override
	public void setC1id(int c1id) {
		this.c1Nid = c1id;
        modified();
	}

	@Override
	public void setC2id(int c2id) {
		this.c2Nid = c2id;
        modified();
	}

	@Override
	public CidCidStrRevision duplicate() {
		throw new UnsupportedOperationException();
	}
    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeInt(c1Nid);
        output.writeInt(c2Nid);
        output.writeString(strValue);
    }
    
    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(4);
        variableNids.add(getC1id());
        variableNids.add(getC2id());
        return variableNids;
    }

}
