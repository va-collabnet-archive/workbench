package org.ihtsdo.rf2.core.dao;

import java.util.Date;

/**
 * Title: FileName Description: Reads Properties File to know file names requires to export Core data in RF2 format Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class DescriptionDAO {

	public String descriptionId;
	public String effectiveTime;
	public String active;
	public String moduleId;
	public String conceptId;
	public String languageCode;
	public String typeId;
	public String term;
	public String caseSignificanceId;

	public Date effectiveDate;
	public String descriptionStatus;
	public String sDescType;
	public String initialCapitalStatus;
	public Boolean initialCaseSignificant = Boolean.FALSE;
	public Boolean writtenToFileFlag = Boolean.FALSE;

	@Override
	public String toString() {
		return "DescriptionDAO [" + "descriptionId=" + descriptionId + ", " + "effectiveTime=" + effectiveTime + ", " + "active=" + active + ",  " + "moduleId=" + moduleId + ", " + "conceptId="
				+ conceptId + ", " + "languageCode=" + languageCode + ", " + "term=" + term + ", " + "typeId=" + typeId + ", " + "caseSignificanceId=" + caseSignificanceId + "]";
	}

	public DescriptionDAO() {

	}

	public DescriptionDAO(String descriptionId, String active, String moduleId, String conceptId, String languageCode, String typeId, String term, String caseSignificanceId) {
		this.descriptionId = descriptionId;
		this.active = active;
		this.moduleId = moduleId;
		this.conceptId = conceptId;
		this.languageCode = languageCode;
		this.typeId = typeId;
		this.term = term;
		this.caseSignificanceId = caseSignificanceId;
	}

	public void updateFrom(DescriptionDAO daoFrom) {
		this.setDescriptionId(daoFrom.getDescriptionId());
		this.setActive(daoFrom.getActive());
		this.setModuleId(daoFrom.getModuleId());
		this.setConceptId(daoFrom.getConceptId());
		this.setLanguageCode(daoFrom.getLanguageCode());
		this.setTypeId(daoFrom.getTypeId());
		this.setTerm(daoFrom.getTerm());
		this.setCaseSignificanceId(daoFrom.getCaseSignificanceId());
		this.setEffectiveDate(daoFrom.getEffectiveDate());
		this.setEffectiveTime(daoFrom.getEffectiveTime());

	}

	public String getDescriptionId() {
		return descriptionId;
	}

	public void setDescriptionId(String descriptionId) {
		this.descriptionId = descriptionId;
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

	public String getConceptId() {
		return conceptId;
	}

	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getCaseSignificanceId() {
		return caseSignificanceId;
	}

	public void setCaseSignificanceId(String caseSignificanceId) {
		this.caseSignificanceId = caseSignificanceId;
	}

	public void setsDescType(String sDescType) {
		this.sDescType = sDescType;
	}

	public String getsDescType() {
		return sDescType;
	}

	public void setInitialCaseSignificant(Boolean initialCaseSignificant) {
		this.initialCaseSignificant = initialCaseSignificant;
	}

	public Boolean isInitialCaseSignificant() {
		return initialCaseSignificant;
	}

	public void setInitialCapitalStatus(String initialCapitalStatus) {
		this.initialCapitalStatus = initialCapitalStatus;
	}

	public String getInitialCapitalStatus() {
		return initialCapitalStatus;
	}

	public void setDescriptionStatus(String descriptionStatus) {
		this.descriptionStatus = descriptionStatus;
	}

	public String getDescriptionStatus() {
		return descriptionStatus;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setWrittenToFileFlag(Boolean writtenToFileFlag) {
		this.writtenToFileFlag = writtenToFileFlag;
	}

	public Boolean isWrittenToFileFlag() {
		return writtenToFileFlag;
	}
}
