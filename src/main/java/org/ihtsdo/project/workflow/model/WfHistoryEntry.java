package org.ihtsdo.project.workflow.model;

import java.util.UUID;

public class WfHistoryEntry {
	
	private Long time;
	private UUID userId;
	private WfState initialState;
	private WfAction action;
	private WfState finalState;
	
	public WfHistoryEntry() {
		super();
	}
	
	public WfHistoryEntry(Long time, UUID userId, WfState initialState,
			WfAction action, WfState finalState) {
		super();
		this.time = time;
		this.userId = userId;
		this.initialState = initialState;
		this.action = action;
		this.finalState = finalState;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public WfState getInitialState() {
		return initialState;
	}

	public void setInitialState(WfState initialState) {
		this.initialState = initialState;
	}

	public WfAction getAction() {
		return action;
	}

	public void setAction(WfAction action) {
		this.action = action;
	}

	public WfState getFinalState() {
		return finalState;
	}

	public void setFinalState(WfState finalState) {
		this.finalState = finalState;
	}
}
