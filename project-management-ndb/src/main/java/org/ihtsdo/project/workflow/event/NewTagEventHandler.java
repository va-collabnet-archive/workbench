package org.ihtsdo.project.workflow.event;

public interface NewTagEventHandler<T> extends EventHandler<NewTagEvent>{
	@Override
	public abstract void handleEvent(NewTagEvent event);
}
