package org.ihtsdo.concept.component.attributes;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.I_ConceptualizeExternally;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ConceptAttributesRevision extends Revision<ConceptAttributesRevision, ConceptAttributes> implements
        I_ConceptAttributePart {

    private boolean defined = false;

    public ConceptAttributesRevision(int statusAtPositionNid, ConceptAttributes primoridalMember) {
        super(statusAtPositionNid, primoridalMember);
    }

    public ConceptAttributesRevision(I_ConceptualizeExternally another, ConceptAttributes primoridalMember) {
        super(Bdb.uuidToNid(another.getStatusUuid()), Bdb.uuidToNid(another.getPathUuid()), another.getTime(),
            primoridalMember);
        this.defined = another.isDefined();
    }

    public ConceptAttributesRevision(I_ConceptAttributePart another, int statusNid, int pathNid, long time,
            ConceptAttributes primoridalMember) {
        super(statusNid, pathNid, time, primoridalMember);
        this.defined = another.isDefined();
    }

    public ConceptAttributesRevision(I_ConceptAttributePart another, ConceptAttributes primoridalMember) {
        super(another.getStatusId(), another.getPathId(), another.getTime(), primoridalMember);
        this.defined = another.isDefined();
    }

    public ConceptAttributesRevision(int statusNid, int pathNid, long time, ConceptAttributes primoridalMember) {
        super(statusNid, pathNid, time, primoridalMember);
    }

    public ConceptAttributesRevision(TupleInput input, ConceptAttributes primoridalMember) {
        super(input, primoridalMember);
        defined = input.readBoolean();
    }

    @Override
    public ConceptAttributesRevision makeAnalog(int statusNid, int pathNid, long time) {
        return new ConceptAttributesRevision(this, statusNid, pathNid, time, this.primordialComponent);
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
        if (obj == null)
            return false;
        if (ConceptAttributesRevision.class.isAssignableFrom(obj.getClass())) {
            ConceptAttributesRevision another = (ConceptAttributesRevision) obj;
            if (this.sapNid == another.sapNid) {
                return true;
            }
        }
        return false;
    }


}
