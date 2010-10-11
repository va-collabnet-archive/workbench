package org.ihtsdo.testmodel;

import java.util.ArrayList;
import java.util.List;

public class DrDefiningRolesSet {
	
	private String rolesSetType;
	private List<DrRelationship> relationships;
	
	//Inferred properties
	private int numberOfIsas = 0;
	
	public DrDefiningRolesSet() {
		super();
		this.relationships = new ArrayList<DrRelationship>();
	}

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
	
	// GroupCondition options = 'any','ungrouped','same group'
	public boolean checkCardinality(String typeUuid, int min, int max, String groupCondition) {
		boolean result = false;
		int counter = 0;
		for (DrRelationship loopRel : relationships) {
			if (loopRel.getTypeUuid().equals(typeUuid)) {
				counter++;
				//TODO:use groupCondition
			}
		}
		if (counter >= min && counter <= max) {
			result = true;
		}
		return result;
	}

}
