package org.ihtsdo.translation.ui.event;

import org.ihtsdo.project.workflow.event.GenericEvent;
import org.ihtsdo.project.workflow.model.WfInstance;

public class ItemSentToSpecialFolderEvent extends GenericEvent {

	private WfInstance wfInstance;
	private WfInstance oldInstance;

	public ItemSentToSpecialFolderEvent(WfInstance wfInstance, WfInstance oldInstance) {
		super();
		this.wfInstance = wfInstance;
		this.setOldInstance(oldInstance);
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

	public void setOldInstance(WfInstance oldInstance) {
		this.oldInstance = oldInstance;
	}

	public WfInstance getOldInstance() {
		return oldInstance;
	}

}
