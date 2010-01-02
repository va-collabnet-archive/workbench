package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.tapi.TerminologyException;

public class ERefsetCidCidStrVersion extends EVersion {

	public static final long serialVersionUID = 1;

	private UUID c1Uuid;
	private UUID c2Uuid;
	private String stringValue;

	public ERefsetCidCidStrVersion(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetCidCidStrVersion(
			I_ThinExtByRefPartConceptConceptString part) throws TerminologyException, IOException {
		c1Uuid = nidToUuid(part.getC1id());
		c2Uuid = nidToUuid(part.getC2id());
		stringValue = part.getStringValue();
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
		stringValue = (String) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(c1Uuid.getMostSignificantBits());
		out.writeLong(c1Uuid.getLeastSignificantBits());
		out.writeLong(c2Uuid.getMostSignificantBits());
		out.writeLong(c2Uuid.getLeastSignificantBits());
		out.writeObject(stringValue);
	}

}
