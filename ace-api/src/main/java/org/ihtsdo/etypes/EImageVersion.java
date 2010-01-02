package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.UUID;

import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.tapi.TerminologyException;

public class EImageVersion extends EVersion {

	public static final long serialVersionUID = 1;

	protected String textDescription;

	protected UUID typeUuid;

	public EImageVersion(DataInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public EImageVersion(I_ImagePart part) throws TerminologyException,
			IOException {
		textDescription = part.getTextDescription();
		typeUuid = nidToUuid(part.getTypeId());
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	public EImageVersion() {
	}

	@Override
	public void readExternal(DataInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
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

}
