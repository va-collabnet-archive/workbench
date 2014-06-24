package org.ihtsdo.rf2.core.dao;

import java.util.UUID;

/**
 * Title: FileName Description: Reads Properties File to know file names requires to export Core data in RF2 format Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RelationshipDAO implements Comparable<RelationshipDAO> {

	public String relationshipId;
	public String effectiveTime;
	public String active;
	public String moduleId;
	public String sourceId;
	public String destinationId;
	public int relationshipGroup;
	public String relTypeId;
	public String characteristicTypeId;
	public String modifierId;

	public boolean invalidState;
	public String statusType;
	public String characteristicType;
	public String refinability;
	public Boolean writtenToFileFlag = Boolean.FALSE;

	/** for Historical Extraction **/
	public String refsetId;
	public String referencedComponentId;
	public String targetComponent;
	public UUID uuid;
	/**
	 * 
	 */
	public String uuidstr;

	public String getUuidstr() {
		return uuidstr;
	}

	public void setUuidstr(String uuidstr) {
		this.uuidstr = uuidstr;
	}

	public boolean isInvalidState() {
		return this.invalidState;
	}

	public void setInvalidState(boolean invalidState) {
		this.invalidState = invalidState;
	}

	public void setStatusType(String statusType) {
		this.statusType = statusType;
	}

	public String getStatusType() {
		return this.statusType;
	}

	public void setCharacteristicType(String characteristicType) {
		this.characteristicType = characteristicType;
	}

	public String getCharacteristicType() {
		return this.characteristicType;
	}

	public void setRefinability(String refinability) {
		this.refinability = refinability;
	}

	public String getRefinability() {
		return this.refinability;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(String destinationId) {
		this.destinationId = destinationId;
	}

	public String getRelationshipId() {
		return relationshipId;
	}

	public void setRelationshipId(String relationshipId) {
		this.relationshipId = relationshipId;
	}

	public String getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(String effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getRelTypeId() {
		return relTypeId;
	}

	public void setRelTypeId(String relTypeId) {
		this.relTypeId = relTypeId;
	}

	public String getCharacteristicTypeId() {
		return characteristicTypeId;
	}

	public void setCharacteristicTypeId(String characteristicTypeId) {
		this.characteristicTypeId = characteristicTypeId;
	}

	public String getModifierId() {
		return modifierId;
	}

	public void setModifierId(String modifierId) {
		this.modifierId = modifierId;
	}

	@Override
	public String toString() {
		return "RelationshipDAO [" + ", relationshipId=" + relationshipId + ", effectiveTime=" + effectiveTime + ", sourceId=" + sourceId + ", destinationId=" + destinationId + ", relTypeId="
				+ relTypeId + ", active=" + active + ", characteristicTypeId=" + characteristicTypeId + ", invalidState=" + invalidState + ", characteristicType=" + characteristicType
				+ ", modifierId=" + modifierId + ", moduleId=" + moduleId + ", relationshipGroup=" + relationshipGroup + "]";
	}

	public RelationshipDAO() {

	}

	public RelationshipDAO(String relationshipId, String effectiveTime, String active, String sourceId, String destinationId, String relTypeId) {
		this.relationshipId = relationshipId;
		this.effectiveTime = effectiveTime;
		this.active = active;
		this.sourceId = sourceId;
		this.destinationId = destinationId;
		this.relTypeId = relTypeId;

	}

	public RelationshipDAO(String relationshipId, String active, String moduleId, String sourceId, String destinationId, int relationshipGroup, String relTypeId, String characteristicTypeId,
			String modifierId) {
		this.relationshipId = relationshipId;
		this.active = active;
		this.moduleId = moduleId;
		this.sourceId = sourceId;
		this.destinationId = destinationId;
		this.relationshipGroup = relationshipGroup;
		this.relTypeId = relTypeId;
		this.characteristicTypeId = characteristicTypeId;
		this.modifierId = modifierId;
	}

	public RelationshipDAO(String relationshipId, String effectiveTime, String active, String moduleId, String sourceId, String destinationId, int relationshipGroup, String relTypeId,
			String characteristicTypeId, String modifierId) {
		this.relationshipId = relationshipId;
		this.effectiveTime = effectiveTime;
		this.active = active;
		this.moduleId = moduleId;
		this.sourceId = sourceId;
		this.destinationId = destinationId;
		this.relationshipGroup = relationshipGroup;
		this.relTypeId = relTypeId;
		this.characteristicTypeId = characteristicTypeId;
		this.modifierId = modifierId;
	}

	public RelationshipDAO(String active, String moduleId, String sourceId, String destinationId, int relationshipGroup, String relTypeId, String characteristicTypeId, String modifierId) {
		this.active = active;
		this.moduleId = moduleId;
		this.sourceId = sourceId;
		this.destinationId = destinationId;
		this.relationshipGroup = relationshipGroup;
		this.relTypeId = relTypeId;
		this.characteristicTypeId = characteristicTypeId;
		this.modifierId = modifierId;
	}
	
	
	public RelationshipDAO(String uuidstr, String relationshipId, String effectiveTime, String active, String moduleId, String sourceId, String destinationId, int relationshipGroup, String relTypeId,
			String characteristicTypeId, String modifierId) {
		this.uuidstr= uuidstr;
		this.relationshipId = relationshipId;
		this.effectiveTime = effectiveTime;
		this.active = active;
		this.moduleId = moduleId;
		this.sourceId = sourceId;
		this.destinationId = destinationId;
		this.relationshipGroup = relationshipGroup;
		this.relTypeId = relTypeId;
		this.characteristicTypeId = characteristicTypeId;
		this.modifierId = modifierId;
	}

	public void updateFrom(RelationshipDAO relDAOFrom) {
		this.relationshipId = relDAOFrom.getRelationshipId();
		this.active = relDAOFrom.getActive();
		this.moduleId = relDAOFrom.getModuleId();
		this.sourceId = relDAOFrom.getSourceId();
		this.destinationId = relDAOFrom.getDestinationId();
		this.relationshipGroup = relDAOFrom.getRelationshipGroup();
		this.relTypeId = relDAOFrom.getRelTypeId();
		this.characteristicTypeId = relDAOFrom.getCharacteristicTypeId();
		this.modifierId = relDAOFrom.getModifierId();
	}

	public int getRelationshipGroup() {
		return relationshipGroup;
	}

	public void setRelationshipGroup(int relationshipGroup) {
		this.relationshipGroup = relationshipGroup;
	}

	public void setWrittenToFileFlag(Boolean writtenToFileFlag) {
		this.writtenToFileFlag = writtenToFileFlag;
	}

	public Boolean isWrittenToFileFlag() {
		return writtenToFileFlag;
	}

	public String getRefsetId() {
		return refsetId;
	}

	public void setRefsetId(String refsetId) {
		this.refsetId = refsetId;
	}

	public String getReferencedComponentId() {
		return referencedComponentId;
	}

	public void setReferencedComponentId(String referencedComponentId) {
		this.referencedComponentId = referencedComponentId;
	}

	public String getTargetComponent() {
		return targetComponent;
	}

	public void setTargetComponent(String targetComponent) {
		this.targetComponent = targetComponent;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public Boolean getWrittenToFileFlag() {
		return writtenToFileFlag;
	}

	/*
	 * public int compareTo(RelationshipDAO o1, RelationshipDAO o2) { logger.error(o1.toString()); logger.error(o2.toString()); //int thisMore = 1; int thisLess = -1; // Sorting by
	 * relationshipId , effectiveTime,sourceId,destinationId,relTypeId,active if (Integer.parseInt(o1.relationshipId) > Integer.parseInt(o2.relationshipId)) { return thisLess; } else if
	 * (Integer.parseInt(o1.effectiveTime) > Integer.parseInt(o2.effectiveTime)) { return thisLess; } else if (Integer.parseInt(o1.sourceId) > Integer.parseInt(o2.sourceId)) { return thisLess; } else
	 * if (Integer.parseInt(o1.destinationId) > Integer.parseInt(o2.destinationId)) { return thisLess; } else if (Integer.parseInt(o1.relTypeId) > Integer.parseInt(o2.relTypeId)) { return thisLess; }
	 * else if (Integer.parseInt(o1.active) > Integer.parseInt(o2.active)) { return thisLess; }
	 * 
	 * return thisLess; //return s1.compareTo(s2); }
	 */

	// Overload compareTo method
	public int compareTo(RelationshipDAO prevObject) {
		RelationshipDAO tmp = (RelationshipDAO) prevObject;
		if (!this.relationshipId.equals(tmp.relationshipId) && (!this.relationshipId.equals(""))) {

			// First compare on relationshipId
			if (Long.parseLong(this.relationshipId) < Long.parseLong(prevObject.relationshipId))
				return -1;
			if (Long.parseLong(this.relationshipId) > Long.parseLong(prevObject.relationshipId))
				return 1;
			return 0;
		} else if (!this.effectiveTime.equals(prevObject.effectiveTime)) {
			// Next, compare on the effectiveTime
			/*
			 * int i = (this.effectiveTime).compareTo(prevObject.effectiveTime); if (i < 0) return -1; if (i > 1) return 1;
			 */
			// logger.error("Inside effectiveTime else if condition==================>");

			if (Long.parseLong(this.effectiveTime) < Long.parseLong(prevObject.effectiveTime))
				return -1;
			if (Long.parseLong(this.effectiveTime) > Long.parseLong(prevObject.effectiveTime))
				return 1;
			return 0;
		} else if (Long.parseLong(this.destinationId) != Long.parseLong(prevObject.destinationId)) {
			// Next, compare on the destinationId
			if (Long.parseLong(this.destinationId) < Long.parseLong(prevObject.destinationId))
				return -1;
			if (Long.parseLong(this.destinationId) > Long.parseLong(prevObject.destinationId))
				return 1;
			return 0;
		} else if (Long.parseLong(this.active) != Long.parseLong(prevObject.active)) {
			// Next, compare on the active
			if (Long.parseLong(this.active) > Long.parseLong(prevObject.active))
				return -1;
			if (Long.parseLong(this.active) < Long.parseLong(prevObject.active))
				return 1;
			return 0;
		} else if (Long.parseLong(this.sourceId) != Long.parseLong(prevObject.sourceId)) {
			// Next, compare on the sourceId
			if (Long.parseLong(this.sourceId) < Long.parseLong(prevObject.sourceId))
				return -1;
			if (Long.parseLong(this.sourceId) > Long.parseLong(prevObject.sourceId))
				return 1;
			return 0;
		} else {
			// Nothing else to compare, just return 0
			return 0;
		}
	}

}
