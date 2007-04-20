package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UniversalAceDescription implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Collection<UUID> descId;

	private Collection<UUID> conceptId;

	private List<UniversalAceDescriptionPart> versions;

	public UniversalAceDescription(Collection<UUID> descId, Collection<UUID> conceptId, int count) {
		super();
		this.descId = descId;
		this.conceptId = conceptId;
		this.versions = new ArrayList<UniversalAceDescriptionPart>(count);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionVersioned#addVersion(org.dwfa.vodb.types.I_DescriptionPart)
	 */
	public boolean addVersion(UniversalAceDescriptionPart newPart) {
		int index = versions.size() - 1;
		if (index == -1) {
			return versions.add(newPart);
		} else if (index >= 0) {
			return versions.add(newPart);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionVersioned#getVersions()
	 */
	public List<UniversalAceDescriptionPart> getVersions() {
		return versions;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionVersioned#versionCount()
	 */
	public int versionCount() {
		return versions.size();
	}


	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionVersioned#getConceptId()
	 */
	public Collection<UUID> getConceptId() {
		return conceptId;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionVersioned#getDescId()
	 */
	public Collection<UUID> getDescId() {
		return descId;
	}


}
