package org.ihtsdo.tk.dto.concept.component.refset.str;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

public class TkRefsetStrMember extends TkRefsetAbstractMember<TkRefsetStrRevision> {

    public static final long serialVersionUID = 1;

    public String strValue;

    public TkRefsetStrMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefsetStrMember() {
        super();
    }

	@Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        
        if (dataVersion < 6) {
            strValue = in.readUTF();
        } else {
            int textlength =  in.readInt();
            if (textlength > 64000) {
                int textBytesLength = in.readInt();
                byte[] textBytes = new byte[textBytesLength];
                in.readFully(textBytes);
                strValue = new String(textBytes, "UTF-8");
            } else {
                strValue = in.readUTF();
            }
            
        }
        int versionSize = in.readInt();
        if (versionSize > 0) {
            revisions = new ArrayList<TkRefsetStrRevision>(versionSize);
            for (int i = 0; i < versionSize; i++) {
                revisions.add(new TkRefsetStrRevision(in, dataVersion));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(strValue.length()); 
        if (strValue.length() > 64000) {
            byte[] textBytes = strValue.getBytes("UTF-8");
            out.writeInt(textBytes.length);
            out.write(textBytes);
        } else {
            out.writeUTF(strValue);
        }
        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());
            for (TkRefsetStrRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    @Override
    public TK_REFSET_TYPE getType() {
        return TK_REFSET_TYPE.STR;
    }

    public List<TkRefsetStrRevision> getRevisionList() {
        return revisions;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(" strValue:");
        buff.append("'" + this.strValue + "'");
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    
    /**
     * Returns a hash code for this <code>ERefsetStrMember</code>.
     * 
     * @return a hash code value for this <tt>ERefsetStrMember</tt>.
     */
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetStrMember</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetStrMember</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkRefsetStrMember.class.isAssignableFrom(obj.getClass())) {
            TkRefsetStrMember another = (TkRefsetStrMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare strValue
            if (!this.strValue.equals(another.strValue)) {
                return false;
            }            
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }

}
