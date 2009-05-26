package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

public class UniversalAceExtByRefPartConceptConceptConcept extends
		UniversalAceExtByRefPart {

	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private Collection<UUID> c1UuidCollection;
	private Collection<UUID> c2UuidCollection;
	private Collection<UUID> c3UuidCollection;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(c1UuidCollection);
		out.writeObject(c2UuidCollection);
		out.writeObject(c3UuidCollection);
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			c1UuidCollection = (Collection<UUID>) in.readObject();
			c2UuidCollection = (Collection<UUID>) in.readObject();
			c3UuidCollection = (Collection<UUID>) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public Collection<UUID> getC1UuidCollection() {
		return c1UuidCollection;
	}

	public void setC1UuidCollection(Collection<UUID> uuidCollection) {
		c1UuidCollection = uuidCollection;
	}

	public Collection<UUID> getC2UuidCollection() {
		return c2UuidCollection;
	}

	public void setC2UuidCollection(Collection<UUID> uuidCollection) {
		c2UuidCollection = uuidCollection;
	}

	public Collection<UUID> getC3UuidCollection() {
		return c3UuidCollection;
	}

	public void setC3UuidCollection(Collection<UUID> uuidCollection) {
		c3UuidCollection = uuidCollection;
	}
}
