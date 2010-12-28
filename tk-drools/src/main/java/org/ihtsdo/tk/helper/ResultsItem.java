package org.ihtsdo.tk.helper;

import java.util.UUID;

public class ResultsItem {
	
	private int errorCode;
	private String message;
	private String severity;
	private String ruleUuid;
	
	public enum Severity {
		
		ERROR(UUID.fromString("f9545a20-12cf-11e0-ac64-0800200c9a66"), "Error"),
		WARNING(UUID.fromString("f9545a21-12cf-11e0-ac64-0800200c9a66"), "Warning"),
		NOTIFICATION(UUID.fromString("f9545a22-12cf-11e0-ac64-0800200c9a66"), "Notification");
		
		private final UUID severityUuid;
		private final String name;
		
		Severity(UUID severityUuid, String name) {
			this.severityUuid = severityUuid;
			this.name = name;
		}

		public UUID getSeverityUuid() {
			return severityUuid;
		}

		public String getName() {
			return name;
		}
		
	}
	
	public ResultsItem() {
		super();
	}
	public ResultsItem(int errorCode, String message) {
		super();
		this.errorCode = errorCode;
		this.message = message;
	}
	
	public ResultsItem(int errorCode, String message, String severity, String ruleUuid) {
		super();
		this.errorCode = errorCode;
		this.message = message;
		this.severity=severity;
		this.ruleUuid=ruleUuid;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	public String getRuleUuid() {
		return ruleUuid;
	}
	public void setRuleUuid(String ruleUuid) {
		this.ruleUuid = ruleUuid;
	}
	
}
