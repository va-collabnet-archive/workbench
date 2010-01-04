package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.tapi.TerminologyException;

public class ERefsetCidCidVersion extends EVersion {

	public static final long serialVersionUID = 1;

	protected UUID c1Uuid;
	protected UUID c2Uuid;

	public ERefsetCidCidVersion(DataInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetCidCidVersion(
			I_ThinExtByRefPartConceptConcept part) throws TerminologyException, IOException {
		c1Uuid = nidToUuid(part.getC1id());
		c2Uuid = nidToUuid(part.getC2id());
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(DataInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		c1Uuid = new UUID(in.readLong(), in.readLong());
		c2Uuid = new UUID(in.readLong(), in.readLong());
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(c1Uuid.getMostSignificantBits());
		out.writeLong(c1Uuid.getLeastSignificantBits());
		out.writeLong(c2Uuid.getMostSignificantBits());
		out.writeLong(c2Uuid.getLeastSignificantBits());
	}

	public UUID getC1Uuid() {
		return c1Uuid;
	}

	public void setC1Uuid(UUID c1Uuid) {
		this.c1Uuid = c1Uuid;
	}

	public UUID getC2Uuid() {
		return c2Uuid;
	}

	public void setC2Uuid(UUID c2Uuid) {
		this.c2Uuid = c2Uuid;
	}

}
