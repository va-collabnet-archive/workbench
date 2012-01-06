package org.ihtsdo.project.workflow.event;

import org.ihtsdo.project.workflow.tag.InboxTag;


public class TagRemovedEvent extends GenericEvent {

	private InboxTag tag;
	
	public TagRemovedEvent(InboxTag tag) {
		super();
		this.tag = tag;
	}

	public InboxTag getTag() {
		return tag;
	}

	public void setTag(InboxTag tag) {
		this.tag = tag;
	}

	@Override
	public EventType getAssociatedType() {
		return EventType.TAG_REMOVED;
	}

}
