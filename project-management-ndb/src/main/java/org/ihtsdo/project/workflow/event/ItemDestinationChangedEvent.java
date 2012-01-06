package org.ihtsdo.project.workflow.event;

import org.ihtsdo.project.workflow.model.WfInstance;

public class ItemDestinationChangedEvent extends GenericEvent {

	private WfInstance wfInstance;

	public ItemDestinationChangedEvent(WfInstance wfInstance) {
		super();
		this.wfInstance = wfInstance;
	}

	public WfInstance getWfInstance() {
		return wfInstance;
	}

	public void setWfInstance(WfInstance wfInstance) {
		this.wfInstance = wfInstance;
	}

	@Override
	public EventType getAssociatedType() {
		return EventType.ITEM_STATE_CHANGED;
	}

}
