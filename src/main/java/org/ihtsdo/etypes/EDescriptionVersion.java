package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.tapi.TerminologyException;

public class EDescriptionVersion extends EVersion {

	private boolean initialCaseSignificant;
	
	private String lang;
	
	private String text;
	
	public EDescriptionVersion(ObjectInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}

	public EDescriptionVersion(I_DescriptionPart part) throws TerminologyException, IOException {
		initialCaseSignificant = part.isInitialCaseSignificant();
		lang = part.getLang();
		text = part.getText();
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

	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(lang);
		out.writeObject(text);
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
}
