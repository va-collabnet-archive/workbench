package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;

public class EDescriptionRevision extends ERevision implements I_DescribeExternally {

    public static final long serialVersionUID = 1;
    protected boolean initialCaseSignificant;

    protected String lang;

    protected String text;

    protected UUID typeUuid;

    public EDescriptionRevision(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public EDescriptionRevision(I_DescriptionPart part) throws TerminologyException, IOException {
        initialCaseSignificant = part.isInitialCaseSignificant();
        lang = part.getLang();
        text = part.getText();
        typeUuid = nidToUuid(part.getTypeId());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public EDescriptionRevision() {
        super();
    }

    @Override
    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        initialCaseSignificant = in.readBoolean();
        lang = in.readUTF();
        text = in.readUTF();
        typeUuid = new UUID(in.readLong(), in.readLong());
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(initialCaseSignificant);
        out.writeUTF(lang);
        out.writeUTF(text);
        out.writeLong(typeUuid.getMostSignificantBits());
        out.writeLong(typeUuid.getLeastSignificantBits());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.etypes.I_DescribeExternally#isInitialCaseSignificant()
     */
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
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
        buff.append(" initialCaseSignificant:");
        buff.append(this.initialCaseSignificant);
        buff.append(" lang:");
        buff.append("'" + this.lang + "'");
        buff.append(" text:");
        buff.append("'" + this.text + "'");
        buff.append(" typeUuid:");
        buff.append(this.typeUuid);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }

    /**
     * Returns a hash code for this <code>EDescriptionVersion</code>.
     * 
     * @return a hash code value for this <tt>EDescriptionVersion</tt>.
     */
    public int hashCode() {
        return HashFunction.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time });
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
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (EDescriptionRevision.class.isAssignableFrom(obj.getClass())) {
            EDescriptionRevision another = (EDescriptionRevision) obj;

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
