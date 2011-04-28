package org.ihtsdo.tk.dto.concept.component.description;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.api.ext.I_DescribeExternally;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

public class TkDescriptionRevision extends TkRevision implements I_DescribeExternally {

    public static final long serialVersionUID = 1;
    public boolean initialCaseSignificant;

    public String lang;

    public String text;

    public UUID typeUuid;

    public TkDescriptionRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkDescriptionRevision() {
        super();
    }

    @Override
    public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        initialCaseSignificant = in.readBoolean();
        lang = in.readUTF();
        if (dataVersion < 7) {
            text = in.readUTF();
        } else {
            int textlength =  in.readInt();
            if (textlength > 32000) {
                int textBytesLength = in.readInt();
                byte[] textBytes = new byte[textBytesLength];
                in.readFully(textBytes);
                text = new String(textBytes, "UTF-8");
            } else {
                text = in.readUTF();
            }
            
        }
        typeUuid = new UUID(in.readLong(), in.readLong());
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(initialCaseSignificant);
        out.writeUTF(lang);
        out.writeInt(text.length()); 
        if (text.length() > 32000) {
            byte[] textBytes = text.getBytes("UTF-8");
            out.writeInt(textBytes.length);
            out.write(textBytes);
        } else {
            out.writeUTF(text);
        }
        out.writeLong(typeUuid.getMostSignificantBits());
        out.writeLong(typeUuid.getLeastSignificantBits());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.etypes.I_DescribeExternally#isInitialCaseSignificant()
     */
    @Override
    public boolean isInitialCaseSignificant() {
        return initialCaseSignificant;
    }

    public void setInitialCaseSignificant(boolean initialCaseSignificant) {
        this.initialCaseSignificant = initialCaseSignificant;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.etypes.I_DescribeExternally#getLang()
     */
    @Override
    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.etypes.I_DescribeExternally#getText()
     */
    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
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
        buff.append(" initialCaseSignificant:");
        buff.append(this.initialCaseSignificant);
        buff.append(" lang:");
        buff.append("'").append(this.lang).append("'");
        buff.append(" text:");
        buff.append("'").append(this.text).append("'");
        buff.append(" typeUuid:");
        buff.append(this.typeUuid);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }


    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EDescriptionVersion</tt> object, and contains the same values, field by field, 
     * as this <tt>EDescriptionVersion</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkDescriptionRevision.class.isAssignableFrom(obj.getClass())) {
            TkDescriptionRevision another = (TkDescriptionRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare initialCaseSignificant
            if (this.initialCaseSignificant != another.initialCaseSignificant) {
                return false;
            }
            // Compare lang
            if (!this.lang.equals(another.lang)) {
                return false;
            }
            // Compare text
            if (!this.text.equals(another.text)) {
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
