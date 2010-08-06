package org.ihtsdo.tk.dto.concept.component.identifier;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;



public class TkIdentifierUuid extends TkIdentifier {

    public static final long serialVersionUID = 1;

    public static UUID generatedUuid = UUID.fromString("2faa9262-8fb2-11db-b606-0800200c9a66");
    public UUID denotation;

    public TkIdentifierUuid(UUID denotation) {
		super();
		this.denotation = denotation;
		this.authorityUuid = generatedUuid;
	}

	public TkIdentifierUuid(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
        denotation = new UUID(in.readLong(), in.readLong());
    }

    public TkIdentifierUuid() {
        super();
    }

    @Override
    public void writeDenotation(DataOutput out) throws IOException {
        out.writeLong(denotation.getMostSignificantBits());
        out.writeLong(denotation.getLeastSignificantBits());
    }

    @Override
    public UUID getDenotation() {
        return denotation;
    }

    @Override
    public IDENTIFIER_PART_TYPES getIdType() {
        return IDENTIFIER_PART_TYPES.UUID;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(" denotation:");
        buff.append(this.denotation);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    
    /**
     * Returns a hash code for this <code>EIdentifierVersionUuid</code>.
     * 
     * @return a hash code value for this <tt>EIdentifierVersionUuid</tt>.
     */
    public int hashCode() {
        return Arrays.hashCode(new int[] { denotation.hashCode(), statusUuid.hashCode(), pathUuid.hashCode(), (int) time, (int) (time >>> 32) });
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EIdentifierVersionUuid</tt> object, and contains the same values, field by field, 
     * as this <tt>EIdentifierVersionUuid</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkIdentifierUuid.class.isAssignableFrom(obj.getClass())) {
            TkIdentifierUuid another = (TkIdentifierUuid) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================

            // Compare denotation
            if (!this.denotation.equals(another.denotation)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }

	@Override
	public void setDenotation(Object denotation) {
		this.denotation = (UUID) denotation;
	}

}
