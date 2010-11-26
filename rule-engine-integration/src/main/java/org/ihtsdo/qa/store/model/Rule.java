package org.ihtsdo.qa.store.model;

import java.util.UUID;

public class Rule {
	
	private UUID ruleUuid;
	private String name;
	private String description;
	private int status; // create enum for this value
	private int severity; // create enum for this value
	private String packageName;
	private String packageUrl;
	private UUID ditaUuid;
	private int errorCode;

	public Rule() {
	}

	public UUID getRuleUuid() {
		return ruleUuid;
	}

	public void setRuleUuid(UUID ruleUuid) {
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

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
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

	public UUID getDitaUuid() {
		return ditaUuid;
	}

	public void setDitaUuid(UUID ditaUuid) {
		this.ditaUuid = ditaUuid;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

}
