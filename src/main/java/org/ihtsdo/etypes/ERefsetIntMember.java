package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetIntMember extends ERefset<ERefsetIntVersion> {

    public static final long serialVersionUID = 1;

    protected int intValue;

    protected List<ERefsetIntVersion> extraVersions;

    public ERefsetIntMember(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public ERefsetIntMember(I_ThinExtByRefVersioned m) throws TerminologyException, IOException {
        convert(nidToIdentifier(m.getMemberId()));
        int partCount = m.getMutableParts().size();
        refsetUuid = nidToUuid(m.getRefsetId());
        componentUuid = nidToUuid(m.getComponentId());

        I_ThinExtByRefPartInteger part = (I_ThinExtByRefPartInteger) m.getMutableParts().get(0);
        intValue = part.getIntValue();
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
        if (partCount > 1) {
            extraVersions = new ArrayList<ERefsetIntVersion>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                extraVersions.add(new ERefsetIntVersion((I_ThinExtByRefPartInteger) m.getMutableParts().get(i)));
            }
        }
    }

    @Override
    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        intValue = in.readInt();
        int versionSize = in.readInt();
        if (versionSize > 0) {
            extraVersions = new ArrayList<ERefsetIntVersion>(versionSize);
            for (int i = 0; i < versionSize; i++) {
                extraVersions.add(new ERefsetIntVersion(in));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(intValue);
        if (extraVersions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(extraVersions.size());
            for (ERefsetIntVersion rmv : extraVersions) {
                rmv.writeExternal(out);
            }
        }
    }

    @Override
    public REFSET_TYPES getType() {
        return REFSET_TYPES.INT;
    }

    public List<ERefsetIntVersion> getExtraVersionsList() {
        return extraVersions;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(super.toString());

        buff.append(", intValue:");
        buff.append(this.intValue);
        buff.append(", extraVersions:");
        buff.append(this.extraVersions);
        buff.append("; ");

        return buff.toString();
    }
}
