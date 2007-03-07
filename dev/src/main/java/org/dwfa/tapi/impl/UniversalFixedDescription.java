package org.dwfa.tapi.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.I_ManifestUniversally;
import org.dwfa.tapi.I_StoreLocalFixedTerminology;
import org.dwfa.tapi.I_StoreUniversalFixedTerminology;
import org.dwfa.tapi.TerminologyException;

public class UniversalFixedDescription implements I_DescribeConceptUniversally {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Collection<UUID> uids;

	private Collection<UUID> statusUids;

	private Collection<UUID> conceptUids;

	private boolean initialCapSig;

	private Collection<UUID> descTypeUids;

	private String text;

	private String langCode;

	public UniversalFixedDescription(Collection<UUID> uids,
			Collection<UUID> statusUids, Collection<UUID> conceptUids,
			boolean initialCapSig, Collection<UUID> descTypeUids, String text,
			String langCode) {
		super();
		this.uids = uids;
		this.statusUids = statusUids;
		this.conceptUids = conceptUids;
		this.initialCapSig = initialCapSig;
		this.descTypeUids = descTypeUids;
		this.text = text;
		this.langCode = langCode;
	}

	public boolean isUniversal() {
		return true;
	}

	public I_DescribeConceptLocally localize()
			throws IOException, TerminologyException {
		I_StoreLocalFixedTerminology localTarget = LocalFixedTerminology.getStore();
		return new LocalFixedDesc(localTarget.getNid(uids), localTarget
				.getNid(statusUids), localTarget.getNid(conceptUids),
				initialCapSig, localTarget.getNid(descTypeUids), text,
				langCode);
	}

	public I_ConceptualizeUniversally getConcept() {
		return UniversalFixedConcept.get(conceptUids);
	}

	public I_ConceptualizeUniversally getDescType() {
		return UniversalFixedConcept.get(descTypeUids);
	}

	public I_ConceptualizeUniversally getStatus() {
		return UniversalFixedConcept.get(statusUids);
	}

	public String getLangCode() {
		return langCode;
	}

	public String getText() {
		return text;
	}

	public boolean isInitialCapSig() {
		return initialCapSig;
	}

	public Collection<UUID> getUids() {
		return uids;
	}

	public String toString() {
		return text + " (desc: " + uids +")";
	}
	public I_ManifestUniversally getExtension(I_ConceptualizeUniversally extensionType, I_StoreUniversalFixedTerminology extensionServer) throws IOException, TerminologyException {
		return extensionServer.getUniversalExtension(this, extensionType);
	}

}
