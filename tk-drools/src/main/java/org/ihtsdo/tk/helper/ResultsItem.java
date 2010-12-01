package org.ihtsdo.tk.helper;

import java.util.UUID;

public class ResultsItem {
	
	private int errorCode;
	private String message;
	private UUID severity;
	private String ruleCode;
	
	public ResultsItem() {
		super();
	}
	public ResultsItem(int errorCode, String message) {
		super();
		this.errorCode = errorCode;
		this.message = message;
	}
	
	public ResultsItem(int errorCode, String message, UUID severity, String ruleCode) {
		super();
		this.errorCode = errorCode;
		this.message = message;
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
	public UUID getSeverity() {
		return severity;
	}
	public void setSeverity(UUID severity) {
		this.severity = severity;
	}
	public String getRuleCode() {
		return ruleCode;
	}
	public void setRuleCode(String ruleCode) {
		this.ruleCode = ruleCode;
	}
	
}
