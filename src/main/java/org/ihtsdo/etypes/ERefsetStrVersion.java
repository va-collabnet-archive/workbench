package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.tapi.TerminologyException;

public class ERefsetStrVersion extends EVersion {

	public static final long serialVersionUID = 1;

	protected String stringValue;

	public ERefsetStrVersion(DataInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetStrVersion(
			I_ThinExtByRefPartString part) throws TerminologyException, IOException {
		stringValue = part.getStringValue();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(DataInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		stringValue = in.readUTF();
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(stringValue);
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

}
