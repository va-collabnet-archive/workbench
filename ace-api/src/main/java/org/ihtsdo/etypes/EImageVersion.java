package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.tapi.TerminologyException;

public class EImageVersion extends EVersion {

	public static final long serialVersionUID = 1;

	private String textDescription;

	private UUID typeUuid;

	public EImageVersion(ObjectInput in) throws IOException,
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

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		textDescription = (String) in.readObject();
		typeUuid = new UUID(in.readLong(), in.readLong());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(textDescription);
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
