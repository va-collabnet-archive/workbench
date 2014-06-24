package org.ihtsdo.tk.helper.templates;

public class RelationshipTemplate extends AbstractTemplate {
	
	private String sourceUuid;
	private String typeUuid;
	private String targetUuid;

	public RelationshipTemplate() {
		setType(TemplateType.RELATIONSHIP);
	}

	public String getSourceUuid() {
		return sourceUuid;
	}

	public void setSourceUuid(String sourceUuid) {
		this.sourceUuid = sourceUuid;
	}

	public String getTypeUuid() {
		return typeUuid;
	}

	public void setTypeUuid(String typeUuid) {
		this.typeUuid = typeUuid;
	}

	public String getTargetUuid() {
		return targetUuid;
	}

	public void setTargetUuid(String targetUuid) {
		this.targetUuid = targetUuid;
	}

}
