package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;

public class ERevision implements I_VersionExternally {

    public static final long serialVersionUID = 1;

    protected static UUID nidToUuid(int nid) throws TerminologyException, IOException {
        return Terms.get().getId(nid).getUUIDs().iterator().next();
    }

    protected static int uuidToNid(Collection<UUID> collection) throws TerminologyException, IOException {
        return Terms.get().uuidToNative(collection);
    }

    protected static I_Identify nidToIdentifier(int nid) throws TerminologyException, IOException {
        return Terms.get().getId(nid);
    }

    protected static Collection<? extends I_ExtendByRef> getRefsetMembers(int nid) throws TerminologyException, IOException {
        return Terms.get().getRefsetExtensionMembers(nid);
    }

    protected static List<? extends I_ExtendByRef> getRefsetMembersForComponent(int nid) throws TerminologyException, IOException {
        return Terms.get().getAllExtensionsForComponent(nid);
    }

    protected UUID pathUuid;
    protected UUID statusUuid;
    protected long time = Long.MIN_VALUE;

    public ERevision(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public ERevision() {
        super();
    }

    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        pathUuid = new UUID(in.readLong(), in.readLong());
        statusUuid = new UUID(in.readLong(), in.readLong());
        time = in.readLong();
        assert time != Long.MIN_VALUE : "Time is Long.MIN_VALUE. Was it initialized?";
    }

    public void writeExternal(DataOutput out) throws IOException {
        assert time != Long.MIN_VALUE : "Time is Long.MIN_VALUE. Was it initialized?";
        out.writeLong(pathUuid.getMostSignificantBits());
        out.writeLong(pathUuid.getLeastSignificantBits());
        out.writeLong(statusUuid.getMostSignificantBits());
        out.writeLong(statusUuid.getLeastSignificantBits());
        out.writeLong(time);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.etypes.I_VersionExternal#getPathUuid()
     */
    public UUID getPathUuid() {
        return pathUuid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.etypes.I_VersionExternal#getStatusUuid()
     */
    public UUID getStatusUuid() {
        return statusUuid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.etypes.I_VersionExternal#getTime()
     */
    public long getTime() {
        return time;
    }

    public void setPathUuid(UUID pathUuid) {
        this.pathUuid = pathUuid;
    }

    public void setStatusUuid(UUID statusUuid) {
        this.statusUuid = statusUuid;
    }

    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(" pathUuid:");
        buff.append(this.pathUuid);
        buff.append(" statusUuid:");
        buff.append(this.statusUuid);
        buff.append(" Time:");
        buff.append("(" + new Date(this.time) + ")");
        return buff.toString();
    }

    /**
     * Returns a hash code for this <code>EVersion</code>.
     * 
     * @return a hash code value for this <tt>EVersion</tt>.
     */
    public int hashCode() {
        return HashFunction.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time });
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EVersion</tt> object, and contains the same values, 
     * field by field, as this <tt>EVersion</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (ERevision.class.isAssignableFrom(obj.getClass())) {
            ERevision another = (ERevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare pathUuid
            if (!this.pathUuid.equals(another.pathUuid)) {
                return false;
            }
            // Compare statusUuid
            if (!this.statusUuid.equals(another.statusUuid)) {
                return false;
            }
            // Compare time
            if (this.time != another.time) {
                return false;
            }
            // Objects are equal! (Don't climb any higher in the hierarchy)
            return true;
        }
        return false;
    }

}
