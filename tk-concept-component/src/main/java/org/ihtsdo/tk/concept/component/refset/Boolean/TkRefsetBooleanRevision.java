package org.ihtsdo.tk.concept.component.refset.Boolean;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.ihtsdo.tk.concept.component.TkRevision;

public class TkRefsetBooleanRevision extends TkRevision {

    public static final long serialVersionUID = 1;

    public boolean booleanValue;

    public TkRefsetBooleanRevision() {
        super();
    }

    public TkRefsetBooleanRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        booleanValue = in.readBoolean();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(booleanValue);
    }

    public boolean isBooleanValue() {
        return booleanValue;
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
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetBooleanVersion</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetBooleanVersion</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkRefsetBooleanRevision.class.isAssignableFrom(obj.getClass())) {
            TkRefsetBooleanRevision another = (TkRefsetBooleanRevision) obj;

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
