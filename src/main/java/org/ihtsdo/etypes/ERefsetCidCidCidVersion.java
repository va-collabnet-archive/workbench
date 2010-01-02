package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.tapi.TerminologyException;

public class ERefsetCidCidCidVersion extends EVersion {

	private UUID c1Uuid;
	private UUID c2Uuid;
	private UUID c3Uuid;

	public ERefsetCidCidCidVersion(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetCidCidCidVersion(
			I_ThinExtByRefPartConceptConceptConcept part) throws TerminologyException, IOException {
		c1Uuid = nidToUuid(part.getC1id());
		c2Uuid = nidToUuid(part.getC2id());
		c3Uuid = nidToUuid(part.getC3id());
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		c1Uuid = new UUID(in.readLong(), in.readLong());
		c2Uuid = new UUID(in.readLong(), in.readLong());
		c3Uuid = new UUID(in.readLong(), in.readLong());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(c1Uuid.getMostSignificantBits());
		out.writeLong(c1Uuid.getLeastSignificantBits());
		out.writeLong(c2Uuid.getMostSignificantBits());
		out.writeLong(c2Uuid.getLeastSignificantBits());
		out.writeLong(c3Uuid.getMostSignificantBits());
		out.writeLong(c3Uuid.getLeastSignificantBits());
	}

}
