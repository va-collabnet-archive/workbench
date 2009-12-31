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
	private UUID conceptUuid;
	
    private String format;

    private byte[] image;
	
    private String textDescription;
    
    private UUID typeUuid;

	private List<EImageVersion> versions;
	
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
			versions = new ArrayList<EImageVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				versions.add(new EImageVersion(imageVer.getMutableParts().get(i)));
			}
		} 
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
			versions = new ArrayList<EImageVersion>(versionLength);
			for (int i = 0; i < versionLength; i++) {
				versions.add(new EImageVersion(in));
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
		if (versions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(versions.size());
			for (EImageVersion eiv: versions) {
				eiv.writeExternal(out);
			}
		}
	}

}
