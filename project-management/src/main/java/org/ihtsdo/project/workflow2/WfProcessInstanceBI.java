/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.project.workflow2;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

import org.ihtsdo.tk.api.ComponentVersionBI;

/**
 * The representation of a single enactment of a process.
 * A process instance is created, managed and (eventually) 
 * terminated by a workflow management system, in accordance 
 * with the process definition.
 * 
 * Each process instance represents one individual enactment 
 * of the process, using its own process instance data, and 
 * which is (normally) capable of independent control and audit 
 * as it progresses towards completion or termination. It 
 * represents the unit of work with respect to a business process 
 * which passes through a workflow management system (for example, 
 * the processing of one insurance claim, or the production of one 
 * engineering design).
 * 
 * Each process instance exhibits internal state, which represents 
 * its progress towards completion and its status with respect to its 
 * constituent activities.
 * 
 * Source: Workflow Management Coalition http://www.wfmc.org/reference-model.html
 * 
 * @author alo
 */
public interface WfProcessInstanceBI {
    
    /** 
     * Gets the UUID from component of this instance
     */
    UUID getComponentPrimUuid();
    
    /** 
     * Gets the WorkList where this instance is member of
     */
    WorkListBI getWorkList();
    
    /** 
     * Gets the Workflow definition used in this instance
     */
    WfProcessDefinitionBI getWorkflowDefinition();
    
    /** 
     * Gets the current state of this instance
     */
    WfStateBI getState();
    
    /** 
     * Sets the current state of this instance
     */
    void setState(WfStateBI state);
    
    /** 
     * Gets the current assigned user for this instance. This can be NULL if
     * assignments are not used in the workflow.
     */
    WfUserBI getAssignedUser();
    
    /** 
     * Sets the current assigned user of this instance
     */
    void setAssignedUser(WfUserBI user);
    
    /** 
     * Gets available actions for the user, based on the roles, state of the
     * instance, and logic defined in the workflow definition
     */
    Collection<WfActivityBI> getActions(WfUserBI user);
    
    /** 
     * Gets available actions for the role, state of the
     * instance, and logic defined in the workflow definition
     */
    Collection<WfActivityBI> getActions(WfRoleBI role);
    
    /** 
     * Gets available actions for the roles, state of the
     * instance, and logic defined in the workflow definition
     */
    Collection<WfActivityBI> getActions(Collection<WfRoleBI> roles);
    
    /** 
     * Gets all available actions for the workflow, override allows to skip
     * logic and perform any action
     */
    Collection<WfActivityBI> getActionsForOverrideMode();
    
    /** 
     * Gets all previously executed activities, each activity instance is a history item
     */
    LinkedList<WfActivityInstanceBI> getActivityInstances();
    
    /** 
     * Gets the due date for this instance
     */
    Long getDueDate();
    
    /** 
     * Gets the creation date for this instance
     */
    Long getCreationDate();
    
    /** 
     * Gets the priority for this instance.
     * 1- Highest
     * 2- High
     * 3- Normal
     * 4- Low
     * 5- Lowest
     * 
     * TODO: move to Enum?
     */
    Integer getPriority();
    
    /**
	 * True when is active, regardless of if is complete or incomplete. 
	 * False when is inactive, meaning that this instance should not
	 * be considered in any operation.
	 */
    boolean isActive();

	/**
	 * True when is complete, Fals when is not.
	 */
	boolean isCompleted();
    
}
