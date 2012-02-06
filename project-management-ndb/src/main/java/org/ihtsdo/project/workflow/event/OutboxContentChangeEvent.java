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
 * The Class OutboxContentChangeEvent.
 */
public class OutboxContentChangeEvent extends GenericEvent{
	
	/** The outbox size. */
	private Integer outboxSize;

	/**
	 * Instantiates a new outbox content change event.
	 *
	 * @param outboxSize the outbox size
	 */
	public OutboxContentChangeEvent(Integer outboxSize) {
		this.setOutboxSize(outboxSize);
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.event.GenericEvent#getAssociatedType()
	 */
	@Override
	public EventType getAssociatedType() {
		return EventType.OUTBOX_CONTENT_CHANGED;
	}

	/**
	 * Sets the outbox size.
	 *
	 * @param outboxSize the new outbox size
	 */
	public void setOutboxSize(Integer outboxSize) {
		this.outboxSize = outboxSize;
	}

	/**
	 * Gets the outbox size.
	 *
	 * @return the outbox size
	 */
	public Integer getOutboxSize() {
		return outboxSize;
	}
	

}
