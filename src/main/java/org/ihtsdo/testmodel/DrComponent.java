package org.ihtsdo.testmodel;

public abstract class DrComponent {
	
	private String statusUuid;
	private String pathUuid;
	private String authorUuid;
	private Long time;
	
	private String factContextName;
	
	//Inferred properties
	private boolean published = false;
	private boolean extensionComponent = false;
	private String extensionId = ""; //TODO: should be UUID?
	private boolean active = false;
	

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

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public boolean isExtensionComponent() {
		return extensionComponent;
	}

	public void setExtensionComponent(boolean extensionComponent) {
		this.extensionComponent = extensionComponent;
	}

	public String getExtensionId() {
		return extensionId;
	}

	public void setExtensionId(String extensionId) {
		this.extensionId = extensionId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
