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

    public ERefsetIntMember() {
        super();
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
        buff.append(" intValue:");
        buff.append(this.intValue);
        buff.append(" extraVersions:");
        buff.append(this.extraVersions);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    
    /**
     * Returns a hash code for this <code>ERefsetIntMember</code>.
     * 
     * @return a hash code value for this <tt>ERefsetIntMember</tt>.
     */
    public int hashCode() {
        return this.primordialComponentUuid.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetIntMember</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetIntMember</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (ERefsetIntMember.class.isAssignableFrom(obj.getClass())) {
            ERefsetIntMember another = (ERefsetIntMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare intValue
            if (this.intValue != another.intValue) {
                return false;
            }
            // Compare extraVersions
            if (this.extraVersions == null) {
                if (another.extraVersions == null) { // Equal!
                } else if (another.extraVersions.size() == 0) { // Equal!
                } else {
                    return false;
                }
            } else if (!this.extraVersions.equals(another.extraVersions)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }
}
