package org.ihtsdo.qa.store.model;

import java.util.UUID;

public class Finding extends ViewPointSpecificObject {
	private UUID findingUuid;
	private UUID executionUuid;
	private UUID ruleUuid;
	private UUID componentUuid;
    private String errorMessage;

	public Finding() {
	}

	public UUID getFindingUuid() {
		return findingUuid;
	}

	public void setFindingUuid(UUID findingUuid) {
		this.findingUuid = findingUuid;
	}

	public UUID getExecutionUuid() {
		return executionUuid;
	}

	public void setExecutionUuid(UUID executionUuid) {
		this.executionUuid = executionUuid;
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

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
