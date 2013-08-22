package org.ihtsdo.workunit.sif;

public class SifSource {
	
	private SifIdentifier moduleId;
	private SifPath path;
	private SifPerson person;
	private SifSoftware software;
	private SifEnvironment environment;
	private SifOrganization organization;

	public SifSource() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the module
	 */
	public SifIdentifier getModuleId() {
		return moduleId;
	}

	/**
	 * @param module the module to set
	 */
	public void setModuleId(SifIdentifier moduleId) {
		this.moduleId = moduleId;
	}

	/**
	 * @return the path
	 */
	public SifPath getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(SifPath path) {
		this.path = path;
	}

	/**
	 * @return the person
	 */
	public SifPerson getPerson() {
		return person;
	}

	/**
	 * @param person the person to set
	 */
	public void setPerson(SifPerson person) {
		this.person = person;
	}

	/**
	 * @return the software
	 */
	public SifSoftware getSoftware() {
		return software;
	}

	/**
	 * @param software the software to set
	 */
	public void setSoftware(SifSoftware software) {
		this.software = software;
	}

	/**
	 * @return the environment
	 */
	public SifEnvironment getEnvironment() {
		return environment;
	}

	/**
	 * @param environment the environment to set
	 */
	public void setEnvironment(SifEnvironment environment) {
		this.environment = environment;
	}

	/**
	 * @return the organization
	 */
	public SifOrganization getOrganization() {
		return organization;
	}

	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(SifOrganization organization) {
		this.organization = organization;
	}


}
