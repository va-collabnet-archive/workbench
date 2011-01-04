package org.ihtsdo.concept.component.attributes;

import java.util.Collection;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.conattr.ConAttrAnalogBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.ext.I_ConceptualizeExternally;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ConceptAttributesRevision extends Revision<ConceptAttributesRevision, ConceptAttributes>
        implements I_ConceptAttributePart<ConceptAttributesRevision>, ConAttrAnalogBI<ConceptAttributesRevision> {

    private boolean defined = false;

    public ConceptAttributesRevision(int statusAtPositionNid, ConceptAttributes primoridalMember) {
        super(statusAtPositionNid, primoridalMember);
    }

    public ConceptAttributesRevision(I_ConceptualizeExternally another, ConceptAttributes primoridalMember) {
        super(Bdb.uuidToNid(another.getStatusUuid()),
                Bdb.uuidToNid(another.getAuthorUuid()),
                Bdb.uuidToNid(another.getPathUuid()), another.getTime(),
                primoridalMember);
        this.defined = another.isDefined();
    }

    public ConceptAttributesRevision(I_ConceptAttributePart another, int statusNid, int authorNid, int pathNid, long time,
            ConceptAttributes primoridalMember) {
        super(statusNid, authorNid, pathNid, time, primoridalMember);
        this.defined = another.isDefined();
    }

    public ConceptAttributesRevision(I_ConceptAttributePart another, ConceptAttributes primoridalMember) {
        super(another.getStatusNid(), another.getAuthorNid(), another.getPathNid(), another.getTime(), primoridalMember);
        this.defined = another.isDefined();
    }

    public ConceptAttributesRevision(int statusNid, int authorNid, int pathNid, long time, ConceptAttributes primoridalMember) {
        super(statusNid, authorNid, pathNid, time, primoridalMember);
    }

    public ConceptAttributesRevision(TupleInput input, ConceptAttributes primoridalMember) {
        super(input, primoridalMember);
        defined = input.readBoolean();
    }

    @Override
    public ConceptAttributesRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
        try {
            return new ConceptAttributesRevision(this, statusNid,
                    Terms.get().getActiveAceFrameConfig().getDbConfig().getUserConcept().getConceptNid(),
                    pathNid, time, this.primordialComponent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConceptAttributesRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            this.setAuthorNid(authorNid);
            return this;
        }
        try {
            return new ConceptAttributesRevision(this, statusNid,
                    authorNid,
                    pathNid, time, this.primordialComponent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isDefined() {
        return defined;
    }

    @Override
    public void setDefined(boolean defined) {
        this.defined = defined;
        modified();
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        return new ArrayIntList(2);
    }

    @Override
    public ConceptAttributesRevision duplicate() {
        return new ConceptAttributesRevision(this, this.primordialComponent);
    }

    @Override
    protected void writeFieldsToBdb(TupleOutput output) {
        output.writeBoolean(defined);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append("conceptAttributes: " + this.primordialComponent.nid);
        buf.append(" defined: " + this.defined);
        buf.append(super.toString());
        return buf.toString();
    }

    // TODO Verify this is a correct implementation
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (ConceptAttributesRevision.class.isAssignableFrom(obj.getClass())) {
            ConceptAttributesRevision another = (ConceptAttributesRevision) obj;
            if (this.sapNid == another.sapNid) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ConceptAttributes.Version getVersion(ViewCoordinate c)
            throws ContraditionException {
        return primordialComponent.getVersion(c);
    }

    @Override
    public Collection<ConceptAttributes.Version> getVersions(
            ViewCoordinate c) {
        return primordialComponent.getVersions(c);
    }

    @Override
    public Collection<ConceptAttributes.Version> getVersions() {
        return ((ConceptAttributes) primordialComponent).getVersions();
    }

    @Override
    public String toUserString() {
        StringBuffer buf = new StringBuffer();
        buf.append("concept ");
        if (defined) {
            buf.append("is fully defined");
        }
        if (defined) {
            buf.append("is primitive");
        }
        return buf.toString();
    }
    
    
}
