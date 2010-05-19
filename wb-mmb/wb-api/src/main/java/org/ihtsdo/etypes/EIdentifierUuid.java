package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.etypes.EComponent.IDENTIFIER_PART_TYPES;

public class EIdentifierUuid extends EIdentifier {

    public static final long serialVersionUID = 1;

    protected static UUID primordialAuthority;
    protected UUID denotation;

    public EIdentifierUuid(DataInput in) throws IOException, ClassNotFoundException {
        super(in);
        denotation = new UUID(in.readLong(), in.readLong());
    }

    public EIdentifierUuid(I_IdPart idp) throws TerminologyException, IOException {
        denotation = (UUID) idp.getDenotation();
        authorityUuid = nidToUuid(idp.getAuthorityNid());
        pathUuid = nidToUuid(idp.getPathId());
        statusUuid = nidToUuid(idp.getStatusId());
        time = idp.getTime();
    }

    public EIdentifierUuid() {
        super();
    }

    public EIdentifierUuid(EComponent<?> eComponent) {
        denotation = eComponent.primordialUuid;
        if (primordialAuthority == null) {
            primordialAuthority = ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids().iterator().next();
        }
        authorityUuid = primordialAuthority;
        pathUuid = eComponent.pathUuid;
        statusUuid = eComponent.statusUuid;
        time = eComponent.time;
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
        buff.append(" primordialAuthority:");
        buff.append(primordialAuthority);
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
        return HashFunction.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time });
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
        if (EIdentifierUuid.class.isAssignableFrom(obj.getClass())) {
            EIdentifierUuid another = (EIdentifierUuid) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare primordialAuthority
            if (!EIdentifierUuid.primordialAuthority.equals(EIdentifierUuid.primordialAuthority)) {
                return false;
            }
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
