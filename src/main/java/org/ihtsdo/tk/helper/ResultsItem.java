package org.ihtsdo.tk.helper;

import java.util.UUID;

public class ResultsItem {
	
	private int errorCode;
	private String message;
	private String severity;
	private String ruleUuid;
	
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
