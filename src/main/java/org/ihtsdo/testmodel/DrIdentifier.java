package org.ihtsdo.testmodel;

public class DrIdentifier {
	private String primordialUuid;
	
	private String componentUuid;
	
	private String authorityUuid;
	private String denotation;
	
	private String statusUuid;
	private String pathUuid;
	private String authorUuid;
	private Long time;
	
	private String factContextName;

	public DrIdentifier() {
		// TODO Auto-generated constructor stub
	}

	public String getPrimordialUuid() {
		return primordialUuid;
	}

	public void setPrimordialUuid(String primordialUuid) {
		this.primordialUuid = primordialUuid;
	}

	public String getComponentUuid() {
		return componentUuid;
	}

	public void setComponentUuid(String componentUuid) {
		this.componentUuid = componentUuid;
	}

	public String getAuthorityUuid() {
		return authorityUuid;
	}

	public void setAuthorityUuid(String authorityUuid) {
		this.authorityUuid = authorityUuid;
	}

	public String getDenotation() {
		return denotation;
	}

	public void setDenotation(String denotation) {
		this.denotation = denotation;
	}

	public String getStatusUuid() {
		return statusUuid;
	}

	public void setStatusUuid(String statusUuid) {
		this.statusUuid = statusUuid;
	}

	public String getPathUuid() {
		return pathUuid;
	}

	public void setPathUuid(String pathUuid) {
		this.pathUuid = pathUuid;
	}

	public String getAuthorUuid() {
		return authorUuid;
	}

	public void setAuthorUuid(String authorUuid) {
		this.authorUuid = authorUuid;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getFactContextName() {
		return factContextName;
	}

	public void setFactContextName(String factContextName) {
		this.factContextName = factContextName;
	}
}
