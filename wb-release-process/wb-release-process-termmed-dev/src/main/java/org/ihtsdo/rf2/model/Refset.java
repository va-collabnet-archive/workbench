package org.ihtsdo.rf2.model;

public class Refset {

	private String name;
	private String id;
	private String termAuxUID;
	private String UID;
	
	public String getUID() {
		return UID;
	}

	public void setUID(String UID) {
		this.UID = UID;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTermAuxUID() {
		return termAuxUID;
	}

	public void setTermAuxUID(String termAuxUID) {
		this.termAuxUID = termAuxUID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
