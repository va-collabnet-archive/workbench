package org.ihtsdo.tk.helper.templates;

public abstract class AbstractTemplate {
	
	public enum TemplateType {
		DESCRIPTION, CONCEPT, RELATIONSHIP, EXTENSION;
	}
	
	private String componentUuid;
	private TemplateType type;
	private String statusUuid;
	private String author;
	public TemplateType getType() {
		return type;
	}
	public void setType(TemplateType type) {
		this.type = type;
	}
	public String getStatusUuid() {
		return statusUuid;
	}
	public void setStatusUuid(String statusUuid) {
		this.statusUuid = statusUuid;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getComponentUuid() {
		return componentUuid;
	}
	public void setComponentUuid(String componentUuid) {
		this.componentUuid = componentUuid;
	}

}
