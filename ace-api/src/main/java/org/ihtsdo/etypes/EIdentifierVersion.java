package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.etypes.EComponent.IDENTIFIER_PART_TYPES;

public abstract class EIdentifierVersion extends EVersion {

	public static final long serialVersionUID = 1;
	protected UUID authorityUuid;

	public EIdentifierVersion(DataInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}

	public EIdentifierVersion() {
		super();
	}

	@Override
	public void readExternal(DataInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		authorityUuid = new UUID(in.readLong(), in.readLong());
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(authorityUuid.getMostSignificantBits());
		out.writeLong(authorityUuid.getLeastSignificantBits());
		writeDenotation(out);
	}
	
	public abstract void writeDenotation(DataOutput out) throws IOException;

	
	public abstract Object getDenotation();
	
	public abstract IDENTIFIER_PART_TYPES getIdType();

	public UUID getAuthorityUuid() {
		return authorityUuid;
	}

	public void setAuthorityUuid(UUID authorityUuid) {
		this.authorityUuid = authorityUuid;
	}

}
