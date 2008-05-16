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
	public UniversalAceConceptAttributes() {
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
	public void setConId(Collection<UUID> conId) {
		this.conId = conId;
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
	public void setVersions(List<UniversalAceConceptAttributesPart> versions) {
		this.versions = versions;
	}
	// END: ADDED TO IMPLEMENT JAVABEANS SPEC
	
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
