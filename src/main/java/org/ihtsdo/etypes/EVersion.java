package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;

public class EVersion implements I_VersionExternally {

    public static final long serialVersionUID = 1;

    private static I_TermFactory tf = LocalVersionedTerminology.get();

    protected static UUID nidToUuid(int nid) throws TerminologyException, IOException {
        return tf.getId(nid).getUUIDs().iterator().next();
    }

    protected static int uuidToNid(Collection<UUID> collection) throws TerminologyException, IOException {
        return tf.uuidToNative(collection);
    }

    protected static I_Identify nidToIdentifier(int nid) throws TerminologyException, IOException {
        return tf.getId(nid);
    }

    protected static List<I_ThinExtByRefVersioned> getRefsetMembers(int nid) throws TerminologyException, IOException {
        return tf.getRefsetExtensionMembers(nid);
    }

    protected UUID pathUuid;
    protected UUID statusUuid;
    protected long time = Long.MIN_VALUE;

    public EVersion(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public EVersion() {
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
        buff.append(", statusUuid:");
        buff.append(this.statusUuid);
        buff.append(", Time:");
        buff.append("(" + new Date(this.time) + ")");

        return buff.toString();
    }
}
