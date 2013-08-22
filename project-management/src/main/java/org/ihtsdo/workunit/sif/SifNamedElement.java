package org.ihtsdo.workunit.sif;

public abstract class SifNamedElement {
	
	private String name;
	private String idScheme;
	private Object id;
	private String abbrev;

	public SifNamedElement() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the idScheme
	 */
	public String getIdScheme() {
		return idScheme;
	}

	/**
	 * @param idScheme the idScheme to set
	 */
	public void setIdScheme(String idScheme) {
		this.idScheme = idScheme;
	}

	/**
	 * @return the id
	 */
	public Object getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Object id) {
		this.id = id;
	}

	/**
	 * @return the abbrev
	 */
	public String getAbbrev() {
		return abbrev;
	}

	/**
	 * @param abbrev the abbrev to set
	 */
	public void setAbbrev(String abbrev) {
		this.abbrev = abbrev;
	}

}
