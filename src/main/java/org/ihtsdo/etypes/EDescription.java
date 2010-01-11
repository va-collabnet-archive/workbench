package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.tapi.TerminologyException;

public class EDescription extends EComponent<EDescriptionVersion> implements I_DescribeExternally {
    public static final long serialVersionUID = 1;

    protected UUID conceptUuid;

    protected boolean initialCaseSignificant;

    protected String lang;

    protected String text;

    protected UUID typeUuid;

    public EDescription(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public EDescription(I_DescriptionVersioned desc) throws TerminologyException, IOException {
        convert(nidToIdentifier(desc.getNid()));
        int partCount = desc.getMutableParts().size();
        I_DescriptionPart part = desc.getMutableParts().get(0);
        conceptUuid = nidToUuid(desc.getConceptId());
        initialCaseSignificant = part.isInitialCaseSignificant();
        lang = part.getLang();
        text = part.getText();
        typeUuid = nidToUuid(part.getTypeId());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
        if (partCount > 1) {
            extraVersions = new ArrayList<EDescriptionVersion>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                extraVersions.add(new EDescriptionVersion(desc.getMutableParts().get(i)));
            }
        }
    }

    public EDescription() {
    }

    @Override
    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        conceptUuid = new UUID(in.readLong(), in.readLong());
        initialCaseSignificant = in.readBoolean();
        lang = in.readUTF();
        text = in.readUTF();
        typeUuid = new UUID(in.readLong(), in.readLong());
        int versionLength = in.readInt();
        if (versionLength > 0) {
            extraVersions = new ArrayList<EDescriptionVersion>(versionLength);
            for (int i = 0; i < versionLength; i++) {
                extraVersions.add(new EDescriptionVersion(in));
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
        out.writeUTF(text);
        out.writeLong(typeUuid.getMostSignificantBits());
        out.writeLong(typeUuid.getLeastSignificantBits());
        if (extraVersions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(extraVersions.size());
            for (EDescriptionVersion edv : extraVersions) {
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

    public List<EDescriptionVersion> getExtraVersionsList() {
        return extraVersions;
    }

    public UUID getTypeUuid() {
        return typeUuid;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(super.toString());

        buff.append(", conceptUuid:");
        buff.append(this.conceptUuid);
        buff.append(", initialCaseSignificant:");
        buff.append(this.initialCaseSignificant);
        buff.append(", lang:");
        buff.append(this.lang);
        buff.append(", text:");
        buff.append(this.text);
        buff.append(", typeUuid:");
        buff.append(this.typeUuid);
        buff.append("; ");

        return buff.toString();
    }

}
