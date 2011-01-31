package org.ihtsdo.qadb.data;

import java.util.Calendar;

public class Rule {

	private String ruleUuid;
	private String name;
	private String description;
	private int status; // create enum for this value
	private Severity severity; // create enum for this value
	private String packageName;
	private String packageUrl;
	private String ditaUuid;
	private String ruleCode;
	private String category;
	private String expectedResult;
	private String suggestedResolution;
	private String example;
	private String standingIssues;
	private boolean isWhitelistAllowed;
	private boolean isWhitelistResetAllowed;
	private boolean isWhitelistResetWhenClosed;
	private String ditaDocumentationLinkUuid;
	private String ditaGeneratedTopicUuid;
	private String modifiedBy;
	private Calendar effectiveTime;
	private String documentationUrl;

	public Rule() {
	}

	public Calendar getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(Calendar effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public String getDocumentationUrl() {
		return documentationUrl;
	}

	public void setDocumentationUrl(String documentationUrl) {
		this.documentationUrl = documentationUrl;
	}

	public String getRuleUuid() {
		return ruleUuid;
	}

	public void setRuleUuid(String ruleUuid) {
		this.ruleUuid = ruleUuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getPackageUrl() {
		return packageUrl;
	}

	public void setPackageUrl(String packageUrl) {
		this.packageUrl = packageUrl;
	}

	public String getDitaUuid() {
		return ditaUuid;
	}

	public void setDitaUuid(String ditaUuid) {
		this.ditaUuid = ditaUuid;
	}

	public String getRuleCode() {
		return ruleCode;
	}

	public void setRuleCode(String ruleCode) {
		this.ruleCode = ruleCode;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	public void setExpectedResult(String expectedResult) {
		this.expectedResult = expectedResult;
	}

	public String getSuggestedResolution() {
		return suggestedResolution;
	}

	public void setSuggestedResolution(String suggestedResolution) {
		this.suggestedResolution = suggestedResolution;
	}

	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	public String getStandingIssues() {
		return standingIssues;
	}

	public void setStandingIssues(String standingIssues) {
		this.standingIssues = standingIssues;
	}

	public boolean isWhitelistAllowed() {
		return isWhitelistAllowed;
	}

	public void setWhitelistAllowed(boolean isWhitelistAllowed) {
		this.isWhitelistAllowed = isWhitelistAllowed;
	}

	public boolean isWhitelistResetAllowed() {
		return isWhitelistResetAllowed;
	}

	public void setWhitelistResetAllowed(boolean isWhitelistResetAllowed) {
		this.isWhitelistResetAllowed = isWhitelistResetAllowed;
	}

	public boolean isWhitelistResetWhenClosed() {
		return isWhitelistResetWhenClosed;
	}

	public void setWhitelistResetWhenClosed(boolean isWhitelistResetWhenClosed) {
		this.isWhitelistResetWhenClosed = isWhitelistResetWhenClosed;
	}

	public String getDitaDocumentationLinkUuid() {
		return ditaDocumentationLinkUuid;
	}

	public void setDitaDocumentationLinkUuid(String ditaDocumentationLinkUuid) {
		this.ditaDocumentationLinkUuid = ditaDocumentationLinkUuid;
	}

	public String getDitaGeneratedTopicUuid() {
		return ditaGeneratedTopicUuid;
	}

	public void setDitaGeneratedTopicUuid(String ditaGeneratedTopicUuid) {
		this.ditaGeneratedTopicUuid = ditaGeneratedTopicUuid;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	@Override
	public String toString() {
		return "Rule [ruleUuid=" + ruleUuid + ", name=" + name + ", status=" + status + ", severity=" + severity + ", ruleCode=" + ruleCode + ", category=" + category + "]";
	}

}
