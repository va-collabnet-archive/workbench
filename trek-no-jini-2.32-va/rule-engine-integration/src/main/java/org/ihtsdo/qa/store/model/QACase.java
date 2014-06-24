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
package org.ihtsdo.qa.store.model;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * The Class QACase.
 */
public class QACase extends ViewPointSpecificObject {
	
	/** The case uuid. */
	private UUID caseUuid;
	
	/** The rule uuid. */
	private UUID ruleUuid;
	
	/** The component uuid. */
	private UUID componentUuid;
	
	/** The is active. */
	private boolean isActive;
	
	/** The disposition status uuid. */
	private UUID dispositionStatusUuid;
	
	/** The disposition reason uuid. */
	private UUID dispositionReasonUuid;
	
	/** The disposition status date. */
	private Calendar dispositionStatusDate;
	
	/** The disposition status editor. */
	private String dispositionStatusEditor;
	
	/** The disposition annotation. */
	private String dispositionAnnotation;
	
	/** The detail. */
	private String detail;
	
	/** The assigned to. */
	private String assignedTo;
	
	/** The assignment editor. */
	private String assignmentEditor;
	
	/** The assignment date. */
	private Calendar assignmentDate;
	
	/** The effective time. */
	private Calendar effectiveTime;
	
	/** The last changed state. */
	private Calendar lastChangedState;
	
	/** The last status changed. */
	private Calendar lastStatusChanged;
	
	/**
	 * Gets the last status changed.
	 *
	 * @return the last status changed
	 */
	public Calendar getLastStatusChanged() {
		return lastStatusChanged;
	}

	/**
	 * Sets the last status changed.
	 *
	 * @param lastStatusChanged the new last status changed
	 */
	public void setLastStatusChanged(Calendar lastStatusChanged) {
		this.lastStatusChanged = lastStatusChanged;
	}
	
	/**
	 * Gets the last changed state.
	 *
	 * @return the last changed state
	 */
	public Calendar getLastChangedState() {
		return lastChangedState;
	}

	/**
	 * Sets the last changed state.
	 *
	 * @param lastChangedState the new last changed state
	 */
	public void setLastChangedState(Calendar lastChangedState) {
		this.lastChangedState = lastChangedState;
	}

	/** The comments. */
	private List<QaCaseComment> comments;

	/**
	 * Gets the assignment editor.
	 *
	 * @return the assignment editor
	 */
	public String getAssignmentEditor() {
		return assignmentEditor;
	}

	/**
	 * Sets the assignment editor.
	 *
	 * @param assignmentEditor the new assignment editor
	 */
	public void setAssignmentEditor(String assignmentEditor) {
		this.assignmentEditor = assignmentEditor;
	}

	/**
	 * Gets the assignment date.
	 *
	 * @return the assignment date
	 */
	public Calendar getAssignmentDate() {
		return assignmentDate;
	}

	/**
	 * Sets the assignment date.
	 *
	 * @param assignmentDate the new assignment date
	 */
	public void setAssignmentDate(Calendar assignmentDate) {
		this.assignmentDate = assignmentDate;
	}

	/**
	 * Instantiates a new qA case.
	 */
	public QACase() {
	}

	/**
	 * Gets the case uuid.
	 *
	 * @return the case uuid
	 */
	public UUID getCaseUuid() {
		return caseUuid;
	}

	/**
	 * Sets the case uuid.
	 *
	 * @param caseUuid the new case uuid
	 */
	public void setCaseUuid(UUID caseUuid) {
		this.caseUuid = caseUuid;
	}

	/**
	 * Gets the rule uuid.
	 *
	 * @return the rule uuid
	 */
	public UUID getRuleUuid() {
		return ruleUuid;
	}

	/**
	 * Sets the rule uuid.
	 *
	 * @param ruleUuid the new rule uuid
	 */
	public void setRuleUuid(UUID ruleUuid) {
		this.ruleUuid = ruleUuid;
	}

	/**
	 * Gets the component uuid.
	 *
	 * @return the component uuid
	 */
	public UUID getComponentUuid() {
		return componentUuid;
	}

	/**
	 * Sets the component uuid.
	 *
	 * @param componentUuid the new component uuid
	 */
	public void setComponentUuid(UUID componentUuid) {
		this.componentUuid = componentUuid;
	}

	/**
	 * Checks if is active.
	 *
	 * @return true, if is active
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Sets the active.
	 *
	 * @param isActive the new active
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * Gets the disposition status uuid.
	 *
	 * @return the disposition status uuid
	 */
	public UUID getDispositionStatusUuid() {
		return dispositionStatusUuid;
	}

	/**
	 * Sets the disposition status uuid.
	 *
	 * @param dispositionStatusUuid the new disposition status uuid
	 */
	public void setDispositionStatusUuid(UUID dispositionStatusUuid) {
		this.dispositionStatusUuid = dispositionStatusUuid;
	}

	/**
	 * Gets the disposition reason uuid.
	 *
	 * @return the disposition reason uuid
	 */
	public UUID getDispositionReasonUuid() {
		return dispositionReasonUuid;
	}

	/**
	 * Sets the disposition reason uuid.
	 *
	 * @param dispositionReasonUuid the new disposition reason uuid
	 */
	public void setDispositionReasonUuid(UUID dispositionReasonUuid) {
		this.dispositionReasonUuid = dispositionReasonUuid;
	}

	/**
	 * Gets the disposition status date.
	 *
	 * @return the disposition status date
	 */
	public Calendar getDispositionStatusDate() {
		return dispositionStatusDate;
	}

	/**
	 * Sets the disposition status date.
	 *
	 * @param dispositionStatusDate the new disposition status date
	 */
	public void setDispositionStatusDate(Calendar dispositionStatusDate) {
		this.dispositionStatusDate = dispositionStatusDate;
	}

	/**
	 * Gets the disposition status editor.
	 *
	 * @return the disposition status editor
	 */
	public String getDispositionStatusEditor() {
		return dispositionStatusEditor;
	}

	/**
	 * Sets the disposition status editor.
	 *
	 * @param dispositionStatusEditor the new disposition status editor
	 */
	public void setDispositionStatusEditor(String dispositionStatusEditor) {
		this.dispositionStatusEditor = dispositionStatusEditor;
	}

	/**
	 * Gets the disposition annotation.
	 *
	 * @return the disposition annotation
	 */
	public String getDispositionAnnotation() {
		return dispositionAnnotation;
	}

	/**
	 * Sets the disposition annotation.
	 *
	 * @param dispositionAnnotation the new disposition annotation
	 */
	public void setDispositionAnnotation(String dispositionAnnotation) {
		this.dispositionAnnotation = dispositionAnnotation;
	}

	/**
	 * Gets the detail.
	 *
	 * @return the detail
	 */
	public String getDetail() {
		return detail;
	}

	/**
	 * Sets the detail.
	 *
	 * @param detail the new detail
	 */
	public void setDetail(String detail) {
		this.detail = detail;
	}

	/**
	 * Gets the assigned to.
	 *
	 * @return the assigned to
	 */
	public String getAssignedTo() {
		return assignedTo;
	}

	/**
	 * Sets the assigned to.
	 *
	 * @param assignedTo the new assigned to
	 */
	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}

	/**
	 * Gets the effective time.
	 *
	 * @return the effective time
	 */
	public Calendar getEffectiveTime() {
		return effectiveTime;
	}

	/**
	 * Sets the effective time.
	 *
	 * @param effectiveTime the new effective time
	 */
	public void setEffectiveTime(Calendar effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	/**
	 * Gets the comments.
	 *
	 * @return the comments
	 */
	public List<QaCaseComment> getComments() {
		return comments;
	}

	/**
	 * Sets the comments.
	 *
	 * @param comments the new comments
	 */
	public void setComments(List<QaCaseComment> comments) {
		this.comments = comments;
	}


}
