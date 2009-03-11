package org.dwfa.tapi.impl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ExtendLocally;
import org.dwfa.tapi.I_ExtendUniversally;
import org.dwfa.tapi.I_ExtendWithInteger;
import org.dwfa.tapi.I_ManifestLocally;
import org.dwfa.tapi.TerminologyException;

public class LocalFixedIntExtension implements I_ExtendWithInteger, I_ExtendLocally {
	private int nid;
	private int intExtension;
	

	private void readObject(ObjectInputStream in) throws IOException {
		throw new IOException("Local components cannot be serialized");
	}
	private void writeObject(ObjectOutputStream in) throws IOException {
		throw new IOException("Local components cannot be serialized");
	}
	public LocalFixedIntExtension(int nid, int intExtension) {
		super();
		this.nid = nid;
		this.intExtension = intExtension;
	}

	public int getIntExtension() {
		return intExtension;
	}

	public boolean isUniversal() {
		return false;
	}

	public I_ExtendUniversally universalize() throws IOException, TerminologyException {
		return new UniversalFixedIntExtension(getUids(), intExtension);
	}

	public Collection<UUID> getUids() throws IOException, TerminologyException {
		return LocalFixedTerminology.getStore().getUids(nid);
	}
	public I_ManifestLocally getExtension(I_ConceptualizeLocally extensionType) throws IOException, TerminologyException {
		return LocalFixedTerminology.getStore().getExtension(this, extensionType);
	}

	public int getNid() {
		return nid;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		LocalFixedIntExtension another = (LocalFixedIntExtension) obj;
		return nid == another.nid; 
	}

	@Override
	public int hashCode() {
		return nid;
	}

	public PropertyDescriptor[] getDataDescriptors() throws IntrospectionException {
		return new PropertyDescriptor[] { new PropertyDescriptor("intExtension", this.getClass(), "getIntExtension", null) };
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " nid: " + nid + " intExtension: " + intExtension;
	}
}
