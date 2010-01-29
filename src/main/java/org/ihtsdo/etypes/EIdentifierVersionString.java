package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.etypes.EComponent.IDENTIFIER_PART_TYPES;

public class EIdentifierVersionString extends EIdentifierVersion {

    public static final long serialVersionUID = 1;

    protected String denotation;

    public EIdentifierVersionString(DataInput in) throws IOException, ClassNotFoundException {
        super(in);
        denotation = in.readUTF();
    }

    public EIdentifierVersionString(I_IdPart idp) throws TerminologyException, IOException {
        denotation = (String) idp.getDenotation();
        authorityUuid = nidToUuid(idp.getAuthorityNid());
        pathUuid = nidToUuid(idp.getPathId());
        statusUuid = nidToUuid(idp.getStatusId());
        time = idp.getTime();
    }

    public EIdentifierVersionString() {
        super();
    }

    @Override
    public void writeDenotation(DataOutput out) throws IOException {
        out.writeUTF(denotation);
    }

    @Override
    public String getDenotation() {
        return denotation;
    }

    @Override
    public IDENTIFIER_PART_TYPES getIdType() {
        return IDENTIFIER_PART_TYPES.STRING;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(" denotation:");
        buff.append("'" + this.denotation + "'");
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    
    /**
     * Returns a hash code for this <code>EIdentifierVersionString</code>.
     * 
     * @return a hash code value for this <tt>EIdentifierVersionString</tt>.
     */
    public int hashCode() {
        return HashFunction.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time });
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EIdentifierVersionString</tt> object, and contains the same values, field by field, 
     * as this <tt>EIdentifierVersionString</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (EIdentifierVersionString.class.isAssignableFrom(obj.getClass())) {
            EIdentifierVersionString another = (EIdentifierVersionString) obj;

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
}
