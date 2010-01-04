package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLong;
import org.dwfa.tapi.TerminologyException;

public class ERefsetLongVersion extends EVersion {

	public static final long serialVersionUID = 1;

	protected long longValue;

	public ERefsetLongVersion(DataInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetLongVersion(
			I_ThinExtByRefPartLong part) throws TerminologyException, IOException {
		longValue = part.getLongValue();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(DataInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		longValue = in.readLong();
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(longValue);
	}

}
