package org.ihtsdo.tk.dto.concept.component.refset.cidlong;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

public class TkRefsetCidLongMember extends TkRefsetAbstractMember<TkRefsetCidLongRevision> {

    public static final long serialVersionUID = 1;

    public UUID c1Uuid;
    public long longValue;

    public List<TkRefsetCidLongRevision> extraVersions;

    public TkRefsetCidLongMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefsetCidLongMember() {
        super();
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        c1Uuid = new UUID(in.readLong(), in.readLong());
        longValue = in.readLong();
        int versionSize = in.readInt();
        if (versionSize > 0) {
            extraVersions = new ArrayList<TkRefsetCidLongRevision>(versionSize);
            for (int i = 0; i < versionSize; i++) {
                extraVersions.add(new TkRefsetCidLongRevision(in, dataVersion));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(c1Uuid.getMostSignificantBits());
        out.writeLong(c1Uuid.getLeastSignificantBits());
        out.writeLong(longValue);
        if (extraVersions == null) {
            out.writeInt(0);
        } else {
        	checkListInt(extraVersions.size());
            out.writeInt(extraVersions.size());
            for (TkRefsetCidLongRevision rmv : extraVersions) {
                rmv.writeExternal(out);
            }
        }
    }

    @Override
    public TK_REFSET_TYPE getType() {
        return TK_REFSET_TYPE.CID_LONG;
    }

    public List<TkRefsetCidLongRevision> getRevisionList() {
        return extraVersions;
    }

    public UUID getC1Uuid() {
        return c1Uuid;
    }

    public void setC1Uuid(UUID c1Uuid) {
        this.c1Uuid = c1Uuid;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    public List<TkRefsetCidLongRevision> getRevisions() {
        return extraVersions;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(" c1Uuid:");
        buff.append(this.c1Uuid);
        buff.append(" longValue:");
        buff.append(this.longValue);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }

    /**
     * Returns a hash code for this <code>ERefsetCidLongMember</code>.
     *
     * @return  a hash code value for this <tt>ERefsetCidLongMember</tt>. 
     */
    public int hashCode() {
        return this.primordialUuid.hashCode(); 
    }
    
    
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidLongMember</tt> object, and contains the same values, 
     * field by field, as this <tt>ERefsetCidLongMember</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) return false; 
        if (TkRefsetCidLongMember.class.isAssignableFrom(obj.getClass())) {
            TkRefsetCidLongMember another = (TkRefsetCidLongMember) obj; 
            
            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare c1Uuid
            if (!this.c1Uuid.equals(another.c1Uuid)) {
                return false; 
            }
            // Compare longValue
            if (this.longValue != another.longValue) {
                return false; 
            } 
            // Compare their parents 
            return super.equals(obj);
        }
        return false;
    }
}
