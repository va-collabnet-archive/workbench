package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;

public class ERefsetCidFloatVersion extends EVersion {

    public static final long serialVersionUID = 1;

    protected UUID c1Uuid;
    protected float floatValue;

    public ERefsetCidFloatVersion(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public ERefsetCidFloatVersion(I_ThinExtByRefPartMeasurement part) throws TerminologyException, IOException {
        c1Uuid = nidToUuid(part.getUnitsOfMeasureId());
        floatValue = (float) part.getMeasurementValue();
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    @Override
    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        c1Uuid = new UUID(in.readLong(), in.readLong());
        floatValue = in.readFloat();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(c1Uuid.getMostSignificantBits());
        out.writeLong(c1Uuid.getLeastSignificantBits());
        out.writeFloat(floatValue);
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    public UUID getC1Uuid() {
        return c1Uuid;
    }

    public void setC1Uuid(UUID c1Uuid) {
        this.c1Uuid = c1Uuid;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(super.toString());

        buff.append(" c1Uuid:");
        buff.append(this.c1Uuid);
        buff.append(" floatValue:");
        buff.append(this.floatValue);
        buff.append("; ");

        return buff.toString();
    }
    
    /**
     * Returns a hash code for this <code>ERefsetCidFloatVersion</code>.
     * 
     * @return a hash code value for this <tt>ERefsetCidFloatVersion</tt>.
     */
    public int hashCode() {
        return HashFunction.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time });
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidFloatVersion</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetCidFloatVersion</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (ERefsetCidFloatVersion.class.isAssignableFrom(obj.getClass())) {
            ERefsetCidFloatVersion another = (ERefsetCidFloatVersion) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare c1Uuid
            if (!this.c1Uuid.equals(another.c1Uuid)) {
                return false;
            }
            // Compare floatValue
            if (this.floatValue != another.floatValue) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }
}
