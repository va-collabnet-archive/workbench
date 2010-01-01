package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.tapi.TerminologyException;

public class EDescription extends EComponent 
	implements I_DescribeExternally {

	private UUID conceptUuid;
	
	private boolean initialCaseSignificant;
	
	private String lang;
	
	private String text;
	
	private UUID typeUuid;
	
	private List<EDescriptionVersion> extraVersions;
	
	public EDescription(ObjectInput in) throws IOException, ClassNotFoundException {
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
			extraVersions = new ArrayList<EDescriptionVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				extraVersions.add(new EDescriptionVersion(desc.getMutableParts().get(i)));
			}
		} 
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		conceptUuid = new UUID(in.readLong(), in.readLong());
		initialCaseSignificant = in.readBoolean();
		lang = (String) in.readObject();
		text = (String) in.readObject();
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
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(conceptUuid.getMostSignificantBits());
		out.writeLong(conceptUuid.getLeastSignificantBits());
		out.writeObject(lang);
		out.writeObject(text);
		out.writeLong(typeUuid.getMostSignificantBits());
		out.writeLong(typeUuid.getLeastSignificantBits());
		if (extraVersions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(extraVersions.size());
			for (EDescriptionVersion edv: extraVersions) {
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

}
