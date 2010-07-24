package org.ihtsdo.tk.concept.component.refset.Boolean;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.tk.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.concept.component.refset.TkRefsetAbstractMember;

public class TkRefsetBooleanMember extends TkRefsetAbstractMember<TkRefsetBooleanRevision> {

    public static final long serialVersionUID = 1;

    public boolean booleanValue;

    public TkRefsetBooleanMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }
    
    public TkRefsetBooleanMember() {
        super();
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        booleanValue = in.readBoolean();
        int versionSize = in.readInt();
        if (versionSize > 0) {
            revisions = new ArrayList<TkRefsetBooleanRevision>(versionSize);
            for (int i = 0; i < versionSize; i++) {
                revisions.add(new TkRefsetBooleanRevision(in, dataVersion));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(booleanValue);
        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());
            for (TkRefsetBooleanRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    @Override
    public TK_REFSET_TYPE getType() {
        return TK_REFSET_TYPE.BOOLEAN;
    }

    public List<TkRefsetBooleanRevision> getRevisionList() {
        return revisions;
    }

    public boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(" booleanValue:");
        buff.append(this.booleanValue);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    
    /**
     * Returns a hash code for this <code>ERefsetBooleanMember</code>.
     * 
     * @return a hash code value for this <tt>ERefsetBooleanMember</tt>.
     */
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetBooleanMember</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetBooleanMember</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkRefsetBooleanMember.class.isAssignableFrom(obj.getClass())) {
            TkRefsetBooleanMember another = (TkRefsetBooleanMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare booleanValue
            if (this.booleanValue != another.booleanValue) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }

}
