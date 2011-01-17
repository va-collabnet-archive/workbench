package org.ihtsdo.concept.component.refsetmember.cidCid;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.concept.component.refsetmember.cidCid.CidCidMember.Version;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid.RefexCnidCnidAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.cidcid.TkRefsetCidCidRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidCidRevision extends RefsetRevision<CidCidRevision, CidCidMember> 
	implements I_ExtendByRefPartCidCid<CidCidRevision>, RefexCnidCnidAnalogBI<CidCidRevision> {

	private int c1Nid;
	private int c2Nid;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();  
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid: ");
        ConceptComponent.addNidToBuffer(buf, c1Nid);
        buf.append(" c2Nid: ");
        ConceptComponent.addNidToBuffer(buf, c2Nid);
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
                return super.equals(obj);
            }
        }
        return false;
    }

    public CidCidRevision(int statusNid, int pathNid, long time, 
			CidCidMember primoridalMember) {
		super(statusNid, pathNid, time, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		c2Nid = primoridalMember.getC2Nid();
	}

    public CidCidRevision(int statusNid, int authorNid, int pathNid, long time, 
			CidCidMember primoridalMember) {
		super(statusNid, authorNid, pathNid, time, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		c2Nid = primoridalMember.getC2Nid();
	}

	public CidCidRevision(int statusAtPositionNid, 
			CidCidMember primoridalMember) {
		super(statusAtPositionNid, 
				primoridalMember);
		c1Nid = primoridalMember.getC1Nid();
		c2Nid = primoridalMember.getC2Nid();
	}
	
	protected CidCidRevision(int statusNid, int pathNid, long time, 
			CidCidRevision another) {
		super(statusNid, pathNid, time, another.primordialComponent);
		c1Nid = another.c1Nid;
		c2Nid = another.c2Nid;
	}

	protected CidCidRevision(int statusNid, int authorNid, int pathNid, long time, 
			CidCidRevision another) {
		super(statusNid, authorNid, pathNid, time, another.primordialComponent);
		c1Nid = another.c1Nid;
		c2Nid = another.c2Nid;
	}


	@Override
	public CidCidRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        CidCidRevision newR = new CidCidRevision(statusNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
	}

	@Override
	public CidCidRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        CidCidRevision newR = new CidCidRevision(statusNid, authorNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
	}

    @Override
    public CidCidRevision makeAnalog() {
         return  new CidCidRevision(getStatusNid(), getPathNid(), getTime(), this);
    }

	public CidCidRevision(TupleInput input, 
			CidCidMember primoridalMember) {
		super(input, primoridalMember);
		c1Nid = input.readInt();
		c2Nid = input.readInt();
	}

	public CidCidRevision(TkRefsetCidCidRevision eVersion,
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
	public I_ExtendByRefPart<CidCidRevision> makePromotionPart(PathBI promotionPath) {
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
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeInt(c1Nid);
        output.writeInt(c2Nid);
    }
    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(4);
        variableNids.add(getC1id());
        variableNids.add(getC2id());
        return variableNids;
    }

    
    @Override
    public CidCidMember.Version getVersion(ViewCoordinate c)
            throws ContraditionException {
        return (Version) ((CidCidMember) primordialComponent).getVersion(c);
    }

    @Override
    public Collection<CidCidMember.Version> getVersions() {
        return ((CidCidMember) primordialComponent).getVersions();
    }

    @Override
    public Collection<? extends RefexVersionBI<CidCidRevision>> getVersions(
            ViewCoordinate c) {
        return ((CidCidMember) primordialComponent).getVersions(c);
    }


	@Override
	public void setCnid1(int cnid) throws PropertyVetoException {
		this.c1Nid = cnid;
        modified();
	}


	@Override
	public void setCnid2(int cnid) throws PropertyVetoException {
		this.c2Nid = cnid;
        modified();
	}

	public int getCnid1() {
		return c1Nid;
	}
	public int getCnid2() {
		return c2Nid;
	}
	protected TK_REFSET_TYPE getTkRefsetType() {
		return TK_REFSET_TYPE.CID_CID;
	}

	protected void addSpecProperties(RefexAmendmentSpec rcs) {
		rcs.with(RefexProperty.CNID1, getCnid1());
		rcs.with(RefexProperty.CNID2, getCnid2());
	}

	@Override
	public int getPartsHashCode() {
		return HashFunction.hashCode(new int[]{ getC1id(), getC2id()});
	}

}
