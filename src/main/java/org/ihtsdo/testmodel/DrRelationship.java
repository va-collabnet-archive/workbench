package org.ihtsdo.testmodel;

import java.util.List;

public class DrRelationship extends DrComponent{
	private String primordialUuid;
	
	private String sourceUuid;
	private String typeUuid;
	private String targetUuid;
	private String characteristicUuid;
	private String modifierUuid;
	private int relGroup;
	
	private List<DrIdentifier> identifiers;
	
	//Inferred properties
	// none yet

	public DrRelationship() {
	}

	public String getPrimordialUuid() {
		return primordialUuid;
	}

	public void setPrimordialUuid(String primordialUuid) {
		this.primordialUuid = primordialUuid;
	}

	public String getCharacteristicUuid() {
		return characteristicUuid;
	}

	public void setCharacteristicUuid(String characteristicUuid) {
		this.characteristicUuid = characteristicUuid;
	}

	public int getRelGroup() {
		return relGroup;
	}

	public void setRelGroup(int relGroup) {
		this.relGroup = relGroup;
	}

	public String getTypeUuid() {
		return typeUuid;
	}

	public void setTypeUuid(String typeUuid) {
		this.typeUuid = typeUuid;
	}

	public List<DrIdentifier> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(List<DrIdentifier> identifiers) {
		this.identifiers = identifiers;
	}

	public String getModifierUuid() {
		return modifierUuid;
	}

	public void setModifierUuid(String modifierUuid) {
		this.modifierUuid = modifierUuid;
	}

	public String getSourceUuid() {
		return sourceUuid;
	}

	public void setSourceUuid(String sourceUuid) {
		this.sourceUuid = sourceUuid;
	}

	public String getTargetUuid() {
		return targetUuid;
	}

	public void setTargetUuid(String targetUuid) {
		this.targetUuid = targetUuid;
	}

}
