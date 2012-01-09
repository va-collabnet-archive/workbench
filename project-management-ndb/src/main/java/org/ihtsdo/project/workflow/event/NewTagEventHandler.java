package org.ihtsdo.project.workflow.event;

public abstract class NewTagEventHandler<T> extends EventHandler<NewTagEvent>{
	public NewTagEventHandler(Object parent) {
		super(parent);
	}

	@Override
	public abstract void handleEvent(NewTagEvent event);
}
