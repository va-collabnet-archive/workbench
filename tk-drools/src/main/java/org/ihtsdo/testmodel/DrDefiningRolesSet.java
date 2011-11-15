package org.ihtsdo.testmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

public class DrDefiningRolesSet {
	
	private String rolesSetType;
	private List<DrRelationship> relationships;
	
	//Inferred properties
	private int numberOfIsas = 0;
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		try {
			sb.append("RolesSetType: " + rolesSetType + ",");
			sb.append("NumberOfIsas: " + numberOfIsas + ",");
			sb.append(" DRCOMPONENT FIELDS: {" + super.toString() + "}, ");

			sb.append("\nRelationships: " + relationships);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
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
		if (groupCondition.equals("ungrouped")) {
			int counter = 0;
			for (DrRelationship loopRel : relationships) {
				if (loopRel.getTypeUuid().equals(typeUuid) && loopRel.getRelGroup() == 0 &&
						loopRel.getStatusUuid().equals("d12702ee-c37f-385f-a070-61d56d4d0f1f")) {
					counter++;
				}
			}
			if (counter >= min && counter <= max) {
				result = true;
			}
		} if (groupCondition.equals("same group")) {
			result = true;
			Map<Integer, Integer> countMap = new HashMap<Integer,Integer>();
			for (DrRelationship loopRel : relationships) {
				if (loopRel.getTypeUuid().equals(typeUuid) &&
						loopRel.getStatusUuid().equals("d12702ee-c37f-385f-a070-61d56d4d0f1f")) {
					int group = loopRel.getRelGroup();
					if (countMap.containsKey(group)) {
						countMap.put(group, countMap.get(group) + 1);
					} else {
						countMap.put(group, 1);
					}
				}
			}
			for (int group : countMap.keySet()) {
				if (countMap.get(group) < min || countMap.get(group) > max) {
					result = false;
				}
			}
		} else { // "any" or unknown value
			int counter = 0;
			for (DrRelationship loopRel : relationships) {
				if (loopRel.getTypeUuid().equals(typeUuid) &&
						loopRel.getStatusUuid().equals("d12702ee-c37f-385f-a070-61d56d4d0f1f")) {
					counter++;
				}
			}
			if (counter >= min && counter <= max) {
				result = true;
			}
		} 
		
		return result;
	}

}
