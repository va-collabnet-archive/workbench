package org.ihtsdo.project.workflow.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WfInstance {
	
	private UUID componentId = null;
	private WorkflowDefinition wfDefinition;
	private WfState state;
	private Map<String,Object> properties;
	private List<WfHistoryEntry> history;
	
	public WfInstance() {
		super();
	}

	public WfInstance(UUID componentId, WorkflowDefinition wfDefinition, WfState state,
			Map<String, Object> properties, List<WfHistoryEntry> history) {
		super();
		this.componentId = componentId;
		this.wfDefinition = wfDefinition;
		this.state = state;
		this.properties = properties;
		this.history = history;
	}

	public UUID getComponentId() {
		return componentId;
	}

	public void setComponentId(UUID componentId) {
		this.componentId = componentId;
	}

	public WorkflowDefinition getWfDefinition() {
		return wfDefinition;
	}

	public void setWfDefinition(WorkflowDefinition wfDefinition) {
		this.wfDefinition = wfDefinition;
	}

	public WfState getState() {
		return state;
	}

	public void setState(WfState state) {
		this.state = state;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public List<WfHistoryEntry> getHistory() {
		return history;
	}

	public void setHistory(List<WfHistoryEntry> history) {
		this.history = history;
	}

}
