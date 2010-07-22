package org.ihtsdo.tk.concept.component.refset.cidcidflt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.concept.component.TkRevision;

public class TkRefsetCidFloatRevision extends TkRevision {

    public static final long serialVersionUID = 1;

    protected UUID c1Uuid;
    protected float floatValue;

    public TkRefsetCidFloatRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefsetCidFloatRevision() {
        super();
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
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
        buff.append(" c1Uuid:");
        buff.append(this.c1Uuid);
        buff.append(" floatValue:");
        buff.append(this.floatValue);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
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
        if (TkRefsetCidFloatRevision.class.isAssignableFrom(obj.getClass())) {
            TkRefsetCidFloatRevision another = (TkRefsetCidFloatRevision) obj;

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
