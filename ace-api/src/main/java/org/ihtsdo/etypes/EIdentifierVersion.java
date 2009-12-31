package org.ihtsdo.etypes;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

public abstract class EIdentifierVersion extends EVersion implements Externalizable {

	protected UUID authorityUuid;

	public EIdentifierVersion(ObjectInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}

	public EIdentifierVersion() {
		super();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		authorityUuid = new UUID(in.readLong(), in.readLong());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(authorityUuid.getMostSignificantBits());
		out.writeLong(authorityUuid.getLeastSignificantBits());
		writeDenotation(out);
	}
	
	public abstract void writeDenotation(ObjectOutput out) throws IOException;

	
	public abstract Object getDenotation();

}
