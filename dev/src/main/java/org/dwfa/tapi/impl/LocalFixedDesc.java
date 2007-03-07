package org.dwfa.tapi.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.I_ManifestLocally;
import org.dwfa.tapi.I_StoreLocalFixedTerminology;
import org.dwfa.tapi.TerminologyException;

public class LocalFixedDesc implements I_DescribeConceptLocally, Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int nid;

	private int statusNid;

	private int conceptNid;

	private boolean initialCapSig;

	private int descTypeNid;

	private String text;

	private String langCode;

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		try {
			int uidCount = in.readInt();
			Collection<UUID> uids = new ArrayList<UUID>(uidCount);
			for (int i = 0; i < uidCount; i++) {
				long msb = in.readLong();
				long lsb = in.readLong();
				uids.add(new UUID(msb, lsb));
			}
			nid = LocalFixedTerminology.getStore().getNid(uids);
		} catch (Exception e) {
			IOException ioe = new IOException();
			ioe.initCause(e);
			throw ioe;
		}
	}

	private Object readResolve() throws ObjectStreamException {
		try {
			return LocalFixedTerminology.getStore().getDescription(nid);
		} catch (Exception e) {
			ObjectStreamException oes = new InvalidObjectException(e
					.getMessage());
			oes.initCause(e);
			throw oes;
		}
	}
	public static I_DescribeConceptLocally get(UUID conceptUid,
			I_StoreLocalFixedTerminology sourceServer) throws Exception {
		int nid = sourceServer.getNid(conceptUid);
		return sourceServer.getDescription(nid);
	}

	public static I_DescribeConceptLocally get(Collection<UUID> uids,
			I_StoreLocalFixedTerminology sourceServer) throws Exception {
		for (UUID id : uids) {
			int nid;
			try {
				nid = sourceServer.getNid(id);
				return sourceServer.getDescription(nid);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		throw new Exception("Can't find: " + uids);
	}


	public void writeExternal(ObjectOutput out) throws IOException {
		try {
			Collection<UUID> uids = LocalFixedTerminology.getStore().getUids(nid);
			out.writeInt(uids.size());
			for (UUID uid : uids) {
				out.writeLong(uid.getMostSignificantBits());
				out.writeLong(uid.getLeastSignificantBits());
			}
		} catch (Exception e) {
			IOException ioe = new IOException();
			ioe.initCause(e);
			throw ioe;
		}
	}

	public LocalFixedDesc() {
		super();
	}

	public LocalFixedDesc(int nid, int statusNid, int conceptNid,
			boolean initialCapSig, int descTypeNid, String text,
			String lanCode) {
		super();
		this.nid = nid;
		this.statusNid = statusNid;
		this.conceptNid = conceptNid;
		this.initialCapSig = initialCapSig;
		this.descTypeNid = descTypeNid;
		this.text = text;
		this.langCode = lanCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.termviewer.I_Describe#getConcept()
	 */
	public I_ConceptualizeLocally getConcept() {
		return LocalFixedConcept.get(conceptNid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.termviewer.I_Describe#getDescType()
	 */
	public I_ConceptualizeLocally getDescType() {
		return LocalFixedConcept.get(descTypeNid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.termviewer.I_Describe#isInitialCapSig()
	 */
	public boolean isInitialCapSig() {
		return initialCapSig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.termviewer.I_Describe#getLangCode()
	 */
	public String getLangCode() {
		return langCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.termviewer.I_Describe#getStatus()
	 */
	public I_ConceptualizeLocally getStatus() {
		return LocalFixedConcept.get(statusNid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.termviewer.I_Describe#getText()
	 */
	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return nid + ": " + text;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (I_DescribeConceptLocally.class.isAssignableFrom(obj.getClass())) {
			I_DescribeConceptLocally another = (I_DescribeConceptLocally) obj;
			return nid == another.getNid();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return nid;
	}

	public Collection<UUID> getUids() throws IOException, TerminologyException {
		return LocalFixedTerminology.getStore().getUids(nid);
	}

	public boolean isUniversal() {
		return false;
	}

	public I_DescribeConceptUniversally universalize() throws IOException, TerminologyException {
		return new UniversalFixedDescription(LocalFixedTerminology.getStore().getUids(nid),
				LocalFixedTerminology.getStore().getUids(statusNid), LocalFixedTerminology.getStore()
						.getUids(conceptNid), initialCapSig, LocalFixedTerminology.getStore()
						.getUids(descTypeNid), text, langCode);
	}

	public I_ManifestLocally getExtension(I_ConceptualizeLocally extensionType)
			throws IOException, TerminologyException {
		return LocalFixedTerminology.getStore().getExtension(this, extensionType);
	}

	public int getNid() {
		return nid;
	}

}
