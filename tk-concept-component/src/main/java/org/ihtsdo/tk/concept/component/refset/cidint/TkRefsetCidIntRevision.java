package org.ihtsdo.tk.concept.component.refset.cidint;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.concept.component.TkRevision;

public class TkRefsetCidIntRevision extends TkRevision {

    public static final long serialVersionUID = 1;

    protected UUID c1Uuid;
    protected int intValue;

    public TkRefsetCidIntRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefsetCidIntRevision() {
        super();
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        c1Uuid = new UUID(in.readLong(), in.readLong());
        intValue = in.readInt();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(c1Uuid.getMostSignificantBits());
        out.writeLong(c1Uuid.getLeastSignificantBits());
        out.writeInt(intValue);
    }

    public UUID getC1Uuid() {
        return c1Uuid;
    }

    public void setC1Uuid(UUID c1Uuid) {
        this.c1Uuid = c1Uuid;
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
        buff.append(" c1Uuid:");
        buff.append(this.c1Uuid);
        buff.append(" intValue:");
        buff.append(this.intValue);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidIntVersion</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetCidIntVersion</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkRefsetCidIntRevision.class.isAssignableFrom(obj.getClass())) {
            TkRefsetCidIntRevision another = (TkRefsetCidIntRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare c1Uuid
            if (!this.c1Uuid.equals(another.c1Uuid)) {
                return false;
            }
            // Compare intValue
            if (this.intValue != another.intValue) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }
}
