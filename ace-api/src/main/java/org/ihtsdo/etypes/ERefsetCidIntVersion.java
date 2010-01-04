package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.tapi.TerminologyException;

public class ERefsetCidIntVersion extends EVersion {

	public static final long serialVersionUID = 1;

	protected UUID c1Uuid;
	protected int intValue;

	public ERefsetCidIntVersion(DataInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetCidIntVersion(
			I_ThinExtByRefPartConceptInt part) throws TerminologyException, IOException {
		c1Uuid = nidToUuid(part.getC1id());
		intValue = part.getIntValue();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	public ERefsetCidIntVersion() {
	}

	@Override
	public void readExternal(DataInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		c1Uuid = new UUID(in.readLong(), in.readLong());
		intValue = in.readInt();
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(c1Uuid.getMostSignificantBits());
		out.writeLong(c1Uuid.getLeastSignificantBits());
		out.writeInt(intValue);
	}

	public UUID getC1Uuid() {
		return c1Uuid;
	}

	public void setC1Uuid(UUID c1Uuid) {
		this.c1Uuid = c1Uuid;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

}
