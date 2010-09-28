package org.ihtsdo.testmodel;

import java.util.List;

public class DrDefiningRolesSet {
	
	private String rolesSetType;
	private List<DrRelationship> relationships;
	
	//Inferred properties
	private int numberOfIsas;

	public String getRolesSetType() {
		return rolesSetType;
	}

	public void setRolesSetType(String rolesSetType) {
		this.rolesSetType = rolesSetType;
	}

	public List<DrRelationship> getRelationships() {
		return relationships;
	}

	public void setRelationships(List<DrRelationship> relationships) {
		this.relationships = relationships;
	}

	public int getNumberOfIsas() {
		return numberOfIsas;
	}

	public void setNumberOfIsas(int numberOfIsas) {
		this.numberOfIsas = numberOfIsas;
	}

}
