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
package org.ihtsdo.project.workflow.event;

/**
 * The Class TagRemovedEventHandler.
 *
 * @param <T> the generic type
 */
public abstract class TagRemovedEventHandler<T> extends EventHandler<TagRemovedEvent>{
	
	/**
	 * Instantiates a new tag removed event handler.
	 *
	 * @param parent the parent
	 */
	public TagRemovedEventHandler(Object parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.event.EventHandler#handleEvent(org.ihtsdo.project.workflow.event.GenericEvent)
	 */
	@Override
	public abstract void handleEvent(TagRemovedEvent event);
}
