package org.ihtsdo.project.workflow.event;

public interface TodoContentsChangedEventHandler<T> extends EventHandler<TodoContentChangeEvent>{
	@Override
	public abstract void handleEvent(TodoContentChangeEvent event);
}
