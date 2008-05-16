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
	
	// START: ADDED TO IMPLEMENT JAVABEANS SPEC
	/**
	 * DO NOT USE THIS METHOD.
	 * 
	 * This method has been included to meet the JavaBeans specification,
	 * however it should not be used as it allows access to attributes that
	 * should not be modifiable and weakens the interface. The method has been
	 * added as a convenience to allow JavaBeans tools access via introspection
	 * but is not intended for general use by developers.
	 * 
	 * @deprecated
	 */
	public UniversalAceDescription() {
		super();
	}	
	
	/**
	 * DO NOT USE THIS METHOD.
	 * 
	 * This method has been included to meet the JavaBeans specification,
	 * however it should not be used as it allows access to attributes that
	 * should not be modifiable and weakens the interface. The method has been
	 * added as a convenience to allow JavaBeans tools access via introspection
	 * but is not intended for general use by developers.
	 * 
	 * @deprecated
	 */
	public void setDescId(Collection<UUID> descId) {
		this.descId = descId;
	}
	
	/**
	 * DO NOT USE THIS METHOD.
	 * 
	 * This method has been included to meet the JavaBeans specification,
	 * however it should not be used as it allows access to attributes that
	 * should not be modifiable and weakens the interface. The method has been
	 * added as a convenience to allow JavaBeans tools access via introspection
	 * but is not intended for general use by developers.
	 * 
	 * @deprecated
	 */
	public void setConceptId(Collection<UUID> conceptId) {
		this.conceptId = conceptId;
	}

	/**
	 * DO NOT USE THIS METHOD.
	 * 
	 * This method has been included to meet the JavaBeans specification,
	 * however it should not be used as it allows access to attributes that
	 * should not be modifiable and weakens the interface. The method has been
	 * added as a convenience to allow JavaBeans tools access via introspection
	 * but is not intended for general use by developers.
	 * 
	 * @deprecated
	 */
	public void setVersions(List<UniversalAceDescriptionPart> versions) {
		this.versions = versions;
	}
	// END: ADDED TO IMPLEMENT JAVABEANS SPEC

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
	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(this.getClass().getSimpleName());
		buff.append(": ");
		buff.append(descId);
		buff.append(" conId:");
		buff.append(conceptId);
		buff.append("\n");
		for (UniversalAceDescriptionPart part : versions) {
			buff.append("     ");
			buff.append(part.toString());
			buff.append("\n");
		}

		return buff.toString();
	}


}
