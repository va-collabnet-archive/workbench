package org.ihtsdo.tk.concept.component.media;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.concept.component.TkRevision;

public class TkMediaRevision extends TkRevision {

    public static final long serialVersionUID = 1;

    protected String textDescription;

    protected UUID typeUuid;

    public TkMediaRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkMediaRevision() {
        super();
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        textDescription = in.readUTF();
        typeUuid = new UUID(in.readLong(), in.readLong());
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeUTF(textDescription);
        out.writeLong(typeUuid.getMostSignificantBits());
        out.writeLong(typeUuid.getLeastSignificantBits());
    }

    public String getTextDescription() {
        return textDescription;
    }

    public void setTextDescription(String textDescription) {
        this.textDescription = textDescription;
    }

    public UUID getTypeUuid() {
        return typeUuid;
    }

    public void setTypeUuid(UUID typeUuid) {
        this.typeUuid = typeUuid;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(" textDescription:");
        buff.append("'" + this.textDescription + "'");
        buff.append(" typeUuid:");
        buff.append(this.typeUuid);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EImageVersion</tt> object, and contains the same values, field by field, 
     * as this <tt>EImageVersion</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkMediaRevision.class.isAssignableFrom(obj.getClass())) {
            TkMediaRevision another = (TkMediaRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare textDescription
            if (!this.textDescription.equals(another.textDescription)) {
                return false;
            }
            // Compare typeUuid
            if (!this.typeUuid.equals(another.typeUuid)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }
}
