package org.ihtsdo.tk.dto.concept.component.description;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.api.ext.I_DescribeExternally;
import org.ihtsdo.tk.dto.concept.component.TkComponent;

public class TkDescription extends TkComponent<TkDescriptionRevision> implements I_DescribeExternally {
    public static final long serialVersionUID = 1;

    public UUID conceptUuid;

    public boolean initialCaseSignificant;

    public String lang;

    public String text;

    public UUID typeUuid;

    public TkDescription(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkDescription() {
        super();
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        conceptUuid = new UUID(in.readLong(), in.readLong());
        initialCaseSignificant = in.readBoolean();
        lang = in.readUTF();
        text = in.readUTF();
        typeUuid = new UUID(in.readLong(), in.readLong());
        int versionLength = in.readInt();
        if (versionLength > 0) {
            revisions = new ArrayList<TkDescriptionRevision>(versionLength);
            for (int i = 0; i < versionLength; i++) {
                revisions.add(new TkDescriptionRevision(in, dataVersion));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(conceptUuid.getMostSignificantBits());
        out.writeLong(conceptUuid.getLeastSignificantBits());
        out.writeBoolean(initialCaseSignificant);
        out.writeUTF(lang);
        if (text == null) {
            text = "null";
        }
        out.writeUTF(text);
        out.writeLong(typeUuid.getMostSignificantBits());
        out.writeLong(typeUuid.getLeastSignificantBits());
        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());
            for (TkDescriptionRevision edv : revisions) {
                edv.writeExternal(out);
            }
        }
    }

    public UUID getConceptUuid() {
        return conceptUuid;
    }

    public void setConceptUuid(UUID conceptUuid) {
        this.conceptUuid = conceptUuid;
    }

    public boolean isInitialCaseSignificant() {
        return initialCaseSignificant;
    }

    public void setInitialCaseSignificant(boolean initialCaseSignificant) {
        this.initialCaseSignificant = initialCaseSignificant;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<TkDescriptionRevision> getRevisionList() {
        return revisions;
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
        buff.append(" text:");
        buff.append("'" + this.text + "'");
        buff.append(" conceptUuid:");
        buff.append(this.conceptUuid);
        buff.append(" initialCaseSignificant:");
        buff.append(this.initialCaseSignificant);
        buff.append(" lang:");
        buff.append("'" + this.lang + "'");
        buff.append(" typeUuid:");
        buff.append(this.typeUuid);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EDescription</tt> object, and contains the same values, field by field, 
     * as this <tt>EDescription</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkDescription.class.isAssignableFrom(obj.getClass())) {
            TkDescription another = (TkDescription) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare conceptUuid
            if (!this.conceptUuid.equals(another.conceptUuid)) {
                return false;
            }
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
