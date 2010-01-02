package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.tapi.TerminologyException;

public class ERefsetCidVersion extends EVersion {

	public static final long serialVersionUID = 1;

	protected UUID c1Uuid;

	public ERefsetCidVersion(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetCidVersion(I_ThinExtByRefPartConcept part) throws TerminologyException, IOException {
		c1Uuid = nidToUuid(part.getC1id());
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		c1Uuid = new UUID(in.readLong(), in.readLong());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(c1Uuid.getMostSignificantBits());
		out.writeLong(c1Uuid.getLeastSignificantBits());
	}

}
