package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.tapi.TerminologyException;

public class ERefsetStrMemberVersion extends EVersion {

	private String stringValue;

	public ERefsetStrMemberVersion(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetStrMemberVersion(
			I_ThinExtByRefPartString part) throws TerminologyException, IOException {
		stringValue = part.getStringValue();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		stringValue = (String) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(stringValue);
	}

}
