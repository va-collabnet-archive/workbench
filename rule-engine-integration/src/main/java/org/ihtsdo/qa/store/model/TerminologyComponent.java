package org.ihtsdo.qa.store.model;

import java.util.UUID;

public class TerminologyComponent {
	
	private UUID componentUuid;
	private String componentName;
	private Long sctid;
	
	public TerminologyComponent() {
		super();
	}
	
	public TerminologyComponent(UUID componentUuid, String componentName,
			Long sctid) {
		super();
		this.componentUuid = componentUuid;
		this.componentName = componentName;
		this.sctid = sctid;
	}
	
	public UUID getComponentUuid() {
		return componentUuid;
	}

	public void setComponentUuid(UUID componentUuid) {
		this.componentUuid = componentUuid;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public Long getSctid() {
		return sctid;
	}

	public void setSctid(Long sctid) {
		this.sctid = sctid;
	}
	
	public String toString() {
		return getComponentName();
	}

}
