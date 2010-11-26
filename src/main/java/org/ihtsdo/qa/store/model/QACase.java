package org.ihtsdo.qa.store.model;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class QACase extends ViewPointSpecificObject {
	
	private UUID caseUuid;
	private UUID ruleUuid;
	private UUID componentUuid;
	private boolean isActive;
	private int dispositionStatusUuid;
	private String editor;
	private Date effectiveTime;
	private List<String> comments;

	public QACase() {
	}

	public UUID getCaseUuid() {
		return caseUuid;
	}

	public void setCaseUuid(UUID caseUuid) {
		this.caseUuid = caseUuid;
	}

	public UUID getRuleUuid() {
		return ruleUuid;
	}

	public void setRuleUuid(UUID ruleUuid) {
		this.ruleUuid = ruleUuid;
	}

	public UUID getComponentUuid() {
		return componentUuid;
	}

	public void setComponentUuid(UUID componentUuid) {
		this.componentUuid = componentUuid;
	}

	public int getWhitelistType() {
		return dispositionStatusUuid;
	}

	public void setWhitelistType(int whitelistType) {
		this.dispositionStatusUuid = whitelistType;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public int getDispositionStatusUuid() {
		return dispositionStatusUuid;
	}

	public void setDispositionStatusUuid(int dispositionStatusUuid) {
		this.dispositionStatusUuid = dispositionStatusUuid;
	}

	public String getEditor() {
		return editor;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	public Date getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(Date effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public List<String> getComments() {
		return comments;
	}

	public void setComments(List<String> comments) {
		this.comments = comments;
	}

}
