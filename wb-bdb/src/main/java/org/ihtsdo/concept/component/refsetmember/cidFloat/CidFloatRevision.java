package org.ihtsdo.concept.component.refsetmember.cidFloat;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_float.RefexCnidFloatAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.cidflt.TkRefsetCidFloatRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CidFloatRevision extends RefsetRevision<CidFloatRevision, CidFloatMember> 
	implements RefexCnidFloatAnalogBI<CidFloatRevision> {

    private int c1Nid;
    private float floatValue;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" c1Nid: ");
        ConceptComponent.addNidToBuffer(buf, c1Nid);
        buf.append(" floatValue:" + this.floatValue);
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (CidFloatRevision.class.isAssignableFrom(obj.getClass())) {
            CidFloatRevision another = (CidFloatRevision) obj;
            return this.c1Nid == another.c1Nid
                    && this.floatValue == another.floatValue
                    && super.equals(obj);
        }
        return false;
    }

    public CidFloatRevision(int statusNid, int pathNid, long time,
            CidFloatMember primoridalMember) {
        super(statusNid, pathNid, time,
                primoridalMember);
        c1Nid = primoridalMember.getC1Nid();
        floatValue = primoridalMember.getFloatValue();
    }

    public CidFloatRevision(int statusNid, int authorNid, int pathNid, long time,
            CidFloatMember primoridalMember) {
        super(statusNid, authorNid, pathNid, time,
                primoridalMember);
        c1Nid = primoridalMember.getC1Nid();
        floatValue = primoridalMember.getFloatValue();
    }

    public CidFloatRevision(int statusAtPositionNid,
            CidFloatMember primoridalMember) {
        super(statusAtPositionNid,
                primoridalMember);
        c1Nid = primoridalMember.getC1Nid();
        floatValue = primoridalMember.getFloatValue();
    }

    protected CidFloatRevision(int statusNid, int pathNid, long time,
            CidFloatRevision another) {
        super(statusNid, pathNid, time, another.primordialComponent);
        c1Nid = another.c1Nid;
        floatValue = another.floatValue;
    }

    protected CidFloatRevision(int statusNid, int authorNid, int pathNid, long time,
            CidFloatRevision another) {
        super(statusNid, authorNid, pathNid, time, another.primordialComponent);
        c1Nid = another.c1Nid;
        floatValue = another.floatValue;
    }

    @Override
    public CidFloatRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        CidFloatRevision newR = new CidFloatRevision(statusNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
    }

    @Override
    public CidFloatRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        CidFloatRevision newR = new CidFloatRevision(statusNid, authorNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
    }

    @Override
    public CidFloatRevision makeAnalog() {
        return new CidFloatRevision(getStatusNid(), getPathNid(), getTime(), this);
    }

    public CidFloatRevision(TupleInput input,
            CidFloatMember primoridalMember) {
        super(input, primoridalMember);
        c1Nid = input.readInt();
        floatValue = input.readFloat();
    }

    public CidFloatRevision(TkRefsetCidFloatRevision eVersion,
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
    public I_ExtendByRefPart<CidFloatRevision> makePromotionPart(PathBI promotionPath) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public int getC1Nid() {
        return c1Nid;
    }

    public void setC1Nid(int c1Nid) {
        this.c1Nid = c1Nid;
        modified();
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
        modified();
    }

    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeInt(c1Nid);
        output.writeFloat(floatValue);
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList variableNids = new ArrayIntList(3);
        variableNids.add(getC1Nid());
        return variableNids;
    }

    @Override
    public CidFloatMember.Version getVersion(ViewCoordinate c)
            throws ContraditionException {
        return (CidFloatMember.Version) ((CidFloatMember) primordialComponent).getVersion(c);
    }

    @Override
    public Collection<CidFloatMember.Version> getVersions() {
        return ((CidFloatMember) primordialComponent).getVersions();
    }

    @Override
    public Collection<? extends RefexVersionBI<CidFloatRevision>> getVersions(
            ViewCoordinate c) {
        return ((CidFloatMember) primordialComponent).getVersions(c);
    }
    

	@Override
	public void setCnid1(int cnid) throws PropertyVetoException {
		this.c1Nid = cnid;
		modified();
	}

	@Override
	public int getCnid1() {
		return c1Nid;
	}

	@Override
	public void setFloat1(float f) throws PropertyVetoException {
		this.floatValue = f;
		modified();
	}

	@Override
	public float getFloat1() {
		return this.floatValue;
	}
	protected TK_REFSET_TYPE getTkRefsetType() {
		return TK_REFSET_TYPE.CID_FLOAT;
	}

	protected void addSpecProperties(RefexAmendmentSpec rcs) {
		rcs.with(RefexProperty.CNID1, getCnid1());
		rcs.with(RefexProperty.FLOAT1, getFloat1());
	}

}
