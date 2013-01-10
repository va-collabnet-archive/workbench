/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.project.view.event;


/**
 * The Class TodoContentChangeEvent.
 */
public class TodoContentChangeEvent extends GenericEvent{
	
	/** The todo size. */
	private Integer todoSize;

	/**
	 * Instantiates a new todo content change event.
	 *
	 * @param todoSize the todo size
	 */
	public TodoContentChangeEvent(Integer todoSize) {
		this.todoSize = todoSize;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.event.GenericEvent#getAssociatedType()
	 */
	@Override
	public EventType getAssociatedType() {
		return EventType.TODO_CONTENTS_CHANGED;
	}

	/**
	 * Sets the todo size.
	 *
	 * @param todoSize the new todo size
	 */
	public void setTodoSize(Integer todoSize) {
		this.todoSize = todoSize;
	}

	/**
	 * Gets the todo size.
	 *
	 * @return the todo size
	 */
	public Integer getTodoSize() {
		return todoSize;
	}


}
