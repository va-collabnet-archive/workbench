package org.ihtsdo.qadb.data;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

public class QACase extends ViewPointSpecificObject implements Serializable {

	private static final long serialVersionUID = -9209282515767518817L;
	private String caseUuid;
	private String ruleUuid;
	private TerminologyComponent componentUuid;
	private boolean isActive;
	private DispositionStatus dispositionStatus;
	private String dispositionReasonUuid;
	private Calendar dispositionStatusDate;
	private String dispositionStatusEditor;
	private String dispositionAnnotation;
	private String databaseUuid;
	private String pathUuid;
	private String detail;
	private String assignedTo;
	private String assignmentEditor;
	private Calendar assignmentDate;
	private Calendar effectiveTime;
	private List<QAComment> comments;

	public QACase() {
	}

	public String getAssignmentEditor() {
		return assignmentEditor;
	}

	public void setAssignmentEditor(String assignmentEditor) {
		this.assignmentEditor = assignmentEditor;
	}

	public Calendar getAssignmentDate() {
		return assignmentDate;
	}

	public void setAssignmentDate(Calendar assignmentDate) {
		this.assignmentDate = assignmentDate;
	}

	public String getPathUuid() {
		return pathUuid;
	}

	public void setPathUuid(String pathUuid) {
		this.pathUuid = pathUuid;
	}

	public String getDatabaseUuid() {
		return databaseUuid;
	}

	public void setDatabaseUuid(String databaseUuid) {
		this.databaseUuid = databaseUuid;
	}

	public String getCaseUuid() {
		return caseUuid;
	}

	public void setCaseUuid(String caseUuid) {
		this.caseUuid = caseUuid;
	}

	public String getRuleUuid() {
		return ruleUuid;
	}

	public void setRuleUuid(String ruleUuid) {
		this.ruleUuid = ruleUuid;
	}

	public TerminologyComponent getComponentUuid() {
		return componentUuid;
	}

	public void setComponentUuid(TerminologyComponent componentUuid) {
		this.componentUuid = componentUuid;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public DispositionStatus getDispositionStatus() {
		return dispositionStatus;
	}

	public void setDispositionStatus(DispositionStatus dispositionStatus) {
		this.dispositionStatus = dispositionStatus;
	}

	public String getDispositionReasonUuid() {
		return dispositionReasonUuid;
	}

	public void setDispositionReasonUuid(String dispositionReasonUuid) {
		this.dispositionReasonUuid = dispositionReasonUuid;
	}

	public Calendar getDispositionStatusDate() {
		return dispositionStatusDate;
	}

	public void setDispositionStatusDate(Calendar dispositionStatusDate) {
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

	public Calendar getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(Calendar effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public List<QAComment> getComments() {
		return comments;
	}

	public void setComments(List<QAComment> comments) {
		this.comments = comments;
	}

	@Override
	public String toString() {
		return "QACase [caseUuid=" + caseUuid + ", ruleUuid=" + ruleUuid + ", componentUuid=" + componentUuid + ", isActive=" + isActive + ", dispositionStatus=" + dispositionStatus
				+ ", dispositionReasonUuid=" + dispositionReasonUuid + ", dispositionStatusDate=" + dispositionStatusDate + ", dispositionStatusEditor=" + dispositionStatusEditor
				+ ", dispositionAnnotation=" + dispositionAnnotation + ", detail=" + detail + ", assignedTo=" + assignedTo + ", effectiveTime=" + effectiveTime + ", comments=" + comments
				+ "]";
	}

}
