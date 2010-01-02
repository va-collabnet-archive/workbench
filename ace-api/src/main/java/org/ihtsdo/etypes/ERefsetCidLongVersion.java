package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartCidLong;
import org.dwfa.tapi.TerminologyException;

public class ERefsetCidLongVersion extends EVersion {

	public static final long serialVersionUID = 1;

	private UUID c1Uuid;
	private long longValue;

	public ERefsetCidLongVersion(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetCidLongVersion(
			I_ThinExtByRefPartCidLong part) throws TerminologyException, IOException {
		c1Uuid = nidToUuid(part.getC1id());
		longValue = part.getLongValue();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		c1Uuid = new UUID(in.readLong(), in.readLong());
		longValue = in.readLong();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(c1Uuid.getMostSignificantBits());
		out.writeLong(c1Uuid.getLeastSignificantBits());
		out.writeLong(longValue);
	}

	public UUID getC1Uuid() {
		return c1Uuid;
	}

	public void setC1Uuid(UUID c1Uuid) {
		this.c1Uuid = c1Uuid;
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

}
