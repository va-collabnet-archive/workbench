package org.ihtsdo.workunit.sif;

import java.util.List;

public class SifConcept extends SifTerminologyComponent {
	
	private SifIdentifier definitionStatusId;
	
	private List<SifDescription> descriptions;
	private List<SifRelationship> relationships;
	private List<SifRefsetMemberSimpleType> members;
	
	public SifConcept() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the definitionStatusId
	 */
	public SifIdentifier getDefinitionStatusId() {
		return definitionStatusId;
	}

	/**
	 * @param definitionStatusId the definitionStatusId to set
	 */
	public void setDefinitionStatusId(SifIdentifier definitionStatusId) {
		this.definitionStatusId = definitionStatusId;
	}

	/**
	 * @return the descriptions
	 */
	public List<SifDescription> getDescriptions() {
		return descriptions;
	}

	/**
	 * @param descriptions the descriptions to set
	 */
	public void setDescriptions(List<SifDescription> descriptions) {
		this.descriptions = descriptions;
	}

	/**
	 * @return the relationships
	 */
	public List<SifRelationship> getRelationships() {
		return relationships;
	}

	/**
	 * @param relationships the relationships to set
	 */
	public void setRelationships(List<SifRelationship> relationships) {
		this.relationships = relationships;
	}

	/**
	 * @return the members
	 */
	public List<SifRefsetMemberSimpleType> getMembers() {
		return members;
	}

	/**
	 * @param members the members to set
	 */
	public void setMembers(List<SifRefsetMemberSimpleType> members) {
		this.members = members;
	}


}
