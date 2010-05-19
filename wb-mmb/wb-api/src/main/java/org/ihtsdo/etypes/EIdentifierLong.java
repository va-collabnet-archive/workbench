package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.etypes.EComponent.IDENTIFIER_PART_TYPES;

public class EIdentifierLong extends EIdentifier {

    public static final long serialVersionUID = 1;

    protected long denotation;

    public EIdentifierLong(DataInput in) throws IOException, ClassNotFoundException {
        super(in);
        denotation = in.readLong();
    }

    public EIdentifierLong(I_IdPart idp) throws TerminologyException, IOException {
        super();
        denotation = (Long) idp.getDenotation();
        authorityUuid = nidToUuid(idp.getAuthorityNid());
        pathUuid = nidToUuid(idp.getPathId());
        statusUuid = nidToUuid(idp.getStatusId());
        time = idp.getTime();
    }

    public EIdentifierLong() {
        super();
    }

    @Override
    public void writeDenotation(DataOutput out) throws IOException {
        out.writeLong(denotation);
    }

    @Override
    public Long getDenotation() {
        return denotation;
    }

    @Override
    public IDENTIFIER_PART_TYPES getIdType() {
        return IDENTIFIER_PART_TYPES.LONG;
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
     * Returns a hash code for this <code>EIdentifierVersionLong</code>.
     * 
     * @return a hash code value for this <tt>EIdentifierVersionLong</tt>.
     */
    public int hashCode() {
        return HashFunction.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time });
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EIdentifierVersionLong</tt> object, and contains the same values, field by field, 
     * as this <tt>EIdentifierVersionLong</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (EIdentifierLong.class.isAssignableFrom(obj.getClass())) {
            EIdentifierLong another = (EIdentifierLong) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare denotation
            if (this.denotation != another.denotation) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }

	@Override
	public void setDenotation(Object denotation) {
		this.denotation = (Long) denotation;
	}
}
