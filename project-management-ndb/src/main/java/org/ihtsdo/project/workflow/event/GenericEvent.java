package org.ihtsdo.project.workflow.event;

public abstract class GenericEvent {
	public enum EventType {
		NEW_TAG_ADDED,
		ITEM_TAGGED, 
		TAG_REMOVED, 
		ITEM_STATE_CHANGED,
		OUTBOX_CONTENT_CHANGED,
		OPEN_INBOX_ITEM, 
		TRANSLATION_STATE_CHANGED, TODO_CONTENTS_CHANGED;
	}
	public abstract EventType getAssociatedType();
	
}
