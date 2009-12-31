package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.tapi.TerminologyException;

public class ERefsetIntMemberVersion extends EVersion {

	private int intValue;

	public ERefsetIntMemberVersion(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetIntMemberVersion(
			I_ThinExtByRefPartInteger part) throws TerminologyException, IOException {
		intValue = part.getIntValue();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		intValue = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(intValue);
	}

}
