package org.ihtsdo.tk.dto.concept.component.refset.str;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.ihtsdo.tk.dto.concept.UtfHelper;

import org.ihtsdo.tk.dto.concept.component.TkRevision;

public class TkRefsetStrRevision extends TkRevision {

    public static final long serialVersionUID = 1;

    public String stringValue;

    public TkRefsetStrRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefsetStrRevision() {
        super();
    }

    @Override
    public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        stringValue = UtfHelper.readUtfV7(in, dataVersion);
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        UtfHelper.writeUtf(out, stringValue);
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" stringValue:");
        buff.append("'").append(this.stringValue).append("'");
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetStrVersion</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetStrVersion</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkRefsetStrRevision.class.isAssignableFrom(obj.getClass())) {
            TkRefsetStrRevision another = (TkRefsetStrRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare stringValue
            if (!this.stringValue.equals(another.stringValue)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }
}
