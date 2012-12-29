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

import org.ihtsdo.tk.api.ComponentVersionBI;

/**
 * A representation of the status of a component in workflow.
 * When a component is sent into a workflow, a new instance is created. A component
 * can have multiple concurrent instances, that progress separately.
 * Most of the time a component focus of a workflow will be concept, in exceptional cases it 
 * can be other kind of components.
 * 
 * @author alo
 */
public interface WfInstanceBI {
    
    /** 
     * Gets the component of this instance
     */
    ComponentVersionBI getComponent();
    
    /** 
     * Gets the WorkList where this instance is member of
     */
    WorkListBI getWorkList();
    
    /** 
     * Gets the Workflow definition used in this instance
     */
    WfDefinitionBI getWorkflowDefinition();
    
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
    Collection<WfActionBI> getActions(WfUserBI user);
    
    /** 
     * Gets available actions for the role, state of the
     * instance, and logic defined in the workflow definition
     */
    Collection<WfActionBI> getActions(WfRoleBI role);
    
    /** 
     * Gets available actions for the roles, state of the
     * instance, and logic defined in the workflow definition
     */
    Collection<WfActionBI> getActions(Collection<WfRoleBI> roles);
    
    /** 
     * Gets all available actions for the workflow, override allows to skip
     * logic and perform any action
     */
    Collection<WfActionBI> getActionsForOverrideMode();
    
    /** 
     * Gets all available actions for the workflow, override allows to skip
     * logic and perform any action
     */
    LinkedList<WfHistoryEntryBI> getHistory();
    
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
