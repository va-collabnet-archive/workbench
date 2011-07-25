package org.ihtsdo.rf2.core.dao;

/**
 * Title: ModuleIDDAO : Stores the ModuleId information to which moduleId 
 *  Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class ModuleIDDAO {

	public String conceptid;
	public String effectiveTime;
	public java.util.Date effectiveDate;


	public ModuleIDDAO(String conceptid, String active, String moduleId, String definitionStatusId) {
		this.conceptid = conceptid;
	
	}
	
	
	public ModuleIDDAO(String conceptid, java.util.Date effectiveDate) {
		this.conceptid = conceptid;
		this.effectiveDate = effectiveDate;		
				
	}
	

	public ModuleIDDAO(String conceptid,  String effectiveTime) {
		this.conceptid = conceptid;
		this.effectiveTime = effectiveTime;		
	}

	public ModuleIDDAO() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "ConceptDAO [conceptid=" + conceptid + ",effectiveTime=" + effectiveTime
				+ ", effectiveDate=" + effectiveDate + "]";
	}

	public String getConceptid() {
		return conceptid;
	}

	public void setConceptid(String conceptid) {
		this.conceptid = conceptid;
	}
	
	public String getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(String effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	
	public void setEffectiveDate(java.util.Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public java.util.Date getEffectiveDate() {
		return effectiveDate;
	}
	
}
