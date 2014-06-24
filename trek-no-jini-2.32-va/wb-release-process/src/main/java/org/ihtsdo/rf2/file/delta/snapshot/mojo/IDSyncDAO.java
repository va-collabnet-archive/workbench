package org.ihtsdo.rf2.file.delta.snapshot.mojo;

/**
 * Title: IDSyncDAO : Stores the information to sync all the cap ids vs. au ids 
 *  Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class IDSyncDAO {

	public String auconceptid;
	public String capconceptid;
	public String capterm;
	public String type;
	
	public IDSyncDAO(String capconceptid , String auconceptid) {
		this.capconceptid = capconceptid;		
		this.auconceptid = auconceptid;
		
	}
	
	public String getAuconceptid() {
		return auconceptid;
	}

	public void setAuconceptid(String auconceptid) {
		this.auconceptid = auconceptid;
	}

	public String getCapconceptid() {
		return capconceptid;
	}

	public void setCapconceptid(String capconceptid) {
		this.capconceptid = capconceptid;
	}

	public String getCapterm() {
		return capterm;
	}

	public void setCapterm(String capterm) {
		this.capterm = capterm;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


	@Override
	public String toString() {
		return "ConceptDAO [conceptid=" + auconceptid + ",capconceptid=" + capconceptid
				+ ", capterm=" + capterm + "]";
	}

	public void add(IDSyncDAO idSyncDAO) {
		// TODO Auto-generated method stub
		
	}

	
	
}
