package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;

public class ERefsetStrVersion extends EVersion {

    public static final long serialVersionUID = 1;

    protected String stringValue;

    public ERefsetStrVersion(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public ERefsetStrVersion(I_ThinExtByRefPartString part) throws TerminologyException, IOException {
        stringValue = part.getStringValue();
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetStrVersion() {
        super();
    }

    @Override
    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        stringValue = in.readUTF();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeUTF(stringValue);
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
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(" stringValue:");
        buff.append("'" + this.stringValue + "'");
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    
    /**
     * Returns a hash code for this <code>ERefsetStrVersion</code>.
     * 
     * @return a hash code value for this <tt>ERefsetStrVersion</tt>.
     */
    public int hashCode() {
        return HashFunction.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time });
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
        if (ERefsetStrVersion.class.isAssignableFrom(obj.getClass())) {
            ERefsetStrVersion another = (ERefsetStrVersion) obj;

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
