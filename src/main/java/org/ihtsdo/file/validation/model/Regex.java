package org.ihtsdo.file.validation.model;

public class Regex {
	private String test;
    private String expression;
    private String description;
    private String successMessage;
    private String failureMessage;

    public String getTest() {
		return test;
	}
	public String getExpression() {
		return expression;
	}
	public String getDescription() {
		return description;
	}
	public String getSuccessMessage() {
		return successMessage;
	}
	public String getFailureMessage() {
		return failureMessage;
	}
	public void setTest(String test) {
		this.test = test;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setSuccessMessage(String successMessage) {
		this.successMessage = successMessage;
	}
	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}
}
