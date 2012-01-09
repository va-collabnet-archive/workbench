package org.ihtsdo.project.workflow.event;


public abstract class TodoContentsChangedEventHandler<T> extends EventHandler<TodoContentChangeEvent>{
	public TodoContentsChangedEventHandler(Object parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public abstract void handleEvent(TodoContentChangeEvent event);
}
