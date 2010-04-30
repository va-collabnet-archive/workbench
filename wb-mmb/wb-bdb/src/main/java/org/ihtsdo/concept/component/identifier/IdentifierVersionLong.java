package org.ihtsdo.concept.component.identifier;

import org.dwfa.ace.api.I_IdPart;
import org.ihtsdo.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.etypes.EIdentifierLong;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVersionLong extends IdentifierVersion {
    private long longDenotation;

    public IdentifierVersionLong(TupleInput input) {
        super(input);
        longDenotation = input.readLong();
    }

    public IdentifierVersionLong(EIdentifierLong idv) {
        super(idv);
        longDenotation = idv.getDenotation();
    }

    public IdentifierVersionLong(IdentifierVersionLong another, int statusNid, int pathNid, long time) {
        super(statusNid, pathNid, time);
        longDenotation = (Long) another.getDenotation();
    }

    public IdentifierVersionLong() {
        super();
    }

    @Override
    public IDENTIFIER_PART_TYPES getType() {
        return IDENTIFIER_PART_TYPES.LONG;
    }

    @Override
    protected void writeSourceIdToBdb(TupleOutput output) {
        output.writeLong(longDenotation);
    }

    @Override
    public Object getDenotation() {
        return longDenotation;
    }

    @Override
    public void setDenotation(Object sourceDenotation) {
        longDenotation = (Long) sourceDenotation;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(this.getClass().getSimpleName() + ": ");
        buf.append("denotation:" + this.longDenotation);
        buf.append(" ");
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (IdentifierVersionLong.class.isAssignableFrom(obj.getClass())) {
            IdentifierVersionLong another = (IdentifierVersionLong) obj;
            return this.getSapNid() == another.getSapNid();
        }
        return false;
    }

    @Override
    public I_IdPart makeIdAnalog(int statusNid, int pathNid, long time) {
        return new IdentifierVersionLong(this, statusNid, pathNid, time);
    }
}
