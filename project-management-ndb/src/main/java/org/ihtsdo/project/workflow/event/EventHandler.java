package org.ihtsdo.project.workflow.event;

public abstract class EventHandler<T extends GenericEvent>{
	private Object parent;
	public EventHandler(Object parent) {
		super();
		this.parent = parent;
	}

	public Object getParent() {
		return parent;
	}

	public void setParent(Object parent) {
		this.parent = parent;
	}

	public abstract void handleEvent(T event);
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof EventHandler<?>){
			EventHandler<?> eventHandler = (EventHandler<?>)obj;
			return this.parent.getClass().equals(eventHandler.getParent().getClass());
		}else{
			return false;
		}
	}
}
