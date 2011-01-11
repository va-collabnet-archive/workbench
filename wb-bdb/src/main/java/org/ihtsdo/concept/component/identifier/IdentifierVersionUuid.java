package org.ihtsdo.concept.component.identifier;

import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.ihtsdo.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierUuid;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVersionUuid extends IdentifierVersion {
    private long msb;
    private long lsb;

    public IdentifierVersionUuid(TupleInput input) {
        super(input);
        msb = input.readLong();
        lsb = input.readLong();
    }

    public IdentifierVersionUuid(TkIdentifierUuid idv) {
        super(idv);
        msb = idv.getDenotation().getMostSignificantBits();
        lsb = idv.getDenotation().getLeastSignificantBits();
    }

    public IdentifierVersionUuid() {
        super();
    }

    public IdentifierVersionUuid(IdentifierVersionUuid another, int statusNid, int authorNid, int pathNid, long time) {
        super(statusNid, authorNid, pathNid, time);
        msb = another.msb;
        lsb = another.lsb;
    }

    public IdentifierVersionUuid(int statusNid, int authorNid, int pathNid,
			long time) {
        super(statusNid, authorNid, pathNid, time);
	}

	@Override
    public IDENTIFIER_PART_TYPES getType() {
        return IDENTIFIER_PART_TYPES.UUID;
    }

    @Override
    protected void writeSourceIdToBdb(TupleOutput output) {
        output.writeLong(msb);
        output.writeLong(lsb);
    }

    public UUID getUuid() {
        return new UUID(msb, lsb);
    }

    @Override
    public Object getDenotation() {
        return new UUID(msb, lsb);
    }

    @Override
    public void setDenotation(Object sourceDenotation) {
        if (sourceDenotation instanceof UUID) {
            UUID uuid = (UUID) sourceDenotation;
            msb = uuid.getMostSignificantBits();
            lsb = uuid.getLeastSignificantBits();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(this.getClass().getSimpleName()).append(": ");
        buf.append("uuid:").append(getUuid());
        buf.append(" ");
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (IdentifierVersionUuid.class.isAssignableFrom(obj.getClass())) {
            IdentifierVersionUuid another = (IdentifierVersionUuid) obj;
            return this.msb == another.msb &&
                    this.lsb == another.lsb &&
                    super.equals(another);
        }
        return false;
    }

    @Override
    public I_IdPart makeIdAnalog(int statusNid, int authorNid, int pathNid, long time) {
        return new IdentifierVersionUuid(this, statusNid, authorNid, pathNid, time);
    }
}
