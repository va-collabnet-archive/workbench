package org.ihtsdo.concept.component.identifier;

import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.ihtsdo.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.EIdentifierUuid;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierUuid;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdentifierVersionUuid extends IdentifierVersion {
    private int uNid;

    public IdentifierVersionUuid(TupleInput input) {
        super(input);
        uNid = input.readInt();
    }

    public IdentifierVersionUuid(TkIdentifierUuid idv) {
        super(idv);
        uNid = Bdb.getUuidsToNidMap().getUNid(idv.getDenotation());
    }

    public IdentifierVersionUuid() {
        super();
    }

    public IdentifierVersionUuid(IdentifierVersionUuid another, int statusNid, int authorNid, int pathNid, long time) {
        super(statusNid, authorNid, pathNid, time);
        uNid = Bdb.getUuidsToNidMap().getUNid((UUID) another.getDenotation());
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
        output.writeInt(uNid);
    }

    public UUID getUuid() {
        return Bdb.getUuidDb().getUuid(uNid);
    }

    @Override
    public Object getDenotation() {
        return Bdb.getUuidDb().getUuid(uNid);
    }

    @Override
    public void setDenotation(Object sourceDenotation) {
        if (sourceDenotation instanceof UUID) {
            UUID uuid = (UUID) sourceDenotation;
            uNid = Bdb.getUuidsToNidMap().getUNid(uuid);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(this.getClass().getSimpleName() + ": ");
        buf.append("uuid:" + getUuid());
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
            return this.uNid == another.uNid && super.equals(another);
        }
        return false;
    }

    @Override
    public I_IdPart makeIdAnalog(int statusNid, int authorNid, int pathNid, long time) {
        return new IdentifierVersionUuid(this, statusNid, authorNid, pathNid, time);
    }
}
