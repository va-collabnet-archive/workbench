package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UniversalAceRelationship implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Collection<UUID> relId;

	private Collection<UUID> componentOneId;

	private Collection<UUID> componentTwoId;

	private List<UniversalAceRelationshipPart> versions;

	public UniversalAceRelationship(Collection<UUID> relId, Collection<UUID> componentOneId, Collection<UUID> componentTwoId,
			int count) {
		super();
		this.relId = relId;
		this.componentOneId = componentOneId;
		this.componentTwoId = componentTwoId;
		this.versions = new ArrayList<UniversalAceRelationshipPart>(count);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#addVersion(org.dwfa.vodb.types.I_RelPart)
	 */
	public boolean addVersion(UniversalAceRelationshipPart rel) {
		int index = versions.size() - 1;
		if (index == -1) {
			return versions.add(rel);
		} else if (index >= 0) {
			return versions.add(rel);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#getVersions()
	 */
	public List<UniversalAceRelationshipPart> getVersions() {
		return versions;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#versionCount()
	 */
	public int versionCount() {
		return versions.size();
	}


	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#getC1Id()
	 */
	public Collection<UUID> getC1Id() {
		return componentOneId;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#getC2Id()
	 */
	public Collection<UUID> getC2Id() {
		return componentTwoId;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#getRelId()
	 */
	public Collection<UUID> getRelId() {
		return relId;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#setC2Id(int)
	 */
	public void setC2Id(Collection<UUID> destId) {
		componentTwoId = destId;
		
	}

}
