package org.ihtsdo.qa.store.model;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class QACase extends ViewPointSpecificObject {
	
	private UUID caseUuid;
	private UUID ruleUuid;
	private UUID componentUuid;
	private boolean isActive;
	private UUID dispositionStatusUuid;
	private UUID dispositionReasonUuid;
	private Date dispositionStatusDate;
	private String dispositionStatusEditor;
	private String dispositionAnnotation;
	private String detail;
	private String assignedTo;
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

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public UUID getDispositionStatusUuid() {
		return dispositionStatusUuid;
	}

	public void setDispositionStatusUuid(UUID dispositionStatusUuid) {
		this.dispositionStatusUuid = dispositionStatusUuid;
	}

	public UUID getDispositionReasonUuid() {
		return dispositionReasonUuid;
	}

	public void setDispositionReasonUuid(UUID dispositionReasonUuid) {
		this.dispositionReasonUuid = dispositionReasonUuid;
	}

	public Date getDispositionStatusDate() {
		return dispositionStatusDate;
	}

	public void setDispositionStatusDate(Date dispositionStatusDate) {
		this.dispositionStatusDate = dispositionStatusDate;
	}

	public String getDispositionStatusEditor() {
		return dispositionStatusEditor;
	}

	public void setDispositionStatusEditor(String dispositionStatusEditor) {
		this.dispositionStatusEditor = dispositionStatusEditor;
	}

	public String getDispositionAnnotation() {
		return dispositionAnnotation;
	}

	public void setDispositionAnnotation(String dispositionAnnotation) {
		this.dispositionAnnotation = dispositionAnnotation;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
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
