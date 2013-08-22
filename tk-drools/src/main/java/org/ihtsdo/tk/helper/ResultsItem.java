package org.ihtsdo.tk.helper;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.testmodel.DrComponent;

public class ResultsItem {
	
	private int errorCode;
	private String message;
	private String severity;
	private String ruleUuid;
	private Set<DrComponent> suspects;
	
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
		this.suspects = new HashSet<DrComponent>();
	}
	
	public ResultsItem(int errorCode, String message) {
		super();
		this.errorCode = errorCode;
		this.message = message;
		this.suspects = new HashSet<DrComponent>();
	}
	
	public ResultsItem(int errorCode, String message, String severity, String ruleUuid) {
		super();
		this.errorCode = errorCode;
		this.message = message;
		this.severity=severity;
		this.ruleUuid=ruleUuid;
		this.suspects = new HashSet<DrComponent>();
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

	/**
	 * @return the suspects
	 */
	public Set<DrComponent> getSuspects() {
		return suspects;
	}

	/**
	 * @param suspects the suspects to set
	 */
	public void setSuspects(Set<DrComponent> suspects) {
		this.suspects = suspects;
	}
	
	public void addSuspect(DrComponent suspect) {
		suspects.add(suspect);
	}
	
	public void removeSuspect(DrComponent suspect) {
		suspects.remove(suspect);
	}
	
}
