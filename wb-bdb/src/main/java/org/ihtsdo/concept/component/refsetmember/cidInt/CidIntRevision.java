package org.ihtsdo.concept.component.refsetmember.cidInt;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_int.RefexCnidIntAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.cidint.TkRefsetCidIntRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidIntRevision
        extends RefsetRevision<CidIntRevision, CidIntMember>
        implements I_ExtendByRefPartCidInt<CidIntRevision>, RefexCnidIntAnalogBI<CidIntRevision> {

    private int c1Nid;
    private int intValue;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid: ");
        ConceptComponent.addNidToBuffer(buf, c1Nid);
        buf.append(" intValue:" + this.intValue);
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (CidIntRevision.class.isAssignableFrom(obj.getClass())) {
            CidIntRevision another = (CidIntRevision) obj;
            return this.c1Nid == another.c1Nid
                    && this.intValue == another.intValue
                    && super.equals(obj);
        }
        return false;
    }

    protected CidIntRevision(int statusNid, int pathNid, long time,
            CidIntMember primoridalMember) {
        super(statusNid, pathNid, time,
                primoridalMember);
        c1Nid = primoridalMember.getC1Nid();
        intValue = primoridalMember.getIntValue();
    }

    protected CidIntRevision(int statusNid, int authorNid, int pathNid, long time,
            CidIntMember primoridalMember) {
        super(statusNid, authorNid, pathNid, time,
                primoridalMember);
        c1Nid = primoridalMember.getC1Nid();
        intValue = primoridalMember.getIntValue();
    }

    protected CidIntRevision(int statusAtPositionNid,
            CidIntMember primoridalMember) {
        super(statusAtPositionNid,
                primoridalMember);
        c1Nid = primoridalMember.getC1Nid();
        intValue = primoridalMember.getIntValue();
    }

    protected CidIntRevision(int statusNid, int pathNid, long time,
            CidIntRevision another) {
        super(statusNid, pathNid, time, another.primordialComponent);
        c1Nid = another.c1Nid;
        intValue = another.intValue;
    }

    protected CidIntRevision(int statusNid, int authorNid, int pathNid, long time,
            CidIntRevision another) {
        super(statusNid, authorNid, pathNid, time, another.primordialComponent);
        c1Nid = another.c1Nid;
        intValue = another.intValue;
    }

    @Override
    public CidIntRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        CidIntRevision newR = new CidIntRevision(statusNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
    }

    @Override
    public CidIntRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        CidIntRevision newR = new CidIntRevision(statusNid, authorNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
    }

    @Override
    public CidIntRevision makeAnalog() {
        return new CidIntRevision(getStatusNid(), getPathNid(), getTime(), this);
    }

    public CidIntRevision(TupleInput input,
            CidIntMember primoridalMember) {
        super(input, primoridalMember);
        c1Nid = input.readInt();
        intValue = input.readInt();
    }

    public CidIntRevision(TkRefsetCidIntRevision eVersion,
            CidIntMember member) {
        super(eVersion, member);
        c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
        intValue = eVersion.getIntValue();
    }

    public CidIntRevision() {
        super();
    }

    @Override
    public UniversalAceExtByRefPart getUniversalPart()
            throws TerminologyException, IOException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public I_ExtendByRefPart<CidIntRevision> makePromotionPart(PathBI promotionPath) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIntValue() {
        return intValue;
    }

    @Override
    public void setIntValue(int intValue) {
        this.intValue = intValue;
        modified();
        modified();
    }

    public int getC1Nid() {
        return c1Nid;
    }

    public void setC1Nid(int c1Nid) {
        this.c1Nid = c1Nid;
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
        output.writeInt(intValue);
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(3);
        variableNids.add(getC1Nid());
        return variableNids;
    }

    @Override
    public CidIntMember.Version getVersion(ViewCoordinate c)
            throws ContraditionException {
        return (CidIntMember.Version) ((CidIntMember) primordialComponent).getVersion(c);
    }

    @Override
    public Collection<CidIntMember.Version> getVersions() {
        return ((CidIntMember) primordialComponent).getVersions();
    }

    @Override
    public Collection<? extends RefexVersionBI<CidIntRevision>> getVersions(
            ViewCoordinate c) {
        return ((CidIntMember) primordialComponent).getVersions(c);
    }

	@Override
	public void setCnid1(int cnid) throws PropertyVetoException {
		this.c1Nid = cnid;
        modified();
	}

	public int getCnid1() {
		return c1Nid;
	}

	@Override
	public void setInt1(int l) throws PropertyVetoException {
		this.intValue = l;
        modified();
	}

	@Override
	public int getInt1() {
		return intValue;
	}
	
    
	protected TK_REFSET_TYPE getTkRefsetType() {
		return TK_REFSET_TYPE.CID_INT;
	}

	protected void addSpecProperties(RefexAmendmentSpec rcs) {
		rcs.with(RefexProperty.CNID1, getCnid1());
		rcs.with(RefexProperty.INTEGER1, getInt1());
	}

	@Override
	public int getPartsHashCode() {
		return HashFunction.hashCode(new int[]{ getC1id(), getIntValue()});
	}
}
