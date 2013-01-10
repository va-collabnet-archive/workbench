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
 * The Class EventHandler.
 *
 * @param <T> the generic type
 */
public abstract class EventHandler<T extends GenericEvent>{
	
	/** The parent. */
	private Object parent;
	
	/**
	 * Instantiates a new event handler.
	 *
	 * @param parent the parent
	 */
	public EventHandler(Object parent) {
		super();
		this.parent = parent;
	}

	/**
	 * Gets the parent.
	 *
	 * @return the parent
	 */
	public Object getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent the new parent
	 */
	public void setParent(Object parent) {
		this.parent = parent;
	}

	/**
	 * Handle event.
	 *
	 * @param event the event
	 */
	public abstract void handleEvent(T event);
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
