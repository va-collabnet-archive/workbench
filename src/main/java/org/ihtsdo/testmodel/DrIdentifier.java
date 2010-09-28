package org.ihtsdo.testmodel;

public class DrIdentifier extends DrComponent {
	
	private String primordialUuid;
	
	private String componentUuid;
	
	private String authorityUuid;
	private String denotation;
	
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
}
