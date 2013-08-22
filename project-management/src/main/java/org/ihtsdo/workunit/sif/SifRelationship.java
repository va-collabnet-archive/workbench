package org.ihtsdo.workunit.sif;


public class SifRelationship extends SifTerminologyComponent {
	
	private SifIdentifier sourceId;
	private SifIdentifier typeId;
	private SifIdentifier destinationId;
	private SifIdentifier characteristicTypeId;
	private SifIdentifier modifierId;
	private int relationshipGroup;
	
	public SifRelationship() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the sourceId
	 */
	public SifIdentifier getSourceId() {
		return sourceId;
	}

	/**
	 * @param sourceId the sourceId to set
	 */
	public void setSourceId(SifIdentifier sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * @return the typeId
	 */
	public SifIdentifier getTypeId() {
		return typeId;
	}

	/**
	 * @param typeId the typeId to set
	 */
	public void setTypeId(SifIdentifier typeId) {
		this.typeId = typeId;
	}

	/**
	 * @return the destinationId
	 */
	public SifIdentifier getDestinationId() {
		return destinationId;
	}

	/**
	 * @param destinationId the destinationId to set
	 */
	public void setDestinationId(SifIdentifier destinationId) {
		this.destinationId = destinationId;
	}

	/**
	 * @return the characteristicTypeId
	 */
	public SifIdentifier getCharacteristicTypeId() {
		return characteristicTypeId;
	}

	/**
	 * @param characteristicTypeId the characteristicTypeId to set
	 */
	public void setCharacteristicTypeId(SifIdentifier characteristicTypeId) {
		this.characteristicTypeId = characteristicTypeId;
	}

	/**
	 * @return the modifierId
	 */
	public SifIdentifier getModifierId() {
		return modifierId;
	}

	/**
	 * @param modifierId the modifierId to set
	 */
	public void setModifierId(SifIdentifier modifierId) {
		this.modifierId = modifierId;
	}

	/**
	 * @return the relationshipGroup
	 */
	public int getRelationshipGroup() {
		return relationshipGroup;
	}

	/**
	 * @param relationshipGroup the relationshipGroup to set
	 */
	public void setRelationshipGroup(int relationshipGroup) {
		this.relationshipGroup = relationshipGroup;
	}

}
