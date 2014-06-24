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
package org.ihtsdo.project.workflow.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * The Class WfHistoryEntry.
 */
public class WfHistoryEntry implements Serializable{
	
	/** The time. */
	private Long time;
	
	/** The user id. */
	private UUID userId;
	
	/** The initial state. */
	private WfState initialState;
	
	/** The action. */
	private WfAction action;
	
	/** The final state. */
	private WfState finalState;
	
	/**
	 * Instantiates a new wf history entry.
	 */
	public WfHistoryEntry() {
		super();
	}
	
	/**
	 * Instantiates a new wf history entry.
	 *
	 * @param time the time
	 * @param userId the user id
	 * @param initialState the initial state
	 * @param action the action
	 * @param finalState the final state
	 */
	public WfHistoryEntry(Long time, UUID userId, WfState initialState,
			WfAction action, WfState finalState) {
		super();
		this.time = time;
		this.userId = userId;
		this.initialState = initialState;
		this.action = action;
		this.finalState = finalState;
	}

	/**
	 * Gets the time.
	 *
	 * @return the time
	 */
	public Long getTime() {
		return time;
	}

	/**
	 * Sets the time.
	 *
	 * @param time the new time
	 */
	public void setTime(Long time) {
		this.time = time;
	}

	/**
	 * Gets the user id.
	 *
	 * @return the user id
	 */
	public UUID getUserId() {
		return userId;
	}

	/**
	 * Sets the user id.
	 *
	 * @param userId the new user id
	 */
	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	/**
	 * Gets the initial state.
	 *
	 * @return the initial state
	 */
	public WfState getInitialState() {
		return initialState;
	}

	/**
	 * Sets the initial state.
	 *
	 * @param initialState the new initial state
	 */
	public void setInitialState(WfState initialState) {
		this.initialState = initialState;
	}

	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	public WfAction getAction() {
		return action;
	}

	/**
	 * Sets the action.
	 *
	 * @param action the new action
	 */
	public void setAction(WfAction action) {
		this.action = action;
	}

	/**
	 * Gets the final state.
	 *
	 * @return the final state
	 */
	public WfState getFinalState() {
		return finalState;
	}

	/**
	 * Sets the final state.
	 *
	 * @param finalState the new final state
	 */
	public void setFinalState(WfState finalState) {
		this.finalState = finalState;
	}
}
