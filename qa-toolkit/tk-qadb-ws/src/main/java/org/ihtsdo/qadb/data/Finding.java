package org.ihtsdo.qadb.data;


public class Finding extends ViewPointSpecificObject {
	private String findingUuid;
	private Execution executionUuid;
	private Rule ruleUuid;
	private TerminologyComponent componentUuid;
	private String details;

	public Finding() {
	}
	
	public String getFindingUuid() {
		return findingUuid;
	}

	public void setFindingUuid(String findingUuid) {
		this.findingUuid = findingUuid;
	}

	public Execution getExecutionUuid() {
		return executionUuid;
	}

	public void setExecutionUuid(Execution executionUuid) {
		this.executionUuid = executionUuid;
	}

	public Rule getRuleUuid() {
		return ruleUuid;
	}

	public void setRuleUuid(Rule ruleUuid) {
		this.ruleUuid = ruleUuid;
	}

	public TerminologyComponent getComponentUuid() {
		return componentUuid;
	}

	public void setComponentUuid(TerminologyComponent componentUuid) {
		this.componentUuid = componentUuid;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

}
