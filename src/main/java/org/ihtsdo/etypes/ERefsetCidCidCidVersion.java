package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.tapi.TerminologyException;

public class ERefsetCidCidCidVersion extends EVersion {

    public static final long serialVersionUID = 1;

    protected UUID c1Uuid;
    protected UUID c2Uuid;
    protected UUID c3Uuid;

    public ERefsetCidCidCidVersion(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public ERefsetCidCidCidVersion(I_ThinExtByRefPartConceptConceptConcept part) throws TerminologyException,
            IOException {
        c1Uuid = nidToUuid(part.getC1id());
        c2Uuid = nidToUuid(part.getC2id());
        c3Uuid = nidToUuid(part.getC3id());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    @Override
    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        c1Uuid = new UUID(in.readLong(), in.readLong());
        c2Uuid = new UUID(in.readLong(), in.readLong());
        c3Uuid = new UUID(in.readLong(), in.readLong());
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(c1Uuid.getMostSignificantBits());
        out.writeLong(c1Uuid.getLeastSignificantBits());
        out.writeLong(c2Uuid.getMostSignificantBits());
        out.writeLong(c2Uuid.getLeastSignificantBits());
        out.writeLong(c3Uuid.getMostSignificantBits());
        out.writeLong(c3Uuid.getLeastSignificantBits());
    }

    public UUID getC1Uuid() {
        return c1Uuid;
    }

    public void setC1Uuid(UUID c1Uuid) {
        this.c1Uuid = c1Uuid;
    }

    public UUID getC2Uuid() {
        return c2Uuid;
    }

    public void setC2Uuid(UUID c2Uuid) {
        this.c2Uuid = c2Uuid;
    }

    public UUID getC3Uuid() {
        return c3Uuid;
    }

    public void setC3Uuid(UUID c3Uuid) {
        this.c3Uuid = c3Uuid;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(super.toString());

        buff.append(", c1Uuid:");
        buff.append(this.c1Uuid);
        buff.append(", c2Uuid:");
        buff.append(this.c2Uuid);
        buff.append(", c3Uuid:");
        buff.append(this.c3Uuid);
        buff.append("; ");

        return buff.toString();
    }
}
