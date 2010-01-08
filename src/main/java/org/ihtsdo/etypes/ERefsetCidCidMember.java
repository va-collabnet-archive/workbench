package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetCidCidMember extends ERefset<ERefsetCidCidVersion> {

    public static final long serialVersionUID = 1;

    protected UUID c1Uuid;
    protected UUID c2Uuid;

    public ERefsetCidCidMember(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public ERefsetCidCidMember(I_ThinExtByRefVersioned m) throws TerminologyException, IOException {
        convert(nidToIdentifier(m.getMemberId()));
        int partCount = m.getMutableParts().size();
        refsetUuid = nidToUuid(m.getRefsetId());
        componentUuid = nidToUuid(m.getComponentId());

        I_ThinExtByRefPartConceptConcept part = (I_ThinExtByRefPartConceptConcept) m.getMutableParts().get(0);
        c1Uuid = nidToUuid(part.getC1id());
        c2Uuid = nidToUuid(part.getC2id());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
        if (partCount > 1) {
            extraVersions = new ArrayList<ERefsetCidCidVersion>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                extraVersions.add(new ERefsetCidCidVersion((I_ThinExtByRefPartConceptConcept) m.getMutableParts()
                    .get(i)));
            }
        }
    }

    @Override
    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        c1Uuid = new UUID(in.readLong(), in.readLong());
        c2Uuid = new UUID(in.readLong(), in.readLong());
        int versionSize = in.readInt();
        if (versionSize > 0) {
            extraVersions = new ArrayList<ERefsetCidCidVersion>(versionSize);
            for (int i = 0; i < versionSize; i++) {
                extraVersions.add(new ERefsetCidCidVersion(in));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(c1Uuid.getMostSignificantBits());
        out.writeLong(c1Uuid.getLeastSignificantBits());
        out.writeLong(c2Uuid.getMostSignificantBits());
        out.writeLong(c2Uuid.getLeastSignificantBits());
        if (extraVersions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(extraVersions.size());
            for (ERefsetCidCidVersion rmv : extraVersions) {
                rmv.writeExternal(out);
            }
        }
    }

    @Override
    public REFSET_TYPES getType() {
        return REFSET_TYPES.CID_CID;
    }

    public List<ERefsetCidCidVersion> getExtraVersionsList() {
        return extraVersions;
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
        buff.append("; ");

        return buff.toString();
    }
}
