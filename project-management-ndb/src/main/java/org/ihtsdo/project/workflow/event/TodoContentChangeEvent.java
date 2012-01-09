package org.ihtsdo.project.workflow.event;


public class TodoContentChangeEvent extends GenericEvent{
	
	private Integer todoSize;

	public TodoContentChangeEvent(Integer todoSize) {
		this.todoSize = todoSize;
	}
	
	@Override
	public EventType getAssociatedType() {
		return EventType.TODO_CONTENTS_CHANGED;
	}

	public void setTodoSize(Integer todoSize) {
		this.todoSize = todoSize;
	}

	public Integer getTodoSize() {
		return todoSize;
	}


}
