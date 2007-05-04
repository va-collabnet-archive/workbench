package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UniversalAceConceptAttributes implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Collection<UUID> conId;

	private List<UniversalAceConceptAttributesPart> versions;

	public UniversalAceConceptAttributes(Collection<UUID> conId, int count) {
		super();
		this.conId = conId;
		this.versions = new ArrayList<UniversalAceConceptAttributesPart>(count);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#addVersion(org.dwfa.vodb.types.ThinConPart)
	 */
	public boolean addVersion(UniversalAceConceptAttributesPart part) {
		int index = versions.size() - 1;
		if (index == -1) {
			return versions.add(part);
		} else if (index >= 0) {
			return versions.add(part);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getVersions()
	 */
	public List<UniversalAceConceptAttributesPart> getVersions() {
		return versions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#versionCount()
	 */
	public int versionCount() {
		return versions.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getConId()
	 */
	public Collection<UUID> getConId() {
		return conId;
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(this.getClass().getSimpleName());
		buff.append(": ");
		buff.append(conId);
		buff.append("\n");
		for (UniversalAceConceptAttributesPart part : versions) {
			buff.append("     ");
			buff.append(part.toString());
			buff.append("\n");
		}

		return buff.toString();
	}

}
