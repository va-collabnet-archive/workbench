package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartLong;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;

public class ERefsetLongRevision extends ERevision {

    public static final long serialVersionUID = 1;

    protected long longValue;

    public ERefsetLongRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public ERefsetLongRevision(I_ExtendByRefPartLong part) throws TerminologyException, IOException {
        longValue = part.getLongValue();
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetLongRevision() {
        super();
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        longValue = in.readLong();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(longValue);
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
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
     * Returns a hash code for this <code>ERefsetLongVersion</code>.
     * 
     * @return a hash code value for this <tt>ERefsetLongVersion</tt>.
     */
    public int hashCode() {
        return HashFunction.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time });
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetLongVersion</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetLongVersion</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (ERefsetLongRevision.class.isAssignableFrom(obj.getClass())) {
            ERefsetLongRevision another = (ERefsetLongRevision) obj;

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
