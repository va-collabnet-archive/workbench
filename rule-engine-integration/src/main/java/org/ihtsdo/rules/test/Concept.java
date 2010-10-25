package org.ihtsdo.rules.test;

public class Concept implements I_Concept, I_IsLastVersion {
	
	String name;
	Integer id;
	Boolean last;

	public Concept() {
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Boolean isLast() {
		return last;
	}

	public void setLast(Boolean last) {
		this.last = last;
	}
	

}
