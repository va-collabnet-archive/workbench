package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.tapi.TerminologyException;

public class EDescriptionVersion extends EVersion implements I_DescribeExternally {

	public static final long serialVersionUID = 1;
	private boolean initialCaseSignificant;
	
	private String lang;
	
	private String text;

	private UUID typeUuid;

	public EDescriptionVersion(ObjectInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}

	public EDescriptionVersion(I_DescriptionPart part) throws TerminologyException, IOException {
		initialCaseSignificant = part.isInitialCaseSignificant();
		lang = part.getLang();
		text = part.getText();
		typeUuid = nidToUuid(part.getTypeId());
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		initialCaseSignificant = in.readBoolean();
		lang = (String) in.readObject();
		text = (String) in.readObject();
		typeUuid = new UUID(in.readLong(), in.readLong());

	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(lang);
		out.writeObject(text);
		out.writeLong(typeUuid.getMostSignificantBits());
		out.writeLong(typeUuid.getLeastSignificantBits());
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.etypes.I_DescribeExternally#isInitialCaseSignificant()
	 */
	public boolean isInitialCaseSignificant() {
		return initialCaseSignificant;
	}

	public void setInitialCaseSignificant(boolean initialCaseSignificant) {
		this.initialCaseSignificant = initialCaseSignificant;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.etypes.I_DescribeExternally#getLang()
	 */
	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	/* (non-Javadoc)
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
}
