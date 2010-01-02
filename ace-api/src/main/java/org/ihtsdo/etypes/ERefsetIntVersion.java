package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.tapi.TerminologyException;

public class ERefsetIntVersion extends EVersion {

	public static final long serialVersionUID = 1;

	protected int intValue;

	public ERefsetIntVersion(DataInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetIntVersion(
			I_ThinExtByRefPartInteger part) throws TerminologyException, IOException {
		intValue = part.getIntValue();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(DataInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		intValue = in.readInt();
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(intValue);
	}

}
