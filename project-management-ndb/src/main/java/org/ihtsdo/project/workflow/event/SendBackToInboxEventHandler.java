package org.ihtsdo.project.workflow.event;


public abstract class SendBackToInboxEventHandler<T> extends EventHandler<SendBackToInboxEvent>{
	public SendBackToInboxEventHandler(Object parent) {
		super(parent);
	}

	@Override
	public abstract void handleEvent(SendBackToInboxEvent event);
}
