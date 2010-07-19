package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartLong;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetLongMember extends ERefsetMember<ERefsetLongRevision> {

    public static final long serialVersionUID = 1;

    protected long longValue;

    public ERefsetLongMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public ERefsetLongMember(I_ExtendByRef m) throws TerminologyException, IOException {
        if (I_Identify.class.isAssignableFrom(m.getClass())) {
            convert((I_Identify) m);
        } else {
            convert(nidToIdentifier(m.getMemberId()));
        }
        int partCount = m.getMutableParts().size();
        refsetUuid = nidToUuid(m.getRefsetId());
        componentUuid = nidToUuid(m.getComponentId());

        I_ExtendByRefPartLong part = (I_ExtendByRefPartLong) m.getMutableParts().get(0);
        longValue = part.getLongValue();
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
        if (partCount > 1) {
            revisions = new ArrayList<ERefsetLongRevision>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                revisions.add(new ERefsetLongRevision((I_ExtendByRefPartLong) m.getMutableParts().get(i)));
            }
        }
    }

    public ERefsetLongMember() {
        super();
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        longValue = in.readLong();
        int versionSize = in.readInt();
        if (versionSize > 0) {
            revisions = new ArrayList<ERefsetLongRevision>(versionSize);
            for (int i = 0; i < versionSize; i++) {
                revisions.add(new ERefsetLongRevision(in, dataVersion));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(longValue);
        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());
            for (ERefsetLongRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    @Override
    public REFSET_TYPES getType() {
        return REFSET_TYPES.INT;
    }

    public List<ERefsetLongRevision> getRevisionList() {
        return revisions;
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
        buff.append(" longValue:");
        buff.append(this.longValue);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    
    /**
     * Returns a hash code for this <code>ERefsetLongMember</code>.
     * 
     * @return a hash code value for this <tt>ERefsetLongMember</tt>.
     */
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetLongMember</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetLongMember</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (ERefsetLongMember.class.isAssignableFrom(obj.getClass())) {
            ERefsetLongMember another = (ERefsetLongMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare longValue
            if (this.longValue != another.longValue) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }

}
