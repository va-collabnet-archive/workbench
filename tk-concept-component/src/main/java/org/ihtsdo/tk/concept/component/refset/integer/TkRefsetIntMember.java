package org.ihtsdo.tk.concept.component.refset.integer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.tk.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.concept.component.refset.TkRefsetAbstractMember;

public class TkRefsetIntMember extends TkRefsetAbstractMember<TkRefsetIntRevision> {

    public static final long serialVersionUID = 1;

    protected int intValue;

    protected List<TkRefsetIntRevision> extraVersions;

    public TkRefsetIntMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefsetIntMember() {
        super();
    }

	@Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        intValue = in.readInt();
        int versionSize = in.readInt();
        if (versionSize > 0) {
            extraVersions = new ArrayList<TkRefsetIntRevision>(versionSize);
            for (int i = 0; i < versionSize; i++) {
                extraVersions.add(new TkRefsetIntRevision(in, dataVersion));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(intValue);
        if (extraVersions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(extraVersions.size());
            for (TkRefsetIntRevision rmv : extraVersions) {
                rmv.writeExternal(out);
            }
        }
    }

    @Override
    public TK_REFSET_TYPE getType() {
        return TK_REFSET_TYPE.INT;
    }

    public List<TkRefsetIntRevision> getRevisionList() {
        return extraVersions;
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
        buff.append(" intValue:");
        buff.append(this.intValue);
        buff.append(" extraVersions:");
        buff.append(this.extraVersions);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    
    /**
     * Returns a hash code for this <code>ERefsetIntMember</code>.
     * 
     * @return a hash code value for this <tt>ERefsetIntMember</tt>.
     */
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetIntMember</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetIntMember</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkRefsetIntMember.class.isAssignableFrom(obj.getClass())) {
            TkRefsetIntMember another = (TkRefsetIntMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare intValue
            if (this.intValue != another.intValue) {
                return false;
            }
            // Compare extraVersions
            if (this.extraVersions == null) {
                if (another.extraVersions == null) { // Equal!
                } else if (another.extraVersions.size() == 0) { // Equal!
                } else {
                    return false;
                }
            } else if (!this.extraVersions.equals(another.extraVersions)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }
}
