package org.ihtsdo.project.workflow.event;

import org.ihtsdo.project.workflow.model.WfInstance;

public class ItemSentToSpecialFolderEvent extends GenericEvent {

	private WfInstance wfInstance;

	public ItemSentToSpecialFolderEvent(WfInstance wfInstance) {
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
		return EventType.ITEM_SENT_TO_SPECIAL_FOLDER;
	}

}
