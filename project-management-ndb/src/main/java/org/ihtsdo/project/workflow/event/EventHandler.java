package org.ihtsdo.project.workflow.event;

public interface EventHandler<T extends GenericEvent>{
	public void handleEvent(T event);
}
