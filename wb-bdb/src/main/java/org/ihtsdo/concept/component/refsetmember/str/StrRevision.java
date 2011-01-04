package org.ihtsdo.concept.component.refsetmember.str;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_str.RefexStrAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class StrRevision extends RefsetRevision<StrRevision, StrMember>
        implements I_ExtendByRefPartStr<StrRevision>, RefexStrAnalogBI<StrRevision> {

    private String stringValue;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getClass().getSimpleName()).append(":{");
        buf.append(" stringValue:" + "'").append(this.stringValue).append("'");
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (StrRevision.class.isAssignableFrom(obj.getClass())) {
            StrRevision another = (StrRevision) obj;
            return stringValue.equals(another.stringValue)
                    && super.equals(obj);
        }
        return false;
    }

    public StrRevision(int statusNid, int pathNid, long time,
            StrMember another) {
        super(statusNid, pathNid, time,
                another);
        stringValue = another.getStringValue();

    }

    public StrRevision(int statusNid, int authorNid, int pathNid, long time,
            StrMember another) {
        super(statusNid, authorNid, pathNid, time,
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

    protected StrRevision(int statusNid, int authorNid, int pathNid, long time,
            StrRevision another) {
        super(statusNid, authorNid, pathNid, time, another.primordialComponent);
        stringValue = another.stringValue;
    }

    @Override
    public StrRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        StrRevision newR = new StrRevision(statusNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
    }

    @Override
    public StrRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            this.setAuthorNid(authorNid);
            return this;
        }
        StrRevision newR = new StrRevision(statusNid, authorNid, pathNid, time, this);
        primordialComponent.addRevision(newR);
        return newR;
    }

    @Override
    public StrRevision makeAnalog() {
        return new StrRevision(getStatusNid(), getPathNid(), getTime(), this);
    }

    public StrRevision(TupleInput input,
            StrMember primoridalMember) {
        super(input, primoridalMember);
        stringValue = input.readString();
    }

    public StrRevision(TkRefsetStrRevision eVersion,
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
    public I_ExtendByRefPart<StrRevision> makePromotionPart(PathBI promotionPath) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
        modified();
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
    
        
    @Override
    public StrMember.Version getVersion(ViewCoordinate c)
            throws ContraditionException {
        return (StrMember.Version) ((StrMember) primordialComponent).getVersion(c);
    }

    @Override
    public Collection<StrMember.Version> getVersions() {
        return ((StrMember) primordialComponent).getVersions();
    }

    @Override
    public Collection<? extends RefexVersionBI<StrRevision>> getVersions(
            ViewCoordinate c) {
        return ((StrMember) primordialComponent).getVersions(c);
    }

	@Override
	public void setStr1(String str) throws PropertyVetoException {
		this.stringValue = str;
		modified();
	}

	@Override
	public String getStr1() {
		return stringValue;
	}
	protected TK_REFSET_TYPE getTkRefsetType() {
		return TK_REFSET_TYPE.STR;
	}

	protected void addSpecProperties(RefexAmendmentSpec rcs) {
		rcs.with(RefexProperty.STRING1, getStr1());
	}

}
