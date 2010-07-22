package org.ihtsdo.tk.concept.component.refset.cidcidcid;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.concept.component.TkRevision;

public class TkRefsetCidCidCidRevision extends TkRevision {

    public static final long serialVersionUID = 1;

    protected UUID c1Uuid;
    protected UUID c2Uuid;
    protected UUID c3Uuid;

    public TkRefsetCidCidCidRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefsetCidCidCidRevision() {
        super();
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
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
        buff.append(" c1Uuid:");
        buff.append(this.c1Uuid);
        buff.append(" c2Uuid:");
        buff.append(this.c2Uuid);
        buff.append(" c3Uuid:");
        buff.append(this.c3Uuid);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidCidCidVersion</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetCidCidCidVersion</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkRefsetCidCidCidRevision.class.isAssignableFrom(obj.getClass())) {
            TkRefsetCidCidCidRevision another = (TkRefsetCidCidCidRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare c1Uuid
            if (!this.c1Uuid.equals(another.c1Uuid)) {
                return false;
            }
            // Compare c2Uuid
            if (!this.c2Uuid.equals(another.c2Uuid)) {
                return false;
            }
            // Compare c3Uuid
            if (!this.c3Uuid.equals(another.c3Uuid)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }
}
