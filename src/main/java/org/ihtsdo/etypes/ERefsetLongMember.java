package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLong;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetLongMember extends ERefset<ERefsetLongVersion> {

    public static final long serialVersionUID = 1;

    protected long longValue;

    public ERefsetLongMember(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public ERefsetLongMember(I_ThinExtByRefVersioned m) throws TerminologyException, IOException {
        convert(nidToIdentifier(m.getMemberId()));
        int partCount = m.getMutableParts().size();
        refsetUuid = nidToUuid(m.getRefsetId());
        componentUuid = nidToUuid(m.getComponentId());

        I_ThinExtByRefPartLong part = (I_ThinExtByRefPartLong) m.getMutableParts().get(0);
        longValue = part.getLongValue();
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
        if (partCount > 1) {
            extraVersions = new ArrayList<ERefsetLongVersion>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                extraVersions.add(new ERefsetLongVersion((I_ThinExtByRefPartLong) m.getMutableParts().get(i)));
            }
        }
    }

    @Override
    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        longValue = in.readLong();
        int versionSize = in.readInt();
        if (versionSize > 0) {
            extraVersions = new ArrayList<ERefsetLongVersion>(versionSize);
            for (int i = 0; i < versionSize; i++) {
                extraVersions.add(new ERefsetLongVersion(in));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(longValue);
        if (extraVersions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(extraVersions.size());
            for (ERefsetLongVersion rmv : extraVersions) {
                rmv.writeExternal(out);
            }
        }
    }

    @Override
    public REFSET_TYPES getType() {
        return REFSET_TYPES.INT;
    }

    public List<ERefsetLongVersion> getExtraVersionsList() {
        return extraVersions;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setIntValue(long longValue) {
        this.longValue = longValue;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(super.toString());

        buff.append(", longValue:");
        buff.append(this.longValue);
        buff.append("; ");

        return buff.toString();
    }

}
