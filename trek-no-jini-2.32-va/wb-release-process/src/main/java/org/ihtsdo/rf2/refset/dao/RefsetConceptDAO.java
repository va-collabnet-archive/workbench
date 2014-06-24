package org.ihtsdo.rf2.refset.dao;

import java.util.UUID;
import java.util.Date;

public class RefsetConceptDAO {

	public UUID uuid;
	public String effectiveTime;
	public String active;
	public String moduleId;
	public String refsetId;
	public String referencedComponentId;
	public String valueId;
	public String status;
	public Boolean enableFlag = Boolean.TRUE;
	public Boolean writeFlag = Boolean.FALSE;
	public Boolean writtenToFileFlag = Boolean.FALSE;
	public String conceptId;
	public Date effectiveDate;

	// public String extractType = "Refinability"; // Default Value; Other values:
	// "Descriptions", "Refinability" , "Concepts"
	public String relTypeId;
	public String characteristicTypeId;
	public String characteristicType;

	public RefsetConceptDAO() {

	}

	public RefsetConceptDAO(UUID uuid, String effectiveTime,
			String active, String moduleId, String refsetId,
			String referencedComponentId, String valueId) {
			this.uuid = uuid;
			this.effectiveTime = effectiveTime;
			this.active = active;
			this.moduleId = moduleId;
			this.refsetId = refsetId;
			this.referencedComponentId = referencedComponentId;
			this.valueId = valueId;
	}

	public String toString() {
		return "RefsetConceptDAO [" + "uuid=" + uuid + ", " + "effectiveTime=" + effectiveTime + ", " + "active=" + active + ",  " + "moduleId=" + moduleId + ", " + "refsetId=" + refsetId + ", "
				+ "referencedComponentId=" + referencedComponentId + ", " + "valueId=" + valueId + ", " + "status=" + status + ", " + "conceptId=" + conceptId + ", " + "effectiveDate="
				+ effectiveDate + ", " + "relTypeId=" + relTypeId + ", " + "characteristicType=" + characteristicType + ", " + "characteristicTypeId=" + characteristicTypeId + ", " + "enableFlag="
				+ enableFlag + ", " + "writeFlag=" + writeFlag + ", " + "writtenToFileFlag=" + writtenToFileFlag + " ]";
	}

	public void updateFrom(RefsetConceptDAO refsetDAO) {
		this.uuid = refsetDAO.uuid;
		this.effectiveTime = refsetDAO.effectiveTime;
		this.active = refsetDAO.active;
		this.moduleId = refsetDAO.moduleId;
		this.refsetId = refsetDAO.refsetId;
		this.referencedComponentId = refsetDAO.referencedComponentId;
		this.valueId = refsetDAO.valueId;
		this.status = refsetDAO.status;
		this.conceptId = refsetDAO.conceptId;
		this.relTypeId = refsetDAO.relTypeId;
		this.characteristicTypeId = refsetDAO.characteristicTypeId;
		// this.extractType = refsetDAO.extractType;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
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

	public String getValueId() {
		return valueId;
	}

	public void setValueId(String valueId) {
		this.valueId = valueId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getEnableFlag() {
		return enableFlag;
	}

	public void setEnableFlag(Boolean enableFlag) {
		this.enableFlag = enableFlag;
	}

	public Boolean getWriteFlag() {
		return writeFlag;
	}

	public void setWriteFlag(Boolean writeFlag) {
		this.writeFlag = writeFlag;
	}

	public String getConceptId() {
		return conceptId;
	}

	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Boolean getWrittenToFileFlag() {
		return writtenToFileFlag;
	}

	public void setWrittenToFileFlag(Boolean writtenToFileFlag) {
		this.writtenToFileFlag = writtenToFileFlag;
	}

	/*
	 * public String getExtractType() { return extractType; }
	 * 
	 * public void setExtractType(String extractType) { this.extractType = extractType; }
	 */

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

	public String getCharacteristicType() {
		return characteristicType;
	}

	public void setCharacteristicType(String characteristicType) {
		this.characteristicType = characteristicType;
	}
}
