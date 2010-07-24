package org.ihtsdo.tk.concept.component.refset.member;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.ihtsdo.tk.concept.component.TkRevision;

public class TkRefsetRevision extends TkRevision {

    public static final long serialVersionUID = 1;

    public TkRefsetRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefsetRevision() {
        super();
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetVersion</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetVersion</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkRefsetRevision.class.isAssignableFrom(obj.getClass())) {
            return super.equals(obj);
        }
        return false;
    }
}
