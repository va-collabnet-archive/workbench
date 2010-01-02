package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.tapi.TerminologyException;

public class EImage extends EComponent {

	public static final long serialVersionUID = 1;

	protected UUID conceptUuid;
	
	protected String format;

	protected byte[] image;
	
	protected String textDescription;
    
	protected UUID typeUuid;

	protected List<EImageVersion> extraVersions;
	
	public EImage(ObjectInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}

	public EImage(I_ImageVersioned imageVer) throws TerminologyException, IOException {
		convert(nidToIdentifier(imageVer.getNid()));
		int partCount = imageVer.getMutableParts().size();
		I_ImagePart part = imageVer.getMutableParts().get(0);
		conceptUuid = nidToUuid(imageVer.getConceptId());
		format = imageVer.getFormat();
		image = imageVer.getImage();
		textDescription = part.getTextDescription();
		typeUuid = nidToUuid(part.getTypeId());
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
		if (partCount > 1) {
			extraVersions = new ArrayList<EImageVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				extraVersions.add(new EImageVersion(imageVer.getMutableParts().get(i)));
			}
		} 
	}

	public EImage() {
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		conceptUuid = new UUID(in.readLong(), in.readLong());
		format = (String) in.readObject();
		int imageSize = in.readInt();
		image = new byte[imageSize];
		in.read(image, 0, imageSize);
		textDescription = (String) in.readObject();
		typeUuid = new UUID(in.readLong(), in.readLong());
		int versionLength = in.readInt();
		if (versionLength > 0) {
			extraVersions = new ArrayList<EImageVersion>(versionLength);
			for (int i = 0; i < versionLength; i++) {
				extraVersions.add(new EImageVersion(in));
			}
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(conceptUuid.getMostSignificantBits());
		out.writeLong(conceptUuid.getLeastSignificantBits());
		out.writeObject(format);
		out.writeInt(image.length);
		out.write(image);
		out.writeObject(textDescription);
		out.writeLong(typeUuid.getMostSignificantBits());
		out.writeLong(typeUuid.getLeastSignificantBits());
		if (extraVersions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(extraVersions.size());
			for (EImageVersion eiv: extraVersions) {
				eiv.writeExternal(out);
			}
		}
	}
	
	public List<EImageVersion> getExtraVersionsList() {
		return extraVersions;
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

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
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
