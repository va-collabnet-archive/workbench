package org.ihtsdo.project.workflow.event;

public class OutboxContentChangeEvent extends GenericEvent{
	
	private Integer outboxSize;

	public OutboxContentChangeEvent(Integer outboxSize) {
		this.setOutboxSize(outboxSize);
	}
	
	@Override
	public EventType getAssociatedType() {
		return EventType.OUTBOX_CONTENT_CHANGED;
	}

	public void setOutboxSize(Integer outboxSize) {
		this.outboxSize = outboxSize;
	}

	public Integer getOutboxSize() {
		return outboxSize;
	}
	

}
