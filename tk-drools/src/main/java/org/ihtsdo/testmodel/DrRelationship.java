package org.ihtsdo.testmodel;

import java.util.List;

public class DrRelationship extends DrComponent{
	private String primordialUuid;
	
	private String c1Uuid;
	private String c2Uuid;
	private String characteristicUuid;
	private String modifierUuid;
	private int relGroup;
	private String typeUuid;
	
	private List<DrIdentifier> identifiers;

	public DrRelationship() {
	}

	public String getPrimordialUuid() {
		return primordialUuid;
	}

	public void setPrimordialUuid(String primordialUuid) {
		this.primordialUuid = primordialUuid;
	}

	public String getC1Uuid() {
		return c1Uuid;
	}

	public void setC1Uuid(String uuid) {
		c1Uuid = uuid;
	}

	public String getC2Uuid() {
		return c2Uuid;
	}

	public void setC2Uuid(String uuid) {
		c2Uuid = uuid;
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

}
