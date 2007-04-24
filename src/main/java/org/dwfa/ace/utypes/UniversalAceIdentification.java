package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UniversalAceIdentification implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<UniversalAceIdentificationPart> versions;
	
	public UniversalAceIdentification(int count) {
		super();
		this.versions = new ArrayList<UniversalAceIdentificationPart>(count);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdVersioned#getVersions()
	 */
	public List<UniversalAceIdentificationPart> getVersions() {
		return versions;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdVersioned#getUIDs()
	 */
	public List<UUID> getUIDs() {
		List<UUID> uids = new ArrayList<UUID>(versions.size());
		for (UniversalAceIdentificationPart p: versions) {
			if (UUID.class.isAssignableFrom(p.getSourceId().getClass())) {
				uids.add((UUID) p.getSourceId());
			}
		}
		return uids;
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdVersioned#addVersion(org.dwfa.vodb.types.I_IdPart)
	 */
	public boolean addVersion(UniversalAceIdentificationPart srcId) {
		int index = versions.size() - 1;
		if (index == -1) {
			return versions.add(srcId);
		} else if (index >= 0) {
			return versions.add(srcId);
		}
		return false;
	}
	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(this.getClass().getSimpleName());
		buff.append(": ");
		buff.append("\n");
		for (UniversalAceIdentificationPart part : versions) {
			buff.append("     ");
			buff.append(part.toString());
			buff.append("\n");
		}

		return buff.toString();
	}
	
}
