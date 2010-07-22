package org.ihtsdo.tk.concept.component.refset;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.concept.component.TkComponent;
import org.ihtsdo.tk.concept.component.TkRevision;

public abstract class TkRefsetAbstractMember<V extends TkRevision> extends TkComponent<V> {

    public static final long serialVersionUID = 1;

    protected UUID refsetUuid;
    protected UUID componentUuid;

    public TkRefsetAbstractMember() {
        super();
    }

    public TkRefsetAbstractMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        refsetUuid = new UUID(in.readLong(), in.readLong());
        componentUuid = new UUID(in.readLong(), in.readLong());
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(refsetUuid.getMostSignificantBits());
        out.writeLong(refsetUuid.getLeastSignificantBits());
        out.writeLong(componentUuid.getMostSignificantBits());
        out.writeLong(componentUuid.getLeastSignificantBits());
    }

    public UUID getRefsetUuid() {
        return refsetUuid;
    }

    public void setRefsetUuid(UUID refsetUuid) {
        this.refsetUuid = refsetUuid;
    }

    public UUID getComponentUuid() {
        return componentUuid;
    }

    public void setComponentUuid(UUID componentUuid) {
        this.componentUuid = componentUuid;
    }

    public abstract TK_REFSET_TYPE getType();

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(" refsetUuid:");
        buff.append(this.refsetUuid);
        buff.append(" componentUuid:");
        buff.append(this.componentUuid);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }

    /**
     * Returns a hash code for this <code>ERefset</code>.
     * 
     * @return a hash code value for this <tt>ERefset</tt>.
     */
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefset</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefset</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkRefsetAbstractMember.class.isAssignableFrom(obj.getClass())) {
            TkRefsetAbstractMember<?> another = (TkRefsetAbstractMember<?>) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare refsetUuid
            if (!this.refsetUuid.equals(another.refsetUuid)) {
                return false;
            }
            // Compare componentUuid
            if (!this.componentUuid.equals(another.componentUuid)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }
}