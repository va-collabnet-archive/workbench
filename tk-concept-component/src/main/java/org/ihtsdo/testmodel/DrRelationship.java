package org.ihtsdo.testmodel;

import java.util.List;

public class DrRelationship {
	private String primordialUuid;
	
	private String c1Uuid;
	private String c2Uuid;
	private String characteristicUuid;
	private String refinabilityUuid;
	private int relGroup;
	private String typeUuid;
	
	private List<DrIdentifier> identifiers;

	private String statusUuid;
	private String pathUuid;
	private String authorUuid;
	private Long time;
	
	private String factContextName;

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

	public String getRefinabilityUuid() {
		return refinabilityUuid;
	}

	public void setRefinabilityUuid(String refinabilityUuid) {
		this.refinabilityUuid = refinabilityUuid;
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

	public String getStatusUuid() {
		return statusUuid;
	}

	public void setStatusUuid(String statusUuid) {
		this.statusUuid = statusUuid;
	}

	public String getPathUuid() {
		return pathUuid;
	}

	public void setPathUuid(String pathUuid) {
		this.pathUuid = pathUuid;
	}

	public String getAuthorUuid() {
		return authorUuid;
	}

	public void setAuthorUuid(String authorUuid) {
		this.authorUuid = authorUuid;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getFactContextName() {
		return factContextName;
	}

	public void setFactContextName(String factContextName) {
		this.factContextName = factContextName;
	}

}
