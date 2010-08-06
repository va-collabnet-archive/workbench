package org.ihtsdo.tk.dto.concept.component.identifier;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.TkRevision;

public abstract class TkIdentifier extends TkRevision {

    public static final long serialVersionUID = 1;
    public UUID authorityUuid;

    public TkIdentifier(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkIdentifier() {
        super();
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
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
        buff.append(" authorityUuid:");
        buff.append(this.authorityUuid);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    
    /**
     * Returns a hash code for this <code>EIdentifierVersion</code>.
     * 
     * @return a hash code value for this <tt>EIdentifierVersion</tt>.
     */
    public int hashCode() {
        return Arrays.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time, (int) (time >>> 32) });
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
        if (TkIdentifier.class.isAssignableFrom(obj.getClass())) {
            TkIdentifier another = (TkIdentifier) obj;

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

	public abstract void setDenotation(Object denotation);
}
