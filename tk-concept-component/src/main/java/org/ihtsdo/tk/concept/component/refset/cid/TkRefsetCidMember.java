package org.ihtsdo.tk.concept.component.refset.cid;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.concept.component.refset.TkRefsetAbstractMember;

public class TkRefsetCidMember extends TkRefsetAbstractMember<TkRefsetCidRevision> {

    public static final long serialVersionUID = 1;

    protected UUID c1Uuid;

    public TkRefsetCidMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }


    public TkRefsetCidMember() {
        super();
    }

	@Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        c1Uuid = new UUID(in.readLong(), in.readLong());
        int versionSize = in.readInt();
        if (versionSize > 0) {
            revisions = new ArrayList<TkRefsetCidRevision>(versionSize);
            for (int i = 0; i < versionSize; i++) {
                revisions.add(new TkRefsetCidRevision(in, dataVersion));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(c1Uuid.getMostSignificantBits());
        out.writeLong(c1Uuid.getLeastSignificantBits());
        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());
            for (TkRefsetCidRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    @Override
    public TK_REFSET_TYPE getType() {
        return TK_REFSET_TYPE.CID;
    }

    public List<TkRefsetCidRevision> getRevisionList() {
        return revisions;
    }

    public UUID getC1Uuid() {
        return c1Uuid;
    }

    public void setC1Uuid(UUID c1Uuid) {
        this.c1Uuid = c1Uuid;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(" c1Uuid:");
        buff.append(this.c1Uuid);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }

    /**
     * Returns a hash code for this <code>ERefsetCidMember</code>.
     * 
     * @return a hash code value for this <tt>ERefsetCidMember</tt>.
     */
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidMember</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetCidMember</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkRefsetCidMember.class.isAssignableFrom(obj.getClass())) {
            TkRefsetCidMember another = (TkRefsetCidMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare c1Uuid
            if (!this.c1Uuid.equals(another.c1Uuid)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }

}
