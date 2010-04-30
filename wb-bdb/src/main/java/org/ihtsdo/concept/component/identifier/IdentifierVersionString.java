package org.ihtsdo.concept.component.identifier;

import org.dwfa.ace.api.I_IdPart;
import org.ihtsdo.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.etypes.EIdentifierString;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVersionString extends IdentifierVersion {

    private String stringDenotation;

    public IdentifierVersionString(TupleInput input) {
        super(input);
        stringDenotation = input.readString();
    }

    public IdentifierVersionString(EIdentifierString idv) {
        super(idv);
        stringDenotation = idv.getDenotation();
    }

    public IdentifierVersionString() {
        super();
    }

    public IdentifierVersionString(IdentifierVersionString another, int statusNid, int pathNid, long time) {
        super(statusNid, pathNid, time);
        stringDenotation = (String) another.getDenotation();
    }

    @Override
    public IDENTIFIER_PART_TYPES getType() {
        return IDENTIFIER_PART_TYPES.STRING;
    }

    @Override
    protected void writeSourceIdToBdb(TupleOutput output) {
        output.writeString(stringDenotation);
    }

    @Override
    public Object getDenotation() {
        return stringDenotation;
    }

    @Override
    public void setDenotation(Object sourceDenotation) {
        stringDenotation = (String) sourceDenotation;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(this.getClass().getSimpleName() + ": ");
        buf.append("denotation:" + "'" + this.stringDenotation + "'");
        buf.append(" ");
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (IdentifierVersionString.class.isAssignableFrom(obj.getClass())) {
            IdentifierVersionString another = (IdentifierVersionString) obj;
            return this.getSapNid() == another.getSapNid();
        }
        return false;
    }

    @Override
    public I_IdPart makeIdAnalog(int statusNid, int pathNid, long time) {
        return new IdentifierVersionString(this, statusNid, pathNid, time);
    }
}
