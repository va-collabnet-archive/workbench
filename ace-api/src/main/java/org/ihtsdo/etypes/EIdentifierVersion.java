package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import org.dwfa.util.HashFunction;
import org.ihtsdo.etypes.EComponent.IDENTIFIER_PART_TYPES;

public abstract class EIdentifierVersion extends EVersion {

    public static final long serialVersionUID = 1;
    protected UUID authorityUuid;

    public EIdentifierVersion(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public EIdentifierVersion() {
        super();
    }

    @Override
    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        authorityUuid = new UUID(in.readLong(), in.readLong());
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(authorityUuid.getMostSignificantBits());
        out.writeLong(authorityUuid.getLeastSignificantBits());
        writeDenotation(out);
    }

    public abstract void writeDenotation(DataOutput out) throws IOException;

    public abstract Object getDenotation();

    public abstract IDENTIFIER_PART_TYPES getIdType();

    public UUID getAuthorityUuid() {
        return authorityUuid;
    }

    public void setAuthorityUuid(UUID authorityUuid) {
        this.authorityUuid = authorityUuid;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append(super.toString());

        buff.append(" authorityUuid:");
        buff.append(this.authorityUuid);
        buff.append("; ");

        return buff.toString();
    }
    
    /**
     * Returns a hash code for this <code>EIdentifierVersion</code>.
     * 
     * @return a hash code value for this <tt>EIdentifierVersion</tt>.
     */
    public int hashCode() {
        return HashFunction.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time });
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EIdentifierVersion</tt> object, and contains the same values, field by field, 
     * as this <tt>EIdentifierVersion</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (EIdentifierVersion.class.isAssignableFrom(obj.getClass())) {
            EIdentifierVersion another = (EIdentifierVersion) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare authorityUuid
            if (!this.authorityUuid.equals(another.authorityUuid)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }
}
