package org.dwfa.tapi.impl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_ExtendLocally;
import org.dwfa.tapi.I_ExtendUniversally;
import org.dwfa.tapi.I_ExtendWithInteger;
import org.dwfa.tapi.I_ManifestUniversally;
import org.dwfa.tapi.I_StoreUniversalFixedTerminology;
import org.dwfa.tapi.TerminologyException;

public class UniversalFixedIntExtension implements I_ExtendWithInteger,
		I_ExtendUniversally {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Collection<UUID> memberUids;
	private int intExtension;
	
	
	public UniversalFixedIntExtension(Collection<UUID> memberUids, int intExtension) {
		super();
		this.memberUids = memberUids;
		this.intExtension = intExtension;
	}

	public int getIntExtension() {
		return intExtension;
	}

	public Collection<UUID> getUids() {
		return memberUids;
	}

	public I_ExtendLocally localize()
	throws IOException, TerminologyException {
		return new LocalFixedIntExtension(LocalFixedTerminology.getStore().getNid(memberUids), intExtension);
	}
	public I_ManifestUniversally getExtension(I_ConceptualizeUniversally extensionType, I_StoreUniversalFixedTerminology extensionServer) throws IOException, TerminologyException {
		return extensionServer.getUniversalExtension(this, extensionType);
	}

	public boolean isUniversal() {
		return true;
	}
	public PropertyDescriptor[] getDataDescriptors() throws IntrospectionException {
		return new PropertyDescriptor[] { new PropertyDescriptor("intExtension", this.getClass(), "getIntExtension", null) };
	}

}
