package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.tapi.TerminologyException;

public class ERefsetBooleanVersion extends EVersion {

	public static final long serialVersionUID = 1;

	protected boolean booleanValue;

	public ERefsetBooleanVersion(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetBooleanVersion(
			I_ThinExtByRefPartBoolean part) throws TerminologyException, IOException {
		booleanValue = part.getBooleanValue();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		booleanValue = in.readBoolean();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeBoolean(booleanValue);
	}

	public boolean isBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

}
