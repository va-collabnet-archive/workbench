package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public abstract class ERefsetMember<V extends ERevision> extends EComponent<V> {

    public static final long serialVersionUID = 1;

    protected UUID refsetUuid;
    protected UUID componentUuid;

    public ERefsetMember() {
        super();
    }

    public ERefsetMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
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

    public abstract REFSET_TYPES getType();

    public static ERefsetMember<?> convert(I_ExtendByRef m) throws TerminologyException, IOException {
        REFSET_TYPES type = REFSET_TYPES.nidToType(m.getTypeId());
        if (type != null) {
            switch (type) {
            case CID:
                return new ERefsetCidMember(m);
            case CID_CID:
                return new ERefsetCidCidMember(m);
            case CID_CID_CID:
                return new ERefsetCidCidCidMember(m);
            case CID_CID_STR:
                return new ERefsetCidCidStrMember(m);
            case INT:
                return new ERefsetIntMember(m);
            case MEMBER:
                return new ERefsetMemberMember(m);
            case STR:
                return new ERefsetStrMember(m);
            case CID_INT:
                return new ERefsetCidIntMember(m);
            default:
                throw new UnsupportedOperationException("Cannot handle: " + type);
            }
        } else {
            AceLog.getAppLog().severe("Can't handle refset type: " + m);
        }
        return null;
    }

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
        if (ERefsetMember.class.isAssignableFrom(obj.getClass())) {
            ERefsetMember<?> another = (ERefsetMember<?>) obj;

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