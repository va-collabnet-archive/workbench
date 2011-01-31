package org.ihtsdo.qadb.data;


public class TerminologyComponent {
	
	private String componentUuid;
	private String componentName;
	private Long sctid;
	
	public TerminologyComponent() {
		super();
	}
	
	public TerminologyComponent(String componentUuid, String componentName,
			Long sctid) {
		super();
		this.componentUuid = componentUuid;
		this.componentName = componentName;
		this.sctid = sctid;
	}
	
	public String getComponentUuid() {
		return componentUuid;
	}

	public void setComponentUuid(String componentUuid) {
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
