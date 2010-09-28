package org.ihtsdo.testmodel;

public abstract class DrComponent {
	
	private String statusUuid;
	private String pathUuid;
	private String authorUuid;
	private Long time;
	
	private String factContextName;
	
	private boolean published;
	private boolean extensionComponent;
	private String extensionId; //TODO: should be UUID?
	private boolean active;
	

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
