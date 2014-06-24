package org.ihtsdo.rf2.core.dao;

/**
 * Title: FileName Description: Reads Properties File to know file names requires to export Core data in RF2 format Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class ConceptDAO {

	public String conceptid;
	public String effectiveTime;
	public String active;
	public String moduleId;
	public String definitionStatusId;

	public String conceptIsPrimitive;
	public String conceptStatus;
	public java.util.Date effectiveDate;

	public boolean writeFlag;
	public boolean defined;
	public Boolean writtenToFileFlag = Boolean.FALSE;

	public ConceptDAO(String conceptid, String active, String moduleId, String definitionStatusId) {
		this.conceptid = conceptid;
		this.active = active;
		this.moduleId = moduleId;
		this.definitionStatusId = definitionStatusId;
	}
	
	
	public ConceptDAO(String conceptid, String active, String moduleId, String definitionStatusId, java.util.Date effectiveDate,  String conceptStatus) {
		this.conceptid = conceptid;
		this.active = active;
		this.moduleId = moduleId;
		this.definitionStatusId = definitionStatusId;
		this.effectiveDate = effectiveDate;		
		this.conceptStatus =conceptStatus;		
	}
	

	public ConceptDAO(String conceptid, String active, String moduleId, String definitionStatusId, String effectiveTime, String conceptIsPrimitive, String conceptStatus) {
		this.conceptid = conceptid;
		this.active = active;
		this.moduleId = moduleId;
		this.definitionStatusId = definitionStatusId;
		this.effectiveTime = effectiveTime;
		this.conceptIsPrimitive = conceptIsPrimitive;
		this.setConceptStatus(conceptStatus);
		this.setWriteFlag(false);
	}

	public ConceptDAO() {
		// TODO Auto-generated constructor stub
	}

	public void updateFrom(ConceptDAO conceptObj) {
		this.conceptid = conceptObj.getConceptid();
		this.active = conceptObj.getActive();
		this.moduleId = conceptObj.getModuleId();
		this.definitionStatusId = conceptObj.getDefinitionStatusId();
		this.effectiveTime = conceptObj.getEffectiveTime();
		this.effectiveDate = conceptObj.getEffectiveDate();
		this.conceptStatus = conceptObj.getConceptStatus();
		this.conceptIsPrimitive = conceptObj.getConceptIsPrimitive();
	}

	@Override
	public String toString() {
		return "ConceptDAO [active=" + active + ", conceptid=" + conceptid + ", definitionStatusId=" + definitionStatusId + ", moduleId=" + moduleId + ", effectiveTime=" + effectiveTime
				+ ", conceptStatus=" + conceptStatus + ", effectiveDate=" + effectiveDate + "]";
	}

	public String getConceptid() {
		return conceptid;
	}

	public void setConceptid(String conceptid) {
		this.conceptid = conceptid;
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

	public String getDefinitionStatusId() {
		return definitionStatusId;
	}

	public void setDefinitionStatusId(String definitionStatusId) {
		this.definitionStatusId = definitionStatusId;
	}

	public String getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(String effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public void setConceptIsPrimitive(String conceptIsPrimitive) {
		this.conceptIsPrimitive = conceptIsPrimitive;
	}

	public String getConceptIsPrimitive() {
		return conceptIsPrimitive;
	}

	public void setConceptStatus(String conceptStatus) {
		this.conceptStatus = conceptStatus;
	}

	public String getConceptStatus() {
		return conceptStatus;
	}

	public void setDefined(boolean defined) {
		this.defined = defined;
	}

	public boolean isDefined() {
		return defined;
	}

	public void setEffectiveDate(java.util.Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public java.util.Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setWriteFlag(boolean writeFlag) {
		this.writeFlag = writeFlag;
	}

	public boolean isWriteFlag() {
		return writeFlag;
	}

	public void setWrittenToFileFlag(Boolean writtenToFileFlag) {
		this.writtenToFileFlag = writtenToFileFlag;
	}

	public Boolean isWrittenToFileFlag() {
		return writtenToFileFlag;
	}
}
