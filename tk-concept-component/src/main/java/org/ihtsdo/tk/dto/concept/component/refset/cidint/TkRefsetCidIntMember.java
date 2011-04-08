package org.ihtsdo.tk.dto.concept.component.refset.cidint;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

public class TkRefsetCidIntMember extends TkRefsetAbstractMember<TkRefsetCidIntRevision> {

    public static final long serialVersionUID = 1;

    public UUID c1Uuid;
    public int intValue;

    public TkRefsetCidIntMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }


    public TkRefsetCidIntMember() {
        super();
    }


	@Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in,dataVersion);
        c1Uuid = new UUID(in.readLong(), in.readLong());
        intValue = in.readInt();
        int versionSize = in.readInt();
        if (versionSize > 0) {
            revisions = new ArrayList<TkRefsetCidIntRevision>(versionSize);
            for (int i = 0; i < versionSize; i++) {
                revisions.add(new TkRefsetCidIntRevision(in, dataVersion));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(c1Uuid.getMostSignificantBits());
        out.writeLong(c1Uuid.getLeastSignificantBits());
        out.writeInt(intValue);
        if (revisions == null) {
            out.writeInt(0);
        } else {
        	TkConcept.checkListInt(revisions.size());
            out.writeInt(revisions.size());
            for (TkRefsetCidIntRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    @Override
    public TK_REFSET_TYPE getType() {
        return TK_REFSET_TYPE.CID_INT;
    }

    public List<TkRefsetCidIntRevision> getRevisionList() {
        return revisions;
    }

    public UUID getC1Uuid() {
        return c1Uuid;
    }

    public void setC1Uuid(UUID c1Uuid) {
        this.c1Uuid = c1Uuid;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(" c1Uuid:");
        buff.append(this.c1Uuid);
        buff.append(" intValue:");
        buff.append(this.intValue);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }

    /**
     * Returns a hash code for this <code>ERefsetCidIntMember</code>.
     *
     * @return  a hash code value for this <tt>ERefsetCidIntMember</tt>. 
     */
    public int hashCode() {
        return this.primordialUuid.hashCode(); 
    }
    
    
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidIntMember</tt> object, and contains the same values, 
     * field by field, as this <tt>ERefsetCidIntMember</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) return false; 
        if (TkRefsetCidIntMember.class.isAssignableFrom(obj.getClass())) {
            TkRefsetCidIntMember another = (TkRefsetCidIntMember) obj; 
            
            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare c1Uuid
            if (!this.c1Uuid.equals(another.c1Uuid)) {
                return false; 
            }
            // Compare intValue
            if (this.intValue != another.intValue) {
                return false; 
            } 
            // Compare their parents 
            return super.equals(obj);
        }
        return false;
    }
}
