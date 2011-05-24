package org.ihtsdo.tk.dto.concept.component.media;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.TkComponent;

public class TkMedia extends TkComponent<TkMediaRevision> {

    public static final long serialVersionUID = 1;

    public UUID conceptUuid;

    public String format;

    public byte[] dataBytes;

    public String textDescription;

    public UUID typeUuid;

    public TkMedia(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkMedia() {
        super();
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        conceptUuid = new UUID(in.readLong(), in.readLong());
        format = in.readUTF();
        int imageSize = in.readInt();
        dataBytes = new byte[imageSize];
        in.readFully(dataBytes);
        textDescription = in.readUTF();
        typeUuid = new UUID(in.readLong(), in.readLong());
        int versionLength = in.readInt();
        if (versionLength > 0) {
            revisions = new ArrayList<TkMediaRevision>(versionLength);
            for (int i = 0; i < versionLength; i++) {
                revisions.add(new TkMediaRevision(in, dataVersion));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(conceptUuid.getMostSignificantBits());
        out.writeLong(conceptUuid.getLeastSignificantBits());
        out.writeUTF(format);
        out.writeInt(dataBytes.length);
        out.write(dataBytes);
        out.writeUTF(textDescription);
        out.writeLong(typeUuid.getMostSignificantBits());
        out.writeLong(typeUuid.getLeastSignificantBits());
        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());
            for (TkMediaRevision eiv : revisions) {
                eiv.writeExternal(out);
            }
        }
    }

    @Override
    public List<TkMediaRevision> getRevisionList() {
        return revisions;
    }

    public UUID getConceptUuid() {
        return conceptUuid;
    }

    public void setConceptUuid(UUID conceptUuid) {
        this.conceptUuid = conceptUuid;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public byte[] getDataBytes() {
        return dataBytes;
    }

    public void setDataBytes(byte[] data) {
        this.dataBytes = data;
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
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" conceptUuid:");
        buff.append(this.conceptUuid);
        buff.append(" format:");
        buff.append("'").append(this.format).append("'");
        buff.append(" image:");       
        buff.append( new String(this.dataBytes));
        buff.append(" textDescription:");
        buff.append("'").append(this.textDescription).append("'");
        buff.append(" typeUuid:");
        buff.append(this.typeUuid);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }

    /**
     * Returns a hash code for this <code>EImage</code>.
     * 
     * @return a hash code value for this <tt>EImage</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EImage</tt> object, and contains the same values, field by field, 
     * as this <tt>EImage</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkMedia.class.isAssignableFrom(obj.getClass())) {
            TkMedia another = (TkMedia) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare conceptUuid
            if (!this.conceptUuid.equals(another.conceptUuid)) {
                return false;
            }
            // Compare format
            if (!this.format.equals(another.format)) {
                return false;
            }
            // Compare image (had to loop through the array)
            for (int i = 0; i < this.dataBytes.length; i++) {
                if (this.dataBytes[i] != another.dataBytes[i])
                    return false;
            }
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
