package org.ihtsdo.project.workflow.event;

import org.ihtsdo.project.workflow.model.WfState;

public class SendBackToInboxEvent extends GenericEvent {

	private WfState oldState;
	private WfState newState;

	public SendBackToInboxEvent(WfState oldState, WfState newState) {
		super();
		this.oldState = oldState;
		this.newState = newState;
	}

	public WfState getOldState() {
		return oldState;
	}

	public void setOldState(WfState oldState) {
		this.oldState = oldState;
	}

	public WfState getNewState() {
		return newState;
	}

	public void setNewState(WfState newState) {
		this.newState = newState;
	}

	@Override
	public EventType getAssociatedType() {
		return EventType.SEND_BACK_TO_INBOX;
	}

}
