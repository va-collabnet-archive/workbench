package org.ihtsdo.project.workflow.event;

import org.ihtsdo.project.workflow.model.WfInstance;

public class SendBackToInboxEvent extends GenericEvent {

	private WfInstance newInstance;

	public SendBackToInboxEvent(WfInstance newInstance) {
		super();
		this.newInstance = newInstance;
	}

	public WfInstance getNewInstance() {
		return newInstance;
	}

	public void setNewInstance(WfInstance newInstance) {
		this.newInstance = newInstance;
	}

	@Override
	public EventType getAssociatedType() {
		return EventType.SEND_BACK_TO_INBOX;
	}

}
